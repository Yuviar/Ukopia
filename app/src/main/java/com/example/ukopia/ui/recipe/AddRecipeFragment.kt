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
import com.example.ukopia.SessionManager
import com.example.ukopia.data.CreateRecipeRequest
import com.example.ukopia.data.SubEquipmentItem
import com.example.ukopia.databinding.FragmentAddRecipeBinding
import com.example.ukopia.ui.equipment.EquipmentFragment
import com.example.ukopia.ui.loyalty.CustomDatePickerDialogFragment
import java.util.*

class AddRecipeFragment : Fragment() {

    private var _binding: FragmentAddRecipeBinding? = null
    private val binding get() = _binding!!

    private val recipeViewModel: RecipeViewModel by activityViewModels()

    // Data dari Fragment Sebelumnya
    private var methodName: String? = null
    private var methodId: Int = 0 // ID Metode (Wajib untuk API)

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

        // Ambil Argument dari RecipeListFragment
        methodName = arguments?.getString("METHOD_NAME")
        methodId = arguments?.getInt("ID_METODE") ?: 0

        setupListeners()
        setupGrindSizeSpinner()
        setupSuffixFormatters()
        setupDatePicker()
        setupEquipmentResultListener()
        setupSelectedEquipmentRecyclerView()

        updateRatios()
        updateSelectedEquipmentDisplay()

        // Observe status loading (Opsional: Tambahkan ProgressBar di layout)
        recipeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.buttonSimpanResep.isEnabled = !isLoading
            binding.buttonSimpanResep.text = if (isLoading) "Menyimpan..." else getString(R.string.add_recipe_button_text)
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

