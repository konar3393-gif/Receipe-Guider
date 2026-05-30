package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.RecipeEntity
import com.example.data.TimerPresetEntity
import com.example.ui.ActiveTimer
import com.example.ui.AppScreen
import com.example.ui.RecipeViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: RecipeViewModel) {
    val recipes by viewModel.recipesState.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteRecipes.collectAsStateWithLifecycle()
    val timerPresets by viewModel.timerPresets.collectAsStateWithLifecycle()
    val activeTimer = viewModel.activeTimer
    
    val currentScreen = viewModel.currentScreen
    val selectedRecipe = viewModel.selectedRecipe

    // Show a fullscreen helper timer dialog if the user clicks details on the active bar
    var showFullTimerDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = when (currentScreen) {
                                AppScreen.Home -> "GourmetTimer"
                                AppScreen.RecipeDetail -> "Recipe Details"
                                AppScreen.AiCreator -> "AI Recipe Creator"
                                AppScreen.Timers -> "Kitchen Timers"
                                AppScreen.Settings -> "Preferences"
                            },
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    if (currentScreen == AppScreen.Home) {
                        IconButton(
                            onClick = { viewModel.navigateTo(AppScreen.Settings) },
                            modifier = Modifier.testTag("top_settings_button")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                }
            )
        },
        bottomBar = {
            Column {
                // Persistent Active Cooking Timer Ribbon
                AnimatedVisibility(
                    visible = activeTimer != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    if (activeTimer != null) {
                        ActiveTimerRibbon(
                            timer = activeTimer,
                            onPlayPause = {
                                if (activeTimer.isRunning) viewModel.pauseCookingTimer()
                                else viewModel.resumeCookingTimer()
                            },
                            onDismiss = { viewModel.cancelCookingTimer() },
                            onOpenFull = { showFullTimerDialog = true }
                        )
                    }
                }

                // Global M3 Navigation Bar
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.Home || currentScreen == AppScreen.RecipeDetail,
                        onClick = { viewModel.navigateTo(AppScreen.Home) },
                        icon = { Icon(Icons.Outlined.MenuBook, contentDescription = "Recipes") },
                        label = { Text("Recipes") },
                        modifier = Modifier.testTag("nav_recipes")
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.AiCreator,
                        onClick = { viewModel.navigateTo(AppScreen.AiCreator) },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Chef") },
                        label = { Text("AI Creator") },
                        modifier = Modifier.testTag("nav_ai")
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.Timers,
                        onClick = { viewModel.navigateTo(AppScreen.Timers) },
                        icon = { Icon(Icons.Outlined.Timer, contentDescription = "Timers") },
                        label = { Text("Timers") },
                        modifier = Modifier.testTag("nav_timers")
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.Settings,
                        onClick = { viewModel.navigateTo(AppScreen.Settings) },
                        icon = { Icon(Icons.Outlined.Tune, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.Home -> DashboardScreen(viewModel, recipes, favorites)
                AppScreen.RecipeDetail -> RecipeDetailScreen(viewModel, selectedRecipe)
                AppScreen.AiCreator -> AiCreatorScreen(viewModel)
                AppScreen.Timers -> TimersScreen(viewModel, timerPresets)
                AppScreen.Settings -> SettingsScreen(viewModel, recipes, favorites)
            }

            // Real-Time Fullscreen Cooking Progress Sheet / Dialogue
            if (showFullTimerDialog && activeTimer != null) {
                FullscreenTimerOverlay(
                    timer = activeTimer,
                    onPlayPause = {
                        if (activeTimer.isRunning) viewModel.pauseCookingTimer()
                        else viewModel.resumeCookingTimer()
                    },
                    onReset = { viewModel.resetCookingTimer() },
                    onDismiss = { showFullTimerDialog = false }
                )
            }
        }
    }
}

