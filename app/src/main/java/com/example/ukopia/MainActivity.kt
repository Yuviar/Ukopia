package com.example.ukopia

import android.content.Context
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

import com.example.ukopia.SessionManager
import android.view.View
import androidx.activity.OnBackPressedCallback // Tambahkan ini lagi
import com.google.android.material.dialog.MaterialAlertDialogBuilder // Ini tidak digunakan, bisa dihapus jika tidak ada toast khusus

class MainActivity : AppCompatActivity(), AkunFragment.OnAkunFragmentInteractionListener {

    private lateinit var bottomNavigationView: BottomNavigationView
    private var backPressedTime: Long = 0 // Untuk double tap back to exit dari HomeFragment

    override fun attachBaseContext(newBase: Context?) {
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
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.container)

                if (supportFragmentManager.backStackEntryCount > 1 && currentFragment !is HomeFragment && currentFragment !is LoyaltyFragment && currentFragment !is RecipeFragment && currentFragment !is AkunFragment) {
                    supportFragmentManager.popBackStack()
                } else if (currentFragment !is HomeFragment) {
                    supportFragmentManager.popBackStack(HomeFragment::class.java.simpleName, 0) // popInclusive = 0 artinya tidak menghapus HomeFragment
                    bottomNavigationView.selectedItemId = R.id.itemHome // Update BottomNav UI
                } else {
                    finishAffinity()
                }
            }
        })
        // ▲▲▲ Akhir onBackPressedCallback ▲▲▲
    }

    // Metode baru untuk memuat fragment utama (tab root)
    private fun loadRootFragment(fragment: Fragment, itemId: Int) {
        val fragmentTag = fragment.javaClass.simpleName

        // Hapus semua fragment dari back stack saat beralih ke tab utama
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        // Ganti fragment dan tambahkan ke back stack.
        // Ini membuatnya menjadi entri dasar untuk tab tersebut.
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment, fragmentTag)
            .addToBackStack(fragmentTag) // Tambahkan ke back stack sebagai root untuk tab ini
            .commit()
    }

    // Metode untuk memuat fragment "detail" atau fragment non-tab root
    fun navigateToFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val fragmentTag = fragment.javaClass.simpleName
        val transaction = supportFragmentManager.beginTransaction()

        transaction.replace(R.id.container, fragment, fragmentTag)

        if (addToBackStack) {
            transaction.addToBackStack(fragmentTag) // Tambahkan ke back stack
        } else {
            // Jika tidak ingin ditambahkan ke back stack (jarang untuk non-root tab),
            // pastikan fragment yang ada di-pop jika namanya sama
            // Ini akan menggantikan fragment yang ada tanpa menambahkan ke stack.
            // Lebih aman menggunakan loadRootFragment untuk root tab.
            supportFragmentManager.popBackStack(fragmentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        transaction.commit()
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

    override fun OnPeralatanClicked() {
        navigateToFragment(PeralatanFragment(), true) // Gunakan navigateToFragment
    }

    // FUNGSI UNTUK MENGONTROL VISIBILITAS NAV BAR
    fun setBottomNavVisibility(visibility: Int) {
        bottomNavigationView.visibility = visibility
    }
}