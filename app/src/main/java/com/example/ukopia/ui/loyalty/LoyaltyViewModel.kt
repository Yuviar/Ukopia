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
import kotlin.math.min

class LoyaltyViewModel(application: Application) : AndroidViewModel(application) {

    private val _loyaltyItems = MutableLiveData<List<LoyaltyItemV2>>()
    val loyaltyItems: LiveData<List<LoyaltyItemV2>> = _loyaltyItems

    private val _loyaltyUserStatus = MutableLiveData<LoyaltyUserStatus>()
    val loyaltyUserStatus: LiveData<LoyaltyUserStatus> = _loyaltyUserStatus

    init {
        _loyaltyItems.value = emptyList()
        _loyaltyUserStatus.value = SessionManager.SessionManager.getLoyaltyUserStatus(application)
    }

    fun addPurchase(item: LoyaltyItemV2) {
        val currentItems = _loyaltyItems.value?.toMutableList() ?: mutableListOf()
        currentItems.add(0, item)
        _loyaltyItems.value = currentItems

        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        val updatedPurchases = currentStatus.totalPurchases + 1
        val updatedPoints = currentStatus.totalPoints + 1

        val newStatus = LoyaltyUserStatus(updatedPurchases, updatedPoints)
        _loyaltyUserStatus.value = newStatus

        SessionManager.SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
    }

    // Sekarang menerima Context sebagai parameter
    fun getLoyaltyLevel(context: Context): Pair<String, Int> {
        val totalPoints = _loyaltyUserStatus.value?.totalPoints ?: 0

        return when {
            totalPoints >= 20 -> Pair(context.getString(R.string.loyalty_level_platinum), R.drawable.ic_badge_platinum)
            totalPoints >= 10 -> Pair(context.getString(R.string.loyalty_level_gold), R.drawable.ic_badge_gold)
            totalPoints >= 5 -> Pair(context.getString(R.string.loyalty_level_silver), R.drawable.ic_badge_silver)
            else -> Pair(context.getString(R.string.loyalty_level_bronze), R.drawable.ic_badge_bronze)
        }
    }

    fun getVisualStampProgress(): Pair<Int, Int> {
        val totalPoints = _loyaltyUserStatus.value?.totalPoints ?: 0
        val visualProgressMax = 20
        val currentVisualProgress = min(totalPoints, visualProgressMax)
        return Pair(currentVisualProgress, visualProgressMax)
    }

    // Sekarang menerima Context sebagai parameter
    fun getRewardProgressMessage(context: Context): String {
        val totalPoints = _loyaltyUserStatus.value?.totalPoints ?: 0

        return when {
            totalPoints < 5 -> {
                context.getString(R.string.progress_intro_format, totalPoints, 5)
            }
            totalPoints < 10 -> {
                context.getString(R.string.reward_10_percent_discount_progress_format, totalPoints, 10)
            }
            totalPoints < 20 -> {
                context.getString(R.string.reward_free_serve_progress_format, totalPoints, 20)
            }
            else -> {
                context.getString(R.string.reward_free_tshirt_earned)
            }
        }
    }

    fun getTotalPoints(): Int {
        return loyaltyUserStatus.value?.totalPoints ?: 0
    }
}