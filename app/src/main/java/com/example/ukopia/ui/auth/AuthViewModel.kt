package com.example.ukopia.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ukopia.models.ApiClient
import com.example.ukopia.models.ForgotPasswordRequest
import com.example.ukopia.models.LoginRequest
import com.example.ukopia.models.LoginResponse
import com.example.ukopia.models.RegisterRequest
import kotlinx.coroutines.launch
import org.json.JSONObject

class AuthViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    // LiveData khusus untuk Login Sukses
    private val _loginSuccess = MutableLiveData<LoginResponse?>()
    val loginSuccess: LiveData<LoginResponse?> = _loginSuccess

    // LiveData khusus untuk Register Sukses (Trigger Popup)
    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> = _registerSuccess

    // LiveData untuk hasil Lupa Password
    private val _forgotPasswordState = MutableLiveData<String?>() // "otp_sent", "otp_verified", "password_reset"
    val forgotPasswordState: LiveData<String?> = _forgotPasswordState

    fun register(request: RegisterRequest) {
        _isLoading.value = true
        _registerSuccess.value = false // Reset state

        viewModelScope.launch {
            try {
                val response = ApiClient.instance.registerUser(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    // SUKSES
                    _message.value = response.body()?.message
                    _registerSuccess.value = true // Trigger Popup di Activity
                } else {
                    // GAGAL (Server merespon, tapi success=false)
                    // Coba baca error body jika ada, atau pakai message dari JSON
                    val errorMsg = response.body()?.message
                        ?: parseError(response.errorBody()?.string())
                        ?: "Gagal mendaftar"
                    _message.value = errorMsg
                }
            } catch (e: Exception) {
                Log.e("AUTH_API", "Error Register: ${e.message}")
                _message.value = "Terjadi kesalahan koneksi"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun login(request: LoginRequest) {
        _isLoading.value = true
        _loginSuccess.value = null // Reset state

        viewModelScope.launch {
            try {
                val response = ApiClient.instance.loginUser(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    // LOGIN SUKSES
                    _loginSuccess.value = response.body()
                } else {
                    // LOGIN GAGAL
                    val errorMsg = response.body()?.message
                        ?: parseError(response.errorBody()?.string())
                        ?: "Login gagal"
                    _message.value = errorMsg
                }
            } catch (e: Exception) {
                Log.e("AUTH_API", "Error Login: ${e.message}")
                _message.value = "Terjadi kesalahan koneksi"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Helper untuk parsing error JSON manual jika response code != 200
    private fun parseError(json: String?): String? {
        return try {
            val obj = JSONObject(json ?: "")
            obj.getString("message")
        } catch (e: Exception) {
            null
        }
    }

    fun sendOtp(email: String) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                val request = ForgotPasswordRequest(action = "send_code", email = email)
                val response = ApiClient.instance.forgotPassword(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _message.postValue(response.body()?.message)
                    _forgotPasswordState.postValue("otp_sent")
                } else {
                    _message.postValue(response.body()?.message ?: "Gagal mengirim kode")
                }
            } catch (e: Exception) {
                _message.postValue("Error: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun verifyOtp(email: String, code: String) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                val request = ForgotPasswordRequest(action = "verify_code", email = email, code = code)
                val response = ApiClient.instance.forgotPassword(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _message.postValue("Kode valid!")
                    _forgotPasswordState.postValue("otp_verified")
                } else {
                    _message.postValue(response.body()?.message ?: "Kode salah")
                }
            } catch (e: Exception) {
                _message.postValue("Error: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun resetPassword(email: String, code: String, newPass: String) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                val request = ForgotPasswordRequest(
                    action = "reset_password",
                    email = email,
                    code = code,
                    new_password = newPass
                )
                val response = ApiClient.instance.forgotPassword(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _message.postValue("Password berhasil diubah")
                    _forgotPasswordState.postValue("password_reset")
                } else {
                    _message.postValue(response.body()?.message ?: "Gagal mereset password")
                }
            } catch (e: Exception) {
                _message.postValue("Error: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

}