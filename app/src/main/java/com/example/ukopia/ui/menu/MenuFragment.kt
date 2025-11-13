package com.example.ukopia.ui.menu

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.SessionManager
import com.example.ukopia.data.MenuItem
import com.example.ukopia.databinding.FragmentMenuBinding
import com.example.ukopia.utils.HorizontalSpaceItemDecoration // Import ItemDecoration Anda

import java.util.Locale

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var menuAdapter: MenuAdapter
    private lateinit var horizontalFilterAdapter: HorizontalFilterAdapter
    private var allMenuItems: MutableList<MenuItem> = mutableListOf()
    private lateinit var currentFilterCategory: String
    private var currentSearchQuery: String = ""

    private lateinit var filterCategories: Array<String>

    private val PREFS_NAME = "UtopiaRatingPrefs"
    private val ITEM_TOTAL_RATING_KEY_PREFIX = "item_total_rating_"
    private val ITEM_RATING_COUNT_KEY_PREFIX = "item_rating_count_"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)

        if (allMenuItems.isEmpty()) {
            allMenuItems.addAll(createDummyMenuItems())
            loadPersistedRatings()
        }

        setupFragmentResultListener()
        // updateGreetingText() // Ini mungkin ada di HomeFragment, tidak relevan untuk MenuFragment

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

        // Tambahkan ItemDecoration untuk rv_filter_categories
        // Periksa apakah sudah ada ItemDecoration sebelumnya untuk menghindari duplikasi
        if (binding.rvFilterCategories.itemDecorationCount == 0) {
            val horizontalSpace = resources.getDimensionPixelSize(R.dimen.filter_item_horizontal_space) // Jarak antar item (misal: 8dp)
            val startPadding = resources.getDimensionPixelSize(R.dimen.margin_16dp) // Padding kiri 16dp
            val endPadding = resources.getDimensionPixelSize(R.dimen.margin_16dp) // Padding kanan 16dp (untuk alignment target)

            binding.rvFilterCategories.addItemDecoration(
                HorizontalSpaceItemDecoration(horizontalSpace, startPadding, endPadding)
            )
        }
        // Hapus paddingStart dan paddingEnd dari XML untuk rv_filter_categories.
        // Padding sekarang dikelola oleh ItemDecoration dan layout_margin dari item itu sendiri.

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
    }

    private fun setupFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener("detailResult", viewLifecycleOwner) { _, bundle ->
            val updatedMenuItem = bundle.getParcelable<MenuItem>("updatedMenuItem")
            updatedMenuItem?.let { itemFromDetail ->
                val index = allMenuItems.indexOfFirst { it.id == itemFromDetail.id }
                if (index != -1) {
                    allMenuItems[index] = itemFromDetail
                    displayFilteredMenu(currentFilterCategory, currentSearchQuery)
                }
            }
        }
    }

    private fun loadPersistedRatings() {
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        allMenuItems.forEach { menuItem ->
            val totalRating = prefs.getFloat(ITEM_TOTAL_RATING_KEY_PREFIX + menuItem.id, -1f)
            val ratingCount = prefs.getInt(ITEM_RATING_COUNT_KEY_PREFIX + menuItem.id, -1)

            if (totalRating != -1f && ratingCount > 0) {
                val averageRating = totalRating / ratingCount
                menuItem.rating = String.format(Locale.ROOT, "%.1f/5.0", averageRating)
            }
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
        binding.rvMenuItems.scrollToPosition(0) // Gulir menu items ke atas
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