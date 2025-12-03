package com.example.ukopia.ui.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.MainActivity
import com.example.ukopia.data.RecipeItem
import com.example.ukopia.databinding.FragmentRecipeDetailBinding
import com.example.ukopia.models.ApiClient
import kotlinx.coroutines.launch

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).setBottomNavVisibility(View.GONE)

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        val basicRecipe = arguments?.getParcelable<RecipeItem>("RECIPE_ITEM")
        if (basicRecipe != null) {
            bindBasicData(basicRecipe)
        }

        val recipeId = arguments?.getInt("ID_RESEP") ?: basicRecipe?.id?.toIntOrNull() ?: 0
        if (recipeId > 0) {
            loadRecipeDetail(recipeId)
        }
    }

    private fun loadRecipeDetail(id: Int) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.getRecipeDetail(id)
                if (response.isSuccessful && response.body()?.success == true) {
                    val fullRecipe = response.body()?.data
                    if (fullRecipe != null) {
                        bindFullData(fullRecipe)
                    }
                } else {
                    Toast.makeText(context, "Gagal memuat detail", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun bindBasicData(item: RecipeItem) {
        binding.tvHeaderTitle.text = item.name.uppercase()
        binding.tvRecipeTitle.text = item.name
        binding.tvRecipeDescription.text = item.description

        binding.tvWaterAmount.text = item.waterAmount
        binding.tvCoffeeAmount.text = item.coffeeAmount
        binding.tvTime.text = item.extractionTime
    }

    private fun bindFullData(item: RecipeItem) {
        binding.tvWaterAmount.text = item.waterAmount
        binding.tvCoffeeAmount.text = item.coffeeAmount
        binding.tvTime.text = item.extractionTime

        binding.tvGrindSize.text = item.grindSize ?: "-"
        binding.tvHeat.text = item.temperature
        binding.tvBrewWeight.text = item.brewWeightText
        binding.tvTds.text = item.tdsText


        val coffeeVal = item.coffeeAmountInt.toDouble()

        val waterVal = item.waterAmountInt.toDouble()
        if (coffeeVal > 0 && waterVal > 0) {
            val ratioWater = waterVal / coffeeVal
            binding.tvCoffeeWaterRatio.text = String.format("1:%.1f", ratioWater)
        } else {
            binding.tvCoffeeWaterRatio.text = "-"
        }
        binding.layoutCoffeeWaterRatio.visibility = View.VISIBLE

        val brewWeightVal = item.brewWeight?.toDouble() ?: 0.0
        if (coffeeVal > 0 && brewWeightVal > 0) {
            val ratioBrew = brewWeightVal / coffeeVal
            binding.tvCoffeeBrewRatio.text = String.format("1:%.1f", ratioBrew)
        } else {
            binding.tvCoffeeBrewRatio.text = "-"
        }
        binding.layoutCoffeeBrewRatio.visibility = View.VISIBLE

        if (!item.equipmentUsed.isNullOrEmpty()) {
            binding.labelEquipmentDetail.visibility = View.VISIBLE
            binding.recyclerRecipeDetailEquipment.visibility = View.VISIBLE
            binding.recyclerRecipeDetailEquipment.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerRecipeDetailEquipment.adapter = RecipeDetailEquipmentAdapter(item.equipmentUsed)
        } else {
            binding.labelEquipmentDetail.visibility = View.GONE
            binding.recyclerRecipeDetailEquipment.visibility = View.GONE
        }

        if (!item.date.isNullOrEmpty()) {
            binding.tvDetailDate.text = item.date
            binding.tvDetailDateLabel.visibility = View.VISIBLE
            binding.tvDetailDate.visibility = View.VISIBLE
        } else {
            binding.tvDetailDateLabel.visibility = View.GONE
            binding.tvDetailDate.visibility = View.GONE
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}