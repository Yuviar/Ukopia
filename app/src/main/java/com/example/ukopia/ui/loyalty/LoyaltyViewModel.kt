// File: D:/github_rama/Ukopia/app/src/main/java/com/example/ukopia/ui/loyalty/LoyaltyViewModel.kt
package com.example.ukopia.ui.loyalty

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ukopia.SessionManager
import com.example.ukopia.data.LoyaltyItemV2
import com.example.ukopia.data.LoyaltyUserStatus
import com.example.ukopia.models.ApiClient
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class LoyaltyViewModel(application: Application) : AndroidViewModel(application) {

    // Ganti nama LiveData agar lebih sesuai, karena sekarang akan menyimpan semua item
    private val _loyaltyItems = MutableLiveData<List<LoyaltyItemV2>>()
    val loyaltyItems: LiveData<List<LoyaltyItemV2>> = _loyaltyItems // Ganti _pendingItems menjadi _loyaltyItems

    private val _loyaltyUserStatus = MutableLiveData<LoyaltyUserStatus>()
    val loyaltyUserStatus: LiveData<LoyaltyUserStatus> = _loyaltyUserStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadLocalData()
        refreshLoyaltyData()
    }

    private fun loadLocalData() {
        val context = getApplication<Application>()
        _loyaltyUserStatus.value = SessionManager.getLoyaltyUserStatus(context)
    }

    private fun updateLoyaltyStatus(newStatus: LoyaltyUserStatus) {
        val context = getApplication<Application>()
        SessionManager.saveLoyaltyUserStatus(context, newStatus)
        _loyaltyUserStatus.value = newStatus
        Log.d("LoyaltyViewModel", "Loyalty status updated: ${newStatus.totalPoints} points")
        Log.d("LoyaltyViewModel", "Claim dates (sample): 5 pts: ${newStatus.discount10ClaimDate}, 10 pts: ${newStatus.freeServeClaimDate}")
    }

    fun refreshLoyaltyData() {
        val context = getApplication<Application>()
        val uid = SessionManager.getUid(context)

        if (uid == 0) {
            Log.w("LoyaltyViewModel", "UID is 0, skipping loyalty data refresh.")
            _isLoading.value = false
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Panggil API secara paralel: list pending, list history, dan status
                val pendingDef = async { ApiClient.instance.getLoyaltyList(uid, "pending") }
                val historyDef = async { ApiClient.instance.getLoyaltyList(uid, "history") } // Panggil untuk history
                val statusDef = async { ApiClient.instance.getLoyaltyStatus(uid) }

                val pendingRes = pendingDef.await()
                val historyRes = historyDef.await() // Ambil hasil history
                val statusRes = statusDef.await()

                // 1. Gabungkan List Pending dan History
                val allLoyaltyItems = mutableListOf<LoyaltyItemV2>()

                if (pendingRes.isSuccessful) {
                    val items = pendingRes.body()?.data ?: emptyList()
                    allLoyaltyItems.addAll(items)
                    Log.d("LoyaltyViewModel", "Pending items fetched: ${items.size}")
                } else {
                    Log.e("LoyaltyViewModel", "Failed to fetch pending items: ${pendingRes.errorBody()?.string()}")
                }

                if (historyRes.isSuccessful) {
                    val items = historyRes.body()?.data ?: emptyList()
                    allLoyaltyItems.addAll(items)
                    Log.d("LoyaltyViewModel", "History items fetched: ${items.size}")
                } else {
                    Log.e("LoyaltyViewModel", "Failed to fetch history items: ${historyRes.errorBody()?.string()}")
                }

                // Sortir berdasarkan tanggal terbaru agar item yang baru selesai muncul di atas
                _loyaltyItems.value = allLoyaltyItems.sortedByDescending { it.tanggal } // Ganti _pendingItems menjadi _loyaltyItems

                // 2. Update Status User (Poin & Klaim Reward)
                if (statusRes.isSuccessful) {
                    statusRes.body()?.data?.let { newStatus ->
                        updateLoyaltyStatus(newStatus)
                    } ?: Log.e("LoyaltyViewModel", "Loyalty status data is null in response.")
                } else {
                    Log.e("LoyaltyViewModel", "Failed to fetch loyalty status: ${statusRes.errorBody()?.string()}")
                }

            } catch (e: Exception) {
                Log.e("LoyaltyViewModel", "Error refreshing loyalty data: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitReview(
        item: LoyaltyItemV2,
        catatan: String,
        aroma: Int, sweetness: Int, acidity: Int, bitterness: Int, body: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val context = getApplication<Application>()
        val uid = SessionManager.getUid(context)

        if (uid == 0) {
            onError("User tidak login atau UID tidak ditemukan.")
            return
        }

        val params = mutableMapOf<String, Any>(
            "id_loyalty" to item.idLoyalty,
            "uid_akun" to uid,
            "catatan" to catatan
        )

        if (item.isCoffee) {
            params["aroma"] = aroma
            params["kemanisan"] = sweetness
            params["keasaman"] = acidity
            params["kepahitan"] = bitterness
            params["kekentalan"] = body
        }

        Log.d("LoyaltyViewModel", "Mengirim review dengan params: $params")

        viewModelScope.launch {
            try {
                val response = ApiClient.instance.updateLoyaltyReview(params)
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d("LoyaltyViewModel", "Review berhasil disimpan untuk id_loyalty: ${item.idLoyalty}")

                    response.body()?.data?.let { newStatus ->
                        updateLoyaltyStatus(newStatus)
                    }

                    refreshLoyaltyData() // Panggil untuk update list item
                    onSuccess()
                } else {
                    val errorBodyString = response.errorBody()?.string() // Dapatkan error body sebagai string
                    val errorMessage = response.body()?.message ?: errorBodyString ?: "Gagal menyimpan review"
                    Log.e("LoyaltyViewModel", "API Error saat submitReview (HTTP ${response.code()}): $errorMessage")
                    Log.e("LoyaltyViewModel", "Raw Response Body (HTTP ${response.code()}): $errorBodyString")
                    onError(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("LoyaltyViewModel", "Kesalahan koneksi saat submitReview: ${e.message}", e)
                onError("Kesalahan koneksi: ${e.message}")
            }
        }
    }
}