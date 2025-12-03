package com.example.ukopia.ui.auth

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.ukopia.R
import com.example.ukopia.databinding.ActivityRegisterBinding
import com.example.ukopia.models.RegisterRequest

class RegisterActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        setupUIListeners()
        setupObservers()
    }

    private fun setupUIListeners() {
        binding.btnTogglePassword.setOnClickListener {
            if (isPasswordVisible) {
                binding.editPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.btnTogglePassword.setImageResource(R.drawable.ic_eye)
                isPasswordVisible = false
            } else {
                binding.editPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.btnTogglePassword.setImageResource(R.drawable.ic_eye_off)
                isPasswordVisible = true
            }
            binding.editPassword.setSelection(binding.editPassword.text.length)
        }

        binding.btnDaftar.setOnClickListener {
            handleRegisterClick()
        }
    }

    private fun handleRegisterClick() {
        val targetBackgroundTint = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
        val targetTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
        val flashColorBackground = ContextCompat.getColor(this, R.color.white)
        val flashColorText = ContextCompat.getColor(this, R.color.black)

        binding.btnDaftar.backgroundTintList = ColorStateList.valueOf(flashColorBackground)
        binding.btnDaftar.setTextColor(flashColorText)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.btnDaftar.backgroundTintList = targetBackgroundTint
            binding.btnDaftar.setTextColor(targetTextColor)

            val nama = binding.editNamaLengkap.text.toString().trim()
            val username = binding.editUsername.text.toString().trim()
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()

            if (nama.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.all_fields_required_error), Toast.LENGTH_SHORT).show()
                return@postDelayed
            }
            authViewModel.register(RegisterRequest(nama, username, email, password))

        }, 150)
    }

    private fun setupObservers() {
        authViewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        authViewModel.message.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }

        authViewModel.registerSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                showVerificationDialog()
            }
        }
    }

    private fun showVerificationDialog() {
        val dialog = VerificationSentDialogFragment.newInstance(
            getString(R.string.verification_email_sent_toast),
            "Registrasi Berhasil!"
        )

        dialog.onOkClickListener = {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        dialog.show(supportFragmentManager, "VerifikasiDialog")
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnDaftar.isEnabled = false
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnDaftar.isEnabled = true
        }
    }
}