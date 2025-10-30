package com.example.ukopia.ui.recipe

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.data.RecipeItem
import com.example.ukopia.databinding.FragmentRecipeDetailBinding

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

        setupListeners()

        val recipeItem = arguments?.getParcelable<RecipeItem>("RECIPE_ITEM")

        recipeItem?.let {
            binding.tvHeaderTitle.text = it.name.uppercase()
            binding.tvRecipeTitle.text = it.name
            binding.tvRecipeDescription.text = it.description
            binding.tvWaterAmount.text = it.waterAmount
            binding.tvCoffeeAmount.text = it.coffeeAmount
            binding.tvGrindSize.text = it.grindSize
            binding.tvHeat.text = it.temperature
            binding.tvTime.text = it.extractionTime

            binding.tvBrewWeight.text = it.brewWeight ?: "?"
            binding.tvTds.text = it.tds ?: "?"
            binding.tvCoffeeBrewRatio.text = it.coffeeBrewRatio ?: "?"
            binding.tvCoffeeWaterRatio.text = it.coffeeWaterRatio ?: "?"

            binding.layoutBrewWeight.visibility = if (it.brewWeight.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.layoutTds.visibility = if (it.tds.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.layoutCoffeeBrewRatio.visibility = if (it.coffeeBrewRatio.isNullOrEmpty() || it.coffeeBrewRatio == "?") View.GONE else View.VISIBLE
            binding.layoutCoffeeWaterRatio.visibility = if (it.coffeeWaterRatio.isNullOrEmpty() || it.coffeeWaterRatio == "?") View.GONE else View.VISIBLE

            // Menampilkan daftar equipment (VERTICAL)
            it.equipmentUsed?.let { equipmentList ->
                if (equipmentList.isNotEmpty()) {
                    binding.labelEquipmentDetail.visibility = View.VISIBLE
                    binding.recyclerRecipeDetailEquipment.visibility = View.VISIBLE
                    // Pastikan ini VERTICAL
                    binding.recyclerRecipeDetailEquipment.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    binding.recyclerRecipeDetailEquipment.adapter = RecipeDetailEquipmentAdapter(equipmentList)
                } else {
                    binding.labelEquipmentDetail.visibility = View.GONE
                    binding.recyclerRecipeDetailEquipment.visibility = View.GONE
                }
            } ?: run {
                binding.labelEquipmentDetail.visibility = View.GONE
                binding.recyclerRecipeDetailEquipment.visibility = View.GONE
            }

            // Menampilkan Tanggal dan Catatan
            it.date?.let { date ->
                if (date.isNotEmpty()) {
                    binding.tvDetailDateLabel.visibility = View.VISIBLE
                    binding.tvDetailDate.visibility = View.VISIBLE
                    binding.tvDetailDate.text = date
                } else {
                    binding.tvDetailDateLabel.visibility = View.GONE
                    binding.tvDetailDate.visibility = View.GONE
                }
            } ?: run {
                binding.tvDetailDateLabel.visibility = View.GONE
                binding.tvDetailDate.visibility = View.GONE
            }

            it.notes?.let { notes ->
                if (notes.isNotEmpty()) {
                    binding.tvDetailNotesLabel.visibility = View.VISIBLE
                    binding.tvDetailNotes.visibility = View.VISIBLE
                    binding.tvDetailNotes.text = notes
                } else {
                    binding.tvDetailNotesLabel.visibility = View.GONE
                    binding.tvDetailNotes.visibility = View.GONE
                }
            } ?: run {
                binding.tvDetailNotesLabel.visibility = View.GONE
                binding.tvDetailNotes.visibility = View.GONE
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            val originalTintList = binding.btnBack.imageTintList
            binding.btnBack.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black))

            Handler(Looper.getMainLooper()).postDelayed({
                if (isAdded && activity != null) {
                    binding.btnBack.imageTintList = originalTintList
                    parentFragmentManager.popBackStack()
                    (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)
                }
            }, 150)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}