// ==========================================
// 1. DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(
    viewModel: RecipeViewModel,
    recipes: List<RecipeEntity>,
    favorites: List<RecipeEntity>
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedPlace by viewModel.selectedPlaceFilter.collectAsStateWithLifecycle()

    val placesList = listOf(
        "All",
        "Italy (Tuscan Kitchen)",
        "India (Royal Spice Kitchen)",
        "Mexico (Oaxacan Bistro)",
        "Japan (Kyoto Tea Pavilion)",
        "France (Parisian Bakery)",
        "America (Backyard Tavern)",
        "Midnight Pantry"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
    ) {
        // --- Real-time Culinary Search Bar ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = { Text("Search ingredients, places, or dishes...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("search_bar"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )

        // --- Categories (Locations / Places) Scrollable chips ---
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(placesList) { place ->
                val isSelected = selectedPlace == place
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setPlaceFilter(place) },
                    label = { 
                        Text(
                            text = place.substringBefore(" ("), 
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // --- Main Visual Container ---
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Featured Herobox (Slightly dynamic) displayed only on "All" filter and empty search
            if (selectedPlace == "All" && searchQuery.isEmpty() && recipes.isNotEmpty()) {
                val featuredRecipe = recipes.lastOrNull { !it.isUserCreated } ?: recipes.first()
                item {
                    FeaturedHeroBox(featuredRecipe = featuredRecipe, onRecipeClick = { viewModel.selectRecipe(featuredRecipe) })
                }
            }

            if (recipes.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterAlt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No matching gourmet dishes found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val activeDietary = viewModel.settingsManager.dietaryPreference
                        Text(
                            text = if (activeDietary != "None") "Try adjusting your '$activeDietary' dietary filter in Settings." else "Try searching for simpler words or query other places.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MutedText,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                // Header Label
                item {
                    Text(
                        text = if (selectedPlace == "All") "Trending Recipes" else "Specialties of $selectedPlace",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                items(recipes.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            RecipeCard(recipe = pair[0], onRecipeClick = { viewModel.selectRecipe(pair[0]) }, onFavToggle = { viewModel.toggleFavorite(pair[0]) })
                        }
                        if (pair.size > 1) {
                            Box(modifier = Modifier.weight(1f)) {
                                RecipeCard(recipe = pair[1], onRecipeClick = { viewModel.selectRecipe(pair[1]) }, onFavToggle = { viewModel.toggleFavorite(pair[1]) })
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturedHeroBox(featuredRecipe: RecipeEntity, onRecipeClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable(onClick = onRecipeClick)
            .testTag("featured_hero_box"),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = featuredRecipe.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Beautiful radial & linear bottom darkness gradient to ensure texts pop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 100f
                        )
                    )
            )

            // Dynamic tags overlay
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Featured Dish", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(featuredRecipe.place.split(" ")[0], fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Recipe info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = featuredRecipe.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = featuredRecipe.description,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = AmberAlight, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${featuredRecipe.prepTimeMinutes + featuredRecipe.cookTimeMinutes} mins", color = Color.LightGray, fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = SkyMint, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(featuredRecipe.difficulty, color = Color.LightGray, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: RecipeEntity, onRecipeClick: () -> Unit, onFavToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRecipeClick)
            .testTag("recipe_card_${recipe.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                startY = 80f
                            )
                        )
                )

                // Favorite Heart Toggle Button
                IconButton(
                    onClick = onFavToggle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .testTag("fav_toggle_${recipe.id}")
                ) {
                    Icon(
                        imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (recipe.isFavorite) ChiliRed else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Place tag overlay on lower card left
                Text(
                    text = recipe.place.substringBefore(" ("),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                )
            }

            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = recipe.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = recipe.description,
                    fontSize = 11.sp,
                    color = MutedText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${recipe.prepTimeMinutes + recipe.cookTimeMinutes} mins",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = recipe.difficulty,
                        fontSize = 10.sp,
                        color = when (recipe.difficulty) {
                            "Easy" -> RosemaryGreen
                            "Medium" -> HoneyGoldDark
                            else -> ChiliRed
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. RECIPE DETAIL SCREEN
// ==========================================
@Composable
fun RecipeDetailScreen(viewModel: RecipeViewModel, recipe: RecipeEntity?) {
    if (recipe == null) return

    val ingredientsList = recipe.ingredients.split("\n").filter { it.isNotBlank() }
    val instructionsList = recipe.instructions.split("\n").filter { it.isNotBlank() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("recipe_detail_screen")
    ) {
        // --- Detail Large Hero banner ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 120f
                        )
                    )
            )

            // Back chevron
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.Home) },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            // Favorite/Delete block
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { viewModel.toggleFavorite(recipe) },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (recipe.isFavorite) ChiliRed else Color.White
                    )
                }
                if (recipe.isUserCreated) {
                    IconButton(
                        onClick = { viewModel.deleteRecipe(recipe) },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .testTag("delete_recipe_btn")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                    }
                }
            }

            // Title block embedded in banner
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(recipe.place, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recipe.title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- Core Recipe Specifications ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Prep Time", fontSize = 11.sp, color = MutedText)
                Text("${recipe.prepTimeMinutes} mins", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Box(modifier = Modifier
                .width(1.dp)
                .height(40.dp)
                .background(MutedText.copy(alpha = 0.3f)))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Cook Time", fontSize = 11.sp, color = MutedText)
                Text("${recipe.cookTimeMinutes} mins", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Box(modifier = Modifier
                .width(1.dp)
                .height(40.dp)
                .background(MutedText.copy(alpha = 0.3f)))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Difficulty", fontSize = 11.sp, color = MutedText)
                Text(recipe.difficulty, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // --- Recipe Description ---
        Text(
            text = recipe.description,
            fontSize = 14.sp,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- Ingredients List Section ---
        Text(
            text = "Ingredients required",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ingredientsList.forEach { ingredient ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RadioButtonUnchecked, // acting as a checklist
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { },
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = ingredient,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Instructions & Cooking Timers Block ---
        Text(
            text = "Stepwise cooking guidelines",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            instructionsList.forEachIndexed { index, step ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    .size(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (index + 1).toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            // Dynamic Cooking Timer Launcher!
                            // Try to extract cooking time keywords (e.g. "cook for 10 minutes", "bake for 45 minutes", "sauté for 2 mins")
                            val secondsToLaunch = extractStepTimerDuration(step)
                            if (secondsToLaunch > 0) {
                                Button(
                                    onClick = {
                                        viewModel.startCookingTimer(
                                            label = "Step ${index + 1} Timer (${recipe.title.substringBefore(" ")})",
                                            durationSeconds = secondsToLaunch
                                        )
                                    },
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Start ${secondsToLaunch / 60}m Timer", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = step,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

/**
 * Super utility function to parse numerical time minutes/seconds from instructions.
 * Translates things like "sauté for 5 minutes" or "knead for 10 minutes" or "bake for 45 minutes".
 */
fun extractStepTimerDuration(instructionLine: String): Int {
    val lower = instructionLine.lowercase()
    val pattern = "([0-9]+)\\s*(minute|minutes|min|mins|seconds|secs|sec)".toRegex()
    val match = pattern.find(lower)
    if (match != null) {
        val number = match.groupValues[1].toIntOrNull() ?: 0
        val unit = match.groupValues[2]
        return if (unit.startsWith("min")) {
            number * 60
        } else {
            number // seconds
        }
    }
    return 0
}

// ==========================================
// 3. AI CREATOR SCREEN (GEMINI LAB)
// ==========================================
@Composable
fun AiCreatorScreen(viewModel: RecipeViewModel) {
    val isGenerating = viewModel.isAiGenerating
    val errorMessage = viewModel.aiErrorMessage
    val generated = viewModel.justGeneratedRecipe

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("ai_creator_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .gradientBackground(listOf(SaffronOrangeDark, HoneyGold, RosemaryGreen), 315f)
                .clip(RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gemini Recipe Lab", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Text(
                    text = "Unlock infinite cooking options. Prompt Gemini with ingredients you have, a region style, or a fitness preference, and have it build Michelin-standard dishes saved directly inside your local database with timers!",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // Prompt Input Field
        OutlinedTextField(
            value = viewModel.aiPrompt,
            onValueChange = { viewModel.aiPrompt = it },
            placeholder = { Text("e.g., 'Fresh pasta with leftover spinach and a slice of salmon' or 'Vegan Mexican street corn salad'") },
            label = { Text("What do you want to cook?") },
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .testTag("ai_prompt_input"),
            shape = RoundedCornerShape(12.dp),
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { 
                if (!isGenerating && viewModel.aiPrompt.isNotBlank()) {
                    viewModel.generateRecipeFromPrompt()
                }
            })
        )

        // Action Button
        Button(
            onClick = { viewModel.generateRecipeFromPrompt() },
            enabled = !isGenerating && viewModel.aiPrompt.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("generate_recipe_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                Spacer(modifier = Modifier.width(12.dp))
                Text("AI is cooking your recipe...")
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate AI Recipe", fontWeight = FontWeight.Bold)
            }
        }

        // Error message card
        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ChiliRed.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ChiliRed)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(errorMessage, fontSize = 12.sp, color = ChiliRed, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Loading and instructions helpful tip cards
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Example Prompts to Try:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                PromptSuggestionRow(text = "Healthy Keto chicken breasts", onClick = { viewModel.aiPrompt = "Healthy Keto chicken breasts" })
                PromptSuggestionRow(text = "Kyoto matcha sweet mochi dessert", onClick = { viewModel.aiPrompt = "Kyoto matcha sweet mochi dessert" })
                PromptSuggestionRow(text = "Quick 5-minute microwave mug cake", onClick = { viewModel.aiPrompt = "Quick 5-minute microwave mug cake" })
            }
        }
    }
}

@Composable
fun PromptSuggestionRow(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.ArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

// ==========================================
// 4. STANDALONE KITCHEN TIMERS SCREEN
// ==========================================
@Composable
fun TimersScreen(viewModel: RecipeViewModel, presets: List<TimerPresetEntity>) {
    var timerLabelInput by remember { mutableStateOf("") }
    var timerMinuteInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("kitchen_timers_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Standalone Timer Generator Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HourglassTop, contentDescription = null, tint = SaffronOrange, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Custom Countdown Preset", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                OutlinedTextField(
                    value = timerLabelInput,
                    onValueChange = { timerLabelInput = it },
                    placeholder = { Text("e.g., 'Perfect Sunny Egg'") },
                    label = { Text("Timer Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = timerMinuteInput,
                    onValueChange = { timerMinuteInput = it },
                    placeholder = { Text("e.g. '5'") },
                    label = { Text("Minutes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )

                Button(
                    onClick = {
                        val minutes = timerMinuteInput.toIntOrNull() ?: 0
                        if (timerLabelInput.isNotBlank() && minutes > 0) {
                            viewModel.addTimerPreset(timerLabelInput, minutes)
                            timerLabelInput = ""
                            timerMinuteInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Add Preset to Storage", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Preset collection header
        Text("Standard Preset Timers", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        if (presets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No stopwatch presets saved", color = MutedText, fontSize = 13.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                presets.forEach { preset ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(preset.label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${preset.durationSeconds / 60} minutes", fontSize = 11.sp, color = MutedText)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.startCookingTimer(preset.label, preset.durationSeconds) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.testTag("run_timer_${preset.id}")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Run", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Run", fontSize = 12.sp)
                                }

                                if (preset.category == "Custom") {
                                    IconButton(onClick = { viewModel.removeTimerPreset(preset) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = ChiliRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. SETTINGS SCREEN
// ==========================================
@Composable
fun SettingsScreen(
    viewModel: RecipeViewModel,
    recipes: List<RecipeEntity>,
    favorites: List<RecipeEntity>
) {
    val dietary = viewModel.dietaryPreference
    val sound = viewModel.isAlarmSoundEnabled
    val vibrate = viewModel.isAlarmVibrateEnabled
    val userCreatedCount = recipes.count { it.isUserCreated }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Statistics section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Recipes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("${recipes.size}", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Box(modifier = Modifier
                    .width(1.dp)
                    .height(35.dp)
                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Loved Foods", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("${favorites.size}", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Box(modifier = Modifier
                    .width(1.dp)
                    .height(35.dp)
                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("AI Recipes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("$userCreatedCount", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        // Dietary preferences
        Text("Dietary Preferences", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val diets = listOf("None", "Vegetarian", "Vegan", "Gluten-Free")
                diets.forEach { d ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateDietaryPreference(d) }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(d, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        RadioButton(
                            selected = dietary == d,
                            onClick = { viewModel.updateDietaryPreference(d) },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        // Alerts configurations
        Text("Cooking Timer Settings", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Sound Alarm
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Sound Alerts", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("Ring audibly when timer finishes", fontSize = 11.sp, color = MutedText)
                    }
                    Switch(
                        checked = sound,
                        onCheckedChange = { viewModel.updateAlarmSound(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }

                // Vibrate Alarm
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Vibrate Alerts", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("Pulse device when cooker is ready", fontSize = 11.sp, color = MutedText)
                    }
                    Switch(
                        checked = vibrate,
                        onCheckedChange = { viewModel.updateAlarmVibrate(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        // Signature Brand Credit
        Text(
            text = "GourmetTimer App\nVersion 1.0.0 (Gourmet Suite)\nCrafted with premium components. Android Studio Build.",
            fontSize = 11.sp,
            color = MutedText,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )
    }
}

// ==========================================
// ACTIVE TIMER PERSISTENT RIBBON (STICKY OVERLAY)
// ==========================================
@Composable
fun ActiveTimerRibbon(
    timer: ActiveTimer,
    onPlayPause: () -> Unit,
    onDismiss: () -> Unit,
    onOpenFull: () -> Unit
) {
    // Elegant pulsing animation upon completion!
    val infiniteTransition = rememberInfiniteTransition()
    val completedPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val ribbonBgColor = if (timer.isFinished) {
        MaterialTheme.colorScheme.error.copy(alpha = completedPulseAlpha)
    } else {
        MaterialTheme.colorScheme.secondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenFull)
            .background(ribbonBgColor)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("active_timer_ribbon"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Elegant micro-canvas showing continuous progress ring
            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                val progress = if (timer.totalSeconds > 0) {
                    timer.secondsRemaining.toFloat() / timer.totalSeconds.toFloat()
                } else 0f

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.25f),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawArc(
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Icon(
                    imageVector = if (timer.isFinished) Icons.Default.NotificationsActive else Icons.Default.Timer,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = timer.label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (timer.isFinished) "Finished! Cooker is ready." else "Remaining: ${formatTime(timer.secondsRemaining)}",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!timer.isFinished) {
                IconButton(onClick = onPlayPause) {
                    Icon(
                        imageVector = if (timer.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White
                    )
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Dismiss", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

// ==========================================
// FULLSCREEN COUNTDOWN DIALOGUE WITH RADIAL SHIFT
// ==========================================
@Composable
fun FullscreenTimerOverlay(
    timer: ActiveTimer,
    onPlayPause: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("fullscreen_timer_overlay"),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top close segment
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(32.dp))
                }
            }

            // Big Timer Circular Segment
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = timer.label,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                // Large progress ring
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val progress = if (timer.totalSeconds > 0) {
                        timer.secondsRemaining.toFloat() / timer.totalSeconds.toFloat()
                    } else 0f

                    val infiniteTransition = rememberInfiniteTransition()
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(if (timer.isFinished) pulseScale else 1f)
                    ) {
                        drawCircle(
                            color = SaffronOrange.copy(alpha = 0.1f),
                            style = Stroke(width = 16.dp.toPx())
                        )
                        drawArc(
                            color = if (timer.isFinished) ChiliRed else SaffronOrange,
                            startAngle = -90f,
                            sweepAngle = progress * 360f,
                            useCenter = false,
                            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatTime(timer.secondsRemaining),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = if (timer.isFinished) "READY!" else "${(progress * 100).toInt()}% Remaining",
                            fontSize = 14.sp,
                            color = MutedText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Actions control block (Play/Pause, Reset, Dismiss)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restart", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onPlayPause()
                        if (timer.isFinished) {
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (timer.isFinished) ChiliRed else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (timer.isFinished) Icons.Default.CheckCircle else if (timer.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (timer.isFinished) "Finished!" else if (timer.isRunning) "Pause" else "Resume",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format("%02d:%02d", m, s)
}

// Custom gradient view drawer modifier
fun Modifier.gradientBackground(colors: List<Color>, angle: Float) = this.then(
    background(
        Brush.linearGradient(
            colors = colors
        )
    )
)
