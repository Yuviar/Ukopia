package com.example.ukopia

import android.content.Context
import android.content.SharedPreferences
import com.example.ukopia.data.LoyaltyUserStatus

/**
 * Singleton object untuk mengelola semua data sesi aplikasi yang disimpan di SharedPreferences.
 * Termasuk status login, data pengguna (DENGAN UID), dan status loyalty.
 * Pemanggilan: SessionManager.isLoggedIn(context)
 */
object SessionManager {
    private const val PREF_NAME = "ukopia_prefs"

    // Kunci untuk Sesi Pengguna
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val KEY_USER_UID = "userUid" // <-- PERUBAHAN: DITAMBAHKAN
    private const val KEY_USER_NAME = "userName"
    private const val KEY_USER_EMAIL = "userEmail"

    // Kunci untuk Loyalty (Tidak disentuh)
    private const val KEY_LOYALTY_TOTAL_POINTS = "loyaltyTotalPoints"
    private const val KEY_LOYALTY_CLAIMED_DISCOUNT_10 = "loyaltyClaimedDiscount10"
    private const val KEY_LOYALTY_CLAIMED_FREE_SERVE = "loyaltyClaimedFreeServe"
    private const val KEY_LOYALTY_CLAIMED_DISCOUNT_15 = "loyaltyClaimedDiscount15"
    private const val KEY_LOYALTY_CLAIMED_TSHIRT = "loyaltyClaimedTshirt"


    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Menghapus semua data sesi pengguna dan loyalty. Dipanggil saat logout.
     */
    fun logout(context: Context) {
        val editor = getSharedPreferences(context).edit()
        // Hapus semua data yang berhubungan dengan sesi
        editor.remove(KEY_IS_LOGGED_IN)
        editor.remove(KEY_USER_UID) // <-- PERUBAHAN: DITAMBAHKAN
        editor.remove(KEY_USER_NAME)
        editor.remove(KEY_USER_EMAIL)
        editor.remove(KEY_LOYALTY_TOTAL_POINTS)
        editor.remove(KEY_LOYALTY_CLAIMED_DISCOUNT_10)
        editor.remove(KEY_LOYALTY_CLAIMED_FREE_SERVE)
        editor.remove(KEY_LOYALTY_CLAIMED_DISCOUNT_15)
        editor.remove(KEY_LOYALTY_CLAIMED_TSHIRT)
        editor.apply()
    }

    // --- Fungsi Sesi Pengguna ---

    fun setLoggedIn(context: Context, loggedIn: Boolean) {
        val editor = getSharedPreferences(context).edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, loggedIn)
        editor.apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // --- PERUBAHAN DI SINI ---
    /**
     * Menyimpan data pengguna lengkap termasuk UID dari database.
     */
    fun saveUserData(context: Context, uid: Int, name: String, email: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putInt(KEY_USER_UID, uid) // <-- PERUBAHAN: DITAMBAHKAN
        editor.putString(KEY_USER_NAME, name)
        editor.putString(KEY_USER_EMAIL, email)
        editor.apply()
    }

    // --- FUNGSI BARU DI SINI ---
    /**
     * Mendapatkan UID pengguna yang sedang login.
     * Penting untuk semua panggilan API.
     */
    fun getUid(context: Context): Int {
        // Mengembalikan 0 jika tidak ada UID (pengguna belum login)
        return getSharedPreferences(context).getInt(KEY_USER_UID, 0)
    }

    fun getUserName(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_USER_NAME, null)
    }

    fun getUserEmail(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_USER_EMAIL, null)
    }

    // --- Fungsi Loyalty (Tidak Disentuh) ---

    fun saveLoyaltyUserStatus(context: Context, status: LoyaltyUserStatus) {
        val editor = getSharedPreferences(context).edit()
        editor.putInt(KEY_LOYALTY_TOTAL_POINTS, status.totalPoints)
        editor.putBoolean(KEY_LOYALTY_CLAIMED_DISCOUNT_10, status.isDiscount10Claimed)
        editor.putBoolean(KEY_LOYALTY_CLAIMED_FREE_SERVE, status.isFreeServeClaimed)
        editor.putBoolean(KEY_LOYALTY_CLAIMED_DISCOUNT_15, status.isDiscount10Slot15Claimed)
        editor.putBoolean(KEY_LOYALTY_CLAIMED_TSHIRT, status.isFreeTshirtClaimed)
        editor.apply()
    }

    fun getLoyaltyUserStatus(context: Context): LoyaltyUserStatus {
        val prefs = getSharedPreferences(context)
        val points = prefs.getInt(KEY_LOYALTY_TOTAL_POINTS, 0)
        val isDiscount10Claimed = prefs.getBoolean(KEY_LOYALTY_CLAIMED_DISCOUNT_10, false)
        val isFreeServeClaimed = prefs.getBoolean(KEY_LOYALTY_CLAIMED_FREE_SERVE, false)
        val isDiscount15Claimed = prefs.getBoolean(KEY_LOYALTY_CLAIMED_DISCOUNT_15, false)
        val isTshirtClaimed = prefs.getBoolean(KEY_LOYALTY_CLAIMED_TSHIRT, false)

        return LoyaltyUserStatus(
            totalPoints = points,
            isDiscount10Claimed = isDiscount10Claimed,
            isFreeServeClaimed = isFreeServeClaimed,
            isDiscount10Slot15Claimed = isDiscount15Claimed,
            isFreeTshirtClaimed = isTshirtClaimed
        )
    }
}