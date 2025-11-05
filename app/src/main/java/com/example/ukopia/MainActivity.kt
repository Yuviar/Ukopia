package com.example.ukopia

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.ukopia.ui.akun.AkunFragment
import com.example.ukopia.ui.akun.LocaleHelper
import com.example.ukopia.ui.home.HomeFragment
import com.example.ukopia.ui.recipe.RecipeFragment
import com.example.ukopia.ui.loyalty.LoyaltyFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    // --- Hapus atau abaikan variabel ini jika tidak digunakan lagi untuk logika keluar ---
    // private var backPressedTime: Long = 0
    // private val BACK_PRESS_INTERVAL = 2000L

    // --- Variabel baru untuk melacak apakah aplikasi siap untuk keluar ---
    private var isReadyToExit = false

    override fun attachBaseContext(newBase: Context?) {
        Log.d("LocaleHelperDebug", "MainActivity attachBaseContext called. NewBase: $newBase")
        super.attachBaseContext(newBase?.let { LocaleHelper.onAttach(it) } ?: newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation_view)
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItemSelected)

        if (savedInstanceState == null) {
            loadRootFragment(HomeFragment(), R.id.itemHome)
            isReadyToExit = false // Pastikan flag direset saat aplikasi dimulai
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.container)

                if (supportFragmentManager.backStackEntryCount > 1 && currentFragment !is HomeFragment && currentFragment !is LoyaltyFragment && currentFragment !is RecipeFragment && currentFragment !is AkunFragment) {
                    // Kasus 1: Pop fragmen biasa dari back stack
                    supportFragmentManager.popBackStack()
                    isReadyToExit = false // Reset flag jika navigasi kembali dari sub-fragmen
                } else if (currentFragment !is HomeFragment) {
                    // Kasus 2: Pop kembali ke HomeFragment (dari root fragment bottom nav lainnya)
                    supportFragmentManager.popBackStack(HomeFragment::class.java.simpleName, 0)
                    bottomNavigationView.selectedItemId = R.id.itemHome
                    isReadyToExit = false // Reset flag jika beralih kembali ke HomeFragment dari tab lain
                } else {
                    // Kasus 3: Pengguna berada di HomeFragment (atau salah satu root fragment bottom nav lain yang aktif),
                    // dan tidak ada fragmen di atasnya yang bisa di-pop secara normal.
                    // Di sini kita menerapkan logika "dua kali tekan tombol kembali untuk keluar"
                    // TANPA BATAS WAKTU antar klik.
                    if (isReadyToExit) {
                        // Ini adalah penekanan tombol kembali ke-2. Keluar dari aplikasi.
                        finishAffinity()
                    } else {
                        // Ini adalah penekanan tombol kembali ke-1.
                        // Atur flag menjadi true, menunggu penekanan kedua.
                        isReadyToExit = true
                        // Tidak ada Toast atau notifikasi.
                    }
                }
            }
        })
    }

    private fun loadRootFragment(fragment: Fragment, itemId: Int) {
        val fragmentTag = fragment.javaClass.simpleName

        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment, fragmentTag)
            .addToBackStack(fragmentTag) // Menambahkan root fragment ke back stack
            .commit()
        isReadyToExit = false // Reset flag setiap kali memuat fragmen root baru (dari bottom nav)
    }

    fun navigateToFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val fragmentTag = fragment.javaClass.simpleName
        val transaction = supportFragmentManager.beginTransaction()

        transaction.replace(R.id.container, fragment, fragmentTag)

        if (addToBackStack) {
            transaction.addToBackStack(fragmentTag)
        } else {
            // Logika ini akan menghapus semua instance fragmen dengan tag yang sama dari back stack
            // hingga fragmen terakhir dengan tag tersebut (inklusif).
            supportFragmentManager.popBackStack(fragmentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        transaction.commit()
        isReadyToExit = false // Reset flag setiap kali menavigasi ke fragmen lain
    }

    private val menuItemSelected = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.itemHome -> {
                loadRootFragment(HomeFragment(), R.id.itemHome)
                return@OnNavigationItemSelectedListener true
            }
            R.id.itemLoyalty -> {
                loadRootFragment(LoyaltyFragment(), R.id.itemLoyalty)
                return@OnNavigationItemSelectedListener true
            }
            R.id.itemRecipe -> {
                loadRootFragment(RecipeFragment(), R.id.itemRecipe)
                return@OnNavigationItemSelectedListener true
            }
            R.id.itemAccount -> {
                loadRootFragment(AkunFragment(), R.id.itemAccount)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    fun setBottomNavVisibility(visibility: Int) {
        bottomNavigationView.visibility = visibility
    }
}