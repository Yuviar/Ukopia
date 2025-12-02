package com.example.ukopia.models

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

// Interface ApiService dan data class GeneralApiResponse telah dipindahkan ke ApiServices.kt

// --- Objek Singleton untuk Akses API ---
object ApiClient {
    private const val BASE_URL = "http://192.168.1.13/si-ukopia/backoffice/api/" // GANTI DENGAN IP ANDA

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Untuk log request dan response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS) // Contoh: 30 detik timeout koneksi
        .readTimeout(30, TimeUnit.SECONDS)    // Contoh: 30 detik timeout baca
        .writeTimeout(30, TimeUnit.SECONDS)   // Contoh: 30 detik timeout tulis
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Gunakan OkHttpClient yang sudah dibuat
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Pastikan ini mengacu pada ApiService yang didefinisikan di ApiServices.kt
    // Karena ApiClient.kt dan ApiServices.kt berada dalam package yang sama (com.example.ukopia.models),
    // ApiService seharusnya otomatis terlihat dan tidak perlu import eksplisit di sini.
    val instance: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}