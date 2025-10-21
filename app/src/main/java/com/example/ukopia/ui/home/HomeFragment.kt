// D:/github_rama/Ukopia/app/src/main/java/com/example/ukopia/ui/home/HomeFragment.kt
package com.example.ukopia.ui.home

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
// import android.widget.Toast // Hapus import ini jika tidak digunakan lagi
// import androidx.activity.OnBackPressedCallback // Hapus import ini
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.SessionManager
import com.example.ukopia.data.MenuItem
import com.example.ukopia.databinding.FragmentHomeBinding
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var menuAdapter: MenuAdapter
    private lateinit var horizontalFilterAdapter: HorizontalFilterAdapter
    private var allMenuItems: MutableList<MenuItem> = mutableListOf()
    private lateinit var currentFilterCategory: String
    private var currentSearchQuery: String = ""

    private lateinit var filterCategories: Array<String>

    // --- HAPUS variabel ini ---
    // private var lastBackPressTime: Long = 0
    // private val BACK_PRESS_INTERVAL = 2000 // 2 detik

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (allMenuItems.isEmpty()) {
            allMenuItems.addAll(createDummyMenuItems())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)

        updateGreetingText()

        filterCategories = arrayOf(
            getString(R.string.category_all),
            getString(R.string.category_black_coffee),
            getString(R.string.category_white_coffee),
            getString(R.string.category_non_coffee),
            getString(R.string.category_artisan_tea),
            getString(R.string.category_flavoured_milk)
        )
        currentFilterCategory = getString(R.string.category_all)

        menuAdapter = MenuAdapter(allMenuItems) { menuItem ->
            val detailMenuFragment = DetailMenuFragment.newInstance(menuItem)
            (requireActivity() as MainActivity).navigateToFragment(detailMenuFragment)
        }

        binding.rvMenuItems.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvMenuItems.adapter = menuAdapter

        horizontalFilterAdapter = HorizontalFilterAdapter(filterCategories.toList(), currentFilterCategory) { selectedCategory ->
            if (currentFilterCategory != selectedCategory) {
                currentFilterCategory = selectedCategory
                displayFilteredMenu(currentFilterCategory, currentSearchQuery)
            }
        }
        binding.rvFilterCategories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvFilterCategories.adapter = horizontalFilterAdapter

        displayFilteredMenu(currentFilterCategory, currentSearchQuery)

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString()
                binding.ivClearSearch.visibility = if (s.isNullOrBlank()) View.GONE else View.VISIBLE
                displayFilteredMenu(currentFilterCategory, currentSearchQuery)
            }

            override fun afterTextChanged(s: Editable?) { }
        })

        binding.ivClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
        }

        // --- HAPUS seluruh blok logika "double back press to exit" ini ---
        // val callback = object : OnBackPressedCallback(true) {
        //     override fun handleOnBackPressed() {
        //         if (System.currentTimeMillis() - lastBackPressTime < BACK_PRESS_INTERVAL) {
        //             requireActivity().finish()
        //         } else {
        //             lastBackPressTime = System.currentTimeMillis()
        //             Toast.makeText(requireContext(), getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show()
        //         }
        //     }
        // }
        // requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        // --- Akhir blok yang dihapus ---
    }

    override fun onResume() {
        super.onResume()
        updateGreetingText()
        displayFilteredMenu(currentFilterCategory, currentSearchQuery)
    }

    private fun updateGreetingText() {
        val userName = SessionManager.SessionManager.getUserName(requireContext())
        if (!userName.isNullOrBlank()) {
            binding.tvGreeting.text = getString(R.string.greeting_salutation, userName)
        } else {
            binding.tvGreeting.text = getString(R.string.greeting_salutation_default)
        }
    }

    private fun displayFilteredMenu(category: String, query: String) {
        var filteredList = if (category == getString(R.string.category_all)) {
            allMenuItems
        } else {
            allMenuItems.filter { it.category == category }
        }

        if (query.isNotBlank()) {
            val lowerCaseQuery = query.toLowerCase(Locale.ROOT)
            filteredList = filteredList.filter {
                it.name.toLowerCase(Locale.ROOT).startsWith(lowerCaseQuery)
            }
        }

        menuAdapter.updateData(filteredList)
        horizontalFilterAdapter.updateSelection(category)
    }

    private fun createDummyMenuItems(): List<MenuItem> {
        val context = requireContext()
        return listOf(
            MenuItem("1", "Espresso", "4.8/5.0", R.drawable.sample_coffee, "Ekstraksi kopi pekat, dasar dari semua minuman kopi.", context.getString(R.string.category_black_coffee)),
            MenuItem("2", "Long Black", "4.5/5.0", R.drawable.sample_coffee, "Air panas dicampur dengan espresso untuk rasa yang kuat.", context.getString(R.string.category_black_coffee)),
            MenuItem("3", "Red Eye", "4.2/5.0", R.drawable.sample_coffee, "Kopi drip dengan satu shot espresso tambahan.", context.getString(R.string.category_black_coffee)),
            MenuItem("4", "Black Eye", "4.3/5.0", R.drawable.sample_coffee, "Kopi drip dengan dua shot espresso tambahan.", context.getString(R.string.category_black_coffee)),
            MenuItem("5", "Dead Eye", "4.4/5.0", R.drawable.sample_coffee, "Kopi drip dengan tiga shot espresso tambahan.", context.getString(R.string.category_black_coffee)),
            MenuItem("6", "Americano", "4.6/5.0", R.drawable.sample_coffee, "Espresso dengan tambahan air panas.", context.getString(R.string.category_black_coffee)),
            MenuItem("7", "Flat White", "4.7/5.0", R.drawable.sample_coffee, "Espresso dengan susu steamed yang lembut, tanpa foam tebal.", context.getString(R.string.category_white_coffee)),
            MenuItem("8", "Cappuccino", "4.6/5.0", R.drawable.sample_coffee, "Kopi klasik dengan foam susu tebal dan taburan bubuk cokelat.", context.getString(R.string.category_white_coffee)),
            MenuItem("9", "Latte", "4.7/5.0", R.drawable.sample_coffee, "Espresso dengan susu steamed dan lapisan microfoam.", context.getString(R.string.category_white_coffee)),
            MenuItem("10", "Piccolo", "4.4/5.0", R.drawable.sample_coffee, "Mini latte dengan ristretto.", context.getString(R.string.category_white_coffee)),
            MenuItem("11", "Magic", "4.5/5.0", R.drawable.sample_coffee, "Double ristretto dengan susu steamed.", context.getString(R.string.category_white_coffee)),
            MenuItem("12", "Macchiato", "4.3/5.0", R.drawable.sample_coffee, "Espresso dengan sedikit foam susu di atasnya.", context.getString(R.string.category_white_coffee)),
            MenuItem("13", "Matcha", "4.7/5.0", R.drawable.sample_coffee, "Minuman teh hijau Jepang yang disajikan dingin atau panas.", context.getString(R.string.category_non_coffee)),
            MenuItem("14", "Coklat", "4.8/5.0", R.drawable.sample_coffee, "Minuman cokelat kaya rasa, bisa panas atau dingin.", context.getString(R.string.category_non_coffee)),
            MenuItem("15", "Chamomile", "4.5/5.0", R.drawable.sample_coffee, "Teh herbal yang dikenal menenangkan.", context.getString(R.string.category_artisan_tea)),
            MenuItem("16", "Rose", "4.6/5.0", R.drawable.sample_coffee, "Teh mawar dengan aroma harum.", context.getString(R.string.category_artisan_tea)),
            MenuItem("17", "Vanilla", "4.4/5.0", R.drawable.sample_coffee, "Teh dengan sentuhan rasa vanila yang lembut.", context.getString(R.string.category_artisan_tea)),
            MenuItem("18", "Blueberry Pancake", "4.9/5.0", R.drawable.sample_coffee, "Susu dengan perpaduan rasa blueberry dan pancake.", context.getString(R.string.category_flavoured_milk)),
            MenuItem("19", "Strawberry", "4.7/5.0", R.drawable.sample_coffee, "Susu dengan rasa strawberry alami.", context.getString(R.string.category_flavoured_milk)),
            MenuItem("20", "Hazelnut", "4.8/5.0", R.drawable.sample_coffee, "Susu dengan aroma dan rasa hazelnut yang kaya.", context.getString(R.string.category_flavoured_milk)),
            MenuItem("21", "Pistachio", "4.6/5.0", R.drawable.sample_coffee, "Susu dengan rasa pistachio yang unik dan gurih.", context.getString(R.string.category_flavoured_milk)),
            MenuItem("22", "Vanilla", "4.5/5.0", R.drawable.sample_coffee, "Susu dengan rasa vanila klasik.", context.getString(R.string.category_flavoured_milk)),
            MenuItem("23", "Butterscotch", "4.7/5.0", R.drawable.sample_coffee, "Susu dengan rasa butterscotch yang manis dan creamy.", context.getString(R.string.category_flavoured_milk))
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}