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
import androidx.lifecycle.observe
import com.example.ukopia.R
import com.example.ukopia.databinding.ActivityRegisterBinding // TAMBAH: Import ViewBinding
import com.example.ukopia.models.RegisterRequest

class RegisterActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private lateinit var binding: ActivityRegisterBinding // TAMBAH: ViewBinding
    private lateinit var authViewModel: AuthViewModel // TAMBAH: ViewModel

    // HAPUS: Variabel lama
    // private lateinit var btnDaftar: Button
    // private lateinit var auth: FirebaseAuth
    // private lateinit var db: FirebaseFirestore
    // private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TAMBAH: Setup ViewBinding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TAMBAH: Inisialisasi ViewModel
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
        // --- Animasi Flash Putih ---
        val targetBackgroundTint = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
        val targetTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
        val flashColorBackground = ContextCompat.getColor(this, R.color.white)
        val flashColorText = ContextCompat.getColor(this, R.color.black)

        binding.btnDaftar.backgroundTintList = ColorStateList.valueOf(flashColorBackground)
        binding.btnDaftar.setTextColor(flashColorText)

        Handler(Looper.getMainLooper()).postDelayed({
            // Kembalikan ke warna target
            binding.btnDaftar.backgroundTintList = targetBackgroundTint
            binding.btnDaftar.setTextColor(targetTextColor)

            // --- Logika Asli Klik ---
            val nama = binding.editNama.text.toString().trim()
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()

            if (nama.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.all_fields_required_error), Toast.LENGTH_SHORT).show()
                return@postDelayed
            }

            // Panggil ViewModel untuk register
            authViewModel.register(RegisterRequest(nama, email, password))

        }, 150)
    }

    private fun setupObservers() {
        authViewModel.message.observe(this) { message ->
            if (message.isNullOrEmpty()) return@observe

            Toast.makeText(this, message, Toast.LENGTH_LONG).show()

            // Jika registrasi berhasil, kembali ke halaman Login
            if (message.contains("berhasil", ignoreCase = true)) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        authViewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }
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