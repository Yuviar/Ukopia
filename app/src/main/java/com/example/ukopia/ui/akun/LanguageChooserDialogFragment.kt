package com.example.ukopia.ui.akun

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.ui.akun.LocaleHelper
import com.example.ukopia.R
import com.example.ukopia.databinding.FragmentLanguageChooserDialogBinding
import androidx.core.content.ContextCompat

class LanguageChooserDialogFragment : DialogFragment() {

    private var _binding: FragmentLanguageChooserDialogBinding? = null
    private val binding get() = _binding!!

    data class LanguageItem(val name: String, val code: String)

    companion object {
        fun newInstance(): LanguageChooserDialogFragment {
            return LanguageChooserDialogFragment()
        }
        const val REQUEST_KEY = "language_chooser_request_key"
        const val BUNDLE_KEY_LANGUAGE_CODE = "selected_language_code"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageChooserDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentLangCode = LocaleHelper.getLanguage(requireContext()) ?: LocaleHelper.DEFAULT_LANGUAGE

        val languages = listOf(
            LanguageItem(getString(R.string.language_english), "en"),
            LanguageItem(getString(R.string.language_indonesian), "in")
        )

        binding.dialogTitle.text = getString(R.string.select_language)
        binding.buttonCancel.text = getString(R.string.cancel_button_text)

        val adapter = LanguageOptionAdapter(languages, currentLangCode) { selectedLanguageCode ->
            val result = Bundle().apply {
                putString(BUNDLE_KEY_LANGUAGE_CODE, selectedLanguageCode)
            }
            parentFragmentManager.setFragmentResult(REQUEST_KEY, result)
            dismiss()
        }

        binding.recyclerViewLanguages.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewLanguages.adapter = adapter

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels

            val horizontalMarginDp = 24
            val horizontalMarginPx = (horizontalMarginDp * displayMetrics.density).toInt() * 2

            val dialogWidth = screenWidth - horizontalMarginPx

            window.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER_HORIZONTAL)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}