package com.example.ukopia.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecipeItem(
    // --- FIELD WAJIB (Ada di by_metode.php & detail.php) ---
    @SerializedName("id_resep") val id: String,
    @SerializedName("nama_resep") val name: String,
    @SerializedName("deskripsi") val description: String,
    @SerializedName("jumlah_air") val waterAmountInt: Int,
    @SerializedName("jumlah_kopi") val coffeeAmountInt: Int,
    @SerializedName("waktu_ekstraksi") val extractionTimeInt: Int,
    @SerializedName("nama_pembuat") val brewerName: String? = null,

    // --- FIELD DETAIL (Hanya ada di detail.php) ---
    // Semua field ini harus Nullable (?) karena tidak dikirim oleh by_metode.php

    @SerializedName("suhu") val temp: Int? = null,
    @SerializedName("grind_size") val grindSize: String? = null, // API kirim key "grind_size", bukan "ukuran_gilingan"
    @SerializedName("brew_weight") val brewWeight: Int? = null, // API kirim key "brew_weight"
    @SerializedName("tds") val tds: Int? = null,

    @SerializedName("ratio_text") val ratioText: String? = null,
    @SerializedName("tanggal") val date: String? = null,
    @SerializedName("metode") val method: String? = null,

    @SerializedName("equipment") val equipmentUsed: List<SubEquipmentItem>? = null,

    // Field Lokal (Tidak dari API)
    var isMine: Boolean = false
) : Parcelable {

    // Helper Properties untuk Format Text UI
    val waterAmount: String get() = "$waterAmountInt ml"
    val coffeeAmount: String get() = "$coffeeAmountInt g"
    val extractionTime: String get() = "${extractionTimeInt}s"
    val temperature: String get() = "${temp ?: 0}°C"
    val brewWeightText: String get() = "${brewWeight ?: 0} g"
    val tdsText: String get() = "${tds ?: 0} %"
}