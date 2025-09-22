package com.example.ukopia

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.ukopia.ui.akun.AkunFragment
import com.example.ukopia.ui.auth.LoginActivity
import com.example.ukopia.ui.home.HomeFragment
import com.example.ukopia.ui.resep.ResepFragment
import com.example.ukopia.ui.royalti.RoyaltiFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

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
        // Session control
        fabAddData = findViewById(R.id.fab_add_data)
        fabAddData.setOnClickListener {
            if (SessionManager.SessionManager.isLoggedIn(this)) {
                Toast.makeText(this, "Tambah Data", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Anda Belum Login", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        if (savedInstanceState == null) {
            addFragment(HomeFragment())
            bottomNavigationView.selectedItemId = R.id.itemHome
            fabAddData.hide()
        }
    }

    private fun addFragment (fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment, fragment.javaClass.simpleName)
            .commit()
    }
    private val menuItemSelected = BottomNavigationView.OnNavigationItemSelectedListener { it ->
        when (it.itemId) {
            R.id.itemHome -> {
                addFragment(HomeFragment())
                fabAddData.hide()
                return@OnNavigationItemSelectedListener true
            }
            R.id.itemRoyalti -> {
                fabAddData.show()
                addFragment(RoyaltiFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.itemResep -> {
                addFragment(ResepFragment())
                fabAddData.hide()
                return@OnNavigationItemSelectedListener true
            }
            R.id.itemAkun -> {
                addFragment(AkunFragment())
                fabAddData.hide()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
}