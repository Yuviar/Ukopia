package com.example.ukopia.ui.recipe

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.MainActivity
import com.example.ukopia.adapter.BrewMethodAdapter
import com.example.ukopia.data.BrewMethod
import com.example.ukopia.databinding.FragmentRecipeBinding

class RecipeFragment : Fragment() {

    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!

    private val recipeViewModel: RecipeViewModel by activityViewModels()
    private lateinit var adapter: BrewMethodAdapter
    private var allMethods: List<BrewMethod> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)

        setupRecyclerView()
        setupSearch()

        // Panggil API untuk ambil data metode
        recipeViewModel.loadBrewMethods()

        // Observasi data dari ViewModel
        recipeViewModel.brewMethods.observe(viewLifecycleOwner) { methods ->
            allMethods = methods
            adapter.submitList(methods)
        }
        recipeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Debugging: Cek di Logcat apakah ini terpanggil
            android.util.Log.d("LOADING_DEBUG", "Status Loading: $isLoading")

            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                // Jangan sembunyikan RecyclerView agar layout tidak "lompat"
                // Cukup biarkan progress bar muncul di atasnya
                binding.recyclerViewRecipe.alpha = 0.5f // Opsional: Efek transparan saat loading
            } else {
                binding.progressBar.visibility = View.GONE
                binding.recyclerViewRecipe.alpha = 1.0f
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = BrewMethodAdapter { method ->
            // Navigasi ke List Resep saat tombol diklik
            val fragment = RecipeListFragment().apply {
                arguments = Bundle().apply {
                    putInt("ID_METODE", method.id)
                    putString("SELECTED_METHOD_NAME", method.name)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(com.example.ukopia.R.id.container, fragment) // Pastikan ID container benar
                .addToBackStack(null)
                .commit()
        }

        binding.recyclerViewRecipe.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@RecipeFragment.adapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                binding.ivClearSearch.isVisible = query.isNotEmpty()

                // Filter list lokal
                val filteredList = if (query.isEmpty()) {
                    allMethods
                } else {
                    allMethods.filter { it.name.contains(query, ignoreCase = true) }
                }
                adapter.submitList(filteredList)
            }
        })

        binding.ivClearSearch.setOnClickListener {
            binding.etSearch.text.clear()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}