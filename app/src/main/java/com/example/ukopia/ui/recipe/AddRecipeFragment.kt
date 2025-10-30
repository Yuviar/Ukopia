package com.example.ukopia.ui.recipe

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.data.RecipeItem
import com.example.ukopia.data.SubEquipmentItem
import com.example.ukopia.databinding.FragmentAddRecipeBinding
import com.example.ukopia.ui.loyalty.CustomDatePickerDialogFragment
import com.example.ukopia.ui.equipment.EquipmentFragment
import java.util.*

class AddRecipeFragment : Fragment() {

    private var _binding: FragmentAddRecipeBinding? = null
    private val binding get() = _binding!!

    private val recipeViewModel: RecipeViewModel by activityViewModels()
    private var methodName: String? = null

    private val selectedEquipmentList = mutableListOf<SubEquipmentItem>()
    private lateinit var selectedEquipmentAdapter: SelectedEquipmentAdapter

    companion object {
        const val ADD_RECIPE_FLOW_TAG = "add_recipe_equipment_selection_flow"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).setBottomNavVisibility(View.GONE)

        methodName = arguments?.getString("METHOD_NAME")

        setupListeners()
        setupGrindSizeSpinner()
        setupSuffixFormatters()
        setupDatePicker()
        setupEquipmentResultListener()
        setupSelectedEquipmentRecyclerView()

        updateRatios()

        updateSelectedEquipmentDisplay()
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

        binding.buttonSimpanResep.setOnClickListener {
            saveRecipe()
        }

