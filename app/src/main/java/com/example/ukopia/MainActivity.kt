package com.example.ukopia

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.ukopia.ui.akun.AkunFragment
import com.example.ukopia.ui.akun.LocaleHelper
import com.example.ukopia.ui.home.HomeFragment // Import HomeFragment yang baru
import com.example.ukopia.ui.menu.MenuFragment // Import MenuFragment
import com.example.ukopia.ui.recipe.RecipeFragment
import com.example.ukopia.ui.loyalty.LoyaltyFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
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
                    supportFragmentManager.popBackStack(HomeFragment::class.java.simpleName, 0)
                    bottomNavigationView.selectedItemId = R.id.itemHome
                    isReadyToExit = false
                } else {
                    if (isReadyToExit) {
                        finishAffinity()
                    } else {
                        isReadyToExit = true
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
            .addToBackStack(fragmentTag)
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
            supportFragmentManager.popBackStack(fragmentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        transaction.commit()
        isReadyToExit = false
    }

    private val menuItemSelected = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.itemHome -> {
                loadRootFragment(HomeFragment(), R.id.itemHome)
                return@OnNavigationItemSelectedListener true
            }
            R.id.itemMenu -> {
                loadRootFragment(MenuFragment(), R.id.itemMenu)
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