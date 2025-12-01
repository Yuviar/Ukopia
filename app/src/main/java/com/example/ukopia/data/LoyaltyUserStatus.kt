// File: D:/github_rama/Ukopia/app/src/main/java/com/example/ukopia/data/LoyaltyUserStatus.kt

package com.example.ukopia.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Data class untuk menyimpan status loyalty user.
 * Menggunakan properti statis untuk tanggal klaim.
 * Nama @SerializedName HARUS cocok dengan kunci di JSON dari backend 'loyalty/status.php'.
 */
@Parcelize
data class LoyaltyUserStatus(
    @SerializedName("total_points") // Asumsi backend mengirim 'total_points'
    val totalPoints: Int = 0,

    // Nama-nama ini juga harus cocok dengan JSON dari 'loyalty/status.php'
    @SerializedName("discount10_claim_date") val discount10ClaimDate: String? = null, // 5 poin
    @SerializedName("free_serve_claim_date") val freeServeClaimDate: String? = null, // 10 poin
    @SerializedName("discount10_slot15_claim_date") val discount10Slot15ClaimDate: String? = null, // 15 poin
    @SerializedName("free_tshirt_claim_date") val freeTshirtClaimDate: String? = null, // 20 poin
    @SerializedName("discount10_25_claim_date") val discount10_25ClaimDate: String? = null, // 25 poin
    @SerializedName("free_serve_30_claim_date") val freeServe_30ClaimDate: String? = null, // 30 poin
    @SerializedName("discount10_35_claim_date") val discount10_35ClaimDate: String? = null, // 35 poin
    @SerializedName("free_serve_40_claim_date") val freeServe_40ClaimDate: String? = null, // 40 poin
    @SerializedName("discount10_45_claim_date") val discount10_45ClaimDate: String? = null, // 45 poin
    @SerializedName("free_serve_50_claim_date") val freeServe_50ClaimDate: String? = null, // 50 poin
    @SerializedName("discount10_55_claim_date") val discount10_55ClaimDate: String? = null, // 55 poin
    @SerializedName("free_serve_60_claim_date") val freeServe_60ClaimDate: String? = null, // 60 poin
    @SerializedName("discount10_65_claim_date") val discount10_65ClaimDate: String? = null, // 65 poin
    @SerializedName("free_serve_70_claim_date") val freeServe_70ClaimDate: String? = null, // 70 poin
    @SerializedName("discount10_75_claim_date") val discount10_75ClaimDate: String? = null, // 75 poin
    @SerializedName("free_serve_80_claim_date") val freeServe_80ClaimDate: String? = null, // 80 poin
    @SerializedName("discount10_85_claim_date") val discount10_85ClaimDate: String? = null, // 85 poin
    @SerializedName("free_serve_90_claim_date") val freeServe_90ClaimDate: String? = null, // 90 poin
    @SerializedName("discount10_95_claim_date") val discount10_95ClaimDate: String? = null // 95 poin
) : Parcelable