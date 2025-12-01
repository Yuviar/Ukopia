package com.example.ukopia.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ukopia.databinding.ActivitySetNewPasswordBinding

class SetNewPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetNewPasswordBinding
    private lateinit var authViewModel: AuthViewModel
    private var email: String? = null
    private var code: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetNewPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil data dari LupaPasswordActivity
        email = intent.getStringExtra("EMAIL")
        code = intent.getStringExtra("CODE")

        // Validasi data
        if (email == null || code == null) {
            Toast.makeText(this, "Terjadi kesalahan data session", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        binding.btnGanti.setOnClickListener {
            val newPass = binding.editNewPassword.text.toString().trim()
            val confirmPass = binding.editConfirmPassword.text.toString().trim()

            if (newPass.isEmpty() || newPass.length < 8) {
                binding.editNewPassword.error = "Minimal 8 karakter"
                return@setOnClickListener
            }
            if (newPass != confirmPass) {
                binding.editConfirmPassword.error = "Password tidak cocok"
                return@setOnClickListener
            }

            // Panggil API Reset Password dengan Email, Kode, dan Password Baru
            authViewModel.resetPassword(email!!, code!!, newPass)
        }

        // Observer Hasil Reset
        authViewModel.forgotPasswordState.observe(this) { state ->
            if (state == "password_reset") {
                Toast.makeText(this, "Password Berhasil Diubah! Silakan Login.", Toast.LENGTH_LONG).show()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        // Observer Error
        authViewModel.message.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}