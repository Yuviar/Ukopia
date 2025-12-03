package com.example.ukopia.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ukopia.databinding.ActivityLupaPasswordBinding

class LupaPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLupaPasswordBinding
    private lateinit var authViewModel: AuthViewModel

    private var userEmail = ""
    private var currentOtpCode = ""

    companion object {
        const val EXTRA_SOURCE = "extra_source"
        const val SOURCE_ACCOUNT = "source_account"
        const val SOURCE_LOGIN = "source_login"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLupaPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        // Pastikan ID tombol ini sesuai XML (tombol di layout Input Email)
        binding.btnKirimKode.setOnClickListener {
            userEmail = binding.editEmail.text.toString().trim()
            if (userEmail.isEmpty()) {
                binding.editEmail.error = "Email wajib diisi"
                return@setOnClickListener
            }
            authViewModel.sendOtp(userEmail)
        }
    }

    private fun setupObservers() {
        authViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnKirimKode.isEnabled = !isLoading
        }

        // Observer Pesan (Hanya muncul jika Error)
        authViewModel.message.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }

        // Observer State Pindah Halaman
        authViewModel.forgotPasswordState.observe(this) { state ->
            when (state) {
                "otp_sent" -> {
                    showOtpDialog()
                }
                "otp_verified" -> {
                    // Navigasi ke halaman Ganti Password
                    val intent = Intent(this, SetNewPasswordActivity::class.java)
                    intent.putExtra("EMAIL", userEmail)
                    // [PENTING] Kirim kode OTP juga! API Reset butuh ini.
                    intent.putExtra("CODE", currentOtpCode)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun showOtpDialog() {
        val dialog = OtpDialogFragment()

        dialog.onVerifyListener = { code ->
            // 1. Simpan kode yang diinput user
            currentOtpCode = code

            // 2. Panggil API Verifikasi
            authViewModel.verifyOtp(userEmail, code)
        }

        dialog.onResendListener = {
            authViewModel.sendOtp(userEmail)
        }

        dialog.show(supportFragmentManager, "OtpDialog")
    }
}