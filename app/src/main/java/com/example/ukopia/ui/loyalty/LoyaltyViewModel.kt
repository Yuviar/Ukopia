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

    fun claimDiscount10_45() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 45 && !currentStatus.isDiscount10_45Claimed) {
            val newStatus = currentStatus.copy(isDiscount10_45Claimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }

    fun claimFreeServe_50() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 50 && !currentStatus.isFreeServe_50Claimed) {
            val newStatus = currentStatus.copy(isFreeServe_50Claimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }

    fun claimDiscount10_55() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 55 && !currentStatus.isDiscount10_55Claimed) {
            val newStatus = currentStatus.copy(isDiscount10_55Claimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }

    // NEW: Function for Free Serve at 60 points
    fun claimFreeServe_60() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 60 && !currentStatus.isFreeServe_60Claimed) {
            val newStatus = currentStatus.copy(isFreeServe_60Claimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }

    fun claimDiscount10_65() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 65 && !currentStatus.isDiscount10_65Claimed) {
            val newStatus = currentStatus.copy(isDiscount10_65Claimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }

    fun claimFreeServe_70() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 70 && !currentStatus.isFreeServe_70Claimed) {
            val newStatus = currentStatus.copy(isFreeServe_70Claimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }

    fun claimDiscount10_75() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 75 && !currentStatus.isDiscount10_75Claimed) {
            val newStatus = currentStatus.copy(isDiscount10_75Claimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }

    fun claimFreeServe_80() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 80 && !currentStatus.isFreeServe_80Claimed) {
            val newStatus = currentStatus.copy(isFreeServe_80Claimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }

    fun claimDiscount10_85() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 85 && !currentStatus.isDiscount10_85Claimed) {
            val newStatus = currentStatus.copy(isDiscount10_85Claimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }

    fun claimFreeServe_90() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 90 && !currentStatus.isFreeServe_90Claimed) {
            val newStatus = currentStatus.copy(isFreeServe_90Claimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }

    fun claimDiscount10_95() {
        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        if (currentStatus.totalPoints >= 95 && !currentStatus.isDiscount10_95Claimed) {
            val newStatus = currentStatus.copy(isDiscount10_95Claimed = true)
            _loyaltyUserStatus.value = newStatus
            SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        }
    }
}