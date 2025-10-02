package com.example.ukopia.ui.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.ukopia.data.RecipeItem
import com.example.ukopia.databinding.FragmentRecipeDetailBinding
import com.example.ukopia.MainActivity // <<-- TAMBAHKAN INI

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ▼▼▼ Sembunyikan nav bar ▼▼▼
        (requireActivity() as MainActivity).setBottomNavVisibility(View.GONE)

        val recipeItem = arguments?.getParcelable<RecipeItem>("RECIPE_ITEM")

        recipeItem?.let {
            binding.tvHeaderTitle.text = it.name.uppercase()
            binding.tvRecipeTitle.text = it.name
            binding.tvRecipeDescription.text = it.description
            binding.tvWaterAmount.text = it.waterAmount
            binding.tvCoffeeAmount.text = it.coffeeAmount
            binding.tvGrindSize.text = it.grindSize
            binding.tvHeat.text = it.heat
            binding.tvTime.text = it.time
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}