package com.example.ukopia

import android.content.Context
import android.content.SharedPreferences
import com.example.ukopia.data.LoyaltyUserStatus
import com.google.firebase.auth.FirebaseAuth

class SessionManager {
    object SessionManager {
        private const val PREF_NAME = "ukopia_prefs"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_EMAIL = "userEmail"

        // Kunci untuk Loyalty
        private const val KEY_LOYALTY_TOTAL_POINTS = "loyaltyTotalPoints"
        // PERBAIKAN: Menambahkan kunci untuk setiap status klaim hadiah
        private const val KEY_LOYALTY_CLAIMED_DISCOUNT_10 = "loyaltyClaimedDiscount10"
        private const val KEY_LOYALTY_CLAIMED_FREE_SERVE = "loyaltyClaimedFreeServe"
        private const val KEY_LOYALTY_CLAIMED_DISCOUNT_15 = "loyaltyClaimedDiscount15"
        private const val KEY_LOYALTY_CLAIMED_TSHIRT = "loyaltyClaimedTshirt"


        private fun getSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }

        // --- Fungsi Login/User (Tidak berubah) ---
        fun setLoggedIn(context: Context, loggedIn: Boolean) {
            val editor = getSharedPreferences(context).edit()
            editor.putBoolean(KEY_IS_LOGGED_IN, loggedIn)
            editor.apply()
        }
        fun isLoggedIn(context: Context): Boolean {
            return getSharedPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
        }
        fun saveUserData(context: Context, name: String, email: String) {
            val editor = getSharedPreferences(context).edit()
            editor.putString(KEY_USER_NAME, name)
            editor.putString(KEY_USER_EMAIL, email)
            editor.apply()
        }
        fun getUserName(context: Context): String?{
            return getSharedPreferences(context).getString(KEY_USER_NAME,null)
        }
        fun getUserEmail(context: Context): String?{
            return getSharedPreferences(context).getString(KEY_USER_EMAIL,null)
        }

        // --- Fungsi Loyalty (Diperbaiki) ---

        // PERBAIKAN: Fungsi ini sekarang menyimpan SEMUA status dari objek LoyaltyUserStatus
        fun saveLoyaltyUserStatus(context: Context, status: LoyaltyUserStatus) {
            val editor = getSharedPreferences(context).edit()
            editor.putInt(KEY_LOYALTY_TOTAL_POINTS, status.totalPoints)
            editor.putBoolean(KEY_LOYALTY_CLAIMED_DISCOUNT_10, status.isDiscount10Claimed)
            editor.putBoolean(KEY_LOYALTY_CLAIMED_FREE_SERVE, status.isFreeServeClaimed)
            editor.putBoolean(KEY_LOYALTY_CLAIMED_DISCOUNT_15, status.isDiscount10Slot15Claimed)
            editor.putBoolean(KEY_LOYALTY_CLAIMED_TSHIRT, status.isFreeTshirtClaimed)
            editor.apply()
        }

        // PERBAIKAN: Fungsi ini sekarang mengambil SEMUA status dan membuat objek yang lengkap
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

        fun logout(context: Context) {
            FirebaseAuth.getInstance().signOut()
            val editor = getSharedPreferences(context).edit()
            editor.clear()
            editor.apply()
        }
    }
}