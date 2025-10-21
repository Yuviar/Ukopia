package com.example.ukopia.ui.recipe

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.R
import com.example.ukopia.adapter.BrewMethodAdapter
import com.example.ukopia.databinding.FragmentRecipeBinding
import com.example.ukopia.MainActivity
import com.example.ukopia.data.BrewMethod

class RecipeFragment : Fragment() {

    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!

    private val recipeViewModel: RecipeViewModel by activityViewModels()
    private lateinit var brewMethodAdapter: BrewMethodAdapter

    private var allBrewMethods: List<BrewMethod> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)

        brewMethodAdapter = BrewMethodAdapter { selectedMethod ->
            navigateToRecipeList(selectedMethod.name)
        }

        binding.recyclerViewRecipe.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = brewMethodAdapter
        }

        recipeViewModel.brewMethods.observe(viewLifecycleOwner) { methods ->
            allBrewMethods = methods
            // Menggunakan submitList dengan callback untuk memastikan gulir ke atas setelah data awal dimuat
            brewMethodAdapter.submitList(methods) {
                binding.recyclerViewRecipe.scrollToPosition(0)
            }
        }

        setupSearchBar()
    }

    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                filterBrewMethods(query)

                binding.ivClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.ivClearSearch.setOnClickListener {
            // Hapus teks di EditText. Ini akan memicu onTextChanged dengan query kosong.
            // onTextChanged kemudian akan memanggil filterBrewMethods(""), yang sekarang memiliki callback gulir.
            binding.etSearch.text.clear()
            binding.ivClearSearch.visibility = View.GONE // Sembunyikan ikon hapus
        }
    }

    private fun filterBrewMethods(query: String) {
        val listToSubmit: List<BrewMethod>
        if (query.isBlank()) {
            listToSubmit = allBrewMethods
        } else {
            listToSubmit = allBrewMethods.filter { brewMethod ->
                brewMethod.name.startsWith(query, ignoreCase = true)
            }
        }

        // Menggunakan submitList dengan callback yang akan dipanggil setelah DiffUtil selesai dan daftar diperbarui.
        // Ini memastikan scrollToPosition(0) dieksekusi pada waktu yang tepat, baik itu dari ketikan manual atau tombol clear.
        brewMethodAdapter.submitList(listToSubmit) {
            binding.recyclerViewRecipe.scrollToPosition(0)
        }
    }

    private fun navigateToRecipeList(methodName: String) {
        val recipeListFragment = RecipeListFragment().apply {
            arguments = Bundle().apply {
                putString("SELECTED_METHOD_NAME", methodName)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, recipeListFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}