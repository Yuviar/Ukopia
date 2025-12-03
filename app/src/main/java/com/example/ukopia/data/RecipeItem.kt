package com.example.ukopia.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecipeItem(

    @SerializedName("id_resep") val id: String,
    @SerializedName("nama_resep") val name: String,
    @SerializedName("deskripsi") val description: String,
    @SerializedName("jumlah_air") val waterAmountInt: Int,
    @SerializedName("jumlah_kopi") val coffeeAmountInt: Int,
    @SerializedName("waktu_ekstraksi") val extractionTimeInt: Int,
    @SerializedName("nama_pembuat") val brewerName: String? = null,

    @SerializedName("suhu") val temp: Int? = null,
    @SerializedName("grind_size") val grindSize: String? = null,
    @SerializedName("brew_weight") val brewWeight: Int? = null,
    @SerializedName("tds") val tds: Int? = null,

    @SerializedName("ratio_text") val ratioText: String? = null,
    @SerializedName("tanggal") val date: String? = null,
    @SerializedName("metode") val method: String? = null,

    @SerializedName("equipment") val equipmentUsed: List<SubEquipmentItem>? = null,

    var isMine: Boolean = false
) : Parcelable {

    val waterAmount: String get() = "$waterAmountInt ml"
    val coffeeAmount: String get() = "$coffeeAmountInt g"
    val extractionTime: String get() = "${extractionTimeInt}s"
    val temperature: String get() = "${temp ?: 0}°C"
    val brewWeightText: String get() = "${brewWeight ?: 0} g"
    val tdsText: String get() = "${tds ?: 0} %"
}