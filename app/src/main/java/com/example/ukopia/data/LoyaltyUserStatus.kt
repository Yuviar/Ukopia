package com.example.ukopia.data

data class LoyaltyUserStatus(
    val totalPoints: Int = 0,
    val isDiscount10Claimed: Boolean = false, // 5 poin
    val isFreeServeClaimed: Boolean = false, // 10 poin
    val isDiscount10Slot15Claimed: Boolean = false, // 15 poin
    val isFreeTshirtClaimed: Boolean = false, // 20 poin
    val isCoffeeGrinderClaimed: Boolean = false, // 100 poin
    val isDiscount10_25Claimed: Boolean = false, // 25 poin
    val isFreeServe_30Claimed: Boolean = false, // 30 poin
    val isDiscount10_35Claimed: Boolean = false, // 35 poin
    val isFreeServe_40Claimed: Boolean = false, // 40 poin
)