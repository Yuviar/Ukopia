package com.example.ukopia

import android.content.Context // Penting: Tambahkan import ini
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.ukopia.ui.home.HomeFragment // Ini tidak digunakan langsung di SplashActivity, tetapi biarkan saja sesuai konteks asli

class SplashActivity : AppCompatActivity() {

    // Tambahkan override ini untuk menerapkan lokal yang dipilih
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { LocaleHelper.onAttach(it) } ?: newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 1000)
    }
}