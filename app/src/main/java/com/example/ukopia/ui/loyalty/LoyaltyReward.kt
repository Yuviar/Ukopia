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
    val iconResId: Int // ID resource ikon reward
)

// Daftar semua reward yang telah didefinisikan
// Ini adalah satu-satunya tempat untuk menambah/mengubah definisi reward
val ALL_LOYALTY_REWARDS = listOf(
    LoyaltyReward(5, LoyaltyRewardType.DISCOUNT_10, "Discount 10%", R.drawable.ic_discount), // UBAH KE ic_discount
    LoyaltyReward(10, LoyaltyRewardType.FREE_SERVE, "Free Serve", R.drawable.ic_coffee_cup),
    LoyaltyReward(15, LoyaltyRewardType.DISCOUNT_10, "Discount 10%", R.drawable.ic_discount), // UBAH KE ic_discount
    LoyaltyReward(20, LoyaltyRewardType.FREE_TSHIRT, "Free T-Shirt", R.drawable.ic_tshirt), // Only T-Shirt reward
    LoyaltyReward(25, LoyaltyRewardType.DISCOUNT_10, "Discount 10%", R.drawable.ic_discount), // UBAH KE ic_discount
    LoyaltyReward(30, LoyaltyRewardType.FREE_SERVE, "Free Serve", R.drawable.ic_coffee_cup),
    LoyaltyReward(35, LoyaltyRewardType.DISCOUNT_10, "Discount 10%", R.drawable.ic_discount), // UBAH KE ic_discount
    LoyaltyReward(40, LoyaltyRewardType.FREE_SERVE, "Free Serve", R.drawable.ic_coffee_cup),
    LoyaltyReward(45, LoyaltyRewardType.DISCOUNT_10, "Discount 10%", R.drawable.ic_discount), // UBAH KE ic_discount
    LoyaltyReward(50, LoyaltyRewardType.FREE_SERVE, "Free Serve", R.drawable.ic_coffee_cup),
    LoyaltyReward(55, LoyaltyRewardType.DISCOUNT_10, "Discount 10%", R.drawable.ic_discount), // UBAH KE ic_discount
    LoyaltyReward(60, LoyaltyRewardType.FREE_SERVE, "Free Serve", R.drawable.ic_coffee_cup),
    LoyaltyReward(65, LoyaltyRewardType.DISCOUNT_10, "Discount 10%", R.drawable.ic_discount), // UBAH KE ic_discount
    LoyaltyReward(70, LoyaltyRewardType.FREE_SERVE, "Free Serve", R.drawable.ic_coffee_cup),
    LoyaltyReward(75, LoyaltyRewardType.DISCOUNT_10, "Discount 10%", R.drawable.ic_discount), // UBAH KE ic_discount
    LoyaltyReward(80, LoyaltyRewardType.FREE_SERVE, "Free Serve", R.drawable.ic_coffee_cup),
    LoyaltyReward(85, LoyaltyRewardType.DISCOUNT_10, "Discount 10%", R.drawable.ic_discount), // UBAH KE ic_discount
    LoyaltyReward(90, LoyaltyRewardType.FREE_SERVE, "Free Serve", R.drawable.ic_coffee_cup),
    LoyaltyReward(95, LoyaltyRewardType.DISCOUNT_10, "Discount 10%", R.drawable.ic_discount) // UBAH KE ic_discount
)