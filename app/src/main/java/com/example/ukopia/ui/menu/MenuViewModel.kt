package com.example.ukopia.ui.menu

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ukopia.models.ApiClient
import com.example.ukopia.models.MenuApiItem
import com.example.ukopia.models.ReviewApiItem
import com.example.ukopia.models.ReviewPostRequest
import kotlinx.coroutines.launch

class MenuViewModel(private val repository: MenuRepository) : ViewModel() {

    // ... (properti lain tetap sama) ...
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

    // --- FUNGSI-FUNGSI ---

    fun fetchMenuItems() {
        viewModelScope.launch {
            repository.refreshMenu()
        }
    }

    // Dipanggil oleh DetailMenuFragment
    fun fetchMenuDetails(menuId: Int, userId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.getMenuDetails(menuId, userId)
                if (response.isSuccessful) {
                    val allReviews = response.body()?.data_ulasan ?: emptyList()

                    // ==========================================================
                    // PERBAIKAN LOGIKA DI SINI
                    // ==========================================================
                    // _reviews sekarang HANYA berisi ulasan orang lain
                    _reviews.value = allReviews.filter { it.is_owner == 0 }

                    // _userReview tetap berisi ulasan milik user
                    _userReview.value = allReviews.find { it.is_owner == 1 }
                    // ==========================================================

                } else {
                    _errorMessage.value = "Gagal memuat detail: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ... (Fungsi submitReview, clearErrorMessage, resetReviewPostStatus tetap sama) ...
    fun submitReview(request: ReviewPostRequest) {
        _isLoading.value = true
        _reviewPostSuccess.value = false
        viewModelScope.launch {
            try {
                val response = apiService.postReview(request)
                if (response.isSuccessful) {
                    _reviewPostSuccess.value = true
                    fetchMenuDetails(request.id_menu, request.uid_akun)
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


// ... (Factory tetap sama) ...
class MenuViewModelFactory(private val repository: MenuRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MenuViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}