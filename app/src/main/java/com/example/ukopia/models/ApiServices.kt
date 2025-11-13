package com.example.ukopia.models

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

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

    // --- ENDPOINT MENU & ULASAN ---

    @GET("menu/get_menu.php")
    suspend fun getMenu(
        @Query("id_kategori") id_kategori: Int? = null
    ): Response<MenuApiResponse>

    @GET("menu/get_detail_menu.php")
    suspend fun getMenuDetails(
        @Query("id_menu") id_menu: Int,
        @Query("uid_akun") uid_akun: Int // uid user yang login
    ): Response<ReviewApiResponse>

    @Headers("Content-Type: application/json")
    @POST("ulasan/post_ulasan.php") // Ini akan INSERT atau UPDATE
    suspend fun postReview(
        @Body requestBody: ReviewPostRequest
    ): Response<GenericResponse>

    @Headers("Content-Type: application/json")
    @POST("ulasan/update_ulasan.php")
    suspend fun updateReview(
        @Body requestBody: ReviewUpdateRequest
    ): Response<GenericResponse>

    @Headers("Content-Type: application/json")
    @POST("ulasan/delete_ulasan.php")
    suspend fun deleteReview(
        @Body requestBody: ReviewDeleteRequest
    ): Response<GenericResponse>
}