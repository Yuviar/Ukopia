package com.example.ukopia

import android.content.Context
import android.content.SharedPreferences
import com.example.ukopia.data.LoyaltyItemV2
import com.example.ukopia.data.LoyaltyUserStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

/**
 * Singleton object untuk mengelola semua data sesi aplikasi yang disimpan di SharedPreferences.
 * Termasuk status login, data pengguna (DENGAN UID), dan status loyalty.
 * Pemanggilan: SessionManager.isLoggedIn(context)
 */
object SessionManager {
    private const val PREF_NAME = "ukopia_prefs"

    // Kunci untuk Sesi Pengguna
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val KEY_USER_UID = "userUid"
    private const val KEY_USER_NAME = "userName"
    private const val KEY_USER_EMAIL = "userEmail"

    // Kunci untuk Loyalty (Diubah untuk menyimpan tanggal klaim sebagai String?)
    private const val KEY_LOYALTY_TOTAL_POINTS = "loyaltyTotalPoints"
    private const val KEY_LOYALTY_CLAIMED_DISCOUNT_10_DATE = "loyaltyClaimedDiscount10Date"
    private const val KEY_LOYALTY_CLAIMED_FREE_SERVE_DATE = "loyaltyClaimedFreeServeDate"
    private const val KEY_LOYALTY_CLAIMED_DISCOUNT_15_DATE = "loyaltyClaimedDiscount15Date" // for discount10Slot15ClaimDate
    private const val KEY_LOYALTY_CLAIMED_TSHIRT_DATE = "loyaltyClaimedTshirtDate"

    // Tambahan kunci untuk semua reward baru
    private const val KEY_LOYALTY_CLAIMED_DISCOUNT_25_DATE = "loyaltyClaimedDiscount25Date"
    private const val KEY_LOYALTY_CLAIMED_FREE_SERVE_30_DATE = "loyaltyClaimedFreeServe30Date"
    private const val KEY_LOYALTY_CLAIMED_DISCOUNT_35_DATE = "loyaltyClaimedDiscount35Date"
    private const val KEY_LOYALTY_CLAIMED_FREE_SERVE_40_DATE = "loyaltyClaimedFreeServe40Date"
    private const val KEY_LOYALTY_CLAIMED_DISCOUNT_45_DATE = "loyaltyClaimedDiscount45Date"
    private const val KEY_LOYALTY_CLAIMED_FREE_SERVE_50_DATE = "loyaltyClaimedFreeServe50Date"
    private const val KEY_LOYALTY_CLAIMED_DISCOUNT_55_DATE = "loyaltyClaimedDiscount55Date"
    private const val KEY_LOYALTY_CLAIMED_FREE_SERVE_60_DATE = "loyaltyClaimedFreeServe60Date"
    private const val KEY_LOYALTY_CLAIMED_DISCOUNT_65_DATE = "loyaltyClaimedDiscount65Date"
    private const val KEY_LOYALTY_CLAIMED_FREE_SERVE_70_DATE = "loyaltyClaimedFreeServe70Date"
    private const val KEY_LOYALTY_CLAIMED_DISCOUNT_75_DATE = "loyaltyClaimedDiscount75Date"
    private const val KEY_LOYALTY_CLAIMED_FREE_SERVE_80_DATE = "loyaltyClaimedFreeServe80Date"
    private const val KEY_LOYALTY_CLAIMED_DISCOUNT_85_DATE = "loyaltyClaimedDiscount85Date"
    private const val KEY_LOYALTY_CLAIMED_FREE_SERVE_90_DATE = "loyaltyClaimedFreeServe90Date"
    private const val KEY_LOYALTY_CLAIMED_DISCOUNT_95_DATE = "loyaltyClaimedDiscount95Date"

    // NEW: Kunci untuk daftar loyalty items
    private const val KEY_LOYALTY_ITEMS = "loyaltyItemsList"


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
        editor.remove(KEY_USER_UID)
        editor.remove(KEY_USER_NAME)
        editor.remove(KEY_USER_EMAIL)

