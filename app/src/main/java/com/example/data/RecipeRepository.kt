package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import android.util.Log

class RecipeRepository(private val recipeDao: RecipeDao) {

    val allRecipes: Flow<List<RecipeEntity>> = recipeDao.getAllRecipes()
    val favoriteRecipes: Flow<List<RecipeEntity>> = recipeDao.getFavoriteRecipes()
    val timerPresets: Flow<List<TimerPresetEntity>> = recipeDao.getAllTimerPresets()

    fun getRecipesByPlace(place: String): Flow<List<RecipeEntity>> {
        return recipeDao.getRecipesByPlace(place)
    }

    fun searchRecipes(query: String): Flow<List<RecipeEntity>> {
        return recipeDao.searchRecipes(query)
    }

    suspend fun getRecipeById(id: Int): RecipeEntity? {
        return recipeDao.getRecipeById(id)
    }

    suspend fun insertRecipe(recipe: RecipeEntity): Long {
        return recipeDao.insertRecipe(recipe)
    }

    suspend fun updateRecipe(recipe: RecipeEntity) {
        recipeDao.updateRecipe(recipe)
    }

    suspend fun deleteRecipe(recipe: RecipeEntity) {
        recipeDao.deleteRecipe(recipe)
    }

    // --- Timer Presets ---
    suspend fun insertTimerPreset(preset: TimerPresetEntity) {
        recipeDao.insertTimerPreset(preset)
    }

    suspend fun deleteTimerPreset(preset: TimerPresetEntity) {
        recipeDao.deleteTimerPreset(preset)
    }

