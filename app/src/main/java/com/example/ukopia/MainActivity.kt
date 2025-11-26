// D:/github rama/Ukopia/app/src/main/java/com/example/ukopia/MainActivity.kt
package com.example.ukopia

import android.app.Activity // Import Activity
import android.content.Context
import android.content.Intent // Import Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts // Import ActivityResultContracts
import androidx.appcompat.app.AlertDialog // Import AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.ukopia.ui.akun.AkunFragment
import com.example.ukopia.ui.akun.LocaleHelper
import com.example.ukopia.ui.home.HomeFragment
import com.example.ukopia.ui.menu.MenuFragment
import com.example.ukopia.ui.recipe.RecipeFragment
import com.example.ukopia.ui.loyalty.LoyaltyFragment
import com.example.ukopia.ui.auth.LoginActivity // Import LoginActivity
import com.example.ukopia.databinding.DialogLoginRequiredBinding // Import DialogLoginRequiredBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private var isReadyToExit = false

    // NEW: ActivityResultLauncher for login specifically for Loyalty tab access
    private val loyaltyLoginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && SessionManager.isLoggedIn(this)) {
            // If login successful, now navigate to LoyaltyFragment
            loadRootFragment(LoyaltyFragment(), R.id.itemLoyalty)
            // Manually set the selected item on the bottom navigation view
            bottomNavigationView.selectedItemId = R.id.itemLoyalty
        }
        // If login failed or cancelled, nothing happens, and the previous tab remains selected
    }

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
            isReadyToExit = false
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.container)

                val rootFragments = listOf(
                    HomeFragment::class.java.simpleName,
                    MenuFragment::class.java.simpleName,
                    LoyaltyFragment::class.java.simpleName,
                    RecipeFragment::class.java.simpleName,
                    AkunFragment::class.java.simpleName
                )

                if (supportFragmentManager.backStackEntryCount > 1 &&
                    currentFragment?.javaClass?.simpleName !in rootFragments
                ) {
                    supportFragmentManager.popBackStack()
                    isReadyToExit = false
                } else if (currentFragment !is HomeFragment) {
                    // Ensure the correct fragment is selected when popping back to a root fragment
                    val targetItemId = when (currentFragment) {
                        is MenuFragment -> R.id.itemMenu
                        is LoyaltyFragment -> R.id.itemLoyalty
                        is RecipeFragment -> R.id.itemRecipe
                        is AkunFragment -> R.id.itemAccount
                        else -> R.id.itemHome // Default to Home
                    }
                    if (currentFragment != null && currentFragment.javaClass.simpleName in rootFragments) {
                        // If we are on a root fragment other than Home, pop to Home
                        supportFragmentManager.popBackStack(HomeFragment::class.java.simpleName, 0)
                        bottomNavigationView.selectedItemId = R.id.itemHome
                    } else {
                        // For non-root fragments, just pop once
                        if (supportFragmentManager.backStackEntryCount > 0) {
                            supportFragmentManager.popBackStack()
                            val fragmentAfterPop = supportFragmentManager.findFragmentById(R.id.container)
                            val newTargetItemId = when (fragmentAfterPop) {
                                is HomeFragment -> R.id.itemHome
                                is MenuFragment -> R.id.itemMenu
                                is LoyaltyFragment -> R.id.itemLoyalty
                                is RecipeFragment -> R.id.itemRecipe
                                is AkunFragment -> R.id.itemAccount
                                else -> bottomNavigationView.selectedItemId // Keep current if unknown
                            }
                            bottomNavigationView.selectedItemId = newTargetItemId
                        } else {
                            // If no back stack, and not Home, go to Home
                            loadRootFragment(HomeFragment(), R.id.itemHome)
                            bottomNavigationView.selectedItemId = R.id.itemHome
                        }
                    }
                    isReadyToExit = false
                } else {
                    if (isReadyToExit) {
                        finishAffinity()
                    } else {
                        isReadyToExit = true
                        // You might want to show a toast message here
                        // Toast.makeText(this, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun loadRootFragment(fragment: Fragment, itemId: Int) {
        val fragmentTag = fragment.javaClass.simpleName

        // Clear back stack to only include the new root fragment
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment, fragmentTag)
            .addToBackStack(fragmentTag) // Add the root fragment to back stack
            .commit()
        isReadyToExit = false
    }

    fun navigateToFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val fragmentTag = fragment.javaClass.simpleName
        val transaction = supportFragmentManager.beginTransaction()

        transaction.replace(R.id.container, fragment, fragmentTag)

        if (addToBackStack) {
            transaction.addToBackStack(fragmentTag)
        } else {
            // This case typically implies replacing the current fragment without adding to backstack,
            // or clearing stack up to a certain fragment.
            // For root fragments, `loadRootFragment` is more appropriate.
            // If replacing current without backstack entry, simply use replace().commit()
            // Be careful with popBackStack(fragmentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            // in navigateToFragment if it's meant for general navigation.
            // For now, leaving it as is based on existing code structure.
        }
        transaction.commit()
        isReadyToExit = false
    }

    private val menuItemSelected = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.itemHome -> {
                loadRootFragment(HomeFragment(), R.id.itemHome)
                true
            }
            R.id.itemMenu -> {
                loadRootFragment(MenuFragment(), R.id.itemMenu)
                true
            }
            R.id.itemLoyalty -> {
                if (SessionManager.isLoggedIn(this)) {
                    loadRootFragment(LoyaltyFragment(), R.id.itemLoyalty)
                    true // Allow the item to be selected
                } else {
                    showLoginRequiredDialogForLoyalty()
                    false // Prevent the item from being selected visually until login is successful
                }
            }
            R.id.itemRecipe -> {
                loadRootFragment(RecipeFragment(), R.id.itemRecipe)
                true
            }
            R.id.itemAccount -> {
                loadRootFragment(AkunFragment(), R.id.itemAccount)
                true
            }
            else -> false
        }
    }

    // NEW: Method to show the login required dialog
    private fun showLoginRequiredDialogForLoyalty() {
        val dialogBinding = DialogLoginRequiredBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this) // Use 'this' for the Activity context
            .setView(dialogBinding.root)
            .create()

        dialogBinding.buttonDialogLogin.setOnClickListener {
            dialog.dismiss()
            // Launch LoginActivity using the launcher
            loyaltyLoginLauncher.launch(Intent(this, LoginActivity::class.java))
        }

        dialogBinding.buttonDialogCancel.setOnClickListener {
            dialog.dismiss()
            // If the user cancels, the currently selected item on the nav bar remains active.
            // No explicit action needed here because `menuItemSelected` returned `false`.
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    fun setBottomNavVisibility(visibility: Int) {
        bottomNavigationView.visibility = visibility
    }
}