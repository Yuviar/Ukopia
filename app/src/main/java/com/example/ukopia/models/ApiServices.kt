package com.example.ukopia.models

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("auth/register.php")
    suspend fun registerUser(
        @Body requestBody: RegisterRequest
    ): Response<GenericResponse>

    @Headers("Content-Type: application/json")
    @POST("auth/login.php")
    suspend fun loginUser(
        @Body requestBody: LoginRequest
    ): Response<LoginResponse>
}