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
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe
import com.example.ukopia.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private lateinit var btnMasuk: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var binding: ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editPassword = findViewById<EditText>(R.id.editPassword)
        val togglePassword = findViewById<ImageView>(R.id.btnTogglePassword)
        progressBar = findViewById(R.id.loginProgressBar)
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
        if (SessionManager.SessionManager.isLoggedIn(this)) {
            goToMainActivity()
            return
        }

        binding.btnMasuk.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password tidak boleh kosong!", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            showLoading(true)
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    showLoading(false)
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        firebaseUser?.let { user ->
                            FirebaseFirestore.getInstance().collection("users")
                                .document(user.uid).get()
                                .addOnSuccessListener { document ->
                                    if (document != null && document.exists()) {
                                        val name = document.getString("nama") ?: ""
                                        val emailResult = document.getString("email") ?: ""

                                        // Simpan data ke SessionManager
                                        SessionManager.SessionManager.setLoggedIn(this, true)
                                        SessionManager.SessionManager.saveUserData(this, name, emailResult)

                                        Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show()
                                        goToMainActivity()
                                    } else {
                                        // Kasus jika user ada di Auth tapi tidak ada di Firestore
                                        Toast.makeText(this, "Data pengguna tidak ditemukan.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Gagal mengambil data: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Login gagal: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

//            btnMasuk.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction {
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
////                overridePendingTransition(R.anim.scale_in, R.anim.scale_out)
//                btnMasuk.scaleX = 1f
//                btnMasuk.scaleY = 1f
//            }.start()
        }

        btnLupaPassword.setOnClickListener {
            val intent = Intent(this, LupaPasswordActivity::class.java).apply {
                // Kirim tanda bahwa panggilan berasal dari LoginActivity
                putExtra(LupaPasswordActivity.EXTRA_SOURCE, LupaPasswordActivity.SOURCE_LOGIN)
            }
            startActivity(intent)
        }

        val text = "Belum Punya Akun? Buat Akun Disini"
        val spannableString = SpannableString(text)
        val start = text.indexOf("Buat Akun Disini")
        val end = start + "Buat Akun Disini".length

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
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            btnMasuk.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            btnMasuk.isEnabled = true
        }
    }
    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

}