package com.example.ukopia.ui.auth

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.ukopia.R

class LupaPasswordActivity : AppCompatActivity() {
    private lateinit var btnVerifikasiAkun: Button

    companion object {
        const val EXTRA_SOURCE = "EXTRA_SOURCE"
        const val SOURCE_LOGIN = "SOURCE_LOGIN"
        const val SOURCE_ACCOUNT = "SOURCE_ACCOUNT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lupa_password)

        val editEmail = findViewById<EditText>(R.id.editEmail)
        btnVerifikasiAkun = findViewById(R.id.btnVerifikasiAkun)

        // Verifikasi apakah elemen UI ditemukan
        if (editEmail == null) {
            Log.e("LupaPasswordActivity", "EditText with ID R.id.editEmail not found!")
            Toast.makeText(this, "Error: editEmail not found", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        if (btnVerifikasiAkun == null) {
            Log.e("LupaPasswordActivity", "Button with ID R.id.btnVerifikasiAkun not found!")
            Toast.makeText(this, "Error: btnVerifikasiAkun not found", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Logic for "Verifikasi Akun" button
        btnVerifikasiAkun.setOnClickListener {
            val email = editEmail.text.toString().trim()
            if (email.isEmpty()) {
                editEmail.error = getString(R.string.error_email_empty)
                Toast.makeText(this, getString(R.string.error_email_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- Animasi Flash Putih ---
            val targetBackgroundTint = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
            val targetTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
            val flashColorBackground = ContextCompat.getColor(this, R.color.white)
            val flashColorText = ContextCompat.getColor(this, R.color.black)

            btnVerifikasiAkun.backgroundTintList = ColorStateList.valueOf(flashColorBackground)
            btnVerifikasiAkun.setTextColor(flashColorText)

            Handler(Looper.getMainLooper()).postDelayed({
                btnVerifikasiAkun.backgroundTintList = targetBackgroundTint
                btnVerifikasiAkun.setTextColor(targetTextColor)

                // MODIFIED: Menampilkan dialog alih-alih Toast
                val dialog = VerificationSentDialogFragment.newInstance(
                    getString(R.string.verification_email_sent_toast),
                    getString(R.string.verification_dialog_title)
                )
                dialog.show(supportFragmentManager, "VerificationSentDialog")

                // Tetap di halaman LupaPasswordActivity
            }, 150) // Durasi flash: 150 milidetik
        }
    }

    override fun onResume() {
        super.onResume()
        // Memastikan warna tombol benar jika aktivitas dilanjutkan (misalnya, dari app switcher)
        // Ini perlu dipanggil di onResume jika tombol mungkin diatur ulang warnanya saat aktivitas di-pause/resume
        // Namun, jika Anda ingin mempertahankan status flash setelah resume (yang tidak umum), logika ini perlu diubah.
        // Untuk sekarang, kita kembalikan ke warna default saat onResume.
        btnVerifikasiAkun.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
        btnVerifikasiAkun.setTextColor(ContextCompat.getColor(this, R.color.white))
    }
}