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

        private const val KEY_LOYALTY_TOTAL_PURCHASES = "loyaltyTotalPurchases"
        private const val KEY_LOYALTY_TOTAL_POINTS = "loyaltyTotalPoints"

        private fun getSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
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

        fun saveLoyaltyUserStatus(context: Context, status: LoyaltyUserStatus) {
            val editor = getSharedPreferences(context).edit()
            editor.putInt(KEY_LOYALTY_TOTAL_PURCHASES, status.totalPurchases)
            editor.putInt(KEY_LOYALTY_TOTAL_POINTS, status.totalPoints)
            editor.apply()
        }

        fun getLoyaltyUserStatus(context: Context): LoyaltyUserStatus {
            val prefs = getSharedPreferences(context)
            val purchases = prefs.getInt(KEY_LOYALTY_TOTAL_PURCHASES, 0)
            val points = prefs.getInt(KEY_LOYALTY_TOTAL_POINTS, 0)
            return LoyaltyUserStatus(purchases, points)
        }

        fun logout(context: Context){
            FirebaseAuth.getInstance().signOut()
            val editor = getSharedPreferences(context).edit()
            editor.clear()
            editor.apply()
        }
        fun clearUserData(context: Context) {
            val editor = getSharedPreferences(context).edit()
            editor.remove(KEY_IS_LOGGED_IN)
            editor.remove(KEY_USER_NAME)
            editor.remove(KEY_USER_EMAIL)
            editor.remove(KEY_LOYALTY_TOTAL_PURCHASES)
            editor.remove(KEY_LOYALTY_TOTAL_POINTS)
            editor.apply()
        }
    }
}