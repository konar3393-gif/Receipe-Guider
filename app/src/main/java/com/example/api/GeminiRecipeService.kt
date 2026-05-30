package com.example.api

import com.example.BuildConfig
import com.example.data.RecipeEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

// --- Network Request/Response Models ---

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class ResponseFormatText(
    val mimeType: String
)

@JsonClass(generateAdapter = true)
data class ResponseFormat(
    val text: ResponseFormatText? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseFormat: ResponseFormat? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

// --- Target Structured JSON response representable on Device ---

@JsonClass(generateAdapter = true)
data class GeminiRecipeJson(
    val title: String,
    val description: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val category: String,
    val place: String,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val difficulty: String
)

// --- Retrofit Interface definition ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

// --- Direct REST API Manager ---

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    /**
     * Call Gemini API to create a custom, fully structured cooking recipe.
     * Uses structured JSON response configuration to ensure clean parsing.
     */
    suspend fun createRecipe(prompt: String): RecipeEntity? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("GeminiClient", "API Key is missing or default placeholder value!")
            return@withContext null
        }

        val formattedPrompt = """
            Generate a creative, detailed cooking recipe based on the user's intent: "$prompt".
            The recipe should capture high-quality ingredients and clear, detailed stepwise cooking instructions.
            Select an appropriate "place" representing the origin of the dish (e.g. "Italy (Tuscan Kitchen)", "India (Royal Spice)", "Midnight Pantry", "Mexico (Oaxacan Bistro)", "France (Parisian Bakery)") and category (e.g. "Breakfast", "Lunch", "Dinner", "Dessert").
            
            Provide the response strictly as a JSON object adhering to this schema:
            {
               "title": "Name of the dish",
               "description": "Appetizing 1-2 sentence description",
               "ingredients": ["1 cup ingredient A", "2 tbsp ingredient B", "salt & pepper to taste"],
               "instructions": ["Step 1 direction", "Step 2 direction", "Step 3 direction"],
               "category": "Dinner",
               "place": "Italy (Tuscan Kitchen)",
               "prepTimeMinutes": 15,
               "cookTimeMinutes": 20,
               "difficulty": "Easy"
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = formattedPrompt)))),
            generationConfig = GenerationConfig(
                responseFormat = ResponseFormat(text = ResponseFormatText(mimeType = "application/json")),
                temperature = 0.5f
            ),
            systemInstruction = Content(parts = listOf(Part(text = "You are an expert culinary chef writing Michelin-star caliber home recipes.")))
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                Log.d("GeminiClient", "Received JSON: $jsonText")
                val adapter = moshi.adapter(GeminiRecipeJson::class.java)
                val recipeJson = adapter.fromJson(jsonText)
                if (recipeJson != null) {
                    // Try to pick a decent free public Unsplash URL for food photography based on name keywords to keep UI visually stunning
                    val keywords = recipeJson.title.lowercase()
                    val imageUrl = when {
                        keywords.contains("pizza") -> "https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=800&auto=format&fit=crop"
                        keywords.contains("pasta") || keywords.contains("noodle") -> "https://images.unsplash.com/photo-1546549032-9571cd6b27df?q=80&w=800&auto=format&fit=crop"
                        keywords.contains("burger") || keywords.contains("sandwich") -> "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?q=80&w=800&auto=format&fit=crop"
                        keywords.contains("dessert") || keywords.contains("cake") || keywords.contains("sweet") || keywords.contains("cookie") -> "https://images.unsplash.com/photo-1578985545062-69928b1d9587?q=80&w=800&auto=format&fit=crop"
                        keywords.contains("salad") -> "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?q=80&w=800&auto=format&fit=crop"
                        keywords.contains("soup") || keywords.contains("ramen") || keywords.contains("curry") -> "https://images.unsplash.com/photo-1547592180-85f173990554?q=80&w=800&auto=format&fit=crop"
                        keywords.contains("chicken") || keywords.contains("meat") || keywords.contains("steak") -> "https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?q=80&w=800&auto=format&fit=crop"
                        keywords.contains("taco") -> "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?q=80&w=800&auto=format&fit=crop"
                        keywords.contains("egg") || keywords.contains("breakfast") -> "https://images.unsplash.com/photo-1525351484163-7529414344d8?q=80&w=800&auto=format&fit=crop"
                        else -> "https://images.unsplash.com/photo-1490645935967-10de6ba17061?q=80&w=800&auto=format&fit=crop" // general culinary
                    }

                    return@withContext RecipeEntity(
                        title = recipeJson.title,
                        description = recipeJson.description,
                        ingredients = recipeJson.ingredients.joinToString("\n"),
                        instructions = recipeJson.instructions.joinToString("\n"),
                        category = recipeJson.category,
                        place = recipeJson.place,
                        imageUrl = imageUrl,
                        prepTimeMinutes = recipeJson.prepTimeMinutes,
                        cookTimeMinutes = recipeJson.cookTimeMinutes,
                        difficulty = recipeJson.difficulty,
                        isFavorite = false,
                        isUserCreated = true
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiClient", "Failed to generate recipe via Gemini: ${e.message}", e)
        }
        return@withContext null
    }
}
