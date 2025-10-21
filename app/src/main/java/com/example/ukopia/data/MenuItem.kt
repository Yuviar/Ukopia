// D:/github_rama/Ukopia/app/src/main/java/com/example/ukopia/data/MenuItem.kt
package com.example.ukopia.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MenuItem(
    val id: String,
    val name: String,
    var rating: String, // <<-- Diubah menjadi 'var' untuk memungkinkan perubahan sementara
    val imageUrl: Int,
    val description: String,
    val category: String,
) : Parcelable