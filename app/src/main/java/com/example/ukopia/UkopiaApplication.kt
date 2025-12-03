package com.example.ukopia

import android.app.Application
import com.example.ukopia.ui.menu.AppDatabase
import com.example.ukopia.ui.menu.MenuRepository

class UkopiaApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { MenuRepository(database.menuDao()) }
}