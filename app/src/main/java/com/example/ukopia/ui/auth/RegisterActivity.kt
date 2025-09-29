package com.example.ukopia

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
            val nama = editNama.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()

            if (nama.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showLoading(true)
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        val userMap = hashMapOf(
                            "nama" to nama,
                            "email" to email
                        )
                        firebaseUser?.let { user ->
                            db.collection("users").document(user.uid)
                                .set(userMap)
                                .addOnSuccessListener {
                                    // Sembunyikan loading
                                    showLoading(false)
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "Pendaftaran berhasil, silakan login",
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
                                        "Gagal menyimpan data: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    } else {
                        showLoading(false)
                        Toast.makeText(
                            this,
                            "Pendaftaran gagal: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        btnDaftar.scaleX = 1f
        btnDaftar.scaleY = 1f
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