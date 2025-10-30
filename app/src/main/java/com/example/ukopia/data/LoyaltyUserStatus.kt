package com.example.ukopia.data // Pastikan package ini sesuai dengan proyek Anda

/**
 * Data class ini menyimpan semua status terkait loyalty card pengguna.
 * - totalPoints: Jumlah poin saat ini.
 * - is...Claimed: Flag boolean untuk setiap hadiah yang bisa diklaim.
 */
data class LoyaltyUserStatus(
    val totalPoints: Int = 0,
    val isDiscount10Claimed: Boolean = false,
    val isFreeServeClaimed: Boolean = false,
    val isFreeTshirtClaimed: Boolean = false,
    val isDiscount10Slot15Claimed: Boolean = false
)