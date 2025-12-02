package com.example.ukopia.ui.menu

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.UkopiaApplication
import com.example.ukopia.databinding.FragmentMenuBinding
import com.example.ukopia.models.MenuApiItem
import java.util.Locale

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    // Inisialisasi ViewModel
    private val viewModel: MenuViewModel by viewModels {
        MenuViewModelFactory((requireActivity().application as UkopiaApplication).repository)
    }

    private lateinit var menuAdapter: MenuAdapter
    private lateinit var horizontalFilterAdapter: HorizontalFilterAdapter

    // Variabel untuk menyimpan daftar penuh dari DB
    private var allMenuItemsFromDb: List<MenuApiItem> = emptyList()

    private lateinit var currentFilterCategoryName: String
    private var currentSearchQuery: String = ""

    // HAPUS: private lateinit var filterCategories: Array<String> (Sudah diganti logic dinamis)

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

        // 1. Set Kategori Default "All"
        val categoryAllString = getString(R.string.category_all)
        currentFilterCategoryName = categoryAllString

        // 2. Setup Menu Adapter (Grid)
        menuAdapter = MenuAdapter(emptyList()) { menuItem ->
            val detailMenuFragment = DetailMenuFragment.newInstance(menuItem)
            (requireActivity() as MainActivity).navigateToFragment(detailMenuFragment)
        }
        binding.rvMenuItems.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvMenuItems.adapter = menuAdapter

        // 3. Setup Filter Adapter (Horizontal) - Awalnya Kosong
        binding.rvFilterCategories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        // Adapter akan di-set setelah data kategori dimuat dari API (di dalam Observer)

        // 4. Setup Search
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString()
                binding.ivClearSearch.visibility = if (s.isNullOrBlank()) View.GONE else View.VISIBLE
                displayFilteredMenu()
            }
            override fun afterTextChanged(s: Editable?) { }
        })

        binding.ivClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
        }

        setupObservers()

        // 5. Panggil Data
        viewModel.fetchMenuItems() // Refresh menu dari API ke DB
        viewModel.fetchCategories(categoryAllString) // Ambil kategori dinamis dari API
    }

    private fun setupObservers() {
        // Observer untuk daftar menu (dari DB)
        viewModel.menuItems.observe(viewLifecycleOwner, Observer { menuList ->
            allMenuItemsFromDb = menuList // Simpan daftar penuh
            displayFilteredMenu() // Terapkan filter yang sedang aktif
        })

        // Observer Kategori (BARU) - Dari API
        viewModel.categories.observe(viewLifecycleOwner, Observer { categoryList ->
            // Inisialisasi adapter filter dengan data dari API
            horizontalFilterAdapter = HorizontalFilterAdapter(categoryList, currentFilterCategoryName) { selectedCategory ->
                if (currentFilterCategoryName != selectedCategory) {
                    currentFilterCategoryName = selectedCategory
                    horizontalFilterAdapter.updateSelection(selectedCategory)
                    displayFilteredMenu() // Filter menu lokal
                }
            }
            binding.rvFilterCategories.adapter = horizontalFilterAdapter
        })
    }

    // Fungsi memfilter daftar yang ada di HP
    private fun displayFilteredMenu() {
        var filteredList = allMenuItemsFromDb
        val categoryAllString = getString(R.string.category_all)

        // 1. Filter Kategori
        if (currentFilterCategoryName != categoryAllString) {
            // Ignore case agar "Coffee" cocok dengan "coffee"
            filteredList = filteredList.filter {
                it.nama_kategori.equals(currentFilterCategoryName, ignoreCase = true)
            }
        }

        // 2. Filter Pencarian
        if (currentSearchQuery.isNotBlank()) {
            val lowerCaseQuery = currentSearchQuery.toLowerCase(Locale.ROOT)
            filteredList = filteredList.filter {
                it.nama_menu.toLowerCase(Locale.ROOT).startsWith(lowerCaseQuery)
            }
        }

        menuAdapter.updateData(filteredList)
        binding.rvMenuItems.scrollToPosition(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}