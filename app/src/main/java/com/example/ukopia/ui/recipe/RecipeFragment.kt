package com.example.ukopia.ui.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.R
import com.example.ukopia.adapter.BrewMethodAdapter
import com.example.ukopia.databinding.FragmentRecipeBinding
import com.example.ukopia.MainActivity // <<-- TAMBAHKAN INI

class RecipeFragment : Fragment() {

    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!

    private val recipeViewModel: RecipeViewModel by activityViewModels()
    private lateinit var brewMethodAdapter: BrewMethodAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ▼▼▼ Pastikan nav bar terlihat di RecipeFragment ▼▼▼
        (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)

        brewMethodAdapter = BrewMethodAdapter { selectedMethod ->
            navigateToRecipeList(selectedMethod.name)
        }

        binding.recyclerViewRecipe.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = brewMethodAdapter
        }

        recipeViewModel.brewMethods.observe(viewLifecycleOwner) { methods ->
            brewMethodAdapter.submitList(methods)
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