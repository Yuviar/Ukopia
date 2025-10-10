// D:/github_rama/Ukopia/app/src/main/java/com/example/ukopia/data/LoyaltyItemV2.kt
package com.example.ukopia.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoyaltyItemV2(
    val id: String = "", // Unique ID, useful for Firestore or Room. Will be generated if empty.
    val isCoffee: Boolean,
    val namaMenu: String,
    val namaBeans: String? = null, // Only for coffee
    val namaNonKopi: String? = null, // Only for non-coffee
    val tanggal: String,
    val catatan: String? = null,

    val aroma: Int? = null,
    val sweetness: Int? = null,
    val acidity: Int? = null,
    val bitterness: Int? = null,
    val body: Int? = null,
    val timestamp: Long = System.currentTimeMillis() // For sorting or creation time
) : Parcelable