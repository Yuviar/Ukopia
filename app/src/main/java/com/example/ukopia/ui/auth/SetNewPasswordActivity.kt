package com.example.ukopia.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ukopia.R
import com.example.ukopia.databinding.ActivitySetNewPasswordBinding

class SetNewPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetNewPasswordBinding
    private lateinit var authViewModel: AuthViewModel
    private var email: String? = null
    private var code: String? = null

    // State untuk visibilitas password
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetNewPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent.getStringExtra("EMAIL")
        code = intent.getStringExtra("CODE")

        if (email == null || code == null) {
            Toast.makeText(this, "Terjadi kesalahan data session", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupPasswordToggles() // Setup fungsi toggle mata
        setupListeners()
        setupObservers()
    }

    private fun setupPasswordToggles() {
        binding.btnToggleNewPassword.setOnClickListener {
            if (isNewPasswordVisible) {
                binding.editNewPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.btnToggleNewPassword.setImageResource(R.drawable.ic_eye)
                isNewPasswordVisible = false
            } else {
                binding.editNewPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.btnToggleNewPassword.setImageResource(R.drawable.ic_eye_off)
                isNewPasswordVisible = true
            }
            binding.editNewPassword.setSelection(binding.editNewPassword.text.length)
        }

        binding.btnToggleConfirmPassword.setOnClickListener {
            if (isConfirmPasswordVisible) {
                binding.editConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.btnToggleConfirmPassword.setImageResource(R.drawable.ic_eye)
                isConfirmPasswordVisible = false
            } else {
                binding.editConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.btnToggleConfirmPassword.setImageResource(R.drawable.ic_eye_off)
                isConfirmPasswordVisible = true
            }
            binding.editConfirmPassword.setSelection(binding.editConfirmPassword.text.length)
        }
    }

    private fun setupListeners() {
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

            authViewModel.resetPassword(email!!, code!!, newPass)
        }
    }

    private fun setupObservers() {
        authViewModel.forgotPasswordState.observe(this) { state ->
            if (state == "password_reset") {
                Toast.makeText(this, "Password Berhasil Diubah! Silakan Login.", Toast.LENGTH_LONG).show()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        authViewModel.message.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}