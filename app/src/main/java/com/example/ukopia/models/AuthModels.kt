package com.example.ukopia.models

import com.google.gson.annotations.SerializedName

// Request untuk Register
data class RegisterRequest(
    val nama: String,
    val username: String,
    val email: String,
    val password: String
)

// Request untuk Login (Identifier bisa email atau username)
data class LoginRequest(
    val identifier: String,
    val password: String
)

// Response Umum (Success/Error + Message)
data class GenericResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    // Optional: kadang API login mengembalikan is_verified
    @SerializedName("is_verified") val isVerified: Boolean? = null
)

// Response Login Sukses (Ada data User)
data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: UserData?
)

// Data User
data class UserData(
    @SerializedName("uid") val uid: Int,
    @SerializedName("nama") val nama: String,
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String
)
// Tambahkan di file com.example.ukopia.models.AuthModels.kt

data class ForgotPasswordRequest(
    val action: String, // "send_code", "verify_code", atau "reset_password"
    val email: String,
    val code: String? = null,        // Optional (hanya untuk verify & reset)
    val new_password: String? = null // Optional (hanya untuk reset)
)