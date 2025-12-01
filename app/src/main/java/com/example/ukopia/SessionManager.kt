// File: D:/github_rama/Ukopia/app/src/main/java/com/example/ukopia/SessionManager.kt

package com.example.ukopia

import android.content.Context
import android.content.SharedPreferences
import com.example.ukopia.data.LoyaltyItemV2
import com.example.ukopia.data.LoyaltyUserStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SessionManager {
    private const val PREF_NAME = "ukopia_prefs"

    // Kunci Sesi Pengguna
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val KEY_USER_UID = "userUid"
    private const val KEY_USER_NAME = "userName"
    private const val KEY_USER_EMAIL = "userEmail"

    // Kunci Loyalty
    private const val KEY_LOYALTY_TOTAL_POINTS = "loyaltyTotalPoints"
    private const val KEY_LOYALTY_ITEMS = "loyaltyItemsList"

    // Kunci untuk setiap status klaim reward (disesuaikan dengan struktur statis LoyaltyUserStatus)
    private const val KEY_CLAIM_DATE_5 = "claimDate5"
    private const val KEY_CLAIM_DATE_10 = "claimDate10"
    private const val KEY_CLAIM_DATE_15 = "claimDate15"
    private const val KEY_CLAIM_DATE_20 = "claimDate20"
    private const val KEY_CLAIM_DATE_25 = "claimDate25"
    private const val KEY_CLAIM_DATE_30 = "claimDate30"
    private const val KEY_CLAIM_DATE_35 = "claimDate35"
    private const val KEY_CLAIM_DATE_40 = "claimDate40"
    private const val KEY_CLAIM_DATE_45 = "claimDate45"
    private const val KEY_CLAIM_DATE_50 = "claimDate50"
    private const val KEY_CLAIM_DATE_55 = "claimDate55"
    private const val KEY_CLAIM_DATE_60 = "claimDate60"
    private const val KEY_CLAIM_DATE_65 = "claimDate65"
    private const val KEY_CLAIM_DATE_70 = "claimDate70"
    private const val KEY_CLAIM_DATE_75 = "claimDate75"
    private const val KEY_CLAIM_DATE_80 = "claimDate80"
    private const val KEY_CLAIM_DATE_85 = "claimDate85"
    private const val KEY_CLAIM_DATE_90 = "claimDate90"
    private const val KEY_CLAIM_DATE_95 = "claimDate95"


    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun logout(context: Context) {
        val editor = getSharedPreferences(context).edit()
        // Hapus data sesi
        editor.remove(KEY_IS_LOGGED_IN)
        editor.remove(KEY_USER_UID)
        editor.remove(KEY_USER_NAME)
        editor.remove(KEY_USER_EMAIL)
        // Hapus data loyalty
        editor.remove(KEY_LOYALTY_TOTAL_POINTS)
        editor.remove(KEY_LOYALTY_ITEMS)
        // Hapus juga semua kunci klaim individual
        editor.remove(KEY_CLAIM_DATE_5)
        editor.remove(KEY_CLAIM_DATE_10)
        editor.remove(KEY_CLAIM_DATE_15)
        editor.remove(KEY_CLAIM_DATE_20)
        editor.remove(KEY_CLAIM_DATE_25)
        editor.remove(KEY_CLAIM_DATE_30)
        editor.remove(KEY_CLAIM_DATE_35)
        editor.remove(KEY_CLAIM_DATE_40)
        editor.remove(KEY_CLAIM_DATE_45)
        editor.remove(KEY_CLAIM_DATE_50)
        editor.remove(KEY_CLAIM_DATE_55)
        editor.remove(KEY_CLAIM_DATE_60)
        editor.remove(KEY_CLAIM_DATE_65)
        editor.remove(KEY_CLAIM_DATE_70)
        editor.remove(KEY_CLAIM_DATE_75)
        editor.remove(KEY_CLAIM_DATE_80)
        editor.remove(KEY_CLAIM_DATE_85)
        editor.remove(KEY_CLAIM_DATE_90)
        editor.remove(KEY_CLAIM_DATE_95)
        editor.apply()
    }

    // --- Fungsi Sesi Pengguna ---
    fun setLoggedIn(context: Context, loggedIn: Boolean) {
        getSharedPreferences(context).edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun saveUserData(context: Context, uid: Int, name: String, email: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putInt(KEY_USER_UID, uid)
        editor.putString(KEY_USER_NAME, name)
        editor.putString(KEY_USER_EMAIL, email)
        editor.apply()
    }

    fun getUid(context: Context): Int = getSharedPreferences(context).getInt(KEY_USER_UID, 0)
    fun getUserName(context: Context): String? = getSharedPreferences(context).getString(KEY_USER_NAME, null)
    fun getUserEmail(context: Context): String? = getSharedPreferences(context).getString(KEY_USER_EMAIL, null)

    // --- Fungsi Loyalty & Reward ---

    /**
     * Menyimpan status loyalty (struktur statis) ke SharedPreferences.
     */
    fun saveLoyaltyUserStatus(context: Context, status: LoyaltyUserStatus) {
        val editor = getSharedPreferences(context).edit()
        editor.putInt(KEY_LOYALTY_TOTAL_POINTS, status.totalPoints)
        editor.putString(KEY_CLAIM_DATE_5, status.discount10ClaimDate)
        editor.putString(KEY_CLAIM_DATE_10, status.freeServeClaimDate)
        editor.putString(KEY_CLAIM_DATE_15, status.discount10Slot15ClaimDate)
        editor.putString(KEY_CLAIM_DATE_20, status.freeTshirtClaimDate)
        editor.putString(KEY_CLAIM_DATE_25, status.discount10_25ClaimDate)
        editor.putString(KEY_CLAIM_DATE_30, status.freeServe_30ClaimDate)
        editor.putString(KEY_CLAIM_DATE_35, status.discount10_35ClaimDate)
        editor.putString(KEY_CLAIM_DATE_40, status.freeServe_40ClaimDate)
        editor.putString(KEY_CLAIM_DATE_45, status.discount10_45ClaimDate)
        editor.putString(KEY_CLAIM_DATE_50, status.freeServe_50ClaimDate)
        editor.putString(KEY_CLAIM_DATE_55, status.discount10_55ClaimDate)
        editor.putString(KEY_CLAIM_DATE_60, status.freeServe_60ClaimDate)
        editor.putString(KEY_CLAIM_DATE_65, status.discount10_65ClaimDate)
        editor.putString(KEY_CLAIM_DATE_70, status.freeServe_70ClaimDate)
        editor.putString(KEY_CLAIM_DATE_75, status.discount10_75ClaimDate)
        editor.putString(KEY_CLAIM_DATE_80, status.freeServe_80ClaimDate)
        editor.putString(KEY_CLAIM_DATE_85, status.discount10_85ClaimDate)
        editor.putString(KEY_CLAIM_DATE_90, status.freeServe_90ClaimDate)
        editor.putString(KEY_CLAIM_DATE_95, status.discount10_95ClaimDate)
        editor.apply()
    }

    /**
     * Mengambil status loyalty (struktur statis) dari SharedPreferences.
     */
    fun getLoyaltyUserStatus(context: Context): LoyaltyUserStatus {
        val prefs = getSharedPreferences(context)
        val points = prefs.getInt(KEY_LOYALTY_TOTAL_POINTS, 0)

        return LoyaltyUserStatus(
            totalPoints = points,
            discount10ClaimDate = prefs.getString(KEY_CLAIM_DATE_5, null),
            freeServeClaimDate = prefs.getString(KEY_CLAIM_DATE_10, null),
            discount10Slot15ClaimDate = prefs.getString(KEY_CLAIM_DATE_15, null),
            freeTshirtClaimDate = prefs.getString(KEY_CLAIM_DATE_20, null),
            discount10_25ClaimDate = prefs.getString(KEY_CLAIM_DATE_25, null),
            freeServe_30ClaimDate = prefs.getString(KEY_CLAIM_DATE_30, null),
            discount10_35ClaimDate = prefs.getString(KEY_CLAIM_DATE_35, null),
            freeServe_40ClaimDate = prefs.getString(KEY_CLAIM_DATE_40, null),
            discount10_45ClaimDate = prefs.getString(KEY_CLAIM_DATE_45, null),
            freeServe_50ClaimDate = prefs.getString(KEY_CLAIM_DATE_50, null),
            discount10_55ClaimDate = prefs.getString(KEY_CLAIM_DATE_55, null),
            freeServe_60ClaimDate = prefs.getString(KEY_CLAIM_DATE_60, null),
            discount10_65ClaimDate = prefs.getString(KEY_CLAIM_DATE_65, null),
            freeServe_70ClaimDate = prefs.getString(KEY_CLAIM_DATE_70, null),
            discount10_75ClaimDate = prefs.getString(KEY_CLAIM_DATE_75, null),
            freeServe_80ClaimDate = prefs.getString(KEY_CLAIM_DATE_80, null),
            discount10_85ClaimDate = prefs.getString(KEY_CLAIM_DATE_85, null),
            freeServe_90ClaimDate = prefs.getString(KEY_CLAIM_DATE_90, null),
            discount10_95ClaimDate = prefs.getString(KEY_CLAIM_DATE_95, null)
        )
    }

    fun saveLoyaltyItems(context: Context, items: List<LoyaltyItemV2>) {
        val editor = getSharedPreferences(context).edit()
        val gson = Gson()
        val json = gson.toJson(items)
        editor.putString(KEY_LOYALTY_ITEMS, json)
        editor.apply()
    }

    fun getLoyaltyItems(context: Context): List<LoyaltyItemV2> {
        val prefs = getSharedPreferences(context)
        val gson = Gson()
        val json = prefs.getString(KEY_LOYALTY_ITEMS, null)
        return if (json != null) {
            val type = object : TypeToken<List<LoyaltyItemV2>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }
}