    suspend fun prepopulateIfEmpty() {
        try {
            val count = recipeDao.getRecipeCount()
            if (count == 0) {
                Log.d("RecipeRepository", "Database is empty! Prepopulating standard recipe dataset...")
                val defaultRecipes = createDefaultRecipes()
                recipeDao.insertRecipes(defaultRecipes)
                
                // Prepopulate some standard handy countdown timers for the kitchen
                val defaultPresets = listOf(
                    TimerPresetEntity(label = "Perfect Hard Boiled Egg", durationSeconds = 540, category = "Boil"),
                    TimerPresetEntity(label = "Al Dente Penne Pasta", durationSeconds = 600, category = "Boil"),
                    TimerPresetEntity(label = "Steep Premium Green Tea", durationSeconds = 180, category = "Beverage"),
                    TimerPresetEntity(label = "Golden Pan Toasting", durationSeconds = 120, category = "Sear"),
                    TimerPresetEntity(label = "Artisan Sourdough Oven Bake", durationSeconds = 2400, category = "Bake"),
                    TimerPresetEntity(label = "Simmer Marinara Sauce", durationSeconds = 1200, category = "Simmer"),
                    TimerPresetEntity(label = "French Drip Pour Over", durationSeconds = 240, category = "Beverage")
                )
                for (preset in defaultPresets) {
                    recipeDao.insertTimerPreset(preset)
                }
                Log.d("RecipeRepository", "Successfully seeded database with recipes and timers.")
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Err seeding database: ${e.message}", e)
        }
    }

    private fun createDefaultRecipes(): List<RecipeEntity> {
        return listOf(
            RecipeEntity(
                title = "Margherita Wood-Fires Pizza",
                description = "Classic Neapolitan pizza featuring sweet San Marzano tomato sauce, fresh creamy buffalo mozzarella, and aromatic basil leaves over a perfectly bubbled charred crust.",
                ingredients = "250g Type 00 Pizza Flour\n150ml Warm Purified Water\n3g Active Dry Yeast\n5g Premium Sea Salt\n100g San Marzano Italian Crushed Tomatoes\n80g Fresh Creamy Buffalo Mozzarella Slice\n4-6 Fresh Sweet Basil Leaves\n15ml Organic Extra Virgin Olive Oil",
                instructions = "Prepare the yeast in warm water and combine with 00 flour and sea salt. Knead for 10 minutes until elastic, then cover to double in volume (approx. 2 hours).\nPreheat your kitchen oven as high as possible (ideally 500°F/260°C or with a pizza stone) for 45 minutes.\nStretch dough gently on a lightly floured countertop keeping air bubbles in the outer crust rim.\nSpread crushed San Marzano tomato sauce thinly, leaving a border for the crust.\nDistribute torn buffalo mozzarella cheese chunks evenly and slide pizza onto your hot stone.\nBake for 7-10 minutes (or 90 seconds in a wood-fired oven) until the crust is highly puffed and charred beautifully.\nTear fresh aromatic basil leaves onto hot melted cheese, drizzle extra virgin olive oil, and serve hot.",
                category = "Dinner",
                place = "Italy (Tuscan Kitchen)",
                imageUrl = "https://images.unsplash.com/photo-1604068549290-dea0e4a305ca?q=80&w=800&auto=format&fit=crop",
                prepTimeMinutes = 20,
                cookTimeMinutes = 10,
                difficulty = "Medium"
            ),
            RecipeEntity(
                title = "Creamy Tuscan Garlic Chicken",
                description = "Enchanting pan-seared chicken breasts simmered in a luscious heavy cream sauce loaded with fresh baby spinach, rich sun-dried tomatoes, and sweet roasted garlic cloves.",
                ingredients = "2 Large Organic Chicken Breasts (halved horizontally)\n15g Pure Salted Butter\n15ml Cold-Pressed Olive Oil\n4 Sun-dried Tomatoes (chopped)\n2 fresh Garlic Cloves (finely minced)\n120ml Double Heavy Whisking Cream\n60ml Dry Italian White Wine or Chicken Broth\n50g Fresh Organic Baby Spinach\n30g Fresh Grated Aged Parmigiano-Reggiano\n5g Dried Oregano and Salt/Pepper to season",
                instructions = "In a heavy skillet, heat olive oil and melt butter over medium-high heat.\nSeason chicken cutlets generously with sea salt, ground black pepper, and half the oregano.\nSear chicken for 5 minutes per side until intensely golden brown. Remove chicken and tent with foil.\nIn the same hot pan, quickly sauté minced garlic and chopped sun-dried tomatoes for 1 minute until highly fragrant.\nPour in dry white wine to deglaze the skillet, scraping up all browned tasty bits from bottom. Let simmer for 2 minutes.\nLower heat to medium and stir in heavy whipping cream, grated parmigiano cheese, and remaining oregano. Let simmer gently for 3-4 minutes to thicken sauce.\nStir in fresh green baby spinach leaves and allow them to wilt gently in creamy sauce.\nSlide seared chicken cutlets back into sauce, spoon sauce on top, and simmer for 2 minutes to serve.",
                category = "Dinner",
                place = "Italy (Tuscan Kitchen)",
                imageUrl = "https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?q=80&w=800&auto=format&fit=crop",
                prepTimeMinutes = 10,
                cookTimeMinutes = 15,
                difficulty = "Easy"
            ),
            RecipeEntity(
                title = "Royal Butter Chicken (Murgh Makhani)",
                description = "A luxurious Mughlai classic. Tender tandoori-spiced chicken thighs cooked in a silky, rich, velvety tomato butter gravy infused with toasted fenugreek leaves.",
                ingredients = "500g Boneless Skinless Chicken Thigh Cutlets\n60g Double Strain Plain Greek Butter Yogurt\n15g Ginger-Garlic Paste\n10g Kashmiri Chili Powder\n5g Ground Toasted Garam Masala\n30g Pure Unsalted Butter\n1 Medium Yellow Onion (pureed)\n200g Sweet Tomato Puree\n120ml Pure Double Cream\n10g Dried Kasuri Methi (Toasted Fenugreek Leaves)\n15ml Fresh Lemon Juice",
                instructions = "Marinate chicken thigh chunks in yogurt, ginger-garlic paste, fresh lemon juice, cumin, kashmiri chili, and salt. Refrigerate for at least 30 minutes.\nSear chicken chunks in a cooking skillet in oil for 6 minutes or broil in an oven until charred on edges. Set aside.\nSauté pureed cooking onion and remaining ginger-garlic paste with butter in a large deep-bottom pot for 5 minutes.\nPour in tomato puree, garam masala, chili powder, and local spices. Simmer for 10 minutes until oil starts separating from tomato paste.\nTransfer sauce to food blender and puree until ultra-smooth, then return back to pan.\nStir in double heavy cream, seared chicken chunks, and let simmer on low heat for 8 minutes to cook fully.\nRub Kasuri Methi (fenugreek) between palms into curry, stir in final butter spoonful, and serve with naan bread.",
                category = "Lunch",
                place = "India (Royal Spice Kitchen)",
                imageUrl = "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?q=80&w=800&auto=format&fit=crop",
                prepTimeMinutes = 25,
                cookTimeMinutes = 20,
                difficulty = "Hard"
            ),
            RecipeEntity(
                title = "Golden Palak Paneer",
                description = "Nutritious and delicious cubes of paneer cotta paneer cheese seared golden-brown, folded inside an aromatic and spiced creamy blanched spinach gravy.",
                ingredients = "250g Paneer (Cottage Cheese) cut in cubes\n300g Fresh Organic Spinach Bunch\n1 Medium White Onion (chopped)\n2 Sweet Red Tomatoes (finely chopped)\n5g Ground Cumin Seeds\n1 fresh Green Serrano Chili (slit)\n10g Mined Fresh Ginger and Garlic\n60ml Double Whisking Heavy Cream\n10ml Ghee or Butter for pan searing",
                instructions = "Blanch fresh clean spinach leaves in boiling salted water for 1 minute, drain, and immediately submerge in ice-water bath to preserve bright green color.\nBlend ice-cooled spinach together with green serano chili to make smooth dark green puree.\nIn a wok, heat ghee and sear paneer cubes for 2-3 minutes until golden brown all around. Spoon paneer out and soak in warm lightly salted water to keep texture tender.\nIn same wok, sauté cumin seeds and chopped onions until translucent and lightly browned.\nAdd minced ginger-garlic and green chili. Sauté for 2 minutes, then stir in tomatoes and let cook until soft.\nPour in spinach puree, garam masala, salt to taste. Simmer on low heat with lid partially on for 5 minutes.\nEmpty soaked soft paneer cubes into the bubbling spinach gravy and cook together for 2 minutes.\nTurn off burner heat, swirl heavy whipping cream, and serve warm with seasoned fluffy basmati rice.",
                category = "Lunch",
                place = "India (Royal Spice Kitchen)",
                imageUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?q=80&w=800&auto=format&fit=crop",
                prepTimeMinutes = 15,
                cookTimeMinutes = 15,
                difficulty = "Medium"
            ),
            RecipeEntity(
                title = "Sizzling Street Tacos al Pastor",
                description = "Legendary Oaxacan street food. Thinly sliced marinated pork neck seared with caramelized pineapple, served over warm stone-ground corn tortillas.",
                ingredients = "400g Thin Sliced Pork Shoulder or Loin\n2 Dried Guajillo Chilies (rehydrated in hot water)\n1 Achote Paste Cube (30g)\n30ml Organic Apple Cider Vinegar\n2 fresh Garlic Cloves\n100g Fresh Ripe Pineapple (diced)\n1 bunch Sweet Fresh Cilantro (chopped)\n1 Sweet White Onion (diced)\n12 Small White Corn Tortillas\n4 Lime Wedges to squeeze",
                instructions = "Blend rehydrated guajillo chilies, achiote paste, cider vinegar, garlic, oregano, and pinch of salt to form a smooth red al pastor marinade.\nCoat thin pork slices heavily in marinade. Pack pork tightly in a storage container, layer slices, and marinate for 2 hours.\nHeat a cast-iron skillet until smoking. Sear pork strips in small batches quickly for 3-4 minutes to achieve blackened delicious charred edges.\nFinely chop cooked pork. Toss with sizzling fresh diced sweet pineapple or sear pineapple slices on the griddle.\nHeat small corn tortillas on skillet for 30 seconds per side until highly pliable.\nLayer pork and caramelized pineapple on two stacked warm tortillas.\nGarnish with chopped raw white onions, sweet cilantro, a squeeze of fresh lime juice, and spicy green salsa.",
                category = "Dinner",
                place = "Mexico (Oaxacan Bistro)",
                imageUrl = "https://images.unsplash.com/photo-1551504734-5ee1c4a1479b?q=80&w=800&auto=format&fit=crop",
                prepTimeMinutes = 30,
                cookTimeMinutes = 10,
                difficulty = "Medium"
            ),
            RecipeEntity(
                title = "Kyoto Matcha Dorayaki",
                description = "Japanese sweet pancakes made with high-grade ceremonial matcha green tea powder, sandwiched with sweet red Azuki bean paste.",
                ingredients = "2 Fresh Organic Eggs\n80g White Superfine Cane Sugar\n15ml Raw Honey\n100g Pastry Cake Flour\n5g Ceremonial Uji Matcha Powder\n3g Baking Powder\n30ml Purified Water\n150g Sweet Red Azuki Bean Paste (Anko)",
                instructions = "In a bowl, vigorously whisk organic eggs, superfine sugar, and raw honey together until fluffy and light yellow.\nSift cake flour, ceremonial matcha powder, and baking powder together into egg bowl. Whisk gently.\nPour in water, mix gently. Cover and let batter rest on counter for 15 minutes to allow baking powder to activate.\nHeat non-stick skillet on low heat. Dampen a paper towel with odorless vegetable oil and rub pan cooking surface lightly.\nPour a small ladle of matcha batter from high up to form a perfect 3-inch circle. Cook until bubbles cover top surface (approx. 2 minutes).\nFlip pancake and cook the other side for exactly 45 seconds. Set on plate, cover with damp cloth to remain soft.\nSpread spoonful of sweet red Azuki bean paste on center of one pancake and sandwich with another, pressing edges to seal.",
                category = "Dessert",
                place = "Japan (Kyoto Tea Pavilion)",
                imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80&w=800&auto=format&fit=crop",
                prepTimeMinutes = 15,
                cookTimeMinutes = 10,
                difficulty = "Easy"
            ),
            RecipeEntity(
                title = "Fluffy French Soufflé Omelette",
                description = "An ethereal Parisian breakfast masterpiece. Whipped aerated organic eggs cooked slowly in salted butter until the exterior is golden and the interior is melt-in-the-mouth foam.",
                ingredients = "3 Large Farm Eggs (whites & yolks separated temp-room)\n15g Pure Salted French Butter\n4g Fresh Green Chives (finely snipped)\n20g Crumbled Aged Goat Cheese\nScant Pinch of Sea Salt & White Pepper",
                instructions = "Place egg whites in a clean metallic bowl with a tiny pinch of salt. Whisk vigorously (ideally with a hand mixer) until stiff peaks form.\nIn a separate bowl, stir yolks with white pepper, snipped green chives, and crumbled goat cheese.\nGently fold 1/3 of whipped whites into yolk bowl to lighten mixture. Then, fold in remaining whites using slow figure-8 sweeps to preserve air context.\nFully heat French butter in a small non-stick pan over medium-low heat until completely foamy.\nPour in airy omelette mixture. Smoothen top, reduce heat to low, cover with dome lid, and cook for 4-5 minutes until bottom is light golden.\nGently check under edge. Omelette should be highly puffed and center slightly wobbly. Fold omelette in half and slide onto a warm serving plate.",
                category = "Breakfast",
                place = "France (Parisian Bakery)",
                imageUrl = "https://images.unsplash.com/photo-1525351484163-7529414344d8?q=80&w=800&auto=format&fit=crop",
                prepTimeMinutes = 10,
                cookTimeMinutes = 5,
                difficulty = "Medium"
            ),
            RecipeEntity(
                title = "Kyoto Tonkotsu Shoyu Ramen",
                description = "Soul-warming master ramen. Chewy wheat noodles submerged in a savory soy-sauce infused triple pork-bone broth, adorned with braised chashu pork belly and soft-boiled tea egg.",
                ingredients = "2 Bundles fresh Wheat Ramen Noodles\n750ml Premium Tonkotsu Bone Broth\n30ml Chashu soy braising liquid (Tare)\n4 Thick Slices Slow-Braised Pork Belly (Chashu)\n1 Soft-Boiled Ajitsuke Tamago (Marinated Egg)\n2 Stalks Green Scallion (finely slivered)\n2 Nori Seaweed Squares\n10g Black Garlic Oil (Mayu)",
                instructions = "In a dedicated pot, heat pre-prepared Tonkotsu pork broth until boiling. Season with salt and soy braising tare liquid.\nIn another pot, boil water and cook fresh ramen noodles for exactly 90 seconds (Futsū - medium-firm texture). Drain extremely well.\nWarm your serving bowls with boiling water, empty the water, and pour hot flavored broth into the bowls.\nAdd the drained ramen noodles, using chopsticks to lift and fold them neatly within the dark rich broth.\nLay down thick pieces of heated braised pork belly alongside halved soy-marinated soft boiled tea egg.\nGarnish neatly with mounds of slivered scallions, slide nori squares along side wall of bowl, and drizzle earthy black garlic oil on top.",
                category = "Dinner",
                place = "Japan (Kyoto Tea Pavilion)",
                imageUrl = "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?q=80&w=800&auto=format&fit=crop",
                prepTimeMinutes = 15,
                cookTimeMinutes = 10,
                difficulty = "Hard"
            ),
            RecipeEntity(
                title = "Backyard Double Smoked Burger",
                description = "Pure tavern satisfaction. Double-stacked chuck patties charred on cast iron with running cheddar cheese, grilled sweet onions, and smoky hickory secret sauce.",
                ingredients = "300g Fresh Ground Beef Chuck (80/20 lean to fat ratio)\n2 Soft Potato Sesame Brioche Buns\n4 Cheddar Cheese Slices\n1 Sweet White Onion (sliced thick)\n30g Salted butter\n1 Sweet Pickle (thinly sliced)\n30ml Thousand Island Dressing combined with Hickory BBQ Sauce",
                instructions = "Portion ground beef chuck into four 75g balls. Keep ground beef cold until ready to cook to lock in fats.\nHeat a flat cast iron griddle over high heat until smoking. Butter potato buns and toast on skillet until golden. Set buns aside.\nLightly oil griddle and place onions. Cook for 5 minutes until soft and heavily caramelized.\nPlace cold beef balls on screaming hot griddle, spacing them out. Cover each with parchment paper and smash flat using a heavy spatula.\nSeason heavily with kosher salt and black pepper. Cook undisturbed for 2 minutes until an intense dark lacy crust forms on bottom.\nScrape patties up from griddle, flip them over, and place cheddar cheese slice on top. Stack cooked patties on top of each other.\nAssemble burger: spread secret sauce on bottom bun, lay pickles, double cheeseburger stack, grilled sweet onions, and crown with top bun.",
                category = "Lunch",
                place = "America (Backyard Tavern)",
                imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?q=80&w=800&auto=format&fit=crop",
                prepTimeMinutes = 15,
                cookTimeMinutes = 8,
                difficulty = "Easy"
            ),
            RecipeEntity(
                title = "Midnight Garlic Butter Noodles",
                description = "The ultimate 10-minute midnight pantry craving. Chewy egg noodles tossed in a rich, salty garlic-butter emulsion topped with dynamic sesame oil and chili flakes.",
                ingredients = "150g Hand-pulled Egg Noodles or Spaghetti\n25g Pure Salted Butter\n4 Garlic Cloves (very finely minced)\n15ml Light Soy Sauce\n10g Oyster Sauce or Sweet Soy Sauce\n5ml Premium Toasted Sesame Oil\n3g Chili Flakes\n1 Stalk Green Scallion (chopped, optional)",
                instructions = "Boil egg noodles or dry spaghetti in salted water for 8 minutes (or according to instructions) until tender. Scoop out 60ml of hot noodle cooking water, then drain noodles.\nWhile noodles boil, melt butter in a frying pan over low heat.\nAdd minced garlic and half of chili flakes to melted butter, cooking on low for 1-2 minutes until garlic is soft and fragrant (do not let garlic burn or brown).\nWhisper soy sauce, oyster sauce, and toasted sesame oil directly into garlic butter.\nDump the hot drained noodles into pan, and stir everything to coat. Splash a small spoonful of noodle cooking water to create a creamy soy glaze.\nTurn off cooking heat, toss with chopped scallions, sprinkle remaining chili flakes, and eat from pan.",
                category = "Dinner",
                place = "Midnight Pantry",
                imageUrl = "https://images.unsplash.com/photo-1585032226651-759b368d7246?q=80&w=800&auto=format&fit=crop",
                prepTimeMinutes = 3,
                cookTimeMinutes = 7,
                difficulty = "Easy"
            )
        )
    }
}
