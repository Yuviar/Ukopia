package com.example.ukopia.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecipeItem(
    val id: String,
    val method: String,
    val name: String,
    val description: String,
    val waterAmount: String,
    val coffeeAmount: String,
    val grindSize: String,
    val heat: String,
    val time: String,
    val isMine: Boolean,
    val steps: List<String>
) : Parcelable