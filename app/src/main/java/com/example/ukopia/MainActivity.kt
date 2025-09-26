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
import com.example.ukopia.ui.home.HomeFragment
import com.example.ukopia.ui.recipe.RecipeFragment
import com.example.ukopia.ui.loyalty.LoyaltyFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.ukopia.ui.loyalty.AddLoyaltyFragment

import com.example.ukopia.SessionManager

class MainActivity : AppCompatActivity() {
    private lateinit var fabAddData: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItemSelected)
        fabAddData = findViewById(R.id.fab_add_data)

        fabAddData.setOnClickListener {
            if (SessionManager.SessionManager.isLoggedIn(this)) {
                addFragment(AddLoyaltyFragment(), true)
            } else {
                Toast.makeText(this, "Anda Belum Login", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.container)
            if (currentFragment is LoyaltyFragment) {
                fabAddData.show()
            } else {
                fabAddData.hide()
            }
        }

        if (savedInstanceState == null) {
            addFragment(HomeFragment(), false)
            bottomNavigationView.selectedItemId = R.id.itemHome
            fabAddData.hide()
        }
    }

    private fun addFragment(fragment: Fragment, addToBackStack: Boolean) {
        val transaction = supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        } else {
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        transaction.commit()
    }

    private val menuItemSelected = BottomNavigationView.OnNavigationItemSelectedListener { it ->
        when (it.itemId) {
            R.id.itemHome -> {
                fabAddData.hide()
                addFragment(HomeFragment(), false)
                return@OnNavigationItemSelectedListener true
            }
            R.id.itemRoyalti -> {
                fabAddData.show()
                addFragment(LoyaltyFragment(), false)
                return@OnNavigationItemSelectedListener true
            }
            R.id.itemResep -> {
                fabAddData.hide()
                addFragment(RecipeFragment(), false)
                return@OnNavigationItemSelectedListener true
            }
            R.id.itemAkun -> {
                fabAddData.hide()
                addFragment(AkunFragment(), false)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
}
