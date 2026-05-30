package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log

enum class AppScreen {
    Home,
    RecipeDetail,
    AiCreator,
    Timers,
    Settings
}

data class ActiveTimer(
    val label: String,
    val totalSeconds: Int,
    val secondsRemaining: Int,
    val isRunning: Boolean,
    val isFinished: Boolean
)

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = RecipeRepository(database.recipeDao())
    val settingsManager = SettingsManager(application)

    // --- Dynamic Settings State ---
    var isDarkTheme by mutableStateOf(settingsManager.isDarkTheme)
        private set

    var dietaryPreference by mutableStateOf(settingsManager.dietaryPreference)
        private set

    var isAlarmSoundEnabled by mutableStateOf(settingsManager.isAlarmSoundEnabled)
        private set

    var isAlarmVibrateEnabled by mutableStateOf(settingsManager.isAlarmVibrateEnabled)
        private set

    // --- Navigation & Interactive State ---
    var currentScreen by mutableStateOf(AppScreen.Home)
    var selectedRecipe by mutableStateOf<RecipeEntity?>(null)
    
    // --- Search & Filters ---
    val searchQuery = MutableStateFlow("")
    val selectedPlaceFilter = MutableStateFlow<String?>("All") // "All" or a specific country/region

    // --- AI Generator State ---
    var aiPrompt by mutableStateOf("")
    var isAiGenerating by mutableStateOf(false)
    var aiErrorMessage by mutableStateOf<String?>(null)
    var justGeneratedRecipe by mutableStateOf<RecipeEntity?>(null)

    // --- Standalone Timer Presets ---
    val timerPresets: StateFlow<List<TimerPresetEntity>> = repository.timerPresets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Cooking Timer Engine State ---
    private var timerJob: Job? = null
    var activeTimer by mutableStateOf<ActiveTimer?>(null)
        private set

    // --- Combined Reactive Recipe Processing (Filtering logic) ---
    val recipesState: StateFlow<List<RecipeEntity>> = combine(
        repository.allRecipes,
        searchQuery,
        selectedPlaceFilter
    ) { rawRecipes, query, placeFilter ->
        var list = rawRecipes

        // 1. Filter by searched text (title or ingredients)
        if (query.isNotBlank()) {
            list = list.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.ingredients.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }

        // 2. Filter by Place/Cuisine Origin
        if (placeFilter != null && placeFilter != "All") {
            list = list.filter { it.place == placeFilter }
        }

        // 3. Filter by User Settings: Dietary Preferences
        val currentDietary = settingsManager.dietaryPreference
        if (currentDietary != "None") {
            list = list.filter { recipe ->
                isDietaryFriendly(recipe, currentDietary)
            }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Observe Favorites independently
    val favoriteRecipes: StateFlow<List<RecipeEntity>> = repository.favoriteRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Pre-populate database with our rich custom recipes if empty
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    // --- Business Functions ---

    fun onSearchQueryChanged(newQuery: String) {
        searchQuery.value = newQuery
    }

    fun setPlaceFilter(place: String?) {
        selectedPlaceFilter.value = place ?: "All"
    }

    fun selectRecipe(recipe: RecipeEntity?) {
        selectedRecipe = recipe
        if (recipe != null) {
            currentScreen = AppScreen.RecipeDetail
        }
    }

    fun navigateTo(screen: AppScreen) {
        currentScreen = screen
        if (screen != AppScreen.RecipeDetail) {
            selectedRecipe = null
        }
    }

    fun toggleFavorite(recipe: RecipeEntity) {
        viewModelScope.launch {
            val updated = recipe.copy(isFavorite = !recipe.isFavorite)
            repository.updateRecipe(updated)
            if (selectedRecipe?.id == recipe.id) {
                selectedRecipe = updated
            }
        }
    }

    fun deleteRecipe(recipe: RecipeEntity) {
        viewModelScope.launch {
            repository.deleteRecipe(recipe)
            if (selectedRecipe?.id == recipe.id) {
                selectedRecipe = null
                currentScreen = AppScreen.Home
            }
        }
    }

    // --- Stand-alone Presets ---
    fun addTimerPreset(label: String, minutes: Int) {
        viewModelScope.launch {
            repository.insertTimerPreset(
                TimerPresetEntity(
                    label = label,
                    durationSeconds = minutes * 60,
                    category = "Custom"
                )
            )
        }
    }

    fun removeTimerPreset(preset: TimerPresetEntity) {
        viewModelScope.launch {
            repository.deleteTimerPreset(preset)
        }
    }

    // --- Active Cooking Timer management ---

    fun startCookingTimer(label: String, durationSeconds: Int) {
        timerJob?.cancel()
        activeTimer = ActiveTimer(
            label = label,
            totalSeconds = durationSeconds,
            secondsRemaining = durationSeconds,
            isRunning = true,
            isFinished = false
        )

        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = activeTimer ?: break
                if (current.secondsRemaining > 1) {
                    activeTimer = current.copy(
                        secondsRemaining = current.secondsRemaining - 1
                    )
                } else {
                    activeTimer = current.copy(
                        secondsRemaining = 0,
                        isRunning = false,
                        isFinished = true
                    )
                    break
                }
            }
        }
    }

    fun pauseCookingTimer() {
        val current = activeTimer ?: return
        timerJob?.cancel()
        activeTimer = current.copy(isRunning = false)
    }

    fun resumeCookingTimer() {
        val current = activeTimer ?: return
        if (current.secondsRemaining <= 0) return
        startCookingTimer(current.label, current.secondsRemaining)
    }

    fun resetCookingTimer() {
        val current = activeTimer ?: return
        startCookingTimer(current.label, current.totalSeconds)
    }

    fun cancelCookingTimer() {
        timerJob?.cancel()
        activeTimer = null
    }

    // --- Settings modification ---

    fun updateDarkTheme(enabled: Boolean) {
        settingsManager.isDarkTheme = enabled
        isDarkTheme = enabled
    }

    fun updateDietaryPreference(pref: String) {
        settingsManager.dietaryPreference = pref
        dietaryPreference = pref
    }

    fun updateAlarmSound(enabled: Boolean) {
        settingsManager.isAlarmSoundEnabled = enabled
        isAlarmSoundEnabled = enabled
    }

    fun updateAlarmVibrate(enabled: Boolean) {
        settingsManager.isAlarmVibrateEnabled = enabled
        isAlarmVibrateEnabled = enabled
    }

    // --- AI Generator Command Call ---

    fun generateRecipeFromPrompt() {
        val prompt = aiPrompt.trim()
        if (prompt.isEmpty()) return

        isAiGenerating = true
        aiErrorMessage = null
        justGeneratedRecipe = null

        viewModelScope.launch {
            try {
                val generated = GeminiClient.createRecipe(prompt)
                if (generated != null) {
                    val savedId = repository.insertRecipe(generated)
                    val finalized = generated.copy(id = savedId.toInt())
                    justGeneratedRecipe = finalized
                    selectedRecipe = finalized
                    aiPrompt = ""
                    currentScreen = AppScreen.RecipeDetail
                } else {
                    aiErrorMessage = "Gemini was unable to create this recipe. Ensure your Google AI Studio Gemini API Key is entered in your Secrets tab."
                }
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Err generating: ${e.message}", e)
                aiErrorMessage = "API Connection error: ${e.message}. Please check your network and API key."
            } finally {
                isAiGenerating = false
            }
        }
    }

    // --- Dietary checker helper to filter meat elements in local recipes ---
    private fun isDietaryFriendly(recipe: RecipeEntity, diet: String): Boolean {
        val ingredientText = (recipe.title + " " + recipe.ingredients + " " + recipe.description).lowercase()

        // Simple keywords representing meat, seafood, dairy, eggs, etc.
        val nonVegKeywords = listOf("chicken", "beef", "pork", "shrimp", "prawn", "salmon", "meat", "bacon", "lamb", "fish", "ham", "steak")
        val nonVeganKeywords = nonVegKeywords + listOf("cheese", "butter", "milk", "yogurt", "cream", "egg", "ghee", "honey")

        return when (diet) {
            "Vegetarian" -> {
                nonVegKeywords.none { ingredientText.contains(it) }
            }
            "Vegan" -> {
                nonVeganKeywords.none { ingredientText.contains(it) }
            }
            "Gluten-Free" -> {
                // Gluten markers: wheat, flour, bread, pasta, semolina, brioche (except certified gluten free)
                val glutenKeywords = listOf("flour", "wheat", "bread", "pasta", "spaghetti", "brioche", "semolina", "rye", "barley")
                glutenKeywords.none { ingredientText.contains(it) }
            }
            else -> true
        }
    }
}
