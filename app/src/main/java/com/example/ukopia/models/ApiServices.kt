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
import com.example.ukopia.data.PromoResponse
import com.example.ukopia.ui.auth.AuthViewModel
import com.example.ukopia.data.RewardHistoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName

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

    @POST("auth/forgot.php")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<GenericResponse>

    @GET("menu/get_menu.php")
    suspend fun getMenu(
        @Query("id_kategori") id_kategori: Int? = null,
        @Query("id_menu") menuId: Int? = null
    ): Response<MenuApiResponse>

    @GET("menu/get_detail_menu.php")
    suspend fun getMenuDetails(
        @Query("id_menu") id_menu: Int,
        @Query("uid_akun") uid_akun: Int
    ): Response<ReviewApiResponse>

    @Headers("Content-Type: application/json")
    @POST("ulasan/post_ulasan.php")
    suspend fun postReview(
        @Body requestBody: ReviewPostRequest
    ): Response<GeneralApiResponse>

    @Headers("Content-Type: application/json")
    @POST("ulasan/update_ulasan.php")
    suspend fun updateReview(
        @Body requestBody: ReviewUpdateRequest
    ): Response<GenericResponse>

    @Headers("Content-Type: application/json")
    @POST("ulasan/delete_ulasan.php")
    suspend fun deleteReview(
        @Body body: Map<String, Int>
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
        @Query("type") type: String,
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
        @Body body: Map<String, Int>
    ): Response<ApiResponse<Any>>

    @GET("loyalty/list.php")
    suspend fun getLoyaltyList(
        @Query("uid") uid: Int,
        @Query("type") type: String
    ): Response<LoyaltyListResponse>

    @POST("loyalty/update.php")
    suspend fun updateLoyaltyReview(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<LoyaltyStatusResponse>

    @GET("loyalty/status.php")
    suspend fun getLoyaltyStatus(
        @Query("uid") uid: Int
    ): Response<LoyaltyStatusResponse>

    @GET("reward/history.php")
    suspend fun getRewardHistory(
        @Query("uid") uid: Int
    ): Response<RewardHistoryResponse>

    @GET("promo/latest.php")
    suspend fun getLatestPromo(): Response<PromoResponse>

    @GET("menu/kategori.php")
    suspend fun getCategories(): Response<CategoryApiResponse>
}

data class GeneralApiResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?
)