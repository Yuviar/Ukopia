package com.example.ukopia.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SubEquipmentItem(
    @SerializedName("id_alat") val id: Int,

    var category: String = "",
    @SerializedName("nama_alat") val name: String,
    @SerializedName("gambar") val imageUrl: String,

    val detail: String? = null,
    var isSelected: Boolean = false
) : Parcelable