package com.example.ukopia.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BrewMethod(
    val name: String,
    val imageUrl: Int
) : Parcelable