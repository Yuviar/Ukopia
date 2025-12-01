package com.example.ukopia.data

import com.google.gson.annotations.SerializedName

/**
 * Class untuk menangkap response dari endpoint:
 * 1. loyalty/update.php (yang sudah diperbaiki)
 * 2. loyalty/status.php (jika nanti Anda buat)
 */
data class LoyaltyStatusResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: LoyaltyUserStatus?
)