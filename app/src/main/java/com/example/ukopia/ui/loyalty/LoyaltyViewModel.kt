package com.example.ukopia.ui.loyalty

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ukopia.R
import com.example.ukopia.SessionManager
import com.example.ukopia.data.LoyaltyItemV2
import com.example.ukopia.data.LoyaltyUserStatus

class LoyaltyViewModel(application: Application) : AndroidViewModel(application) {

    private val _loyaltyItems = MutableLiveData<List<LoyaltyItemV2>>()
    val loyaltyItems: LiveData<List<LoyaltyItemV2>> = _loyaltyItems

    private val _loyaltyUserStatus = MutableLiveData<LoyaltyUserStatus>()
    val loyaltyUserStatus: LiveData<LoyaltyUserStatus> = _loyaltyUserStatus

    init {
        // Inisialisasi dari SessionManager saat ViewModel dibuat
        _loyaltyItems.value = emptyList() // Atau muat dari penyimpanan jika ada
        _loyaltyUserStatus.value = SessionManager.getLoyaltyUserStatus(application)
    }

    fun addPurchase(item: LoyaltyItemV2) {
        val currentItems = _loyaltyItems.value?.toMutableList() ?: mutableListOf()
        currentItems.add(0, item)
        _loyaltyItems.value = currentItems

        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        val updatedPoints = currentStatus.totalPoints + 1 // Setiap pembelian = 1 poin/stempel

        val newStatus = currentStatus.copy(
            totalPoints = updatedPoints
        )
        _loyaltyUserStatus.value = newStatus

        SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
    }

    // Fungsi untuk mengklaim diskon 10% di slot 5
    fun claimDiscount10() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 5 && !currentStatus.isDiscount10Claimed) {
            val newStatus = currentStatus.copy(isDiscount10Claimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }

    // Fungsi untuk mengklaim gratis 1 serve di slot 10
    fun claimFreeServe() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 10 && !currentStatus.isFreeServeClaimed) {
            val newStatus = currentStatus.copy(isFreeServeClaimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }

    // PERBAIKAN: Menambahkan fungsi yang hilang untuk klaim di slot 15
    fun claimDiscount10Slot15() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 15 && !currentStatus.isDiscount10Slot15Claimed) {
            val newStatus = currentStatus.copy(isDiscount10Slot15Claimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }

    // Fungsi untuk mengklaim gratis kaos di slot 20
    fun claimFreeTshirt() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 20 && !currentStatus.isFreeTshirtClaimed) {
            val newStatus = currentStatus.copy(isFreeTshirtClaimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }
}