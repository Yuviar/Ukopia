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

    private val _loginSuccess = MutableLiveData<LoginResponse?>()
    val loginSuccess: LiveData<LoginResponse?> = _loginSuccess

    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> = _registerSuccess

    private val _forgotPasswordState = MutableLiveData<String?>()
    val forgotPasswordState: LiveData<String?> = _forgotPasswordState

    fun register(request: RegisterRequest) {
        _isLoading.postValue(true)
        _registerSuccess.postValue(false)

        viewModelScope.launch {
            try {
                val response = ApiClient.instance.registerUser(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _message.postValue(response.body()?.message ?: "Registrasi berhasil")
                    _registerSuccess.postValue(true)
                } else {
                    val errorMsg = response.body()?.message
                        ?: parseError(response.errorBody()?.string())
                        ?: "Gagal mendaftar"
                    _message.postValue(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Register Error", e)
                _message.postValue("Gagal terhubung ke server")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun login(request: LoginRequest) {
        _isLoading.postValue(true)
        _loginSuccess.postValue(null)

        viewModelScope.launch {
            try {
                val response = ApiClient.instance.loginUser(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _loginSuccess.postValue(response.body())
                } else {
                    val errorMsg = response.body()?.message
                        ?: parseError(response.errorBody()?.string())
                        ?: "Login gagal"
                    _message.postValue(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login Error", e)
                _message.postValue("Gagal terhubung ke server")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun sendOtp(email: String) {
        _isLoading.postValue(true)
        _forgotPasswordState.postValue(null)
        viewModelScope.launch {
            try {
                val request = ForgotPasswordRequest(action = "send_code", email = email)
                val response = ApiClient.instance.forgotPassword(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _forgotPasswordState.postValue("otp_sent")
                    _message.postValue(response.body()?.message)
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
                    _message.postValue(response.body()?.message ?: "Gagal mengubah password")
                }
            } catch (e: Exception) {
                _message.postValue("Error: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private fun parseError(jsonString: String?): String? {
        if (jsonString.isNullOrEmpty()) return null
        return try {
            val jsonObject = JSONObject(jsonString)
            jsonObject.getString("message")
        } catch (e: Exception) {
            null
        }
    }
}