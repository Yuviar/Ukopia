package com.example.ukopia.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecipeStep(
    val id: String,
    val title: String,
    val description: String?,
    val iconResId: Int,
    val hasDuration: Boolean = false,
    var currentDurationInput: String? = null,
    var currentWaterAmountInput: String? = null
) : Parcelable