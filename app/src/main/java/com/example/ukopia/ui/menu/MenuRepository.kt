package com.example.ukopia.ui.menu

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.ukopia.models.ApiClient
import com.example.ukopia.models.MenuApiItem

class MenuRepository(private val menuDao: MenuDao) {

    private val apiService = ApiClient.instance

    // 1. "Sumber Kebenaran" (Source of Truth)
    // ViewModel akan meng-observe LiveData ini (data dari DB).
    val allMenuItems: LiveData<List<MenuApiItem>> = menuDao.getMenu()

    // 2. Fungsi untuk "Merefresh" data dari network
    suspend fun refreshMenu() {
        try {
            // Panggil API, selalu ambil SEMUA data (parameter null)
            val response = apiService.getMenu(null)

            if (response.isSuccessful) {
                response.body()?.data?.let {
                    // Jika sukses, hapus cache lama
                    menuDao.clearMenu()
                    // Masukkan data baru
                    menuDao.insertAll(it)
                }
            }
        } catch (e: Exception) {
            // Jika API gagal (misal: offline), data lama di cache akan tetap tampil.
            Log.e("MenuRepository", "Gagal refresh menu: ${e.message}")
        }
    }
}