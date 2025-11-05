package com.example.ukopia.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ukopia.models.ApiClient
import com.example.ukopia.models.LoginRequest
import com.example.ukopia.models.LoginResponse
import com.example.ukopia.models.RegisterRequest
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    // Untuk pesan umum (registrasi, error)
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    // KHUSUS untuk data login yang sukses
    private val _loginResult = MutableLiveData<LoginResponse?>()
    val loginResult: LiveData<LoginResponse?> = _loginResult

    // Untuk status loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun register(request: RegisterRequest) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                val response = ApiClient.instance.registerUser(request)
                if (response.isSuccessful) {
                    _message.postValue(response.body()?.message ?: "Registrasi berhasil!")
                } else {
                    _message.postValue("Error ${response.code()}: Gagal mendaftar.")
                }
            } catch (e: Exception) {
                // Catat error ke Logcat agar mudah di-debug
                Log.e("AuthViewModel", "Register failed with exception", e)
                _message.postValue("Gagal terhubung ke server: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun login(request: LoginRequest) {
        _isLoading.postValue(true)
        _loginResult.postValue(null) // Reset state dari login sebelumnya

        viewModelScope.launch {
            try {
                val response = ApiClient.instance.loginUser(request)
                if (response.isSuccessful && response.body() != null) {
                    // Kirim seluruh response object agar Activity bisa ambil data user
                    _loginResult.postValue(response.body())
                } else {
                    // Coba baca pesan error dari server jika ada
                    val errorBody = response.errorBody()?.string()
                    Log.w("AuthViewModel", "Login failed. Code: ${response.code()}, Error Body: $errorBody")
                    _message.postValue("Gagal Login. " + (errorBody ?: "Pastikan email dan password benar."))
                }
            } catch (e: Exception) {
                // INI BAGIAN PALING PENTING UNTUK DEBUGGING
                // Mencatat exception lengkap ke Logcat dengan level Error
                Log.e("AuthViewModel", "Exception during login API call", e)
                _message.postValue("Gagal terhubung ke server. Periksa koneksi internet Anda.")
            } finally {
                // Pastikan loading selalu berhenti, baik sukses maupun gagal
                _isLoading.postValue(false)
            }
        }
    }
}