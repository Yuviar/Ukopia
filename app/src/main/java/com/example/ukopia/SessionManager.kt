package com.example.ukopia

import android.content.Context
import android.content.SharedPreferences

class SessionManager {
    object SessionManager {
        private const val PREF_NAME = "ukopia_prefs"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"

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
    }
}