package com.example.ukopia

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth

class SessionManager {
    object SessionManager {
        private const val PREF_NAME = "ukopia_prefs"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_EMAIL = "userEmail"

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
        fun logout(context: Context){
            FirebaseAuth.getInstance().signOut()
            val editor = getSharedPreferences(context).edit()
            editor.clear()
            editor.apply()
        }
        fun clearUserData(context: Context) {
            val editor = getSharedPreferences(context).edit()
            editor.remove(KEY_IS_LOGGED_IN) // Set status login ke false (atau hapus)
            editor.remove(KEY_USER_NAME)
            editor.remove(KEY_USER_EMAIL)
            // Atau, jika Anda ingin menghapus semua data dari preferensi ini:
            // editor.clear()
            editor.apply()
            // Anda juga bisa secara eksplisit set isLoggedIn ke false jika Anda menghapus semua data.
            // setLoggedIn(context, false) // Jika Anda menggunakan editor.clear(), ini tidak perlu.
        }
    }
}