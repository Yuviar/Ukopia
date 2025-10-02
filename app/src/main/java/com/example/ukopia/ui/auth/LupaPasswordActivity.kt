package com.example.ukopia

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LupaPasswordActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private lateinit var btnSelesai: Button
    private var source: String? = null // Variabel untuk menyimpan sumber panggilan

    // ▼▼▼ TAMBAHKAN COMPANION OBJECT INI ▼▼▼
    companion object {
        const val EXTRA_SOURCE = "EXTRA_SOURCE"
        const val SOURCE_LOGIN = "SOURCE_LOGIN"
        const val SOURCE_AKUN = "SOURCE_AKUN"
    }
    // ▲▲▲ AKHIR BAGIAN TAMBAHAN ▲▲▲

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lupa_password)

        // ▼▼▼ TAMBAHKAN BARIS INI UNTUK MENANGKAP SUMBER PANGGILAN ▼▼▼
        source = intent.getStringExtra(EXTRA_SOURCE)
        // ▲▲▲ AKHIR BAGIAN TAMBAHAN ▲▲▲

        val editPasswordBaru = findViewById<EditText>(R.id.editPasswordBaru)
        val togglePasswordBaru = findViewById<ImageView>(R.id.btnTogglePasswordBaru)
        btnSelesai = findViewById(R.id.btnSelesai)

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

        // ▼▼▼ MODIFIKASI ONCLICKLISTENER INI ▼▼▼
        btnSelesai.setOnClickListener {
            btnSelesai.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction {
                Toast.makeText(this, getString(R.string.password_change_success), Toast.LENGTH_SHORT).show()
                // Tentukan tujuan navigasi berdasarkan sumber
                val destinationIntent = when (source) {
                    SOURCE_AKUN -> {
                        // Jika dari AkunFragment, kembali ke MainActivity
                        Intent(this, MainActivity::class.java).apply {
                            // Flag ini penting untuk membawa MainActivity yang sudah ada ke depan
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                    }
                    else -> {
                        // Default: jika dari LoginActivity atau sumber tidak diketahui, kembali ke LoginActivity
                        Intent(this, LoginActivity::class.java)
                    }
                }

                startActivity(destinationIntent)

                // Reset animasi tombol
                btnSelesai.scaleX = 1f
                btnSelesai.scaleY = 1f
                finish() // Tutup activity ini
            }.start()
        }
        // ▲▲▲ AKHIR MODIFIKASI ▲▲▲
    }

    override fun onResume() {
        super.onResume()
        btnSelesai.scaleX = 1f
        btnSelesai.scaleY = 1f
    }
}