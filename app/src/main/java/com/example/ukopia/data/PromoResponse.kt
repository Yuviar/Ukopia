package com.example.ukopia.data

import com.google.gson.annotations.SerializedName

data class PromoResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("has_promo") val hasPromo: Boolean,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("message") val message: String?
)