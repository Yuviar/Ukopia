package com.example.ukopia.data

import com.google.gson.annotations.SerializedName

data class EquipmentItem(
    @SerializedName("id_kategori") val id: Int,
    @SerializedName("nama_kategori") val name: String,
    @SerializedName("jumlah") val count: Int
)