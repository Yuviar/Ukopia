package com.example.ukopia.ui.recipe

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import com.example.ukopia.R
import com.example.ukopia.data.RecipeItem
import com.example.ukopia.data.RecipeStep
import com.example.ukopia.databinding.FragmentAddRecipeBinding
import com.example.ukopia.databinding.LayoutRecipeStepItemInputBinding
import com.example.ukopia.MainActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.core.view.children
import androidx.core.view.forEach
import java.util.Locale

class AddRecipeFragment : Fragment() {

    private var _binding: FragmentAddRecipeBinding? = null
    private val binding get() = _binding!!

    private val recipeViewModel: RecipeViewModel by activityViewModels()
    private var methodName: String? = null
    private val selectedRecipeSteps = mutableListOf<RecipeStep>() // Untuk melacak langkah yang sudah ditambahkan

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

        // Listener untuk menerima langkah resep yang dipilih dari AddRecipeStepFragment (Fragment)
        // Baik langkah standar maupun kustom kini sepenuhnya dikonfigurasi di AddRecipeStepFragment.
        setFragmentResultListener(AddRecipeStepFragment.REQUEST_KEY_ADD_STEP) { _, bundle ->
            val selectedStep = bundle.getParcelable<RecipeStep>(AddRecipeStepFragment.BUNDLE_KEY_SELECTED_STEP)
            selectedStep?.let { step ->
                // Semua langkah (standar atau kustom) sekarang sudah dikonfigurasi sepenuhnya
                // oleh AddRecipeStepFragment. Langsung tambahkan ke tata letak.
                addStepToLayout(step)
                Toast.makeText(requireContext(), getString(R.string.step_added_successfully_toast, step.title), Toast.LENGTH_SHORT).show()
            }
        }

