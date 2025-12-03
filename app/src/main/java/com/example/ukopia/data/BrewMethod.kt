package com.example.ukopia.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BrewMethod(
    @SerializedName("id_metode") val id: Int,
    @SerializedName("nama_metode") val name: String,
    @SerializedName("gambar") val imageUrl: String
) : Parcelable