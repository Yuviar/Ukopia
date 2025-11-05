package com.example.ukopia.ui.auth

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.ukopia.ui.auth.LoginActivity
import com.example.ukopia.MainActivity
import com.example.ukopia.R

class LupaPasswordActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private lateinit var btnSelesai: Button
    private var source: String? = null

    companion object {
        const val EXTRA_SOURCE = "EXTRA_SOURCE"
        const val SOURCE_LOGIN = "SOURCE_LOGIN"
        const val SOURCE_ACCOUNT = "SOURCE_ACCOUNT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lupa_password)

        source = intent.getStringExtra(EXTRA_SOURCE)
        Log.d("LupaPasswordActivity", "Activity launched with source: $source")

        val editPasswordBaru = findViewById<EditText>(R.id.editPasswordBaru)
        val togglePasswordBaru = findViewById<ImageView>(R.id.btnTogglePasswordBaru)
        btnSelesai = findViewById(R.id.btnSelesai)

        // Verifikasi apakah element UI ditemukan
        if (editPasswordBaru == null) {
            Log.e("LupaPasswordActivity", "EditText with ID R.id.editPasswordBaru not found!")
            Toast.makeText(this, "Error: editPasswordBaru not found", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        if (togglePasswordBaru == null) {
            Log.e("LupaPasswordActivity", "ImageView with ID R.id.btnTogglePasswordBaru not found!")
            Toast.makeText(this, "Error: btnTogglePasswordBaru not found", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        if (btnSelesai == null) {
            Log.e("LupaPasswordActivity", "Button with ID R.id.btnSelesai not found!")
            Toast.makeText(this, "Error: btnSelesai not found", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        togglePasswordBaru.setOnClickListener {
            if (isPasswordVisible) {
                editPasswordBaru.transformationMethod = PasswordTransformationMethod.getInstance()
                togglePasswordBaru.setImageResource(R.drawable.ic_eye)
                isPasswordVisible = false
            } else {
                editPasswordBaru.transformationMethod = HideReturnsTransformationMethod.getInstance()
                togglePasswordBaru.setImageResource(R.drawable.ic_eye_off)
                isPasswordVisible = true
            }
            editPasswordBaru.setSelection(editPasswordBaru.text.length)
        }

        btnSelesai.setOnClickListener {
            // --- Animasi Flash Putih ---
            // Definisikan warna yang diinginkan setelah flash: latar belakang hitam, teks putih
            val targetBackgroundTint = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
            val targetTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))

            // Definisikan warna flash sementara: latar belakang putih, teks hitam
            val flashColorBackground = ContextCompat.getColor(this, R.color.white)
            val flashColorText = ContextCompat.getColor(this, R.color.black)

            btnSelesai.backgroundTintList = ColorStateList.valueOf(flashColorBackground)
            btnSelesai.setTextColor(flashColorText)

            Handler(Looper.getMainLooper()).postDelayed({
                // Kembalikan ke warna target (hitam background, putih teks)
                btnSelesai.backgroundTintList = targetBackgroundTint
                btnSelesai.setTextColor(targetTextColor)

                // --- Logika Asli Klik ---
                Toast.makeText(this, getString(R.string.password_change_success), Toast.LENGTH_SHORT).show()

                val destinationIntent = when (source) {
                    SOURCE_ACCOUNT -> {
                        Log.d("LupaPasswordActivity", "Navigating to MainActivity (SOURCE_ACCOUNT)")
                        Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                    }
                    else -> {
                        Log.d("LupaPasswordActivity", "Navigating to LoginActivity (default/SOURCE_LOGIN)")
                        Intent(this, LoginActivity::class.java)
                    }
                }
                startActivity(destinationIntent)
                finish()
                // --- Akhir Logika Asli Klik ---
            }, 150) // Durasi flash: 150 milidetik
        }
    }

    override fun onResume() {
        super.onResume()
    }
}