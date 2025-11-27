package com.example.ukopia.ui.recipe

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ukopia.data.*
import com.example.ukopia.models.ApiClient
import kotlinx.coroutines.launch

class RecipeViewModel : ViewModel() {

    // --- Brew Methods (Kategori Resep) ---
    private val _brewMethods = MutableLiveData<List<BrewMethod>>()
    val brewMethods: LiveData<List<BrewMethod>> = _brewMethods

    // --- Recipes List ---
    private val _allRecipes = MutableLiveData<List<RecipeItem>>()
    val allRecipes: LiveData<List<RecipeItem>> = _allRecipes

    // --- Equipment Categories ---
    private val _equipmentCategories = MutableLiveData<List<EquipmentItem>>()
    val equipmentCategories: LiveData<List<EquipmentItem>> = _equipmentCategories

    // --- Sub Equipment List ---
    private val _subEquipmentList = MutableLiveData<List<SubEquipmentItem>>()
    val subEquipmentList: LiveData<List<SubEquipmentItem>> = _subEquipmentList

    // --- Loading & Error State (Optional tapi bagus) ---
    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    // 1. Load Metode (Dipanggil di RecipeFragment)
    fun loadBrewMethods() {
        viewModelScope.launch {
            isLoading.postValue(true)
            try {
                val response = ApiClient.instance.getBrewMethods()
                if (response.isSuccessful && response.body()?.success == true) {
                    _brewMethods.value = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("API_METODE", "Error: ${e.message}")
            }finally {
                isLoading.postValue(false)
            }
        }
    }

    // 2. Load Resep (Dipanggil di RecipeListFragment)
    fun loadRecipes(methodId: Int, type: String, uid: Int) {
        isLoading.value = true
        Log.d("DEBUG_API", "Requesting: MethodID=$methodId, Type=$type, UID=$uid") // <--- CEK LOG INI

        viewModelScope.launch {
            try {
                val response = ApiClient.instance.getRecipes(methodId, type, uid)
                if (response.isSuccessful && response.body()?.success == true) {
                    val recipes = response.body()?.data ?: emptyList()
                    _allRecipes.value = recipes
                    Log.d("DEBUG_API", "Sukses! Dapat ${recipes.size} resep") // <--- CEK LOG INI
                } else {
                    Log.e("DEBUG_API", "Gagal: ${response.message()}")
                    _allRecipes.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("DEBUG_API", "Error Koneksi: ${e.message}")
                _allRecipes.value = emptyList()
            } finally {
                isLoading.value = false
            }
        }
    }

    // 3. Create Resep (Dipanggil di AddRecipeFragment)
    fun createRecipe(request: CreateRecipeRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.instance.createRecipe(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess()
                } else {
                    onError(response.body()?.message ?: "Gagal menyimpan")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error jaringan")
            } finally {
                isLoading.value = false
            }
        }
    }

    // 4. Load Kategori Alat
    fun loadEquipmentCategories() {
        viewModelScope.launch {
            try {
                val res = ApiClient.instance.getEquipmentCategories()
                if(res.isSuccessful) _equipmentCategories.value = res.body()?.data ?: emptyList()
            } catch (e: Exception) { Log.e("API_EQ", "${e.message}") }
        }
    }

    // 5. Load Alat by Kategori
    fun loadToolsByCategory(catId: Int) {
        viewModelScope.launch {
            try {
                val res = ApiClient.instance.getToolsByCategory(catId)
                if(res.isSuccessful) _subEquipmentList.value = res.body()?.data ?: emptyList()
            } catch (e: Exception) { Log.e("API_TOOL", "${e.message}") }
        }
    }
}