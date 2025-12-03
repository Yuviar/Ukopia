package com.example.ukopia

import android.content.Context
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
import com.example.ukopia.ui.akun.LocaleHelper
import com.example.ukopia.ui.home.HomeFragment
import com.example.ukopia.ui.loyalty.LoyaltyFragment
import com.example.ukopia.ui.auth.LoginActivity
import com.example.ukopia.ui.menu.MenuFragment
import com.example.ukopia.ui.recipe.RecipeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val loyaltyLoginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (SessionManager.isLoggedIn(this)) {
            loadFragment(LoyaltyFragment())
            binding.bottomNavigationView.selectedItemId = R.id.itemLoyalty
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            super.attachBaseContext(LocaleHelper.onAttach(newBase))
        } else {
            super.attachBaseContext(newBase)
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

    private fun showLoginRequiredDialog() {
        val dialogBinding = DialogLoginRequiredBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.tvDialogLoginTitle.text = getString(R.string.login_required_dialog_title)
        dialogBinding.tvDialogLoginMessage.text = getString(R.string.login_required_dialog_message)
        dialogBinding.buttonDialogLogin.text = getString(R.string.login_dialog_button_text)
        dialogBinding.buttonDialogCancel.text = getString(R.string.cancel_dialog_button_text)


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

    fun setBottomNavVisibility(visibility: Int) {
        binding.bottomNavigationView.visibility = visibility
    }

    fun navigateToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }
}