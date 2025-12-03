package com.example.ukopia.ui.menu

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.ukopia.models.ApiClient
import com.example.ukopia.models.MenuApiItem

class MenuRepository(private val menuDao: MenuDao) {

    private val apiService = ApiClient.instance

    val allMenuItems: LiveData<List<MenuApiItem>> = menuDao.getMenu()

    suspend fun refreshMenu() {
        try {
            val response = apiService.getMenu(null)

            if (response.isSuccessful) {
                response.body()?.data?.let {
                    menuDao.clearMenu()
                    menuDao.insertAll(it)
                }
            }
        } catch (e: Exception) {
            Log.e("MenuRepository", "Gagal refresh menu: ${e.message}")
        }
    }
    suspend fun getCategories(): List<String> {
        return try {
            val response = apiService.getCategories()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.map { it.name } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MenuRepository", "Gagal fetch kategori: ${e.message}")
            emptyList()
        }
        }
}