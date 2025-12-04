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

    private val _brewMethods = MutableLiveData<List<BrewMethod>>()
    val brewMethods: LiveData<List<BrewMethod>> = _brewMethods

    private val _allRecipes = MutableLiveData<List<RecipeItem>>()
    val allRecipes: LiveData<List<RecipeItem>> = _allRecipes

    private val _equipmentCategories = MutableLiveData<List<EquipmentItem>>()
    val equipmentCategories: LiveData<List<EquipmentItem>> = _equipmentCategories

    private val _subEquipmentList = MutableLiveData<List<SubEquipmentItem>>()
    val subEquipmentList: LiveData<List<SubEquipmentItem>> = _subEquipmentList

    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    private var lastLoadedParams: Triple<Int, String, Int>? = null

    fun loadBrewMethods() {
        if (!_brewMethods.value.isNullOrEmpty()) return

        viewModelScope.launch {
            isLoading.postValue(true)
            try {
                val response = ApiClient.instance.getBrewMethods()
                if (response.isSuccessful && response.body()?.success == true) {
                    _brewMethods.value = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("API_METODE", "Error: ${e.message}")
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun loadRecipes(methodId: Int, type: String, uid: Int) {
        // Simpan parameter saat ini
        val currentParams = Triple(methodId, type, uid)

        if (currentParams == lastLoadedParams && !_allRecipes.value.isNullOrEmpty()) {
            Log.d("RecipeViewModel", "Using cached recipes")
            return
        }

        isLoading.value = true
        lastLoadedParams = currentParams // Update parameter terakhir

        viewModelScope.launch {
            try {
                val response = ApiClient.instance.getRecipes(methodId, type, uid)
                if (response.isSuccessful && response.body()?.success == true) {
                    val recipes = response.body()?.data ?: emptyList()
                    _allRecipes.value = recipes
                    Log.d("DEBUG_API", "Sukses! Dapat ${recipes.size} resep")
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

    fun loadEquipmentCategories() {
        if (!_equipmentCategories.value.isNullOrEmpty()) return

        viewModelScope.launch {
            try {
                val res = ApiClient.instance.getEquipmentCategories()
                if(res.isSuccessful) _equipmentCategories.value = res.body()?.data ?: emptyList()
            } catch (e: Exception) { Log.e("API_EQ", "${e.message}") }
        }
    }

    fun loadToolsByCategory(catId: Int) {
        viewModelScope.launch {
            try {
                val res = ApiClient.instance.getToolsByCategory(catId)
                if(res.isSuccessful) _subEquipmentList.value = res.body()?.data ?: emptyList()
            } catch (e: Exception) { Log.e("API_TOOL", "${e.message}") }
        }
    }
    fun refreshRecipes(methodId: Int, type: String, uid: Int) {
        lastLoadedParams = null
        loadRecipes(methodId, type, uid)
    }
    fun refreshBrewMethods() {
        viewModelScope.launch {
            isLoading.postValue(true)
            try {
                val response = ApiClient.instance.getBrewMethods()
                if (response.isSuccessful && response.body()?.success == true) {
                    _brewMethods.value = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("API_METODE", "Error: ${e.message}")
            } finally {
                isLoading.postValue(false)
            }
        }
    }
}