        // Hapus semua data loyalty, termasuk tanggal klaim yang baru
        editor.remove(KEY_LOYALTY_TOTAL_POINTS)
        editor.remove(KEY_LOYALTY_CLAIMED_DISCOUNT_10_DATE)
        editor.remove(KEY_LOYALTY_CLAIMED_FREE_SERVE_DATE)
        editor.remove(KEY_LOYALTY_CLAIMED_DISCOUNT_15_DATE)
        editor.remove(KEY_LOYALTY_CLAIMED_TSHIRT_DATE)
        editor.remove(KEY_LOYALTY_CLAIMED_DISCOUNT_25_DATE)
        editor.remove(KEY_LOYALTY_CLAIMED_FREE_SERVE_30_DATE)
        editor.remove(KEY_LOYALTY_CLAIMED_DISCOUNT_35_DATE)
        editor.remove(KEY_LOYALTY_CLAIMED_FREE_SERVE_40_DATE)
        editor.remove(KEY_LOYALTY_CLAIMED_DISCOUNT_45_DATE)
        editor.remove(KEY_LOYALTY_CLAIMED_FREE_SERVE_50_DATE)
        editor.remove(KEY_LOYALTY_CLAIMED_DISCOUNT_55_DATE)
        editor.remove(KEY_LOYALTY_CLAIMED_FREE_SERVE_60_DATE)
        editor.remove(KEY_LOYALTY_CLAIMED_DISCOUNT_65_DATE)
        editor.remove(KEY_LOYALTY_CLAIMED_FREE_SERVE_70_DATE)
        editor.remove(KEY_LOYALTY_CLAIMED_DISCOUNT_75_DATE)
        editor.remove(KEY_LOYALTY_CLAIMED_FREE_SERVE_80_DATE)
        editor.remove(KEY_LOYALTY_CLAIMED_DISCOUNT_85_DATE)
        editor.remove(KEY_LOYALTY_CLAIMED_FREE_SERVE_90_DATE)
        editor.remove(KEY_LOYALTY_CLAIMED_DISCOUNT_95_DATE)

        // NEW: Hapus daftar loyalty items
        editor.remove(KEY_LOYALTY_ITEMS)

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

    /**
     * Menyimpan data pengguna lengkap termasuk UID dari database.
     */
    fun saveUserData(context: Context, uid: Int, name: String, email: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putInt(KEY_USER_UID, uid)
        editor.putString(KEY_USER_NAME, name)
        editor.putString(KEY_USER_EMAIL, email)
        editor.apply()
    }

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

    // --- Fungsi Loyalty (Diperbarui) ---

    fun saveLoyaltyUserStatus(context: Context, status: LoyaltyUserStatus) {
        val editor = getSharedPreferences(context).edit()
        editor.putInt(KEY_LOYALTY_TOTAL_POINTS, status.totalPoints)

        // Simpan tanggal klaim sebagai String?
        editor.putString(KEY_LOYALTY_CLAIMED_DISCOUNT_10_DATE, status.discount10ClaimDate)
        editor.putString(KEY_LOYALTY_CLAIMED_FREE_SERVE_DATE, status.freeServeClaimDate)
        editor.putString(KEY_LOYALTY_CLAIMED_DISCOUNT_15_DATE, status.discount10Slot15ClaimDate)
        editor.putString(KEY_LOYALTY_CLAIMED_TSHIRT_DATE, status.freeTshirtClaimDate)
        editor.putString(KEY_LOYALTY_CLAIMED_DISCOUNT_25_DATE, status.discount10_25ClaimDate)
        editor.putString(KEY_LOYALTY_CLAIMED_FREE_SERVE_30_DATE, status.freeServe_30ClaimDate)
        editor.putString(KEY_LOYALTY_CLAIMED_DISCOUNT_35_DATE, status.discount10_35ClaimDate)
        editor.putString(KEY_LOYALTY_CLAIMED_FREE_SERVE_40_DATE, status.freeServe_40ClaimDate)
        editor.putString(KEY_LOYALTY_CLAIMED_DISCOUNT_45_DATE, status.discount10_45ClaimDate)
        editor.putString(KEY_LOYALTY_CLAIMED_FREE_SERVE_50_DATE, status.freeServe_50ClaimDate)
        editor.putString(KEY_LOYALTY_CLAIMED_DISCOUNT_55_DATE, status.discount10_55ClaimDate)
        editor.putString(KEY_LOYALTY_CLAIMED_FREE_SERVE_60_DATE, status.freeServe_60ClaimDate)
        editor.putString(KEY_LOYALTY_CLAIMED_DISCOUNT_65_DATE, status.discount10_65ClaimDate)
        editor.putString(KEY_LOYALTY_CLAIMED_FREE_SERVE_70_DATE, status.freeServe_70ClaimDate)
        editor.putString(KEY_LOYALTY_CLAIMED_DISCOUNT_75_DATE, status.discount10_75ClaimDate)
        editor.putString(KEY_LOYALTY_CLAIMED_FREE_SERVE_80_DATE, status.freeServe_80ClaimDate)
        editor.putString(KEY_LOYALTY_CLAIMED_DISCOUNT_85_DATE, status.discount10_85ClaimDate)
        editor.putString(KEY_LOYALTY_CLAIMED_FREE_SERVE_90_DATE, status.freeServe_90ClaimDate)
        editor.putString(KEY_LOYALTY_CLAIMED_DISCOUNT_95_DATE, status.discount10_95ClaimDate)

        editor.apply()
    }

