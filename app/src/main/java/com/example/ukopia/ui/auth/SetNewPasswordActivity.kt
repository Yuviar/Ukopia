package com.example.ukopia.ui.auth

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.ukopia.R

class SetNewPasswordActivity : AppCompatActivity() {
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private lateinit var btnGanti: Button // Changed name to reflect new function

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_new_password) // Use the new layout

        val editNewPassword = findViewById<EditText>(R.id.editNewPassword)
        val toggleNewPassword = findViewById<ImageView>(R.id.btnToggleNewPassword)
        val editConfirmPassword = findViewById<EditText>(R.id.editConfirmPassword)
        val toggleConfirmPassword = findViewById<ImageView>(R.id.btnToggleConfirmPassword)
        btnGanti = findViewById(R.id.btnGanti)

        // Input validation for UI elements
        if (editNewPassword == null || toggleNewPassword == null ||
            editConfirmPassword == null || toggleConfirmPassword == null ||
            btnGanti == null) {
            Toast.makeText(this, "Error: UI elements not found in SetNewPasswordActivity", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Toggle visibility for New Password field
        toggleNewPassword.setOnClickListener {
            if (isNewPasswordVisible) {
                editNewPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                toggleNewPassword.setImageResource(R.drawable.ic_eye)
                isNewPasswordVisible = false
            } else {
                editNewPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                toggleNewPassword.setImageResource(R.drawable.ic_eye_off)
                isNewPasswordVisible = true
            }
            editNewPassword.setSelection(editNewPassword.text.length)
        }

        // Toggle visibility for Confirm New Password field
        toggleConfirmPassword.setOnClickListener {
            if (isConfirmPasswordVisible) {
                editConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                toggleConfirmPassword.setImageResource(R.drawable.ic_eye)
                isConfirmPasswordVisible = false
            } else {
                editConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                toggleConfirmPassword.setImageResource(R.drawable.ic_eye_off)
                isConfirmPasswordVisible = true
            }
            editConfirmPassword.setSelection(editConfirmPassword.text.length)
        }

        btnGanti.setOnClickListener {
            val newPassword = editNewPassword.text.toString().trim()
            val confirmPassword = editConfirmPassword.text.toString().trim()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, getString(R.string.all_fields_required_error), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                editConfirmPassword.error = getString(R.string.password_mismatch_error) // New string needed
                Toast.makeText(this, getString(R.string.password_mismatch_error), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- Animasi Flash Putih ---
            val targetBackgroundTint = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
            val targetTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
            val flashColorBackground = ContextCompat.getColor(this, R.color.white)
            val flashColorText = ContextCompat.getColor(this, R.color.black)

            btnGanti.backgroundTintList = ColorStateList.valueOf(flashColorBackground)
            btnGanti.setTextColor(flashColorText)

            Handler(Looper.getMainLooper()).postDelayed({
                btnGanti.backgroundTintList = targetBackgroundTint
                btnGanti.setTextColor(targetTextColor)

                // Simulate password change success
                Toast.makeText(this, getString(R.string.password_change_success), Toast.LENGTH_SHORT).show()

                // Navigate to LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish() // Finish SetNewPasswordActivity
            }, 150) // Flash duration
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure button color is correct if activity is resumed
        btnGanti.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
        btnGanti.setTextColor(ContextCompat.getColor(this, R.color.white))
    }
}