package com.example.ukopia.ui.menu

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ukopia.models.ApiClient
import com.example.ukopia.models.MenuApiItem
import com.example.ukopia.models.ReviewApiItem
import com.example.ukopia.models.ReviewPostRequest
import kotlinx.coroutines.launch

class MenuViewModel(private val repository: MenuRepository) : ViewModel() {

    private val apiService = ApiClient.instance
    val menuItems: LiveData<List<MenuApiItem>> = repository.allMenuItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _reviews = MutableLiveData<List<ReviewApiItem>>()
    val reviews: LiveData<List<ReviewApiItem>> = _reviews

    private val _userReview = MutableLiveData<ReviewApiItem?>()
    val userReview: LiveData<ReviewApiItem?> = _userReview

    private val _reviewPostSuccess = MutableLiveData<Boolean>()
    val reviewPostSuccess: LiveData<Boolean> = _reviewPostSuccess

    private val _currentDetailMenuItem = MutableLiveData<MenuApiItem?>()
    val currentDetailMenuItem: LiveData<MenuApiItem?> = _currentDetailMenuItem

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    fun fetchMenuItems() {
        viewModelScope.launch {
            repository.refreshMenu()
        }
    }

    fun fetchCategories(defaultCategoryName: String) {
        viewModelScope.launch {
            val apiCategories = repository.getCategories()

            val fullList = mutableListOf(defaultCategoryName)
            fullList.addAll(apiCategories)

            _categories.value = fullList
        }
    }

    fun fetchMenuDetails(menuId: Int, userId: Int) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val responseReviews = apiService.getMenuDetails(menuId, userId)
                if (responseReviews.isSuccessful) {
                    val allReviews = responseReviews.body()?.data_ulasan ?: emptyList()
                    _reviews.value = allReviews.filter { it.is_owner == 0 }
                    _userReview.value = allReviews.find { it.is_owner == 1 }
                } else {
                    _errorMessage.value = "Gagal memuat ulasan: ${responseReviews.message()}"
                }

                val responseMenuItem = apiService.getMenu(id_kategori = null, menuId = menuId)
                if (responseMenuItem.isSuccessful) {
                    val menuItemData = responseMenuItem.body()?.data
                    if (menuItemData != null && menuItemData.isNotEmpty()) {
                        _currentDetailMenuItem.value = menuItemData.firstOrNull()
                    } else {
                        _currentDetailMenuItem.value = null
                    }
                } else {
                    _errorMessage.value = "Gagal memuat detail menu: ${responseMenuItem.message()}"
                }

            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                Log.e("MenuViewModel", "Error fetching menu details", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitReview(request: ReviewPostRequest) {
        _isLoading.value = true
        _reviewPostSuccess.value = false
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val response = apiService.postReview(request)
                if (response.isSuccessful) {
                    _reviewPostSuccess.value = true
                    fetchMenuDetails(request.id_menu, request.uid_akun)
                    repository.refreshMenu()
                } else {
                    _errorMessage.value = "Gagal mengirim ulasan: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun resetReviewPostStatus() {
        _reviewPostSuccess.value = false
    }
}