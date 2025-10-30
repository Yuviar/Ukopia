package com.example.ukopia.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SubEquipmentItem(
    val id: String,
    val category: String,
    val name: String, // Ini akan berisi "nama sub-equipment" saja (misal: "V60 Dripper (Hario)")
    val detail: String? = null, // Detail di sini akan null karena sudah digabung ke 'name' saat selection
    val iconResId: Int
) : Parcelable