        // Listener untuk menerima langkah yang sudah dikonfigurasi dari ConfigureStepDialogFragment
        // Listener ini mungkin akan menjadi tidak terpakai untuk alur penambahan langkah awal
        // tetapi bisa tetap dipertahankan jika ConfigureStepDialogFragment digunakan untuk
        // mengedit langkah yang sudah ada (fitur yang belum ada di sini).
        setFragmentResultListener(ConfigureStepDialogFragment.REQUEST_KEY_CONFIGURE_STEP) { _, bundle ->
            val configuredStep = bundle.getParcelable<RecipeStep>(ConfigureStepDialogFragment.BUNDLE_KEY_CONFIGURED_STEP)
            configuredStep?.let { step ->
                addStepToLayout(step)
                Toast.makeText(requireContext(), getString(R.string.step_added_successfully_toast, step.title), Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setupListeners() {
        binding.buttonSimpanResep.setOnClickListener {
            saveRecipe()
        }

        binding.buttonAddNewStep.setOnClickListener {
            // Navigasi ke AddRecipeStepFragment untuk memilih langkah (fragment terpisah)
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, AddRecipeStepFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupSuffixFormatters() {
        // Ini hanya untuk input utama resep, bukan untuk step
        binding.editTextCoffeeAmount.addTextChangedListener(SuffixTextWatcher(binding.editTextCoffeeAmount, " g") { /* no-op */ })
        binding.editTextWaterAmount.addTextChangedListener(SuffixTextWatcher(binding.editTextWaterAmount, " ml") { /* no-op */ })
        binding.editTextHeat.addTextChangedListener(SuffixTextWatcher(binding.editTextHeat, "°C") { /* no-op */ })
    }

    private fun setupGrindSizeSpinner() {
        val grindSizes = resources.getStringArray(R.array.grind_sizes)
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_bold_left, grindSizes)
        binding.spinnerGrindSize.adapter = adapter
    }

    private fun addStepToLayout(step: RecipeStep) {
        // Periksa apakah ini langkah kustom yang sudah ada dengan ID yang sama
        // (ini mungkin hanya relevan jika ada fitur edit/update custom step di AddRecipeStepFragment
        // yang mengirimkan ID yang sama, untuk kasus add saat ini ID custom step selalu unik)
        val existingStepIndex = selectedRecipeSteps.indexOfFirst { it.id == step.id && it.id.startsWith("custom_step") }
        if (existingStepIndex != -1) {
            binding.containerRecipeSteps.removeViewAt(existingStepIndex)
            selectedRecipeSteps.removeAt(existingStepIndex)
        }

        selectedRecipeSteps.add(step)

        val stepViewBinding = LayoutRecipeStepItemInputBinding.inflate(LayoutInflater.from(context))
        stepViewBinding.textViewStepTitle.text = step.title
        stepViewBinding.imageViewStepIcon.setImageResource(step.iconResId)

        // Water Amount Input (Conditional) - TAMPILAN READ-ONLY
        val llWaterAmountInput = stepViewBinding.linearLayoutWaterAmountInput
        val etWaterAmount = stepViewBinding.editTextStepWaterAmount
        if ((step.id == "pour_water" || step.id == "bloom") && !step.currentWaterAmountInput.isNullOrEmpty()) {
            llWaterAmountInput.visibility = View.VISIBLE
            // Teks sudah ditambahkan dengan " ml" dari pembaruan sebelumnya
            etWaterAmount.setText("${step.currentWaterAmountInput} ml")
            etWaterAmount.isEnabled = false // read-only
            etWaterAmount.background = null // opsional: hilangkan border
        } else {
            llWaterAmountInput.visibility = View.GONE
        }

        // Duration Input (Always visible now) - TAMPILAN READ-ONLY
        val durationEditText = stepViewBinding.editTextStepDuration
        stepViewBinding.linearLayoutDurationInput.visibility = View.VISIBLE

        // Teks sudah ditambahkan dengan " s" dari pembaruan sebelumnya
        step.currentDurationInput?.let { durationEditText.setText("$it s") }
        durationEditText.isEnabled = false // read-only
        durationEditText.background = null // opsional: hilangkan border


        stepViewBinding.buttonRemoveStep.setOnClickListener {
            binding.containerRecipeSteps.removeView(stepViewBinding.root)
            selectedRecipeSteps.remove(step)
            updateTotalProcessTime()
        }

        binding.containerRecipeSteps.addView(stepViewBinding.root)
        updateTotalProcessTime()
    }

    // FUNGSI showCustomStepDialog() INI TELAH DIHAPUS DAN DIPINDAHKAN KE AddRecipeStepFragment.kt
    // Karena logikanya sudah dipindahkan, ini tidak lagi diperlukan di sini.

    private fun updateTotalProcessTime() {
        var totalSeconds = 0

        selectedRecipeSteps.forEach { step ->
            val durationValue = step.currentDurationInput?.toIntOrNull() ?: 0
            totalSeconds += durationValue
        }
        binding.editTextProcessTime.text = formatTime(totalSeconds)
    }

    private fun formatTime(totalSeconds: Int): String {
        if (totalSeconds < 0) return "00:00"
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun parseTime(timeString: String?): Int {
        return timeString?.toIntOrNull() ?: 0
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
        val heatStr = binding.editTextHeat.text.toString().removeSuffix("°C").trim()

        val grindSize = binding.spinnerGrindSize.selectedItem.toString()
        val totalProcessTime = binding.editTextProcessTime.text.toString().trim()

        var isValid = true
        if (name.isEmpty()) {
            binding.editTextRecipeName.error = getString(R.string.error_recipe_name_required)
            isValid = false
        }
        if (description.isEmpty()) {
            binding.editTextDescription.error = getString(R.string.error_description_required)
            isValid = false
        }
        if (coffeeAmountStr.isEmpty()) {
            binding.editTextCoffeeAmount.error = getString(R.string.error_coffee_amount_required)
            isValid = false
        }
        if (waterAmountStr.isEmpty()) {
            binding.editTextWaterAmount.error = getString(R.string.error_water_amount_required)
            isValid = false
        }
        if (heatStr.isEmpty()) {
            binding.editTextHeat.error = getString(R.string.error_temperature_required)
            isValid = false
        }

        val finalSteps = mutableListOf<String>()
        var allStepsAreValid = true

        selectedRecipeSteps.forEach { step ->
            val stepTitle = step.title
            val stepDescription = step.description
            val stepDuration = step.currentDurationInput?.trim() ?: ""
            val stepWaterAmount = step.currentWaterAmountInput?.trim() ?: ""

            // Validasi durasi setiap langkah
            if (stepDuration.isEmpty() || stepDuration.toIntOrNull() == null || stepDuration.toInt() <= 0) {
                allStepsAreValid = false
                // Lakukan scrolling ke langkah yang memiliki error jika diperlukan
            }

            // Validasi water amount untuk Pour Water dan Bloom
            if ((step.id == "pour_water" || step.id == "bloom") && (stepWaterAmount.isEmpty() || stepWaterAmount.toDoubleOrNull() == null || stepWaterAmount.toDouble() <= 0.0)) {
                allStepsAreValid = false
                // Lakukan scrolling ke langkah yang memiliki error jika diperlukan
            }

            // Bentuk string langkah untuk ditampilkan di RecipeDetailFragment
            var stepFormattedText = stepTitle
            if (step.id == "pour_water" || step.id == "bloom") {
                if (stepWaterAmount.isNotEmpty()) {
                    stepFormattedText += " (${stepWaterAmount}ml)"
                }
            }
            if (stepDuration.isNotEmpty()) {
                stepFormattedText += " (${stepDuration}s)"
            }
            if (!stepDescription.isNullOrEmpty()) {
                stepFormattedText += " - $stepDescription"
            }
            finalSteps.add(stepFormattedText)
        }

        if (finalSteps.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.error_minimum_one_step_required), Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (!allStepsAreValid || !isValid) {
            Toast.makeText(requireContext(), getString(R.string.error_complete_all_recipe_data), Toast.LENGTH_SHORT).show()
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
            heat = "$heatStr°C",
            time = totalProcessTime,
            isMine = true,
            steps = finalSteps
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

// =========================================================================
// SuffixTextWatcher yang diperbarui dengan perbaikan posisi kursor
// =========================================================================
class SuffixTextWatcher(
    private val editText: EditText,
    private val suffix: String,
    private val onRawValueChange: (String) -> Unit // Callback untuk nilai numerik mentah
) : TextWatcher {
    private var isUpdating = false
    private var currentRawValue = "" // Stores the numeric part without suffix

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
        if (isUpdating) return // Prevent infinite loop

        isUpdating = true // Start updating flag

        val newText = s.toString()
        val oldSelection = editText.selectionStart // Simpan posisi kursor sebelum perubahan

        // Hapus suffix untuk mendapatkan nilai mentah
        val rawValueInput = newText.removeSuffix(suffix).trim()

        val numberRegex = "^\\d*\\.?\\d*$".toRegex() // Regex untuk angka (integer atau desimal)

        if (rawValueInput.matches(numberRegex)) {
            // Input valid: update currentRawValue dan format teks
            currentRawValue = rawValueInput
            val formattedText = if (currentRawValue.isNotEmpty()) "$currentRawValue$suffix" else ""

            if (editText.text.toString() != formattedText) {
                editText.setText(formattedText)

                // Hitung posisi kursor baru
                // Posisi kursor harus berada di dalam bagian numerik yang valid.
                // oldSelection - (panjang_suffix_sebelumnya - panjang_suffix_sekarang)
                // Jika suffix tidak berubah, oldSelection sudah relatif ke bagian numerik
                val newSelection = (oldSelection - (newText.length - formattedText.length))
                    .coerceAtLeast(0)
                    .coerceAtMost(currentRawValue.length) // Pastikan tidak melebihi panjang raw value

                editText.setSelection(newSelection)
            }
        } else {
            // Input tidak valid: kembalikan ke nilai terakhir yang valid + suffix
            val formattedText = if (currentRawValue.isNotEmpty()) "$currentRawValue$suffix" else ""
            if (editText.text.toString() != formattedText) {
                editText.setText(formattedText)
                // Kursor di akhir bagian angka yang valid
                val newSelection = currentRawValue.length
                    .coerceAtLeast(0)
                    .coerceAtMost(formattedText.length - suffix.length.coerceAtMost(formattedText.length))
                editText.setSelection(newSelection)
            }
        }

        onRawValueChange(currentRawValue) // Notifikasi perubahan nilai mentah

        isUpdating = false // End updating flag
    }
}