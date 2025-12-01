// File: D:/github_rama/Ukopia/app/src/main/java/com/example/ukopia/models/ApiServices.kt
package com.example.ukopia.models

import com.example.ukopia.data.ApiResponse
import com.example.ukopia.data.BrewMethod
import com.example.ukopia.data.CreateRecipeRequest
import com.example.ukopia.data.EquipmentItem
import com.example.ukopia.data.RecipeItem
import com.example.ukopia.data.SubEquipmentItem
import com.example.ukopia.data.LoyaltyListResponse
import com.example.ukopia.data.LoyaltyUserStatus
import com.example.ukopia.data.LoyaltyStatusResponse
import com.example.ukopia.data.RewardHistoryResponse
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
    @GET("resep/metode.php")
    suspend fun getBrewMethods(): Response<ApiResponse<List<BrewMethod>>>

    @GET("alat/kategori_with_count.php")
    suspend fun getEquipmentCategories(): Response<ApiResponse<List<EquipmentItem>>>

    @GET("alat/by_kategori.php")
    suspend fun getToolsByCategory(
        @Query("id_kategori") categoryId: Int
    ): Response<ApiResponse<List<SubEquipmentItem>>>

    @GET("resep/by_metode.php")
    suspend fun getRecipes(
        @Query("id_metode") methodId: Int,
        @Query("type") type: String, // 'all' atau 'my'
        @Query("uid") uid: Int = 0
    ): Response<ApiResponse<List<RecipeItem>>>

    @GET("resep/detail.php")
    suspend fun getRecipeDetail(
        @Query("id_resep") recipeId: Int
    ): Response<ApiResponse<RecipeItem>>

    @POST("resep/create.php")
    suspend fun createRecipe(
        @Body request: CreateRecipeRequest
    ): Response<ApiResponse<Any>>

    @POST("resep/delete.php")
    suspend fun deleteRecipe(
        @Body body: Map<String, Int> // Kirim {"id_resep": 123}
    ): Response<ApiResponse<Any>>

    @GET("loyalty/list.php")
    suspend fun getLoyaltyList(
        @Query("uid") uid: Int,
        @Query("type") type: String // 'pending' atau 'history'
    ): Response<LoyaltyListResponse>

    // 2. Update Review (Mengembalikan status lengkap)
    @POST("loyalty/update.php")
    suspend fun updateLoyaltyReview(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<LoyaltyStatusResponse> // <--- Revisi

    // 3. Ambil Status Reward & Total Poin
    @GET("loyalty/status.php")
    suspend fun getLoyaltyStatus(
        @Query("uid") uid: Int
    ): Response<LoyaltyStatusResponse>

    @GET("reward/history.php")
    suspend fun getRewardHistory(
        @Query("uid") uid: Int
    ): Response<RewardHistoryResponse>
}