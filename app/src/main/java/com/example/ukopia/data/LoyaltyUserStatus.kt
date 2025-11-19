package com.example.ukopia.data

data class LoyaltyUserStatus(
    val totalPoints: Int = 0,
    val isDiscount10Claimed: Boolean = false, // 5 poin
    val isFreeServeClaimed: Boolean = false, // 10 poin
    val isDiscount10Slot15Claimed: Boolean = false, // 15 poin
    val isFreeTshirtClaimed: Boolean = false, // 20 poin - Only T-shirt reward
    val isDiscount10_25Claimed: Boolean = false, // 25 poin
    val isFreeServe_30Claimed: Boolean = false, // 30 poin
    val isDiscount10_35Claimed: Boolean = false, // 35 poin
    val isFreeServe_40Claimed: Boolean = false, // 40 poin
    val isDiscount10_45Claimed: Boolean = false, // 45 poin
    val isFreeServe_50Claimed: Boolean = false, // 50 poin
    val isDiscount10_55Claimed: Boolean = false, // 55 poin
    val isFreeServe_60Claimed: Boolean = false, // 60 poin - Changed from Free T-shirt to Free Serve
    val isDiscount10_65Claimed: Boolean = false, // 65 poin
    val isFreeServe_70Claimed: Boolean = false, // 70 poin
    val isDiscount10_75Claimed: Boolean = false, // 75 poin
    val isFreeServe_80Claimed: Boolean = false, // 80 poin
    val isDiscount10_85Claimed: Boolean = false, // 85 poin
    val isFreeServe_90Claimed: Boolean = false, // 90 poin
    val isDiscount10_95Claimed: Boolean = false // 95 poin
)