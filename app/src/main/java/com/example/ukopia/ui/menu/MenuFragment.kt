package com.example.ukopia.ui.menu

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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

    private val viewModel: MenuViewModel by activityViewModels {
        MenuViewModelFactory((requireActivity().application as UkopiaApplication).repository)
    }

    private lateinit var menuAdapter: MenuAdapter
    private lateinit var horizontalFilterAdapter: HorizontalFilterAdapter

    private var allMenuItemsFromDb: List<MenuApiItem> = emptyList()

    private lateinit var currentFilterCategoryName: String
    private var currentSearchQuery: String = ""


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

        val categoryAllString = getString(R.string.category_all)
        currentFilterCategoryName = categoryAllString

        menuAdapter = MenuAdapter(emptyList()) { menuItem ->
            val detailMenuFragment = DetailMenuFragment.newInstance(menuItem)
            (requireActivity() as MainActivity).navigateToFragment(detailMenuFragment)
        }
        binding.rvMenuItems.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvMenuItems.adapter = menuAdapter

        binding.rvFilterCategories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

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
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.forceRefreshMenu()
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (!isLoading) {
                binding.swipeRefresh.isRefreshing = false
            }
        }
        setupObservers()

        viewModel.fetchMenuItems()
        viewModel.fetchCategories(categoryAllString)
    }

    private fun setupObservers() {
        viewModel.menuItems.observe(viewLifecycleOwner, Observer { menuList ->
            allMenuItemsFromDb = menuList
            displayFilteredMenu()
        })


        viewModel.categories.observe(viewLifecycleOwner, Observer { categoryList ->
            horizontalFilterAdapter = HorizontalFilterAdapter(categoryList, currentFilterCategoryName) { selectedCategory ->
                if (currentFilterCategoryName != selectedCategory) {
                    currentFilterCategoryName = selectedCategory
                    horizontalFilterAdapter.updateSelection(selectedCategory)
                    displayFilteredMenu()
                }
            }
            binding.rvFilterCategories.adapter = horizontalFilterAdapter
        })
    }

    private fun displayFilteredMenu() {
        var filteredList = allMenuItemsFromDb
        val categoryAllString = getString(R.string.category_all)

        if (currentFilterCategoryName != categoryAllString) {
            filteredList = filteredList.filter {
                it.nama_kategori.equals(currentFilterCategoryName, ignoreCase = true)
            }
        }

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