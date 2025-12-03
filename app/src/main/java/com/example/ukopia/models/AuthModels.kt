package com.example.ukopia.models

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val nama: String,
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val identifier: String,
    val password: String
)

data class GenericResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("is_verified") val isVerified: Boolean? = null
)

data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: UserData?
)

data class UserData(
    @SerializedName("uid") val uid: Int,
    @SerializedName("nama") val nama: String,
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String
)

data class ForgotPasswordRequest(
    val action: String,
    val email: String,
    val code: String? = null,
    val new_password: String? = null
)