        binding.buttonSimpanResep.setOnClickListener {
            saveRecipeToApi() // Panggil fungsi simpan ke API
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
            selectedEquipmentList.remove(itemToDelete)
            selectedEquipmentAdapter.notifyDataSetChanged()
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
        binding.editTextCoffeeAmount.addTextChangedListener(SuffixTextWatcher(binding.editTextCoffeeAmount, " g") { updateRatios() })
        binding.editTextWaterAmount.addTextChangedListener(SuffixTextWatcher(binding.editTextWaterAmount, " ml") { updateRatios() })
        binding.editTextHeat.addTextChangedListener(SuffixTextWatcher(binding.editTextHeat, "°C") {})
        binding.editTextBrewWeight.addTextChangedListener(SuffixTextWatcher(binding.editTextBrewWeight, " g") { updateRatios() })
        binding.editTextTds.addTextChangedListener(SuffixTextWatcher(binding.editTextTds, " %") {})
        binding.editTextExtractionTime.addTextChangedListener(SuffixTextWatcher(binding.editTextExtractionTime, " s") {})
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
            val category = bundle.getString(EquipmentFragment.BUNDLE_KEY_SELECTED_CATEGORY) ?: ""
            val nameWithDetail = bundle.getString(EquipmentFragment.BUNDLE_KEY_SELECTED_SUB_EQUIPMENT_NAME) ?: ""

            // [PERUBAHAN] Menerima ID dan ImageUrl dari EquipmentFragment (Nanti disesuaikan di tahap selanjutnya)
            val equipId = bundle.getInt("BUNDLE_KEY_EQUIPMENT_ID", 0)
            val imageUrl = bundle.getString("BUNDLE_KEY_EQUIPMENT_IMAGE_URL") ?: ""

            if (equipId != 0) {
                val newEquipment = SubEquipmentItem(
                    id = equipId,
                    category = category,
                    name = nameWithDetail,
                    imageUrl = imageUrl,
                    detail = null
                )

                selectedEquipmentList.add(newEquipment)
                selectedEquipmentAdapter.notifyItemInserted(selectedEquipmentList.size - 1)
                updateSelectedEquipmentDisplay()
            }
        }
    }

    private fun updateSelectedEquipmentDisplay() {
        binding.recyclerSelectedEquipment.visibility = if (selectedEquipmentList.isNotEmpty()) View.VISIBLE else View.GONE
        binding.fabAddEquipmentRecipe.visibility = View.VISIBLE
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

    // --- FUNGSI UTAMA: SIMPAN KE API ---
    private fun saveRecipeToApi() {
        // 1. Ambil UID dari Session (Wajib Login)
        val uid = SessionManager.getUid(requireContext())

        // 2. Cek Data Wajib
        if (methodId == 0 || methodName == null) {
            Toast.makeText(requireContext(), getString(R.string.error_brewing_method_unknown), Toast.LENGTH_SHORT).show()
            return
        }
        if (uid == 0) {
            Toast.makeText(requireContext(), "Sesi berakhir, silakan login ulang.", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Ambil Nilai Inputan
        val name = binding.editTextRecipeName.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val coffeeStr = binding.editTextCoffeeAmount.text.toString().removeSuffix(" g").trim()
        val waterStr = binding.editTextWaterAmount.text.toString().removeSuffix(" ml").trim()
        val tempStr = binding.editTextHeat.text.toString().removeSuffix("°C").trim()
        val grind = binding.spinnerGrindSize.selectedItem.toString()
        val brewWeightStr = binding.editTextBrewWeight.text.toString().removeSuffix(" g").trim()
        val tdsStr = binding.editTextTds.text.toString().removeSuffix(" %").trim()
        val timeStr = binding.editTextExtractionTime.text.toString().removeSuffix(" s").trim()
        val dateStr = binding.editTextDate.text.toString()

        // 4. Validasi Input (Sama seperti sebelumnya)
        var isValid = true
        var firstErrorView: View? = null

        if (name.isEmpty()) { binding.editTextRecipeName.error = getString(R.string.error_recipe_name_required); isValid = false; firstErrorView = binding.editTextRecipeName }
        if (description.isEmpty()) { binding.editTextDescription.error = getString(R.string.error_description_required); isValid = false; if(firstErrorView==null) firstErrorView = binding.editTextDescription }
        if (coffeeStr.isEmpty()) { binding.editTextCoffeeAmount.error = getString(R.string.error_coffee_amount_required); isValid = false; if(firstErrorView==null) firstErrorView = binding.editTextCoffeeAmount }
        if (waterStr.isEmpty()) { binding.editTextWaterAmount.error = getString(R.string.error_water_amount_required); isValid = false; if(firstErrorView==null) firstErrorView = binding.editTextWaterAmount }
        if (tempStr.isEmpty()) { binding.editTextHeat.error = getString(R.string.error_temperature_required); isValid = false; if(firstErrorView==null) firstErrorView = binding.editTextHeat }
        if (timeStr.isEmpty()) { binding.editTextExtractionTime.error = getString(R.string.error_extraction_time_required); isValid = false; if(firstErrorView==null) firstErrorView = binding.editTextExtractionTime }
        if (dateStr.isEmpty()) { binding.editTextDate.error = getString(R.string.error_date_required); isValid = false; if(firstErrorView==null) firstErrorView = binding.editTextDate }
        if (selectedEquipmentList.isEmpty()) { Toast.makeText(requireContext(), getString(R.string.error_equipment_required), Toast.LENGTH_SHORT).show(); isValid = false }

        if (!isValid) {
            Toast.makeText(requireContext(), getString(R.string.error_complete_all_recipe_data), Toast.LENGTH_SHORT).show()
            firstErrorView?.requestFocus()
            return
        }

        // 5. Buat Object Request
        val request = CreateRecipeRequest(
            uid = uid,
            methodId = methodId,
            name = name,
            description = description,
            coffee = coffeeStr.toIntOrNull() ?: 0,
            water = waterStr.toIntOrNull() ?: 0,
            temp = tempStr.toIntOrNull() ?: 90,
            grindSize = grind,
            time = timeStr.toIntOrNull() ?: 0,
            weight = brewWeightStr.toIntOrNull() ?: 0,
            tds = tdsStr.toIntOrNull() ?: 0, // Database pakai INT
            equipmentIds = selectedEquipmentList.map { it.id } // Kirim Array ID Alat
        )

        // 6. Kirim ke ViewModel
        recipeViewModel.createRecipe(
            request = request,
            onSuccess = {
                Toast.makeText(requireContext(), getString(R.string.recipe_saved_success_toast, name), Toast.LENGTH_SHORT).show()

                // Kembali ke list dan refresh otomatis (karena data diambil dari API/DB)
                parentFragmentManager.popBackStack()
                (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)
            },
            onError = { msg ->
                Toast.makeText(requireContext(), "Gagal: $msg", Toast.LENGTH_LONG).show()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Class SuffixTextWatcher tetap sama
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
        // Regex untuk angka (termasuk desimal jika perlu, tapi di API kita pakai int, jadi bisa disesuaikan)
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
            // Revert jika input invalid
            val formattedText = if (currentRawValue.isNotEmpty()) "$currentRawValue$suffix" else ""
            if (editText.text.toString() != formattedText) {
                editText.setText(formattedText)
                val newSelection = currentRawValue.length
            }
        }
        onRawValueChange(currentRawValue)
        isUpdating = false
    }
}