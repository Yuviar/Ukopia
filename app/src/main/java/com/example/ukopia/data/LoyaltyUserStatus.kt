package com.example.ukopia.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoyaltyUserStatus(
    @SerializedName("total_points")
    val totalPoints: Int = 0,

    @SerializedName("discount10_claim_date") val discount10ClaimDate: String? = null,
    @SerializedName("free_serve_claim_date") val freeServeClaimDate: String? = null,
    @SerializedName("discount10_slot15_claim_date") val discount10Slot15ClaimDate: String? = null,
    @SerializedName("free_tshirt_claim_date") val freeTshirtClaimDate: String? = null,
    @SerializedName("discount10_25_claim_date") val discount10_25ClaimDate: String? = null,
    @SerializedName("free_serve_30_claim_date") val freeServe_30ClaimDate: String? = null,
    @SerializedName("discount10_35_claim_date") val discount10_35ClaimDate: String? = null,
    @SerializedName("free_serve_40_claim_date") val freeServe_40ClaimDate: String? = null,
    @SerializedName("discount10_45_claim_date") val discount10_45ClaimDate: String? = null,
    @SerializedName("free_serve_50_claim_date") val freeServe_50ClaimDate: String? = null,
    @SerializedName("discount10_55_claim_date") val discount10_55ClaimDate: String? = null,
    @SerializedName("free_serve_60_claim_date") val freeServe_60ClaimDate: String? = null,
    @SerializedName("discount10_65_claim_date") val discount10_65ClaimDate: String? = null,
    @SerializedName("free_serve_70_claim_date") val freeServe_70ClaimDate: String? = null,
    @SerializedName("discount10_75_claim_date") val discount10_75ClaimDate: String? = null,
    @SerializedName("free_serve_80_claim_date") val freeServe_80ClaimDate: String? = null,
    @SerializedName("discount10_85_claim_date") val discount10_85ClaimDate: String? = null,
    @SerializedName("free_serve_90_claim_date") val freeServe_90ClaimDate: String? = null,
    @SerializedName("discount10_95_claim_date") val discount10_95ClaimDate: String? = null
) : Parcelable