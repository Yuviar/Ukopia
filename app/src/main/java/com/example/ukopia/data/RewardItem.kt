// D:/github rama/Ukopia/app/src/main/java/com/example/ukopia/ui/loyalty/RewardItem.kt
package com.example.ukopia.ui.loyalty

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class RewardItem(
    val id: Int,
    val name: String,
    val pointsRequired: Int,
    @DrawableRes val iconResId: Int,
    val pointsMet: Boolean,
    val claimedDate: String?
) : Parcelable