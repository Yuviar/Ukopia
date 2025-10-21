package com.example.ukopia

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.example.ukopia.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.ukopia.ui.auth.LupaPasswordActivity

class LoginActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private lateinit var btnMasuk: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var binding: ActivityLoginBinding

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { LocaleHelper.onAttach(it) } ?: newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val editEmail = binding.editEmail
        val editPassword = binding.editPassword
        val togglePassword = binding.btnTogglePassword
        progressBar = binding.loginProgressBar
        btnMasuk = binding.btnMasuk
        val btnLupaPassword = binding.btnLupaPassword
        val txtBuatAkun = binding.txtBuatAkun

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

        btnMasuk.setOnClickListener {
            // --- Animasi Flash Putih ---
            // Definisikan warna yang diinginkan setelah flash: latar belakang hitam, teks putih
            val targetBackgroundTint = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
            val targetTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))

            // Definisikan warna flash sementara: latar belakang putih, teks hitam
            val flashColorBackground = ContextCompat.getColor(this, R.color.white)
            val flashColorText = ContextCompat.getColor(this, R.color.black)

            btnMasuk.backgroundTintList = ColorStateList.valueOf(flashColorBackground)
            btnMasuk.setTextColor(flashColorText)

            Handler(Looper.getMainLooper()).postDelayed({
                // Kembalikan ke warna target (hitam background, putih teks)
                btnMasuk.backgroundTintList = targetBackgroundTint
                btnMasuk.setTextColor(targetTextColor)

                // --- Logika Asli Klik ---
                val email = editEmail.text.toString().trim()
                val password = editPassword.text.toString().trim()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, getString(R.string.empty_email_password_error), Toast.LENGTH_SHORT).show()
                    return@postDelayed // Gunakan return@postDelayed untuk keluar dari lambda
                }
                showLoading(true)
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        showLoading(false)
                        if (task.isSuccessful) {
                            val firebaseUser = auth.currentUser
                            firebaseUser?.let { user ->
                                FirebaseFirestore.getInstance().collection(getString(R.string.firestore_users_collection))
                                    .document(user.uid).get()
                                    .addOnSuccessListener { document ->
                                        if (document != null && document.exists()) {
                                            val name = document.getString(getString(R.string.firestore_field_name)) ?: ""
                                            val emailResult = document.getString(getString(R.string.firestore_field_email)) ?: ""

                                            SessionManager.SessionManager.setLoggedIn(this, true)
                                            SessionManager.SessionManager.saveUserData(this, name, emailResult)

                                            Toast.makeText(this, getString(R.string.login_success_toast), Toast.LENGTH_SHORT).show()
                                            goToMainActivity()
                                        } else {
                                            Toast.makeText(this, getString(R.string.user_not_found_error), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, getString(R.string.failed_retrieve_user_data_error) + e.message, Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.login_failed_error) + task.exception?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                // --- Akhir Logika Asli Klik ---
            }, 150) // Durasi flash: 150 milidetik
        }

        btnLupaPassword.setOnClickListener {
            val intent = Intent(this, LupaPasswordActivity::class.java).apply {
                putExtra(LupaPasswordActivity.EXTRA_SOURCE, LupaPasswordActivity.SOURCE_LOGIN)
            }
            startActivity(intent)
        }

        val prefixText = getString(R.string.no_account_prefix)
        val clickablePartText = getString(R.string.create_account_clickable_part)
        val fullText = prefixText + clickablePartText

        val spannableString = SpannableString(fullText)
        val start = fullText.indexOf(clickablePartText)
        val end = start + clickablePartText.length

        if (start != -1) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                    startActivity(intent)
                }

                override fun updateDrawState(ds: android.text.TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                    ds.color = ContextCompat.getColor(this@LoginActivity, R.color.blue)
                    ds.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
            }
            spannableString.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        txtBuatAkun.text = spannableString
        txtBuatAkun.movementMethod = LinkMovementMethod.getInstance()
        txtBuatAkun.highlightColor = android.graphics.Color.TRANSPARENT
    }

    override fun onResume() {
        super.onResume()
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