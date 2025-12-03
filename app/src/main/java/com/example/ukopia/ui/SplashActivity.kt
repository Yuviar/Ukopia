package com.example.ukopia

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.ukopia.ui.akun.LocaleHelper
import com.google.android.material.snackbar.Snackbar

class SplashActivity : AppCompatActivity() {

    private val initialSplashDelay: Long = 1000
    private val internetCheckInterval: Long = 3000

    private val handler = Handler(Looper.getMainLooper())
    private var noInternetSnackbar: Snackbar? = null

    private val checkInternetAndNavigate = object : Runnable {
        override fun run() {
            if (hasInternetConnection()) {
                noInternetSnackbar?.dismiss()
                navigateToMain()
            } else {
                showNoInternetWarning()
                handler.postDelayed(this, internetCheckInterval)
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { LocaleHelper.onAttach(it) } ?: newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        handler.postDelayed({
            checkInternetAndNavigate.run()
        }, initialSplashDelay)
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }

    private fun showNoInternetWarning() {
        val rootView: View = findViewById(android.R.id.content)

        if (noInternetSnackbar == null || !noInternetSnackbar!!.isShownOrQueued) {
            noInternetSnackbar = Snackbar.make(
                rootView,
                getString(R.string.no_internet_warning),
                Snackbar.LENGTH_INDEFINITE
            )
            noInternetSnackbar?.show()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkInternetAndNavigate)
        noInternetSnackbar?.dismiss()
    }
}