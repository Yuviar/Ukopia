package com.example.ukopia.models

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val nama: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

// --- Response Models ---

data class GenericResponse(
    @SerializedName("message")
    val message: String
)

data class LoginResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: UserData?
)

data class UserData(
    @SerializedName("uid")
    val uid: Int,
    @SerializedName("nama")
    val nama: String,
    @SerializedName("email")
    val email: String
)