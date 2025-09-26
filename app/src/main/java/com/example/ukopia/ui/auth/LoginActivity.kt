package com.example.ukopia

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class LoginActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private lateinit var btnMasuk: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val editPassword = findViewById<EditText>(R.id.editPassword)
        val togglePassword = findViewById<ImageView>(R.id.btnTogglePassword)
        btnMasuk = findViewById(R.id.btnMasuk)
        val btnLupaPassword = findViewById<Button>(R.id.btnLupaPassword)
        val txtBuatAkun = findViewById<TextView>(R.id.txtBuatAkun)

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

        btnMasuk.setOnClickListener {
            btnMasuk.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction {
                SessionManager.SessionManager.setLoggedIn(this, true)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
//                overridePendingTransition(R.anim.scale_in, R.anim.scale_out)
                btnMasuk.scaleX = 1f
                btnMasuk.scaleY = 1f
            }.start()
            finish()
        }

        btnLupaPassword.setOnClickListener {
            val intent = Intent(this, LupaPasswordActivity::class.java)
            startActivity(intent)
//            overridePendingTransition(R.anim.scale_in, R.anim.scale_out)
        }

        val text = "Belum punya akun? Buat akun di sini"
        val spannableString = SpannableString(text)
        val start = text.indexOf("Buat akun di sini")
        val end = start + "Buat akun di sini".length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
//                overridePendingTransition(R.anim.scale_in, R.anim.scale_out)
            }

            override fun updateDrawState(ds: android.text.TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ContextCompat.getColor(this@LoginActivity, R.color.blue)
                ds.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        }

        spannableString.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        txtBuatAkun.text = spannableString
        txtBuatAkun.movementMethod = LinkMovementMethod.getInstance()
        txtBuatAkun.highlightColor = android.graphics.Color.TRANSPARENT
    }

    override fun onResume() {
        super.onResume()
        btnMasuk.scaleX = 1f
        btnMasuk.scaleY = 1f
    }
}