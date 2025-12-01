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
import com.example.ukopia.data.RewardHistoryItem
import com.example.ukopia.data.RewardHistoryResponse // <--- Import ini WAJIB ada
import com.example.ukopia.models.ApiClient
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class LoyaltyViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData untuk daftar Loyalty (Jurnal/Review)
    private val _loyaltyItems = MutableLiveData<List<LoyaltyItemV2>>()
    val loyaltyItems: LiveData<List<LoyaltyItemV2>> = _loyaltyItems

    // LiveData untuk Status User (Poin Total)
    private val _loyaltyUserStatus = MutableLiveData<LoyaltyUserStatus>()
    val loyaltyUserStatus: LiveData<LoyaltyUserStatus> = _loyaltyUserStatus

    // --- LiveData untuk Riwayat Reward ---
    // Menggunakan List? agar bisa menghandle state awal
    private val _rewardHistory = MutableLiveData<List<RewardHistoryItem>?>()
    val rewardHistory: LiveData<List<RewardHistoryItem>?> = _rewardHistory

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
    }

    // --- FUNGSI FETCH REWARD HISTORY (VERSI PALING AMAN) ---
    fun fetchRewardHistory(uid: Int) {
        if (uid == 0) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // 1. Panggil API
                val response = ApiClient.instance.getRewardHistory(uid)

                // 2. Simpan body ke variabel
                val body = response.body()

                // 3. Cek sukses dengan Safe Call (?.)
                // body?.success == true otomatis mengembalikan false jika body null
                if (response.isSuccessful && body?.success == true) {

                    // 4. Ambil data dengan Elvis Operator
                    // Jika body.data NULL, maka diganti dengan list kosong (emptyList)
                    // Ini mencegah Type Mismatch Error
                    val items = body.data ?: emptyList()

                    _rewardHistory.value = items
                    Log.d("LoyaltyViewModel", "Reward history fetched: ${items.size} items")
                } else {
                    // 5. Handle Error dengan aman
                    val msg = body?.message ?: response.errorBody()?.string() ?: "Unknown error"
                    Log.e("LoyaltyViewModel", "Failed to fetch reward history: $msg")

                    _rewardHistory.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("LoyaltyViewModel", "Error fetching reward history: ${e.message}", e)
                _rewardHistory.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshLoyaltyData() {
        val context = getApplication<Application>()
        val uid = SessionManager.getUid(context)

        if (uid == 0) {
            _isLoading.value = false
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val pendingDef = async { ApiClient.instance.getLoyaltyList(uid, "pending") }
                val historyDef = async { ApiClient.instance.getLoyaltyList(uid, "history") }
                val statusDef = async { ApiClient.instance.getLoyaltyStatus(uid) }

                val pendingRes = pendingDef.await()
                val historyRes = historyDef.await()
                val statusRes = statusDef.await()

                val allLoyaltyItems = mutableListOf<LoyaltyItemV2>()

                // Gunakan ?.data ?: emptyList() untuk keamanan
                if (pendingRes.isSuccessful) {
                    allLoyaltyItems.addAll(pendingRes.body()?.data ?: emptyList())
                }

                if (historyRes.isSuccessful) {
                    allLoyaltyItems.addAll(historyRes.body()?.data ?: emptyList())
                }

                _loyaltyItems.value = allLoyaltyItems.sortedByDescending { it.tanggal }

                if (statusRes.isSuccessful) {
                    statusRes.body()?.data?.let { updateLoyaltyStatus(it) }
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
            onError("User tidak login.")
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

        viewModelScope.launch {
            try {
                val response = ApiClient.instance.updateLoyaltyReview(params)
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { updateLoyaltyStatus(it) }
                    refreshLoyaltyData()
                    onSuccess()
                } else {
                    onError(response.body()?.message ?: "Gagal menyimpan review")
                }
            } catch (e: Exception) {
                onError("Kesalahan koneksi: ${e.message}")
            }
        }
    }
}