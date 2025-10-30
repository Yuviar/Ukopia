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
    val temperature: String,
    val extractionTime: String,
    val isMine: Boolean,
    val steps: List<String>,

    val brewWeight: String? = null,
    val tds: String? = null,
    val coffeeBrewRatio: String? = null,
    val coffeeWaterRatio: String? = null,
    val date: String? = null,
    val notes: String? = null,
    // <<< BARU: Properti untuk menyimpan daftar equipment >>>
    val equipmentUsed: List<SubEquipmentItem>? = null
) : Parcelable