        binding.fabAddEquipmentRecipe.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, EquipmentFragment.newInstance())
                .addToBackStack(ADD_RECIPE_FLOW_TAG)
                .commit()
            (requireActivity() as MainActivity).setBottomNavVisibility(View.GONE)
        }
    }

    private fun setupSelectedEquipmentRecyclerView() {
        selectedEquipmentAdapter = SelectedEquipmentAdapter(selectedEquipmentList) { itemToDelete ->
            selectedEquipmentAdapter.removeItem(itemToDelete)
            updateSelectedEquipmentDisplay()
            Toast.makeText(requireContext(), "Removed: ${itemToDelete.name}", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerSelectedEquipment.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerSelectedEquipment.adapter = selectedEquipmentAdapter
    }


    private fun setupGrindSizeSpinner() {
        val grindSizes = resources.getStringArray(R.array.grind_sizes)
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_bold_left, grindSizes)
        binding.spinnerGrindSize.adapter = adapter
    }

    private fun setupSuffixFormatters() {
        binding.editTextCoffeeAmount.addTextChangedListener(SuffixTextWatcher(binding.editTextCoffeeAmount, " g") { rawValue ->
            updateRatios()
        })
        binding.editTextWaterAmount.addTextChangedListener(SuffixTextWatcher(binding.editTextWaterAmount, " ml") { rawValue ->
            updateRatios()
        })
        binding.editTextHeat.addTextChangedListener(SuffixTextWatcher(binding.editTextHeat, "°C") { /* no-op */ })
        binding.editTextBrewWeight.addTextChangedListener(SuffixTextWatcher(binding.editTextBrewWeight, " g") { rawValue ->
            updateRatios()
        })
        binding.editTextTds.addTextChangedListener(SuffixTextWatcher(binding.editTextTds, " %") { /* no-op */ })
        binding.editTextExtractionTime.addTextChangedListener(SuffixTextWatcher(binding.editTextExtractionTime, " s") { /* no-op */ })
    }

    private fun setupDatePicker() {
        childFragmentManager.setFragmentResultListener(
            CustomDatePickerDialogFragment.REQUEST_KEY_DATE_PICKER,
            viewLifecycleOwner
        ) { _, bundle ->
            val selectedDate = bundle.getString(CustomDatePickerDialogFragment.BUNDLE_KEY_SELECTED_DATE)
            selectedDate?.let {
                binding.editTextDate.setText(it)
            }
        }

        binding.editTextDate.setOnClickListener {
            showCustomDatePickerDialog()
        }
    }

    private fun showCustomDatePickerDialog() {
        val initialDate = binding.editTextDate.text.toString().ifEmpty { null }
        val customDatePickerDialog = CustomDatePickerDialogFragment.newInstance(initialDate)
        customDatePickerDialog.show(childFragmentManager, "CUSTOM_DATE_PICKER")
    }

    private fun setupEquipmentResultListener() {
        setFragmentResultListener(EquipmentFragment.REQUEST_KEY_EQUIPMENT_SELECTION) { _, bundle ->
            val category = bundle.getString(EquipmentFragment.BUNDLE_KEY_SELECTED_CATEGORY)
            val nameWithDetail = bundle.getString(EquipmentFragment.BUNDLE_KEY_SELECTED_SUB_EQUIPMENT_NAME)
            val iconResId = bundle.getInt(EquipmentFragment.BUNDLE_KEY_SELECTED_SUB_EQUIPMENT_ICON, 0)

            if (category != null && nameWithDetail != null && iconResId != 0) {
                val newEquipment = SubEquipmentItem(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    name = nameWithDetail,
                    detail = null,
                    iconResId = iconResId
                )

                selectedEquipmentList.add(newEquipment)
                selectedEquipmentAdapter.notifyItemInserted(selectedEquipmentList.size - 1)
                updateSelectedEquipmentDisplay()
            }
        }
    }

    private fun updateSelectedEquipmentDisplay() {
        if (selectedEquipmentList.isNotEmpty()) {
            binding.recyclerSelectedEquipment.visibility = View.VISIBLE
        } else {
            binding.recyclerSelectedEquipment.visibility = View.GONE
        }
        binding.fabAddEquipmentRecipe.visibility = View.VISIBLE
    }


    private fun clearSelectedEquipment() {
        selectedEquipmentList.clear()
        selectedEquipmentAdapter.notifyDataSetChanged()
        updateSelectedEquipmentDisplay()
        Toast.makeText(requireContext(), "All equipment cleared", Toast.LENGTH_SHORT).show()
    }

    private fun updateRatios() {
        val coffeeAmount = binding.editTextCoffeeAmount.text.toString().removeSuffix(" g").trim().toDoubleOrNull()
        val waterAmount = binding.editTextWaterAmount.text.toString().removeSuffix(" ml").trim().toDoubleOrNull()
        val brewWeight = binding.editTextBrewWeight.text.toString().removeSuffix(" g").trim().toDoubleOrNull()

        if (coffeeAmount != null && brewWeight != null && brewWeight != 0.0) {
            val ratio = coffeeAmount / brewWeight
            binding.tvCoffeeBrewRatio.text = String.format(Locale.getDefault(), "1:%.2f", 1.0 / ratio)
        } else {
            binding.tvCoffeeBrewRatio.text = "?"
        }

        if (coffeeAmount != null && waterAmount != null && waterAmount != 0.0) {
            val ratio = coffeeAmount / waterAmount
            binding.tvCoffeeWaterRatio.text = String.format(Locale.getDefault(), "1:%.2f", 1.0 / ratio)
        } else {
            binding.tvCoffeeWaterRatio.text = "?"
        }
    }

    private fun saveRecipe() {
        if (methodName == null) {
            Toast.makeText(requireContext(), getString(R.string.error_brewing_method_unknown), Toast.LENGTH_SHORT).show()
            return
        }

        val name = binding.editTextRecipeName.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val coffeeAmountStr = binding.editTextCoffeeAmount.text.toString().removeSuffix(" g").trim()
        val waterAmountStr = binding.editTextWaterAmount.text.toString().removeSuffix(" ml").trim()
        val temperatureStr = binding.editTextHeat.text.toString().removeSuffix("°C").trim()
        val grindSize = binding.spinnerGrindSize.selectedItem.toString()
        val brewWeightStr = binding.editTextBrewWeight.text.toString().removeSuffix(" g").trim()
        val tdsStr = binding.editTextTds.text.toString().removeSuffix(" %").trim()
        val extractionTimeStr = binding.editTextExtractionTime.text.toString().removeSuffix(" s").trim()
        val coffeeBrewRatio = binding.tvCoffeeBrewRatio.text.toString()
        val coffeeWaterRatio = binding.tvCoffeeWaterRatio.text.toString()

        val dateStr = binding.editTextDate.text.toString()
        val notesStr = binding.editTextCatatan.text.toString()


        var isValid = true
        var firstErrorView: View? = null // Untuk fokus ke view pertama yang error

        // --- Validasi Kolom Wajib ---
        if (name.isEmpty()) {
            binding.editTextRecipeName.error = getString(R.string.error_recipe_name_required)
            if (firstErrorView == null) firstErrorView = binding.editTextRecipeName
            isValid = false
        }
        if (description.isEmpty()) {
            binding.editTextDescription.error = getString(R.string.error_description_required)
            if (firstErrorView == null) firstErrorView = binding.editTextDescription
            isValid = false
        }
        if (coffeeAmountStr.isEmpty()) {
            binding.editTextCoffeeAmount.error = getString(R.string.error_coffee_amount_required)
            if (firstErrorView == null) firstErrorView = binding.editTextCoffeeAmount
            isValid = false
        }
        if (waterAmountStr.isEmpty()) {
            binding.editTextWaterAmount.error = getString(R.string.error_water_amount_required)
            if (firstErrorView == null) firstErrorView = binding.editTextWaterAmount
            isValid = false
        }
        if (temperatureStr.isEmpty()) {
            binding.editTextHeat.error = getString(R.string.error_temperature_required)
            if (firstErrorView == null) firstErrorView = binding.editTextHeat
            isValid = false
        }
        if (extractionTimeStr.isEmpty()) {
            binding.editTextExtractionTime.error = getString(R.string.error_extraction_time_required) // Pastikan string ini ada
            if (firstErrorView == null) firstErrorView = binding.editTextExtractionTime
            isValid = false
        }
        if (dateStr.isEmpty()) {
            binding.editTextDate.error = getString(R.string.error_date_required)
            if (firstErrorView == null) firstErrorView = binding.editTextDate
            isValid = false
        }
        // Validasi Equipment List (minimal 1)
        if (selectedEquipmentList.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.error_equipment_required), Toast.LENGTH_SHORT).show() // Pastikan string ini ada
            // Tidak bisa memberi error pada FAB, jadi beri toast dan fokus ke area terdekat
            if (firstErrorView == null) firstErrorView = binding.fabAddEquipmentRecipe
            isValid = false
        }
        // --- Akhir Validasi Kolom Wajib ---

        if (!isValid) {
            Toast.makeText(requireContext(), getString(R.string.error_complete_all_recipe_data), Toast.LENGTH_SHORT).show()
            firstErrorView?.requestFocus()
            binding.layoutAddRecipe.post {
                if (firstErrorView != null) {
                    binding.layoutAddRecipe.smoothScrollTo(0, firstErrorView.top)
                }
            }
            return
        }

        val newRecipe = RecipeItem(
            id = System.currentTimeMillis().toString(),
            method = methodName!!,
            name = name,
            description = description,
            waterAmount = "$waterAmountStr ml",
            coffeeAmount = "$coffeeAmountStr g",
            grindSize = grindSize,
            temperature = "$temperatureStr°C",
            extractionTime = "$extractionTimeStr s",
            isMine = true,
            steps = listOf(),

            brewWeight = if (brewWeightStr.isNotEmpty()) "$brewWeightStr g" else null,
            tds = if (tdsStr.isNotEmpty()) "$tdsStr %" else null,
            coffeeBrewRatio = if (coffeeBrewRatio != "?") coffeeBrewRatio else null,
            coffeeWaterRatio = if (coffeeWaterRatio != "?") coffeeWaterRatio else null,
            date = dateStr,
            notes = notesStr.ifEmpty { null },
            equipmentUsed = selectedEquipmentList.toList()
        )

        recipeViewModel.addRecipe(newRecipe)
        Toast.makeText(requireContext(), getString(R.string.recipe_saved_success_toast, name), Toast.LENGTH_SHORT).show()

        val listFragment = RecipeListFragment().apply {
            arguments = Bundle().apply {
                putString("SELECTED_METHOD_NAME", methodName)
                putBoolean("SHOW_MY_RECIPES", true)
                putString("SPECIFIC_RECIPE_TITLE", newRecipe.name)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, listFragment)
            .commit()
        (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// SuffixTextWatcher class (tetap sama dan di sini)
class SuffixTextWatcher(
    private val editText: EditText,
    private val suffix: String,
    private val onRawValueChange: (String) -> Unit
) : TextWatcher {
    private var isUpdating = false
    private var currentRawValue = ""

    init {
        val textWithoutSuffix = editText.text.toString().removeSuffix(suffix).trim()
        if (textWithoutSuffix.isNotEmpty()) {
            currentRawValue = textWithoutSuffix
            onRawValueChange(currentRawValue)
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (isUpdating) return
        isUpdating = true

        val newText = s.toString()
        val oldSelection = editText.selectionStart
        val rawValueInput = newText.removeSuffix(suffix).trim()
        val numberRegex = "^\\d*\\.?\\d*$".toRegex()

        if (rawValueInput.matches(numberRegex)) {
            currentRawValue = rawValueInput
            val formattedText = if (currentRawValue.isNotEmpty()) "$currentRawValue$suffix" else ""
            if (editText.text.toString() != formattedText) {
                editText.setText(formattedText)
                val newSelection = (oldSelection - (newText.length - formattedText.length))
                    .coerceAtLeast(0)
                    .coerceAtMost(currentRawValue.length)
                editText.setSelection(newSelection)
            }
        } else {
            val formattedText = if (currentRawValue.isNotEmpty()) "$currentRawValue$suffix" else ""
            if (editText.text.toString() != formattedText) {
                editText.setText(formattedText)
                val newSelection = currentRawValue.length
                    .coerceAtLeast(0)
                    .coerceAtMost(formattedText.length - suffix.length.coerceAtMost(formattedText.length))
                editText.setSelection(newSelection)
            }
        }
        onRawValueChange(currentRawValue)
        isUpdating = false
    }
}