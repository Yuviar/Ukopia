package com.example.ukopia.ui.recipe

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.adapter.RecipeStepAdapter
import com.example.ukopia.data.RecipeStep
import com.example.ukopia.databinding.FragmentAddRecipeStepBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

class AddRecipeStepFragment : Fragment() {

    private var _binding: FragmentAddRecipeStepBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecipeStepAdapter

    companion object {
        const val REQUEST_KEY_ADD_STEP = "add_step_request_key"
        const val BUNDLE_KEY_SELECTED_STEP = "selected_step_bundle_key"

        fun newInstance(): AddRecipeStepFragment {
            return AddRecipeStepFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRecipeStepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).setBottomNavVisibility(View.GONE)

        adapter = RecipeStepAdapter { selectedStep ->
            // Untuk custom_step, tampilkan dialog konfigurasi khusus di fragment ini
            if (selectedStep.id == "custom_step") {
                showCustomStepConfigurationDialog(selectedStep)
            } else {
                // Tampilkan dialog konfigurasi untuk langkah non-custom
                showConfigureStepDialog(selectedStep)
            }
        }

        binding.recyclerViewRecipeSteps.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewRecipeSteps.adapter = adapter

        adapter.submitList(createRecipeSteps())
    }

    private fun showConfigureStepDialog(step: RecipeStep) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_configure_recipe_step, null)
        val etDescription: EditText = dialogView.findViewById(R.id.editTextStepDescription)
        val llDurationInput: LinearLayout = dialogView.findViewById(R.id.linearLayoutDurationInput)
        val etDuration: EditText = dialogView.findViewById(R.id.editTextStepDuration)
        val labelWaterAmount: TextView = dialogView.findViewById(R.id.labelWaterAmount)
        val etWaterAmount: EditText = dialogView.findViewById(R.id.editTextStepWaterAmount)

        var configuredDuration: String? = null
        var configuredDescription: String? = null
        var configuredWaterAmount: String? = null

        // Pre-fill existing description if any
        step.description?.let {
            etDescription.setText(it)
            configuredDescription = it
        }

        if (step.id == "pour_water" || step.id == "bloom") {
            labelWaterAmount.visibility = View.VISIBLE
            etWaterAmount.visibility = View.VISIBLE
            // Pre-fill existing water amount
            step.currentWaterAmountInput?.let {
                etWaterAmount.setText(it)
                configuredWaterAmount = it
            }
            etWaterAmount.addTextChangedListener(SuffixTextWatcher(etWaterAmount, " ml") { rawValue ->
                configuredWaterAmount = rawValue
            })
        } else {
            labelWaterAmount.visibility = View.GONE
            etWaterAmount.visibility = View.GONE
        }

        llDurationInput.visibility = View.VISIBLE
        // Pre-fill existing duration
        step.currentDurationInput?.let {
            etDuration.setText(it)
            configuredDuration = it
        }

        etDuration.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        etDuration.hint = getString(R.string.duration_hint_seconds) // Use new string resource

        etDuration.addTextChangedListener(SuffixTextWatcher(etDuration, " s") { rawValue ->
            configuredDuration = rawValue
        })

        etDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                configuredDescription = s?.toString()?.trim()
            }
        })

        MaterialAlertDialogBuilder(requireContext(), R.style.FullScreenDialogTheme)
            .setTitle(step.title) // Dialog title from step name
            .setView(dialogView)
            .setPositiveButton(getString(R.string.add_button_text)) { dialog, _ ->
                val finalDescription = configuredDescription?.takeIf { it.isNotBlank() }
                val finalDuration = configuredDuration // This is already the raw numeric string
                val finalWaterAmount = configuredWaterAmount // Water amount dikonfigurasi di sini

                // Validate duration
                if (finalDuration.isNullOrEmpty() || finalDuration.toIntOrNull() == null || finalDuration.toInt() <= 0) { // Duration must be > 0
                    Toast.makeText(requireContext(), getString(R.string.error_step_duration_required), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton // Do not dismiss dialog if there's an error
                }

                // Validate water amount if it's a pour/bloom step
                if ((step.id == "pour_water" || step.id == "bloom") && (finalWaterAmount.isNullOrEmpty() || finalWaterAmount.toDoubleOrNull() == null || finalWaterAmount.toDouble() <= 0.0)) {
                    Toast.makeText(requireContext(), getString(R.string.error_water_amount_required), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val resultStep = step.copy(
                    description = finalDescription,
                    currentDurationInput = finalDuration, // Duration must always be present (as raw number string)
                    currentWaterAmountInput = finalWaterAmount // Water amount dikonfigurasi di sini
                )

                // Send result back to AddRecipeFragment
                setFragmentResult(REQUEST_KEY_ADD_STEP, Bundle().apply {
                    putParcelable(BUNDLE_KEY_SELECTED_STEP, resultStep)
                })
                parentFragmentManager.popBackStack() // Go back to AddRecipeFragment
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel_button_text)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showCustomStepConfigurationDialog(step: RecipeStep) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_custom_step, null)
        val etCustomStepTitle: EditText = dialogView.findViewById(R.id.editTextCustomStepTitle)
        val etCustomStepDuration: EditText = dialogView.findViewById(R.id.editTextCustomStepDuration)

        var customStepDurationValue: String? = null

        // Pre-fill if editing an existing custom step (not applicable for initial add, but good practice)
        // Check if the title is different from the default "Langkah Kustom" to pre-fill
        step.title.takeIf { it != getString(R.string.step_custom_step) }?.let {
            etCustomStepTitle.setText(it)
        }
        step.currentDurationInput?.let {
            etCustomStepDuration.setText(it)
            customStepDurationValue = it
        }

        etCustomStepDuration.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        etCustomStepDuration.hint = getString(R.string.duration_hint_seconds)

        etCustomStepDuration.addTextChangedListener(SuffixTextWatcher(etCustomStepDuration, " s") { rawValue ->
            customStepDurationValue = rawValue
        })

        MaterialAlertDialogBuilder(requireContext(), R.style.FullScreenDialogTheme)
            .setTitle(getString(R.string.custom_step_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.add_button_text)) { dialog, _ ->
                val customTitle = etCustomStepTitle.text.toString().trim()
                val customDuration = customStepDurationValue

                if (customTitle.isEmpty()) {
                    Toast.makeText(requireContext(), getString(R.string.error_custom_step_title_required), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (customDuration.isNullOrEmpty() || customDuration.toIntOrNull() == null || customDuration.toInt() <= 0) {
                    Toast.makeText(requireContext(), getString(R.string.error_step_duration_required), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val customStepResult = RecipeStep(
                    id = "custom_step_${System.currentTimeMillis()}", // Beri ID unik untuk setiap langkah kustom baru
                    title = customTitle,
                    description = null, // Langkah kustom saat ini tidak memiliki deskripsi dalam alur ini
                    iconResId = R.drawable.ic_custom_step,
                    hasDuration = true,
                    currentDurationInput = customDuration
                )

                setFragmentResult(REQUEST_KEY_ADD_STEP, Bundle().apply {
                    putParcelable(BUNDLE_KEY_SELECTED_STEP, customStepResult)
                })
                parentFragmentManager.popBackStack()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel_button_text)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun createRecipeSteps(): List<RecipeStep> {
        return listOf(
            // NOTE: Semua langkah sekarang memiliki hasDuration = true
            RecipeStep("pour_water", getString(R.string.step_pour_water), getString(R.string.desc_pour_water), R.drawable.ic_pour_water, hasDuration = true),
            RecipeStep("bloom", getString(R.string.step_bloom), getString(R.string.desc_bloom), R.drawable.ic_bloom, hasDuration = true),
            RecipeStep("wait", getString(R.string.step_wait), getString(R.string.desc_wait), R.drawable.ic_wait, hasDuration = true),
            RecipeStep("swirl", getString(R.string.step_swirl), getString(R.string.desc_swirl), R.drawable.ic_swirl, hasDuration = true),
            RecipeStep("stir", getString(R.string.step_stir), getString(R.string.desc_stir), R.drawable.ic_stir, hasDuration = true),
            RecipeStep("press", getString(R.string.step_press), getString(R.string.desc_press), R.drawable.ic_press, hasDuration = true),
            RecipeStep("invert", getString(R.string.step_invert), getString(R.string.desc_invert), R.drawable.ic_invert, hasDuration = true),
            RecipeStep("place_plunger", getString(R.string.step_place_plunger), getString(R.string.desc_place_plunger), R.drawable.ic_place_plunger, hasDuration = true),
            RecipeStep("remove_plunger", getString(R.string.step_remove_plunger), getString(R.string.desc_remove_plunger), R.drawable.ic_remove_plunger, hasDuration = true),

            RecipeStep("pull_plunger", getString(R.string.step_pull_plunger), getString(R.string.desc_pull_plunger), R.drawable.ic_pull_plunger, hasDuration = true), // Reusing ic_remove_plunger
            RecipeStep("add_coffee", getString(R.string.step_add_coffee), getString(R.string.desc_add_coffee), R.drawable.ic_add_coffee, hasDuration = true),
            RecipeStep("brew", getString(R.string.step_brew), getString(R.string.desc_brew), R.drawable.ic_brew, hasDuration = true),

            RecipeStep("put_the_lid_on", getString(R.string.step_put_the_lid_on), getString(R.string.desc_put_the_lid_on), R.drawable.ic_put_the_lid_on, hasDuration = true),
            RecipeStep("put_the_prismo_on", getString(R.string.step_put_the_prismo_on), getString(R.string.desc_put_the_prismo_on), R.drawable.ic_put_the_prismo_on, hasDuration = true),
            RecipeStep("custom_step", getString(R.string.step_custom_step), getString(R.string.desc_custom_step), R.drawable.ic_custom_step, hasDuration = true)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)
    }
}