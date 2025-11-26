package com.example.ukopia.ui.loyalty

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ukopia.SessionManager
import com.example.ukopia.data.LoyaltyItemV2
import com.example.ukopia.data.LoyaltyUserStatus
import java.util.UUID // Import UUID for generating unique IDs

class LoyaltyViewModel(application: Application) : AndroidViewModel(application) {

    private val _loyaltyItems = MutableLiveData<List<LoyaltyItemV2>>()
    val loyaltyItems: LiveData<List<LoyaltyItemV2>> = _loyaltyItems

    private val _loyaltyUserStatus = MutableLiveData<LoyaltyUserStatus>()
    val loyaltyUserStatus: LiveData<LoyaltyUserStatus> = _loyaltyUserStatus

    init {
        // Muat status awal dari SessionManager
        _loyaltyUserStatus.value = SessionManager.getLoyaltyUserStatus(application)
        // Muat loyalty items dari SessionManager
        loadLoyaltyItems()
    }

    // NEW: Helper function to load loyalty items
    private fun loadLoyaltyItems() {
        _loyaltyItems.value = SessionManager.getLoyaltyItems(getApplication())
    }

    // NEW: Call this to force a refresh of loyalty items, e.g., after an edit or a new item is added by admin
    fun refreshLoyaltyItems() {
        loadLoyaltyItems()
        // Also refresh user status if it can change externally (e.g., admin modifies points)
        _loyaltyUserStatus.value = SessionManager.getLoyaltyUserStatus(getApplication())
    }

    fun addPurchase(item: LoyaltyItemV2) {
        val currentItems = _loyaltyItems.value?.toMutableList() ?: mutableListOf()
        // Generate a unique ID if the item doesn't have one
        val itemWithId = item.copy(id = item.id.ifEmpty { UUID.randomUUID().toString() })
        currentItems.add(0, itemWithId)
        _loyaltyItems.value = currentItems

        val currentStatus = _loyaltyUserStatus.value ?: LoyaltyUserStatus()
        val updatedPoints = currentStatus.totalPoints + 1

        val newStatus = currentStatus.copy(
            totalPoints = updatedPoints
            // Tidak ada perubahan pada tanggal klaim di sini, karena klaim adalah sisi admin.
            // Ketika admin mengklaim, status yang dimuat oleh SessionManager akan berbeda.
        )
        _loyaltyUserStatus.value = newStatus

        SessionManager.saveLoyaltyUserStatus(getApplication(), newStatus)
        SessionManager.saveLoyaltyItems(getApplication(), currentItems) // NEW: Save the updated list of items
    }

    // NEW: Function to update an existing loyalty item
    fun updatePurchase(updatedItem: LoyaltyItemV2) {
        val currentItems = _loyaltyItems.value?.toMutableList() ?: mutableListOf()
        val index = currentItems.indexOfFirst { it.id == updatedItem.id }
        if (index != -1) {
            currentItems[index] = updatedItem
            _loyaltyItems.value = currentItems
            SessionManager.saveLoyaltyItems(getApplication(), currentItems) // NEW: Save the updated list
        }
    }

    // Semua fungsi claim...() dihapus karena user tidak dapat mengklaim rewards.
    // Status klaim akan diupdate secara eksternal (misalnya, oleh API admin dan kemudian dimuat melalui SessionManager).
}