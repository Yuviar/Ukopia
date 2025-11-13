package com.example.ukopia.ui.loyalty

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ukopia.SessionManager
import com.example.ukopia.data.LoyaltyItemV2
import com.example.ukopia.data.LoyaltyUserStatus

class LoyaltyViewModel(application: Application) : AndroidViewModel(application) {

    private val _loyaltyItems = MutableLiveData<List<LoyaltyItemV2>>()
    val loyaltyItems: LiveData<List<LoyaltyItemV2>> = _loyaltyItems

    private val _loyaltyUserStatus = MutableLiveData<LoyaltyUserStatus>()
    val loyaltyUserStatus: LiveData<LoyaltyUserStatus> = _loyaltyUserStatus

    init {
        _loyaltyItems.value = emptyList()
        _loyaltyUserStatus.value = SessionManager.getLoyaltyUserStatus(application)
    }

    fun addPurchase(item: LoyaltyItemV2) {
        val currentItems = _loyaltyItems.value?.toMutableList() ?: mutableListOf()
        currentItems.add(0, item)
        _loyaltyItems.value = currentItems

        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        val updatedPoints = currentStatus.totalPoints + 1

        val newStatus = currentStatus.copy(
            totalPoints = updatedPoints
        )
        _loyaltyUserStatus.value = newStatus

        SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
    }

    fun claimDiscount10() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 5 && !currentStatus.isDiscount10Claimed) {
            val newStatus = currentStatus.copy(isDiscount10Claimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }

    fun claimFreeServe() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 10 && !currentStatus.isFreeServeClaimed) {
            val newStatus = currentStatus.copy(isFreeServeClaimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }

    fun claimDiscount10Slot15() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 15 && !currentStatus.isDiscount10Slot15Claimed) {
            val newStatus = currentStatus.copy(isDiscount10Slot15Claimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }

    fun claimFreeTshirt() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 20 && !currentStatus.isFreeTshirtClaimed) {
            val newStatus = currentStatus.copy(isFreeTshirtClaimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }

    fun claimCoffeeGrinder() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 100 && !currentStatus.isCoffeeGrinderClaimed) {
            val newStatus = currentStatus.copy(isCoffeeGrinderClaimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }

    // BARU: Fungsi klaim untuk reward tambahan (contoh)
    fun claimDiscount10_25() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 25 && !currentStatus.isDiscount10_25Claimed) {
            val newStatus = currentStatus.copy(isDiscount10_25Claimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }

    fun claimFreeServe_30() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 30 && !currentStatus.isFreeServe_30Claimed) {
            val newStatus = currentStatus.copy(isFreeServe_30Claimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }

    fun claimDiscount10_35() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 35 && !currentStatus.isDiscount10_35Claimed) {
            val newStatus = currentStatus.copy(isDiscount10_35Claimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }

    fun claimFreeServe_40() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 40 && !currentStatus.isFreeServe_40Claimed) {
            val newStatus = currentStatus.copy(isFreeServe_40Claimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }
}