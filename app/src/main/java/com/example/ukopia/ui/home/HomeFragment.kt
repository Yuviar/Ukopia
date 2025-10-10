package com.example.ukopia.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ukopia.MainActivity
import com.example.ukopia.R // Pastikan import R
import com.example.ukopia.data.MenuItem
import com.example.ukopia.databinding.FragmentHomeBinding

import com.example.ukopia.ui.home.MenuFilterDialogFragment.OnCategorySelectedListener

class HomeFragment : Fragment(), OnCategorySelectedListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var menuAdapter: MenuAdapter
    private var allMenuItems: List<MenuItem> = emptyList()
    private lateinit var currentFilterCategory: String // Akan diinisialisasi di onCreate/onViewCreated

    private lateinit var filterCategories: Array<String>

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

        // Inisialisasi filterCategories menggunakan string resources
        filterCategories = arrayOf(
            getString(R.string.category_all),
            getString(R.string.category_black_coffee),
            getString(R.string.category_white_coffee),
            getString(R.string.category_non_coffee),
            getString(R.string.category_artisan_tea),
            getString(R.string.category_flavoured_milk)
        )
        // Set currentFilterCategory awal ke "All" menggunakan string resource
        currentFilterCategory = getString(R.string.category_all)

        // Pastikan dummy items menggunakan localized category strings
        allMenuItems = createDummyMenuItems()

        menuAdapter = MenuAdapter(emptyList())
        binding.rvMenuItems.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvMenuItems.adapter = menuAdapter

        setupFilterButton()

        displayFilteredMenu(currentFilterCategory)
        updateFilterButtonText(currentFilterCategory)
    }

    private fun setupFilterButton() {
        binding.buttonFilter.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun showFilterDialog() {
        val dialog = MenuFilterDialogFragment.newInstance(filterCategories, currentFilterCategory)
        dialog.setTargetFragment(this, 0)
        dialog.show(parentFragmentManager, "MenuFilterDialog")
    }

    override fun onCategorySelected(category: String) {
        if (currentFilterCategory != category) {
            currentFilterCategory = category
            displayFilteredMenu(currentFilterCategory)
            updateFilterButtonText(currentFilterCategory)
        }
    }

    private fun displayFilteredMenu(category: String) {
        val filteredList = if (category == getString(R.string.category_all)) { // Menggunakan string resource
            allMenuItems
        } else {
            allMenuItems.filter { it.category == category }
        }
        menuAdapter.updateData(filteredList)
    }

    private fun updateFilterButtonText(category: String) {
        binding.buttonFilter.text = category
    }

    // Mengubah createDummyMenuItems agar menggunakan string resources untuk 'category'
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