package com.example.ukopia.ui.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.ukopia.R
import com.example.ukopia.data.RecipeItem
import com.example.ukopia.databinding.FragmentRecipeListBinding
import com.google.android.material.button.MaterialButton
import com.example.ukopia.MainActivity

class RecipeListFragment : Fragment() {

    private var _binding: FragmentRecipeListBinding? = null
    private val binding get() = _binding!!

    private val recipeViewModel: RecipeViewModel by activityViewModels()

    private var selectedMethodName: String? = null
    private var isMyRecipeActive = false

    private var lastViewedAllRecipes: MutableMap<String, RecipeItem> = mutableMapOf()
    private var lastViewedMyRecipes: MutableMap<String, RecipeItem> = mutableMapOf()

    private var currentDisplayedRecipe: RecipeItem? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecipeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).setBottomNavVisibility(View.GONE)

        selectedMethodName = arguments?.getString("SELECTED_METHOD_NAME")
        // Menggunakan string resource untuk default title jika methodName null
        binding.tvHeaderTitle.text = selectedMethodName?.uppercase()?.replace(" ", "\n") ?: getString(R.string.recipe_list_title)

        setupListeners()

        recipeViewModel.allRecipes.observe(viewLifecycleOwner) {
            val showMyRecipesFromArgs = arguments?.getBoolean("SHOW_MY_RECIPES", false) ?: false
            val specificRecipeTitleFromArgs = arguments?.getString("SPECIFIC_RECIPE_TITLE")

            isMyRecipeActive = showMyRecipesFromArgs

            displayRecipesBasedOnFilter(specificRecipeTitleFromArgs)

            updateButtonStyles(
                if (isMyRecipeActive) binding.buttonResepSaya else binding.buttonSemuaResep,
                if (isMyRecipeActive) binding.buttonSemuaResep else binding.buttonResepSaya
            )

            arguments?.remove("SHOW_MY_RECIPES")
            arguments?.remove("SPECIFIC_RECIPE_TITLE")
        }
    }

    private fun setupListeners() {
        binding.buttonSemuaResep.setOnClickListener {
            selectedMethodName?.let { method ->
                if (isMyRecipeActive) {
                    currentDisplayedRecipe?.let { lastViewedMyRecipes[method] = it }
                } else {
                    currentDisplayedRecipe?.let { lastViewedAllRecipes[method] = it }
                }
            }

            isMyRecipeActive = false
            displayRecipesBasedOnFilter()
            updateButtonStyles(binding.buttonSemuaResep, binding.buttonResepSaya)
        }

        binding.buttonResepSaya.setOnClickListener {
            selectedMethodName?.let { method ->
                if (isMyRecipeActive) {
                    currentDisplayedRecipe?.let { lastViewedMyRecipes[method] = it }
                } else {
                    currentDisplayedRecipe?.let { lastViewedAllRecipes[method] = it }
                }
            }

            isMyRecipeActive = true
            displayRecipesBasedOnFilter()
            updateButtonStyles(binding.buttonResepSaya, binding.buttonSemuaResep)
        }

        binding.fabAddRecipe.setOnClickListener {
            val addFragment = AddRecipeFragment().apply {
                arguments = Bundle().apply {
                    putString("METHOD_NAME", selectedMethodName)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, addFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.recipeContentLayout.setOnClickListener {
            currentDisplayedRecipe?.let { navigateToRecipeDetail(it) }
        }
    }

    private fun displayRecipesBasedOnFilter(specificRecipeTitle: String? = null) {
        val allRecipesForMethod = recipeViewModel.allRecipes.value
            ?.filter { it.method.equals(selectedMethodName, ignoreCase = true) }
            ?: emptyList()

        val recipesForCurrentFilter: List<RecipeItem> = if (isMyRecipeActive) {
            allRecipesForMethod.filter { it.isMine }
        } else {
            allRecipesForMethod.filter { !it.isMine }
        }

        var recipeToDisplay: RecipeItem? = null

        if (specificRecipeTitle != null) {
            recipeToDisplay = recipesForCurrentFilter.firstOrNull { it.name.equals(specificRecipeTitle, ignoreCase = true) }
        }

        if (recipeToDisplay == null && currentDisplayedRecipe != null && recipesForCurrentFilter.contains(currentDisplayedRecipe!!)) {
            recipeToDisplay = currentDisplayedRecipe
        }

        if (recipeToDisplay == null && selectedMethodName != null) {
            recipeToDisplay = if (isMyRecipeActive) {
                lastViewedMyRecipes[selectedMethodName]?.takeIf { recipesForCurrentFilter.contains(it) }
            } else {
                lastViewedAllRecipes[selectedMethodName]?.takeIf { recipesForCurrentFilter.contains(it) }
            }
        }

        if (recipeToDisplay == null) {
            recipeToDisplay = recipesForCurrentFilter.firstOrNull()
        }

        currentDisplayedRecipe = recipeToDisplay
        updateHeaderAndRecipeDetails(currentDisplayedRecipe)
    }

    private fun updateHeaderAndRecipeDetails(recipe: RecipeItem?) {
        recipe?.let {
            binding.tvRecipeTitle.text = it.name
            binding.tvRecipeDescription.text = it.description
            binding.tvWaterAmount.text = it.waterAmount
            binding.tvCoffeeAmount.text = it.coffeeAmount
            binding.tvTime.text = it.time

            binding.tvRecipeDescription.visibility = View.VISIBLE
            binding.layoutWaterAmount.visibility = View.VISIBLE
            binding.layoutCoffeeAmount.visibility = View.VISIBLE
            binding.layoutTimer.visibility = View.VISIBLE

        } ?: run {
            if (isMyRecipeActive) {
                binding.tvRecipeTitle.text = getString(R.string.no_my_recipes_added_yet)
                binding.tvRecipeDescription.visibility = View.GONE
                binding.layoutWaterAmount.visibility = View.GONE
                binding.layoutCoffeeAmount.visibility = View.GONE
                binding.layoutTimer.visibility = View.GONE
            } else {
                binding.tvRecipeTitle.text = getString(R.string.no_recipes_available)
                binding.tvRecipeDescription.text = getString(R.string.add_new_recipes_prompt)
                binding.tvRecipeDescription.visibility = View.VISIBLE

                binding.tvWaterAmount.text = "-";
                binding.tvCoffeeAmount.text = "-";
                binding.tvTime.text = "-";
                binding.layoutWaterAmount.visibility = View.GONE
                binding.layoutCoffeeAmount.visibility = View.GONE
                binding.layoutTimer.visibility = View.GONE
            }
        }
    }

    private fun navigateToRecipeDetail(recipe: RecipeItem) {
        val detailFragment = RecipeDetailFragment().apply {
            arguments = Bundle().apply {
                putParcelable("RECIPE_ITEM", recipe)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun updateButtonStyles(activeButton: MaterialButton, inactiveButton: MaterialButton) {
        context?.let {
            val black = ContextCompat.getColor(it, R.color.black)
            val white = ContextCompat.getColor(it, R.color.white)
            activeButton.setBackgroundColor(black)
            activeButton.setTextColor(white)
            inactiveButton.setBackgroundColor(white)
            inactiveButton.setTextColor(black)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}