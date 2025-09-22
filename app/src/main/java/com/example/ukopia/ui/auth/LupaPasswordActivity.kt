package com.example.ukopia

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class LupaPasswordActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private lateinit var btnSelesai: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lupa_password)

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

        btnSelesai.setOnClickListener {
            btnSelesai.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
//                overridePendingTransition(R.anim.scale_in, R.anim.scale_out)
                btnSelesai.scaleX = 1f
                btnSelesai.scaleY = 1f
                finish()
            }.start()
        }
    }

    override fun onResume() {
        super.onResume()
        btnSelesai.scaleX = 1f
        btnSelesai.scaleY = 1f
    }
}
