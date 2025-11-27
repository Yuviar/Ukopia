package com.example.ukopia.data

import com.google.gson.annotations.SerializedName

data class CreateRecipeRequest(
    @SerializedName("uid_akun") val uid: Int,
    @SerializedName("id_metode") val methodId: Int,
    @SerializedName("nama_resep") val name: String,
    @SerializedName("deskripsi") val description: String,
    @SerializedName("jumlah_kopi") val coffee: Int,
    @SerializedName("jumlah_air") val water: Int,
    @SerializedName("suhu") val temp: Int,
    @SerializedName("ukuran_gilingan") val grindSize: String,
    @SerializedName("waktu_ekstraksi") val time: Int,
    @SerializedName("berat_minuman") val weight: Int,
    @SerializedName("tds") val tds: Int,
    @SerializedName("alat") val equipmentIds: List<Int>
)