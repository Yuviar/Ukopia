package com.example.ukopia.ui.menu

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ukopia.models.MenuApiItem

@Dao
interface MenuDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(menuItems: List<MenuApiItem>)

    @Query("SELECT * FROM menu_items")
    fun getMenu(): LiveData<List<MenuApiItem>>

    @Query("DELETE FROM menu_items")
    suspend fun clearMenu()
}