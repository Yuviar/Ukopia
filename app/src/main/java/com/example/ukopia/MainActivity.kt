package com.example.ukopia

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.ukopia.ui.akun.AkunFragment
import com.example.ukopia.ui.peralatan.PeralatanFragment
import com.example.ukopia.ui.home.HomeFragment
import com.example.ukopia.ui.recipe.RecipeFragment
import com.example.ukopia.ui.loyalty.LoyaltyFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.ukopia.ui.loyalty.AddLoyaltyFragment

import com.example.ukopia.SessionManager
import android.view.View // <<--- TAMBAHKAN INI

class MainActivity : AppCompatActivity(), AkunFragment.OnAkunFragmentInteractionListener {

    private lateinit var bottomNavigationView: BottomNavigationView // <<-- Ubah ini menjadi lateinit var

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation_view) // <<-- Inisialisasi di sini
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItemSelected)

        if (savedInstanceState == null) {
            addFragment(HomeFragment(), false)
            bottomNavigationView.selectedItemId = R.id.itemHome
        }
    }

    private fun addFragment(fragment: Fragment, addToBackStack: Boolean) {
        val transaction = supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        } else {
            // Ini akan menghapus semua fragment dari back stack saat beralih ke tab utama
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        transaction
            .replace(R.id.container, fragment, fragment.javaClass.simpleName)
            .addToBackStack(null) // Tambahkan fragment ini ke back stack juga
            .commit()
    }

    private val menuItemSelected = BottomNavigationView.OnNavigationItemSelectedListener { it ->
        when (it.itemId) {
            R.id.itemHome -> {
                addFragment(HomeFragment(), false)
                return@OnNavigationItemSelectedListener true
            }
            R.id.itemRoyalti -> {
                addFragment(LoyaltyFragment(), false)
                return@OnNavigationItemSelectedListener true
            }
            R.id.itemResep -> {
                addFragment(RecipeFragment(), false)
                return@OnNavigationItemSelectedListener true
            }
            R.id.itemAkun -> {
                addFragment(AkunFragment(), false)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun OnPeralatanClicked() {
        addFragment(PeralatanFragment(),true)
    }

    // ▼▼▼ FUNGSI BARU UNTUK MENGONTROL VISIBILITAS NAV BAR ▼▼▼
    fun setBottomNavVisibility(visibility: Int) {
        bottomNavigationView.visibility = visibility
    }
    // ▲▲▲ AKHIR FUNGSI BARU ▲▲▲
}
