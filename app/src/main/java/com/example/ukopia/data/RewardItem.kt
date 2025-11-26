// D:/github rama/Ukopia/app/src/main/java/com/example/ukopia/ui/loyalty/RewardItem.kt
package com.example.ukopia.ui.loyalty

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class RewardItem(
    val id: Int, // Bisa menggunakan pointsRequired sebagai ID unik
    val name: String,
    val pointsRequired: Int,
    @DrawableRes val iconResId: Int,
    val pointsMet: Boolean, // True jika totalPoints user >= pointsRequired
    val claimedDate: String? // Tanggal klaim oleh admin (format "YYYY-MM-DD"), null jika belum diklaim
) : Parcelable