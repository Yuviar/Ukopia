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

    // API Service untuk fitur ulasan (non-cache)
    private val apiService = ApiClient.instance

    // === Untuk MenuFragment (Daftar Menu dari ROOM) ===
    val menuItems: LiveData<List<MenuApiItem>> = repository.allMenuItems

    // === Loading & Error (Umum) ===
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // === Untuk DetailMenuFragment (Daftar Ulasan dari API) ===
    private val _reviews = MutableLiveData<List<ReviewApiItem>>()
    val reviews: LiveData<List<ReviewApiItem>> = _reviews
    private val _userReview = MutableLiveData<ReviewApiItem?>()
    val userReview: LiveData<ReviewApiItem?> = _userReview

    // === Untuk RatingFragment (Hasil Post/Update ke API) ===
    private val _reviewPostSuccess = MutableLiveData<Boolean>()
    val reviewPostSuccess: LiveData<Boolean> = _reviewPostSuccess


    // --- FUNGSI-FUNGSI ---

    // Dipanggil oleh MenuFragment
    fun fetchMenuItems() {
        viewModelScope.launch {
            repository.refreshMenu() // Minta repository untuk refresh
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
                    _reviews.value = allReviews
                    _userReview.value = allReviews.find { it.is_owner }
                } else {
                    _errorMessage.value = "Gagal memuat detail: ${response.message()}"
                }
            } catch (e: Exception) {
                // Pastikan blok catch memiliki kurung kurawal
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Dipanggil oleh RatingFragment
    fun submitReview(request: ReviewPostRequest) {
        _isLoading.value = true
        _reviewPostSuccess.value = false
        viewModelScope.launch {
            try {
                val response = apiService.postReview(request)
                if (response.isSuccessful) {
                    _reviewPostSuccess.value = true
                    // Refresh data ulasan di detail (opsional)
                    fetchMenuDetails(request.id_menu, request.uid_akun)
                } else {
                    _errorMessage.value = "Gagal mengirim ulasan: ${response.message()}"
                }
            } catch (e: Exception) { // PERBAIKAN: Tambahkan kurung kurawal di sini
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // PERBAIKAN: Fungsi ini dipindahkan ke dalam kelas MenuViewModel
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // PERBAIKAN: Fungsi ini dipindahkan ke dalam kelas MenuViewModel
    fun resetReviewPostStatus() {
        _reviewPostSuccess.value = false
    }
}


// TAMBAHKAN: Factory untuk ViewModel
class MenuViewModelFactory(private val repository: MenuRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MenuViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}