package com.example.ukopia.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MenuItem(
    val id: String,
    val name: String,
    val rating: String,
    val imageUrl: Int,
    val description: String,
    val category: String
) : Parcelable