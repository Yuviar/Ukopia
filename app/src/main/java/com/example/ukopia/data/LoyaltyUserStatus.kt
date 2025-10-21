package com.example.ukopia.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoyaltyUserStatus(
    val totalPurchases: Int = 0,
    val totalPoints: Int = 0
) : Parcelable