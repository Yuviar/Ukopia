package com.example.ukopia.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoyaltyItemV2(
    val id: String = "",
    val isCoffee: Boolean,
    val namaMenu: String,
    val namaBeans: String? = null,
    val namaNonKopi: String? = null,
    val tanggal: String,
    val catatan: String? = null,

    val aroma: Int? = null,
    val sweetness: Int? = null,
    val acidity: Int? = null,
    val bitterness: Int? = null,
    val body: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable