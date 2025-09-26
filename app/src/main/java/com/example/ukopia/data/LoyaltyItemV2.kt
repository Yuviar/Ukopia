package com.example.ukopia.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoyaltyItemV2(
    val isCoffee: Boolean,
    val namaMenu: String,
    val tanggal: String,
    val catatan: String?,
    val namaBeans: String?,
    val aroma: Int?,
    val sweetness: Int?,
    val acidity: Int?,
    val bitterness: Int?,
    val body: Int?,
    val namaNonKopi: String?
) : Parcelable
