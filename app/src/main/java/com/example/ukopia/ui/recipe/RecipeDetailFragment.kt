package com.example.ukopia.ui.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.ukopia.data.RecipeItem
import com.example.ukopia.databinding.FragmentRecipeDetailBinding
import com.example.ukopia.MainActivity
import com.example.ukopia.R

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

            // Menampilkan langkah-langkah resep
            if (it.steps.isNotEmpty()) {
                binding.tvStepsTitle.visibility = View.VISIBLE
                binding.containerRecipeDetailSteps.visibility = View.VISIBLE
                // Hapus semua view lama sebelum menambahkan yang baru (penting saat recreate view)
                binding.containerRecipeDetailSteps.removeAllViews()
                it.steps.forEachIndexed { index, stepText ->
                    val stepView = LayoutInflater.from(context).inflate(R.layout.item_recipe_detail_step_display, binding.containerRecipeDetailSteps, false)
                    val tvStepNumber = stepView.findViewById<TextView>(R.id.textViewStepNumber)
                    val tvStepDescription = stepView.findViewById<TextView>(R.id.textViewStepDescription)

                    tvStepNumber.text = (index + 1).toString()
                    tvStepDescription.text = stepText

                    binding.containerRecipeDetailSteps.addView(stepView)
                }
            } else {
                binding.tvStepsTitle.visibility = View.GONE
                binding.containerRecipeDetailSteps.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}