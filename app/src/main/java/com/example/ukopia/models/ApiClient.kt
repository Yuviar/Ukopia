package com.example.ukopia.models

import com.google.gson.GsonBuilder // Import ini
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit // Tambahkan import ini jika belum ada

object ApiClient {

    private const val BASE_URL = "http://192.168.18.11/SI-ukopia/BackOffice/api/" // Pastikan ini URL yang benar

    // Interceptor untuk logging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Konfigurasi OkHttpClient dengan interceptor dan timeout
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS) // Contoh: 30 detik
        .readTimeout(30, TimeUnit.SECONDS)    // Contoh: 30 detik
        .writeTimeout(30, TimeUnit.SECONDS)   // Contoh: 30 detik
        .build()

    // --- MODIFIKASI DIMULAI DI SINI ---
    // Konfigurasi Gson agar lebih toleran terhadap JSON yang tidak standar
    private val lenientGson = GsonBuilder()
        .setLenient() // Membuat Gson lebih toleran
        .create()
    // --- MODIFIKASI BERAKHIR DI SINI ---

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            // --- GUNAKAN lenientGson DI SINI ---
            .addConverterFactory(GsonConverterFactory.create(lenientGson)) // PENTING: Gunakan lenientGson
            .build()
    }

    val instance: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}