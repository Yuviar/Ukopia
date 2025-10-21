package com.example.ukopia

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private lateinit var btnDaftar: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val editNama = findViewById<EditText>(R.id.editNama)
        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editPassword = findViewById<EditText>(R.id.editPassword)
        val togglePassword = findViewById<ImageView>(R.id.btnTogglePassword)
        progressBar = findViewById(R.id.progressBar)
        btnDaftar = findViewById(R.id.btnDaftar)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

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
            // --- Animasi Flash Putih ---
            // Definisikan warna yang diinginkan setelah flash: latar belakang hitam, teks putih
            val targetBackgroundTint = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
            val targetTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))

            // Definisikan warna flash sementara: latar belakang putih, teks hitam
            val flashColorBackground = ContextCompat.getColor(this, R.color.white)
            val flashColorText = ContextCompat.getColor(this, R.color.black)

            btnDaftar.backgroundTintList = ColorStateList.valueOf(flashColorBackground)
            btnDaftar.setTextColor(flashColorText)

            Handler(Looper.getMainLooper()).postDelayed({
                // Kembalikan ke warna target (hitam background, putih teks)
                btnDaftar.backgroundTintList = targetBackgroundTint
                btnDaftar.setTextColor(targetTextColor)

                // --- Logika Asli Klik ---
                val nama = editNama.text.toString().trim()
                val email = editEmail.text.toString().trim()
                val password = editPassword.text.toString().trim()

                if (nama.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, getString(R.string.all_fields_required_error), Toast.LENGTH_SHORT).show()
                    return@postDelayed // Gunakan return@postDelayed untuk keluar dari lambda
                }
                showLoading(true)
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val firebaseUser = auth.currentUser
                            val userMap = hashMapOf(
                                getString(R.string.firestore_field_name) to nama,
                                getString(R.string.firestore_field_email) to email
                            )
                            firebaseUser?.let { user ->
                                db.collection(getString(R.string.firestore_users_collection)).document(user.uid)
                                    .set(userMap)
                                    .addOnSuccessListener {
                                        showLoading(false)
                                        Toast.makeText(
                                            this@RegisterActivity,
                                            getString(R.string.registration_success_message),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val intent =
                                            Intent(this@RegisterActivity, LoginActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        showLoading(false)
                                        Toast.makeText(
                                            this@RegisterActivity,
                                            getString(R.string.failed_save_data_error) + e.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        } else {
                            showLoading(false)
                            Toast.makeText(
                                this,
                                getString(R.string.registration_failed_error) + task.exception?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                // --- Akhir Logika Asli Klik ---
            }, 150) // Durasi flash: 150 milidetik
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            btnDaftar.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            btnDaftar.isEnabled = true
        }
    }
}