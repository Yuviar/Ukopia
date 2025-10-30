package com.example.ukopia.ui.loyalty

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.example.ukopia.R
import com.example.ukopia.data.LoyaltyItemV2
import com.example.ukopia.databinding.FragmentLoyaltyDetailDialogBinding

class LoyaltyDetailDialogFragment : DialogFragment() {

    private var _binding: FragmentLoyaltyDetailDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoyaltyDetailDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val item = arguments?.getParcelable<LoyaltyItemV2>(ARG_ITEM)

        item?.let {
            binding.textViewDialogNamaMenu.text = it.namaMenu.uppercase()

            binding.textViewDialogTanggal.text = getString(R.string.date_prefix ) + it.tanggal

            if (it.catatan.isNullOrBlank()) {
                binding.textViewDialogCatatan.visibility = View.GONE
            } else {
                binding.textViewDialogCatatan.text = getString(R.string.notes_prefix) + it.catatan
                binding.textViewDialogCatatan.visibility = View.VISIBLE
            }

            if (it.isCoffee) {
                binding.linearLayoutDialogKopi.visibility = View.VISIBLE
                binding.linearLayoutDialogNonKopi.visibility = View.GONE

                binding.textViewBeanData.text = getString(R.string.coffee_bean_data_title)
                binding.textViewDialogNamaBeans.text = getString(R.string.coffee_bean_name_prefix ) + (it.namaBeans ?: getString(R.string.not_available_text))

                binding.textViewTasteProfil.text = getString(R.string.taste_profile_title)
                binding.textViewDetailAroma.text = getString(R.string.aroma_prefix ) + (it.aroma ?: 0)
                binding.textViewDetailSweetness.text = getString(R.string.sweetness_prefix ) + (it.sweetness ?: 0)
                binding.textViewDetailAcidity.text = getString(R.string.acidity_prefix ) + (it.acidity ?: 0)
                binding.textViewDetailBitterness.text = getString(R.string.bitterness_prefix ) + (it.bitterness ?: 0)
                binding.textViewDetailBody.text = getString(R.string.body_prefix ) + (it.body ?: 0)
            } else {
                binding.linearLayoutDialogKopi.visibility = View.GONE
                binding.linearLayoutDialogNonKopi.visibility = View.VISIBLE

                binding.textViewMenuData.text = getString(R.string.menu_data_title)
                binding.textViewDialogNamaNonKopi.text = getString(R.string.non_coffee_name_prefix ) + (it.namaNonKopi ?: getString(R.string.not_available_text))
            }
        }

        binding.buttonDialogClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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


    companion object {
        private const val ARG_ITEM = "loyalty_item"

        fun newInstance(item: LoyaltyItemV2): LoyaltyDetailDialogFragment {
            val fragment = LoyaltyDetailDialogFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(ARG_ITEM, item)
            }
            return fragment
        }
    }
}

