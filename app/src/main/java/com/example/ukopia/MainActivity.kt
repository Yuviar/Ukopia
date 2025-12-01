package com.example.ukopia

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.ukopia.databinding.ActivityMainBinding
import com.example.ukopia.databinding.DialogLoginRequiredBinding
import com.example.ukopia.ui.akun.AkunFragment
import com.example.ukopia.ui.auth.LoginActivity
import com.example.ukopia.ui.home.HomeFragment
import com.example.ukopia.ui.loyalty.LoyaltyFragment
import com.example.ukopia.ui.menu.MenuFragment
import com.example.ukopia.ui.recipe.RecipeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Launcher Login: Jika user login sukses, langsung arahkan ke Loyalty
    private val loyaltyLoginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (SessionManager.isLoggedIn(this)) {
            loadFragment(LoyaltyFragment())
            binding.bottomNavigationView.selectedItemId = R.id.itemLoyalty
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.itemHome -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.itemMenu -> {
                    loadFragment(MenuFragment())
                    true
                }
                // --- LOGIC: Cek Login sebelum buka Loyalty ---
                R.id.itemLoyalty -> {
                    if (SessionManager.isLoggedIn(this)) {
                        loadFragment(LoyaltyFragment())
                        true
                    } else {
                        showLoginRequiredDialog()
                        false // Batalkan perpindahan tab
                    }
                }
                R.id.itemRecipe -> {
                    loadFragment(RecipeFragment())
                    true
                }
                R.id.itemAccount -> {
                    loadFragment(AkunFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    // Menampilkan Dialog dari XML `dialog_login_required.xml`
    private fun showLoginRequiredDialog() {
        val dialogBinding = DialogLoginRequiredBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.buttonDialogLogin.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, LoginActivity::class.java)
            loyaltyLoginLauncher.launch(intent)
        }

        dialogBinding.buttonDialogCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Helper untuk menyembunyikan bottom nav (dipakai di EditLoyaltyFragment)
    fun setBottomNavVisibility(visibility: Int) {
        binding.bottomNavigationView.visibility = visibility
    }

    // Helper navigasi antar fragment
    fun navigateToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }
}