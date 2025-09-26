package com.example.ukopia.ui.loyalty

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ukopia.data.LoyaltyItemV2

class LoyaltyViewModel : ViewModel() {

    // MutableLiveData untuk menyimpan daftar item loyalitas
    private val _loyaltyItems = MutableLiveData<List<LoyaltyItemV2>>()
    val loyaltyItems: LiveData<List<LoyaltyItemV2>> = _loyaltyItems

    init {
        // Menginisialisasi daftar dengan daftar kosong saat ViewModel dibuat
        _loyaltyItems.value = emptyList()
    }

    /**
     * Menambahkan item loyalitas baru ke daftar.
     * @param item Item loyalitas yang akan ditambahkan.
     */
    fun addLoyaltyItemV2(item: LoyaltyItemV2) {
        // Mengambil daftar saat ini, menambahkan item baru, dan memperbarui LiveData
        val currentList = _loyaltyItems.value?.toMutableList() ?: mutableListOf()
        currentList.add(item)
        _loyaltyItems.value = currentList
    }
}
