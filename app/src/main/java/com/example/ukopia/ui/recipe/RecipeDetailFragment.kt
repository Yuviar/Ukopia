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

        // 1. Tampilkan Data Dasar (dari List) agar user tidak menunggu kosong
        val basicRecipe = arguments?.getParcelable<RecipeItem>("RECIPE_ITEM")
        if (basicRecipe != null) {
            bindBasicData(basicRecipe)
        }

        // 2. Ambil Data Lengkap dari Server
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

        // Set Data Dasar
        binding.tvWaterAmount.text = item.waterAmount
        binding.tvCoffeeAmount.text = item.coffeeAmount
        binding.tvTime.text = item.extractionTime
    }

    private fun bindFullData(item: RecipeItem) {
        // Update Data Dasar (Pastikan data terbaru tampil)
        binding.tvWaterAmount.text = item.waterAmount
        binding.tvCoffeeAmount.text = item.coffeeAmount
        binding.tvTime.text = item.extractionTime

        // Update Data Detail
        binding.tvGrindSize.text = item.grindSize ?: "-"
        binding.tvHeat.text = item.temperature
        binding.tvBrewWeight.text = item.brewWeightText
        binding.tvTds.text = item.tdsText

        // --- PERHITUNGAN RATIO ---

        val coffeeVal = item.coffeeAmountInt.toDouble()

        // 1. WATER RATIO (Kopi : Jumlah Air)
        // Rumus: Air / Kopi
        val waterVal = item.waterAmountInt.toDouble()
        if (coffeeVal > 0 && waterVal > 0) {
            val ratioWater = waterVal / coffeeVal
            // Format 1 angka belakang koma, misal: 1:16.7
            binding.tvCoffeeWaterRatio.text = String.format("1:%.1f", ratioWater)
        } else {
            binding.tvCoffeeWaterRatio.text = "-"
        }
        binding.layoutCoffeeWaterRatio.visibility = View.VISIBLE

        // 2. BREW RATIO (Kopi : Berat Minuman)
        // Rumus: Berat Minuman / Kopi
        val brewWeightVal = item.brewWeight?.toDouble() ?: 0.0
        if (coffeeVal > 0 && brewWeightVal > 0) {
            val ratioBrew = brewWeightVal / coffeeVal
            // Format 1 angka belakang koma, misal: 1:14.5
            binding.tvCoffeeBrewRatio.text = String.format("1:%.1f", ratioBrew)
        } else {
            // Jika berat minuman tidak diisi (0), tampilkan strip
            binding.tvCoffeeBrewRatio.text = "-"
        }
        binding.layoutCoffeeBrewRatio.visibility = View.VISIBLE

        // --- END PERHITUNGAN ---

        // Bind Alat (Equipment)
        if (!item.equipmentUsed.isNullOrEmpty()) {
            binding.labelEquipmentDetail.visibility = View.VISIBLE
            binding.recyclerRecipeDetailEquipment.visibility = View.VISIBLE
            binding.recyclerRecipeDetailEquipment.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerRecipeDetailEquipment.adapter = RecipeDetailEquipmentAdapter(item.equipmentUsed)
        } else {
            binding.labelEquipmentDetail.visibility = View.GONE
            binding.recyclerRecipeDetailEquipment.visibility = View.GONE
        }

        // Tanggal & Notes
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