// File: D:/github_rama/Ukopia/app/src/main/java/com/example/ukopia/data/LoyaltyReward.kt
package com.example.ukopia.data

import com.example.ukopia.R

// Enum untuk tipe reward
enum class LoyaltyRewardType {
    DISCOUNT_10,
    FREE_SERVE,
    FREE_TSHIRT
}

// Data class untuk merepresentasikan satu reward
data class LoyaltyReward(
    val threshold: Int,
    val type: LoyaltyRewardType,
    val title: String, // Deskripsi singkat reward
    val iconResId: Int, // ID resource ikon reward
    val claimDateKey: String // Kunci field di LoyaltyUserStatus
)

// Daftar semua reward yang telah didefinisikan (Icon diperbaiki di sini)
val ALL_LOYALTY_REWARDS = listOf(
    LoyaltyReward(5, LoyaltyRewardType.DISCOUNT_10, "Discount 10%", R.drawable.ic_discount, "discount10_claim_date"),
    LoyaltyReward(10, LoyaltyRewardType.FREE_SERVE, "Free Serve", R.drawable.ic_coffee_cup, "free_serve_claim_date"),
    LoyaltyReward(15, LoyaltyRewardType.DISCOUNT_10, "Discount 10%", R.drawable.ic_discount, "discount10_slot15_claim_date"),
    LoyaltyReward(20, LoyaltyRewardType.FREE_TSHIRT, "Free T-Shirt", R.drawable.ic_tshirt, "free_tshirt_claim_date"),
    LoyaltyReward(25, LoyaltyRewardType.DISCOUNT_10, "Discount 10%", R.drawable.ic_discount, "discount10_25_claim_date"),
    LoyaltyReward(30, LoyaltyRewardType.FREE_SERVE, "Free Serve", R.drawable.ic_coffee_cup, "free_serve_30_claim_date"),
    LoyaltyReward(35, LoyaltyRewardType.DISCOUNT_10, "Discount 10%", R.drawable.ic_discount, "discount10_35_claim_date"),
    LoyaltyReward(40, LoyaltyRewardType.FREE_SERVE, "Free Serve", R.drawable.ic_coffee_cup, "free_serve_40_claim_date"),
    LoyaltyReward(45, LoyaltyRewardType.DISCOUNT_10, "Discount 10%", R.drawable.ic_discount, "discount10_45_claim_date"),
    LoyaltyReward(50, LoyaltyRewardType.FREE_SERVE, "Free Serve", R.drawable.ic_coffee_cup, "free_serve_50_claim_date"),
    LoyaltyReward(55, LoyaltyRewardType.DISCOUNT_10, "Discount 10%", R.drawable.ic_discount, "discount10_55_claim_date"),
    LoyaltyReward(60, LoyaltyRewardType.FREE_SERVE, "Free Serve", R.drawable.ic_coffee_cup, "free_serve_60_claim_date"),
    LoyaltyReward(65, LoyaltyRewardType.DISCOUNT_10, "Discount 10%", R.drawable.ic_discount, "discount10_65_claim_date"),
    LoyaltyReward(70, LoyaltyRewardType.FREE_SERVE, "Free Serve", R.drawable.ic_coffee_cup, "free_serve_70_claim_date"),
    LoyaltyReward(75, LoyaltyRewardType.DISCOUNT_10, "Discount 10%", R.drawable.ic_discount, "discount10_75_claim_date"),
    LoyaltyReward(80, LoyaltyRewardType.FREE_SERVE, "Free Serve", R.drawable.ic_coffee_cup, "free_serve_80_claim_date"),
    LoyaltyReward(85, LoyaltyRewardType.DISCOUNT_10, "Discount 10%", R.drawable.ic_discount, "discount10_85_claim_date"),
    LoyaltyReward(90, LoyaltyRewardType.FREE_SERVE, "Free Serve", R.drawable.ic_coffee_cup, "free_serve_90_claim_date"),
    LoyaltyReward(95, LoyaltyRewardType.DISCOUNT_10, "Discount 10%", R.drawable.ic_discount, "discount10_95_claim_date")
)

/**
 * Helper function untuk menentukan status reward berdasarkan poin user.
 */
fun LoyaltyReward.getStatus(currentPoints: Int, status: LoyaltyUserStatus): String {
    // Gunakan fungsi getClaimDate untuk mengambil tanggal klaim dari LoyaltyUserStatus
    fun getClaimDate(threshold: Int, status: LoyaltyUserStatus): String? {
        return when(threshold) {
            5 -> status.discount10ClaimDate
            10 -> status.freeServeClaimDate
            15 -> status.discount10Slot15ClaimDate
            20 -> status.freeTshirtClaimDate
            25 -> status.discount10_25ClaimDate
            30 -> status.freeServe_30ClaimDate
            35 -> status.discount10_35ClaimDate
            40 -> status.freeServe_40ClaimDate
            45 -> status.discount10_45ClaimDate
            50 -> status.freeServe_50ClaimDate
            55 -> status.discount10_55ClaimDate
            60 -> status.freeServe_60ClaimDate
            65 -> status.discount10_65ClaimDate
            70 -> status.freeServe_70ClaimDate
            75 -> status.discount10_75ClaimDate
            80 -> status.freeServe_80ClaimDate
            85 -> status.discount10_85ClaimDate
            90 -> status.freeServe_90ClaimDate
            95 -> status.discount10_95ClaimDate
            else -> null
        }
    }

    val claimDate = getClaimDate(this.threshold, status)

    return if (currentPoints < this.threshold) {
        "NOT ACHIEVED"
    } else if (claimDate != null) {
        "CLAIMED"
    } else {
        "NOT YET CLAIMED"
    }
}