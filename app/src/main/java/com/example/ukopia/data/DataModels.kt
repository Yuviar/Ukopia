package com.example.ukopia.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class LoyaltyListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<LoyaltyItemV2>?
)

@Parcelize
data class LoyaltyItemV2(
    @SerializedName("id_loyalty") val idLoyalty: Int,
    @SerializedName("nama_menu") val namaMenu: String,
    @SerializedName("kategori") val kategori: String,
    @SerializedName("biji_kopi") val bijiKopi: String?,
    @SerializedName("tanggal") val tanggal: String,
    @SerializedName("nilai") val nilai: LoyaltyNilai?
) : Parcelable {
    val isCoffee: Boolean get() = !bijiKopi.isNullOrEmpty() && bijiKopi != "-"
    val namaBeans: String? get() = if (isCoffee) bijiKopi else null
}

@Parcelize
data class LoyaltyNilai(
    @SerializedName("keasaman") val keasaman: Int,
    @SerializedName("kepahitan") val kepahitan: Int,
    @SerializedName("aroma") val aroma: Int,
    @SerializedName("kemanisan") val kemanisan: Int,
    @SerializedName("kekentalan") val kekentalan: Int,
    @SerializedName("catatan") val catatan: String?
) : Parcelable

data class RewardHistoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<RewardHistoryItem>?
)

@Parcelize
data class RewardHistoryItem(
    @SerializedName("id_riwayat") val idRiwayat: Int,
    @SerializedName("nama_reward") val namaReward: String,
    @SerializedName("deskripsi") val deskripsi: String?,
    @SerializedName("tanggal_dapat") val tanggalDapat: String,
    @SerializedName("status_klaim") val statusKlaim: String,
    @SerializedName("kode_unik") val kodeUnik: String?
) : Parcelable