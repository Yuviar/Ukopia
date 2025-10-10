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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.ukopia.R
import com.example.ukopia.data.RecipeStep
import com.example.ukopia.databinding.DialogConfigureRecipeStepBinding
// IMPOR SuffixTextWatcher dari AddRecipeFragment.kt
import com.example.ukopia.ui.recipe.SuffixTextWatcher

class ConfigureStepDialogFragment : DialogFragment() {

    private var _binding: DialogConfigureRecipeStepBinding? = null
    private val binding get() = _binding!!

    private lateinit var originalStep: RecipeStep
    private var configuredDuration: String? = null
    private var configuredDescription: String? = null
    private var configuredWaterAmount: String? = null

    companion object {
        const val REQUEST_KEY_CONFIGURE_STEP = "configure_step_request_key"
        const val BUNDLE_KEY_CONFIGURED_STEP = "configured_step_bundle_key"
        private const val BUNDLE_KEY_ORIGINAL_STEP = "original_step_bundle_key"

        fun newInstance(step: RecipeStep): ConfigureStepDialogFragment {
            val fragment = ConfigureStepDialogFragment()
            val args = Bundle().apply {
                putParcelable(BUNDLE_KEY_ORIGINAL_STEP, step)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Gunakan FullScreenDialogTheme Anda untuk gaya dialog secara keseluruhan
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogConfigureRecipeStepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        originalStep = arguments?.getParcelable(BUNDLE_KEY_ORIGINAL_STEP) ?: throw IllegalStateException("RecipeStep must be provided!")

        val etDescription = binding.editTextStepDescription
        val llDurationInput = binding.linearLayoutDurationInput
        val etDuration = binding.editTextStepDuration
        val labelWaterAmount = binding.labelWaterAmount
        val etWaterAmount = binding.editTextStepWaterAmount

        // Pre-fill existing data if any
        originalStep.description?.let {
            etDescription.setText(it)
            configuredDescription = it
        }
        originalStep.currentDurationInput?.let {
            etDuration.setText(it)
            configuredDuration = it
        }
        originalStep.currentWaterAmountInput?.let {
            etWaterAmount.setText(it)
            configuredWaterAmount = it
        }

        // --- Logika Water Amount ---
        if (originalStep.id == "pour_water" || originalStep.id == "bloom") {
            labelWaterAmount.visibility = View.VISIBLE
            etWaterAmount.visibility = View.VISIBLE
            etWaterAmount.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            etWaterAmount.hint = getString(R.string.water_amount_hint_placeholder)
            etWaterAmount.addTextChangedListener(SuffixTextWatcher(etWaterAmount, " ml") { rawValue ->
                configuredWaterAmount = rawValue
            })
        } else {
            labelWaterAmount.visibility = View.GONE
            etWaterAmount.visibility = View.GONE
        }
        // --- Akhir Logika Water Amount ---


        etDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                configuredDescription = s?.toString()?.trim()
            }
        })

        // Karena semua langkah sekarang punya durasi, linearLayoutDurationInput selalu terlihat
        llDurationInput.visibility = View.VISIBLE
        // Input durasi sebagai detik saja
        etDuration.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        etDuration.hint = getString(R.string.duration_hint_seconds) // Gunakan string resource

        etDuration.addTextChangedListener(SuffixTextWatcher(etDuration, " s") { rawValue ->
            configuredDuration = rawValue
        })

        // Tombol OK/Cancel akan disediakan oleh MaterialAlertDialogBuilder di AddRecipeFragment
        // Jadi, tidak perlu mengatur listener tombol di sini, tetapi akan diatur saat dialog di-show.
    }

    // Fungsi ini akan dipanggil oleh AddRecipeFragment saat tombol PositiveButton dari MaterialAlertDialogBuilder ditekan
    fun getResultStep(): RecipeStep {
        return originalStep.copy(
            description = configuredDescription?.takeIf { it.isNotBlank() },
            currentDurationInput = configuredDuration?.takeIf { it.isNotBlank() },
            currentWaterAmountInput = configuredWaterAmount?.takeIf { it.isNotBlank() }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}