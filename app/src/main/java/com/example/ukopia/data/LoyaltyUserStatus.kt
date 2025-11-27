// D:/github rama/Ukopia/app/src/main/java/com/example/ukopia/data/LoyaltyUserStatus.kt
package com.example.ukopia.data

data class LoyaltyUserStatus(
    val totalPoints: Int = 0,
    // Mengubah boolean menjadi String? untuk menyimpan tanggal klaim (format "YYYY-MM-DD")
    // Jika null, berarti reward belum diklaim.
    val discount10ClaimDate: String? = null, // 5 poin
    val freeServeClaimDate: String? = null, // 10 poin
    val discount10Slot15ClaimDate: String? = null, // 15 poin
    val freeTshirtClaimDate: String? = null, // 20 poin - Only T-shirt reward
    val discount10_25ClaimDate: String? = null, // 25 poin
    val freeServe_30ClaimDate: String? = null, // 30 poin
    val discount10_35ClaimDate: String? = null, // 35 poin
    val freeServe_40ClaimDate: String? = null, // 40 poin
    val discount10_45ClaimDate: String? = null, // 45 poin
    val freeServe_50ClaimDate: String? = null, // 50 poin
    val discount10_55ClaimDate: String? = null, // 55 poin
    val freeServe_60ClaimDate: String? = null, // 60 poin - Changed from Free T-shirt to Free Serve
    val discount10_65ClaimDate: String? = null, // 65 poin
    val freeServe_70ClaimDate: String? = null, // 70 poin
    val discount10_75ClaimDate: String? = null, // 75 poin
    val freeServe_80ClaimDate: String? = null, // 80 poin
    val discount10_85ClaimDate: String? = null, // 85 poin
    val freeServe_90ClaimDate: String? = null, // 90 poin
    val discount10_95ClaimDate: String? = null // 95 poin
)