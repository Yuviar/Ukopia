package com.example.ukopia.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// === UNTUK GET_MENU.PHP (Sekaligus Tabel Room) ===

data class MenuApiResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val data: List<MenuApiItem>?
)

@Parcelize
@Entity(tableName = "menu_items") // Menandakan ini adalah tabel Room
data class MenuApiItem(
    @PrimaryKey // id_menu adalah Kunci Utama
    @SerializedName("id_menu")
    val id_menu: Int,

    @SerializedName("nama_kategori")
    val nama_kategori: String,
    @SerializedName("deskripsi")
    val deskripsi: String,
    @SerializedName("gambar_url")
    val gambar_url: String, // Ini String (URL)
    @SerializedName("nama_menu")
    val nama_menu: String,
    @SerializedName("average_rating")
    val average_rating: Double,
    @SerializedName("total_reviews")
    val total_reviews: Int
) : Parcelable


// === UNTUK GET_DETAIL_MENU.PHP ===

data class ReviewApiResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("data_ulasan")
    val data_ulasan: List<ReviewApiItem>?
)

@Parcelize
data class ReviewApiItem(
    @SerializedName("id_ulasan")
    val id_ulasan: Int,
    @SerializedName("nama")
    val nama: String,
    @SerializedName("rating")
    val rating: Double,
    @SerializedName("tanggal_waktu")
    val tanggal_waktu: String,
    @SerializedName("komentar")
    val komentar: String,
    @SerializedName("is_owner")
    val is_owner: Boolean // GSON akan konversi 0/1 ke boolean
) : Parcelable


// === UNTUK POST/UPDATE/DELETE ULASAN ===

data class ReviewPostRequest(
    val id_menu: Int,
    val uid_akun: Int,
    val rating: Float,
    val komentar: String
)

data class ReviewUpdateRequest(
    val id_ulasan: Int,
    val uid_akun: Int,
    val rating: Float,
    val komentar: String
)

data class ReviewDeleteRequest(
    val id_ulasan: Int,
    val uid_akun: Int
)