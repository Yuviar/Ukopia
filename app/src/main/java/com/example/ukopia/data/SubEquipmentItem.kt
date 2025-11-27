package com.example.ukopia.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SubEquipmentItem(
    @SerializedName("id_alat") val id: Int,
    // Field 'category' tidak selalu ada di API by_kategori,
    // kita bisa isi manual di Fragment atau biarkan nullable/default
    var category: String = "",
    @SerializedName("nama_alat") val name: String,
    @SerializedName("gambar") val imageUrl: String,

    // Field tambahan untuk UI (tidak dari API)
    val detail: String? = null,
    var isSelected: Boolean = false
) : Parcelable