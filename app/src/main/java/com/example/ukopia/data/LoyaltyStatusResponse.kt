package com.example.ukopia.data

import com.google.gson.annotations.SerializedName

data class LoyaltyStatusResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: LoyaltyUserStatus?
)