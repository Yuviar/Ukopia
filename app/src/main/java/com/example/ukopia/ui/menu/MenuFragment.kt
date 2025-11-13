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
import com.example.ukopia.UkopiaApplication // IMPORT
import com.example.ukopia.databinding.FragmentMenuBinding
import com.example.ukopia.models.MenuApiItem // IMPORT
import com.example.ukopia.utils.HorizontalSpaceItemDecoration
import java.util.Locale

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    // Inisialisasi ViewModel dengan Factory
    private val viewModel: MenuViewModel by viewModels {
        MenuViewModelFactory((requireActivity().application as UkopiaApplication).repository)
    }

    private lateinit var menuAdapter: MenuAdapter
    private lateinit var horizontalFilterAdapter: HorizontalFilterAdapter

    // Variabel untuk menyimpan daftar penuh dari DB
    private var allMenuItemsFromDb: List<MenuApiItem> = emptyList()

    private lateinit var currentFilterCategoryName: String
    private var currentSearchQuery: String = ""

    private lateinit var filterCategories: Array<String>

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

        // Setup filter (GANTI INI SESUAI NAMA KATEGORI DI DB ANDA)
        filterCategories = arrayOf(
            getString(R.string.category_all),
            "Coffe", // <-- Sesuaikan dengan 'nama_kategori' di DB Anda
            "Milk"   // <-- Sesuaikan dengan 'nama_kategori' di DB Anda
        )
        currentFilterCategoryName = getString(R.string.category_all)

        menuAdapter = MenuAdapter(emptyList()) { menuItem ->
            val detailMenuFragment = DetailMenuFragment.newInstance(menuItem)
            (requireActivity() as MainActivity).navigateToFragment(detailMenuFragment)
        }

        binding.rvMenuItems.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvMenuItems.adapter = menuAdapter

        // Logika klik filter
        horizontalFilterAdapter = HorizontalFilterAdapter(filterCategories.toList(), currentFilterCategoryName) { selectedCategory ->
            if (currentFilterCategoryName != selectedCategory) {
                currentFilterCategoryName = selectedCategory
                horizontalFilterAdapter.updateSelection(selectedCategory)
                displayFilteredMenu() // Filter data lokal
            }
        }
        binding.rvFilterCategories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvFilterCategories.adapter = horizontalFilterAdapter
        // ... (ItemDecoration Anda tetap sama) ...

        // Setup Search
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString()
                binding.ivClearSearch.visibility = if (s.isNullOrBlank()) View.GONE else View.VISIBLE
                displayFilteredMenu() // Filter data lokal
            }
            override fun afterTextChanged(s: Editable?) { }
        })

        binding.ivClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
        }

        setupObservers()

        // Panggil refresh data (akan memuat dari API ke DB)
        viewModel.fetchMenuItems()
    }

    private fun setupObservers() {
        // Observer untuk daftar menu (dari DB)
        viewModel.menuItems.observe(viewLifecycleOwner, Observer { menuList ->
            allMenuItemsFromDb = menuList // Simpan daftar penuh
            displayFilteredMenu() // Terapkan filter yang sedang aktif
        })

        // (Opsional) Tampilkan loading/error
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { /* ... */ })
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { /* ... */ })
    }

    // Fungsi ini sekarang memfilter daftar yang ada di HP
    private fun displayFilteredMenu() {
        var filteredList = allMenuItemsFromDb

        // 1. Filter Kategori
        if (currentFilterCategoryName != getString(R.string.category_all)) {
            filteredList = filteredList.filter { it.nama_kategori == currentFilterCategoryName }
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