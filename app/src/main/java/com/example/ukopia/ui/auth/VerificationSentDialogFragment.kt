package com.example.ukopia.ui.auth

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity // Import Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window // Import Window
import androidx.fragment.app.DialogFragment
import com.example.ukopia.R
import com.example.ukopia.databinding.FragmentVerificationSentDialogBinding

class VerificationSentDialogFragment : DialogFragment() {

    private var _binding: FragmentVerificationSentDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVerificationSentDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    // NEW: Tambahkan onCreateDialog untuk menghapus title bar default dan mengatur background transparan
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val message = arguments?.getString(ARG_MESSAGE) ?: getString(R.string.verification_email_sent_toast)
        val title = arguments?.getString(ARG_TITLE) ?: getString(R.string.verification_dialog_title)

        binding.textViewDialogTitle.text = title
        binding.textViewDialogMessage.text = message

        binding.buttonDialogOk.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // MODIFIED: Sesuaikan ukuran dan gravitasi dialog agar mirip LanguageChooserDialogFragment
    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels

            val horizontalMarginDp = 24 // Sama dengan yang di fragment_logout_confirmation_dialog.xml
            val horizontalMarginPx = (horizontalMarginDp * displayMetrics.density).toInt() * 2 // dikali 2 karena margin kanan dan kiri

            val dialogWidth = screenWidth - horizontalMarginPx

            window.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER_HORIZONTAL) // Set dialog ke tengah horizontal
        }
    }


    companion object {
        private const val ARG_MESSAGE = "message"
        private const val ARG_TITLE = "title"

        fun newInstance(message: String, title: String): VerificationSentDialogFragment {
            val fragment = VerificationSentDialogFragment()
            val args = Bundle().apply {
                putString(ARG_MESSAGE, message)
                putString(ARG_TITLE, title)
            }
            fragment.arguments = args
            return fragment
        }
    }
}