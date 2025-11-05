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
import androidx.lifecycle.observe
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.SessionManager
import com.example.ukopia.databinding.ActivityLoginBinding
import com.example.ukopia.models.LoginRequest
import com.example.ukopia.ui.akun.LocaleHelper

class LoginActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private lateinit var binding: ActivityLoginBinding
    private lateinit var authViewModel: AuthViewModel // TAMBAH: ViewModel

    // Hapus: Variabel Firebase tidak lagi digunakan
    // private lateinit var btnMasuk: Button
    // private lateinit var auth: FirebaseAuth
    // private lateinit var progressBar: ProgressBar

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { LocaleHelper.onAttach(it) } ?: newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TAMBAH: Inisialisasi ViewModel
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        // Hapus: Inisialisasi Firebase
        // auth = FirebaseAuth.getInstance()

        setupUIListeners()
        setupObservers() // TAMBAH: Panggil method untuk mengamati ViewModel

        if (SessionManager.isLoggedIn(this)) {
            goToMainActivity()
            return
        }
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

        binding.btnMasuk.setOnClickListener {
            handleLoginClick()
        }

        binding.btnLupaPassword.setOnClickListener {
            val intent = Intent(this, LupaPasswordActivity::class.java).apply {
                putExtra(LupaPasswordActivity.EXTRA_SOURCE, LupaPasswordActivity.SOURCE_LOGIN)
            }
            startActivity(intent)
        }

        setupClickableRegisterText()
    }

    private fun handleLoginClick() {
        // --- Animasi Flash Putih ---
        val targetBackgroundTint = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
        val targetTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
        val flashColorBackground = ContextCompat.getColor(this, R.color.white)
        val flashColorText = ContextCompat.getColor(this, R.color.black)

        binding.btnMasuk.backgroundTintList = ColorStateList.valueOf(flashColorBackground)
        binding.btnMasuk.setTextColor(flashColorText)

        // TAMBAHKAN LOG DI SINI UNTUK MEMASTIKAN FUNGSI DIPANGGIL
        Log.d("LoginDebug", "handleLoginClick: Button clicked, starting handler.")

        Handler(Looper.getMainLooper()).postDelayed({
            // Kembalikan ke warna target
            binding.btnMasuk.backgroundTintList = targetBackgroundTint
            binding.btnMasuk.setTextColor(targetTextColor)

            // --- Logika Login dengan ViewModel ---
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()

            // TAMBAHKAN LOG UNTUK MELIHAT NILAI VARIABEL
            Log.d("LoginDebug", "Checking credentials: Email='${email}', Password='${password}'")

            if (email.isEmpty() || password.isEmpty()) {
                // TAMBAHKAN LOG UNTUK MELIHAT APAKAH VALIDASI GAGAL
                Log.w("LoginDebug", "Validation failed: Fields are empty.")
                Toast.makeText(this, getString(R.string.empty_email_password_error), Toast.LENGTH_SHORT).show()
                return@postDelayed
            }

            // TAMBAHKAN LOG UNTUK MELIHAT APAKAH VIEWMODEL DIPANGGIL
            Log.d("LoginDebug", "Validation successful. Calling ViewModel to login...")
            // Panggil ViewModel untuk login
            authViewModel.login(LoginRequest(email, password))

        }, 150)
    }

    // TAMBAH: Method untuk mengamati LiveData dari ViewModel
    private fun setupObservers() {
        authViewModel.loginResult.observe(this) { loginResponse ->
            if (loginResponse != null && loginResponse.data != null) {
                // Login Berhasil
                val user = loginResponse.data
                SessionManager.setLoggedIn(this, true)
                SessionManager.saveUserData(this, user.nama, user.email) // Simpan data user

                Toast.makeText(this, getString(R.string.login_success_toast), Toast.LENGTH_SHORT).show()
                goToMainActivity()
            }
            // Penanganan error sudah di handle oleh observer `message`
        }

        authViewModel.message.observe(this) { message ->
            // Menampilkan pesan error atau pesan lainnya
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }

        authViewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loginProgressBar.visibility = View.VISIBLE
            binding.btnMasuk.isEnabled = false
        } else {
            binding.loginProgressBar.visibility = View.GONE
            binding.btnMasuk.isEnabled = true
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupClickableRegisterText() {
        val prefixText = getString(R.string.no_account_prefix)
        val clickablePartText = getString(R.string.create_account_clickable_part)
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
                    ds.color = ContextCompat.getColor(this@LoginActivity, R.color.blue)
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