package com.example.ukopia.ui.recipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ukopia.R
import com.example.ukopia.data.BrewMethod
import com.example.ukopia.data.RecipeItem

class RecipeViewModel : ViewModel() {

    private val _brewMethods = MutableLiveData<List<BrewMethod>>()
    val brewMethods: LiveData<List<BrewMethod>> = _brewMethods

    private val _allRecipes = MutableLiveData<List<RecipeItem>>()
    val allRecipes: LiveData<List<RecipeItem>> = _allRecipes

    init {
        _brewMethods.value = listOf(
            BrewMethod("AEROPRESS", R.drawable.ic_aeropress),
            BrewMethod("CHEMEX", R.drawable.ic_chemex),
            BrewMethod("DELTER PRESS", R.drawable.ic_delterpress),
            BrewMethod("FRENCH PRESS", R.drawable.ic_frenchpress),
        )

        _allRecipes.value = listOf(
            RecipeItem(
                id = "aero_2018_winner",
                method = "AEROPRESS",
                name = "2018 Winner - Inverted",
                description = "Resep juara dunia Aeropress 2018. Menggunakan metode inverted untuk ekstraksi penuh.",
                waterAmount = "200.0 ml", coffeeAmount = "35.0 g", grindSize = "Medium-Fine", heat = "80°C", time = "01:45", isMine = false,
                steps = emptyList() // PASTIKAN ADA
            ),
            RecipeItem(
                id = "aero_standard_upright",
                method = "AEROPRESS",
                name = "Standard Upright",
                description = "Metode standar Aeropress untuk pemula. Cepat dan konsisten.",
                waterAmount = "220.0 ml", coffeeAmount = "15.0 g", grindSize = "Fine", heat = "85°C", time = "01:10", isMine = true,
                steps = emptyList() // PASTIKAN ADA
            ),
            RecipeItem(
                id = "chemex_classic",
                method = "CHEMEX",
                name = "Chemex Classic Brew",
                description = "Resep dasar untuk Chemex, menghasilkan kopi yang bersih dan jernih.",
                waterAmount = "400.0 ml", coffeeAmount = "25.0 g", grindSize = "Medium-Coarse", heat = "90°C", time = "04:00", isMine = false,
                steps = emptyList() // PASTIKAN ADA
            ),
            RecipeItem(
                id = "french_press_full_immersion",
                method = "FRENCH PRESS",
                name = "Full Immersion Standard",
                description = "Resep French Press untuk body kopi yang tebal dan kaya rasa.",
                waterAmount = "350.0 ml", coffeeAmount = "22.0 g", grindSize = "Coarse", heat = "95°C", time = "04:30", isMine = false,
                steps = emptyList() // PASTIKAN ADA
            ),
            RecipeItem(
                id = "delter_basic_inverted",
                method = "DELTER PRESS",
                name = "Delter Basic Inverted",
                description = "Resep awal untuk Delter Press dengan metode inverted.",
                waterAmount = "180.0 ml", coffeeAmount = "18.0 g", grindSize = "Fine-Medium", heat = "88°C", time = "01:30", isMine = false,
                steps = emptyList() // PASTIKAN ADA
            )
        )
    }

    fun addRecipe(recipe: RecipeItem) {
        val currentList = _allRecipes.value?.toMutableList() ?: mutableListOf()
        currentList.add(0, recipe)
        _allRecipes.value = currentList
    }
}