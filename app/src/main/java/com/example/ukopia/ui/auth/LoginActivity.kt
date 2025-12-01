package com.example.ukopia.ui.auth

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.SessionManager
import com.example.ukopia.databinding.ActivityLoginBinding
import com.example.ukopia.models.LoginRequest
import com.example.ukopia.ui.akun.LocaleHelper // Pastikan file ini ada, jika tidak bisa dihapus

class LoginActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private lateinit var binding: ActivityLoginBinding
    private lateinit var authViewModel: AuthViewModel

    // Konfigurasi Bahasa (Opsional, biarkan jika sudah ada file LocaleHelper)
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { LocaleHelper.onAttach(it) } ?: newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Cek apakah user sudah login?
        if (SessionManager.isLoggedIn(this)) {
            goToMainActivity()
            return
        }

        // 2. Inisialisasi ViewModel
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupUIListeners()
        setupObservers()
    }

    private fun setupUIListeners() {
        // Toggle Visibility Password
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
            // Kembalikan kursor ke akhir teks
            binding.editPassword.setSelection(binding.editPassword.text.length)
        }

        // Tombol Masuk
        binding.btnMasuk.setOnClickListener {
            handleLoginClick()
        }

        // Tombol Lupa Password
        binding.btnLupaPassword.setOnClickListener {
            val intent = Intent(this, LupaPasswordActivity::class.java).apply {
                putExtra(LupaPasswordActivity.EXTRA_SOURCE, LupaPasswordActivity.SOURCE_LOGIN)
            }
            startActivity(intent)
        }

        // Teks "Daftar di sini"
        setupClickableRegisterText()
    }

    private fun handleLoginClick() {
        // Animasi Tombol (Visual Feedback)
        val targetBackgroundTint = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
        val targetTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
        val flashColorBackground = ContextCompat.getColor(this, R.color.white)
        val flashColorText = ContextCompat.getColor(this, R.color.black)

        binding.btnMasuk.backgroundTintList = ColorStateList.valueOf(flashColorBackground)
        binding.btnMasuk.setTextColor(flashColorText)

        Handler(Looper.getMainLooper()).postDelayed({
            // Kembalikan warna tombol
            binding.btnMasuk.backgroundTintList = targetBackgroundTint
            binding.btnMasuk.setTextColor(targetTextColor)

            // Ambil Input
            val identifier = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()

            if (identifier.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.empty_email_password_error), Toast.LENGTH_SHORT).show()
                return@postDelayed
            }

            // Panggil ViewModel untuk Login
            authViewModel.login(LoginRequest(identifier, password))

        }, 150)
    }

    private fun setupObservers() {
        // Observer: Loading State
        authViewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        // Observer: Pesan Error / Info
        authViewModel.message.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }

        // Observer: Login Sukses (Data User)
        authViewModel.loginSuccess.observe(this) { loginResponse ->
            if (loginResponse != null && loginResponse.data != null) {
                val user = loginResponse.data

                // Simpan Sesi ke SharedPreferences
                SessionManager.setLoggedIn(this, true)
                // Simpan UID, Nama, dan Email (Penting untuk fitur lain seperti Resep)
                SessionManager.saveUserData(this, user.uid, user.nama, user.email)

                Toast.makeText(this, getString(R.string.login_success_toast), Toast.LENGTH_SHORT).show()
                goToMainActivity()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loginProgressBar.visibility = View.VISIBLE
            binding.btnMasuk.isEnabled = false
            binding.editEmail.isEnabled = false
            binding.editPassword.isEnabled = false
        } else {
            binding.loginProgressBar.visibility = View.GONE
            binding.btnMasuk.isEnabled = true
            binding.editEmail.isEnabled = true
            binding.editPassword.isEnabled = true
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        // Clear backstack agar user tidak bisa kembali ke login dengan tombol back
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupClickableRegisterText() {
        val prefixText = getString(R.string.no_account_prefix) // "Belum punya akun? "
        val clickablePartText = getString(R.string.create_account_clickable_part) // "Daftar di sini"
        val fullText = prefixText + clickablePartText
        val spannableString = SpannableString(fullText)
        val start = fullText.indexOf(clickablePartText)
        val end = start + clickablePartText.length

        if (start != -1) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                    startActivity(intent)
                }
                override fun updateDrawState(ds: android.text.TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                    ds.color = ContextCompat.getColor(this@LoginActivity, R.color.blue) // Pastikan warna blue ada di colors.xml
                    ds.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
            }
            spannableString.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.txtBuatAkun.text = spannableString
        binding.txtBuatAkun.movementMethod = LinkMovementMethod.getInstance()
        binding.txtBuatAkun.highlightColor = android.graphics.Color.TRANSPARENT
    }
}