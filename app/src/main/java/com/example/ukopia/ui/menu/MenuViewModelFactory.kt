package com.example.ukopia.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MenuViewModelFactory(private val menuRepository: MenuRepository) : ViewModelProvider.Factory { // UBAH 'repository' menjadi 'menuRepository'
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MenuViewModel(menuRepository) as T // UBAH 'repository' menjadi 'menuRepository'
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}