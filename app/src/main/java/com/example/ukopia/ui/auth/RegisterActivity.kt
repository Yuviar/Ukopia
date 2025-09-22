package com.example.ukopia

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private lateinit var btnDaftar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val editPassword = findViewById<EditText>(R.id.editPassword)
        val togglePassword = findViewById<ImageView>(R.id.btnTogglePassword)
        btnDaftar = findViewById(R.id.btnDaftar)

        togglePassword.setOnClickListener {
            if (isPasswordVisible) {
                editPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                togglePassword.setImageResource(R.drawable.ic_eye)
                isPasswordVisible = false
            } else {
                editPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                togglePassword.setImageResource(R.drawable.ic_eye_off)
                isPasswordVisible = true
            }
            editPassword.setSelection(editPassword.text.length)
        }

        btnDaftar.setOnClickListener {
            btnDaftar.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
//                overridePendingTransition(R.anim.scale_in, R.anim.scale_out)
                btnDaftar.scaleX = 1f
                btnDaftar.scaleY = 1f
                // Perbaikan: Pindahkan finish() ke dalam blok withEndAction
                finish()
            }.start()
        }
    }
    override fun onResume(){
        super.onResume()
        btnDaftar.scaleX = 1f
        btnDaftar.scaleY = 1f
    }
}