    fun getLoyaltyUserStatus(context: Context): LoyaltyUserStatus {
        val prefs = getSharedPreferences(context)
        val points = prefs.getInt(KEY_LOYALTY_TOTAL_POINTS, 0)

        // Ambil tanggal klaim sebagai String?
        val discount10ClaimDate = prefs.getString(KEY_LOYALTY_CLAIMED_DISCOUNT_10_DATE, null)
        val freeServeClaimDate = prefs.getString(KEY_LOYALTY_CLAIMED_FREE_SERVE_DATE, null)
        val discount10Slot15ClaimDate = prefs.getString(KEY_LOYALTY_CLAIMED_DISCOUNT_15_DATE, null)
        val freeTshirtClaimDate = prefs.getString(KEY_LOYALTY_CLAIMED_TSHIRT_DATE, null)
        val discount10_25ClaimDate = prefs.getString(KEY_LOYALTY_CLAIMED_DISCOUNT_25_DATE, null)
        val freeServe_30ClaimDate = prefs.getString(KEY_LOYALTY_CLAIMED_FREE_SERVE_30_DATE, null)
        val discount10_35ClaimDate = prefs.getString(KEY_LOYALTY_CLAIMED_DISCOUNT_35_DATE, null)
        val freeServe_40ClaimDate = prefs.getString(KEY_LOYALTY_CLAIMED_FREE_SERVE_40_DATE, null)
        val discount10_45ClaimDate = prefs.getString(KEY_LOYALTY_CLAIMED_DISCOUNT_45_DATE, null)
        val freeServe_50ClaimDate = prefs.getString(KEY_LOYALTY_CLAIMED_FREE_SERVE_50_DATE, null)
        val discount10_55ClaimDate = prefs.getString(KEY_LOYALTY_CLAIMED_DISCOUNT_55_DATE, null)
        val freeServe_60ClaimDate = prefs.getString(KEY_LOYALTY_CLAIMED_FREE_SERVE_60_DATE, null)
        val discount10_65ClaimDate = prefs.getString(KEY_LOYALTY_CLAIMED_DISCOUNT_65_DATE, null)
        val freeServe_70ClaimDate = prefs.getString(KEY_LOYALTY_CLAIMED_FREE_SERVE_70_DATE, null)
        val discount10_75ClaimDate = prefs.getString(KEY_LOYALTY_CLAIMED_DISCOUNT_75_DATE, null)
        val freeServe_80ClaimDate = prefs.getString(KEY_LOYALTY_CLAIMED_FREE_SERVE_80_DATE, null)
        val discount10_85ClaimDate = prefs.getString(KEY_LOYALTY_CLAIMED_DISCOUNT_85_DATE, null)
        val freeServe_90ClaimDate = prefs.getString(KEY_LOYALTY_CLAIMED_FREE_SERVE_90_DATE, null)
        val discount10_95ClaimDate = prefs.getString(KEY_LOYALTY_CLAIMED_DISCOUNT_95_DATE, null)

        return LoyaltyUserStatus(
            totalPoints = points,
            discount10ClaimDate = discount10ClaimDate,
            freeServeClaimDate = freeServeClaimDate,
            discount10Slot15ClaimDate = discount10Slot15ClaimDate,
            freeTshirtClaimDate = freeTshirtClaimDate,
            discount10_25ClaimDate = discount10_25ClaimDate,
            freeServe_30ClaimDate = freeServe_30ClaimDate,
            discount10_35ClaimDate = discount10_35ClaimDate,
            freeServe_40ClaimDate = freeServe_40ClaimDate,
            discount10_45ClaimDate = discount10_45ClaimDate,
            freeServe_50ClaimDate = freeServe_50ClaimDate,
            discount10_55ClaimDate = discount10_55ClaimDate,
            freeServe_60ClaimDate = freeServe_60ClaimDate,
            discount10_65ClaimDate = discount10_65ClaimDate,
            freeServe_70ClaimDate = freeServe_70ClaimDate,
            discount10_75ClaimDate = discount10_75ClaimDate,
            freeServe_80ClaimDate = freeServe_80ClaimDate,
            discount10_85ClaimDate = discount10_85ClaimDate,
            freeServe_90ClaimDate = freeServe_90ClaimDate,
            discount10_95ClaimDate = discount10_95ClaimDate
        )
    }

    // NEW: Fungsi untuk menyimpan daftar LoyaltyItemV2
    fun saveLoyaltyItems(context: Context, items: List<LoyaltyItemV2>) {
        val editor = getSharedPreferences(context).edit()
        val gson = Gson()
        val json = gson.toJson(items)
        editor.putString(KEY_LOYALTY_ITEMS, json)
        editor.apply()
    }

    // NEW: Fungsi untuk mengambil daftar LoyaltyItemV2
    fun getLoyaltyItems(context: Context): List<LoyaltyItemV2> {
        val sharedPreferences = getSharedPreferences(context)
        val gson = Gson()
        val json = sharedPreferences.getString(KEY_LOYALTY_ITEMS, null)
        return if (json != null) {
            val type = object : TypeToken<List<LoyaltyItemV2>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }
}