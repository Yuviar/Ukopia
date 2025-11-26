package com.example.ukopia.ui.loyalty

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.data.LoyaltyItemV2
import com.example.ukopia.databinding.FragmentEditLoyaltyBinding // Assuming a new binding for this fragment

class EditLoyaltyFragment : Fragment() {

    private var _binding: FragmentEditLoyaltyBinding? = null
    private val binding get() = _binding!!
    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels()

    private lateinit var originalLoyaltyItem: LoyaltyItemV2

    companion object {
        private const val ARG_ITEM = "loyalty_item_to_edit"
        const val REQUEST_KEY_LOYALTY_EDITED = "request_key_loyalty_edited"
        const val BUNDLE_KEY_LOYALTY_EDITED = "bundle_key_loyalty_edited"

        fun newInstance(item: LoyaltyItemV2): EditLoyaltyFragment {
            val fragment = EditLoyaltyFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(ARG_ITEM, item)
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditLoyaltyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).setBottomNavVisibility(View.GONE)

        originalLoyaltyItem = arguments?.getParcelable(ARG_ITEM)
            ?: throw IllegalArgumentException("LoyaltyItemV2 must be passed to EditLoyaltyFragment")

        setupViews(originalLoyaltyItem)
        setupSeekBars()

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

        binding.btnSave.setOnClickListener {
            saveEditedData()
        }
    }

    private fun setupViews(item: LoyaltyItemV2) {
        binding.tvHeaderTitle.text = getString(R.string.edit_loyalty_title)

        // Non-editable fields
        binding.textViewMenuNameLabel.text = getString(R.string.menu_name_label)
        binding.textViewMenuNameValue.text = item.namaMenu.uppercase()

        if (item.isCoffee) {
            binding.linearLayoutCoffeeDetails.visibility = View.VISIBLE
            binding.linearLayoutNonCoffeeDetails.visibility = View.GONE

            binding.textViewCoffeeBeanLabel.text = getString(R.string.coffee_bean_name_label)
            binding.textViewCoffeeBeanValue.text = item.namaBeans ?: getString(R.string.not_available_text)

            // Set initial values for seek bars
            binding.seekBarAroma.progress = item.aroma ?: 0
            binding.textViewAromaValue.text = (item.aroma ?: 0).toString()
            binding.seekBarSweetness.progress = item.sweetness ?: 0
            binding.textViewSweetnessValue.text = (item.sweetness ?: 0).toString()
            binding.seekBarAcidity.progress = item.acidity ?: 0
            binding.textViewAcidityValue.text = (item.acidity ?: 0).toString()
            binding.seekBarBitterness.progress = item.bitterness ?: 0
            binding.textViewBitternessValue.text = (item.bitterness ?: 0).toString()
            binding.seekBarBody.progress = item.body ?: 0
            binding.textViewBodyValue.text = (item.body ?: 0).toString()

            binding.textViewAromaMin.text = "0"
            binding.textViewAromaMax.text = "5"
            binding.textViewSweetnessMin.text = "0"
            binding.textViewSweetnessMax.text = "5"
            binding.textViewAcidityMin.text = "0"
            binding.textViewAcidityMax.text = "5"
            binding.textViewBitternessMin.text = "0"
            binding.textViewBitternessMax.text = "5"
            binding.textViewBodyMin.text = "0"
            binding.textViewBodyMax.text = "5"

        } else {
            binding.linearLayoutCoffeeDetails.visibility = View.GONE
            binding.linearLayoutNonCoffeeDetails.visibility = View.VISIBLE

            binding.textViewNonCoffeeItemLabel.text = getString(R.string.non_coffee_item_name_label)
            binding.textViewNonCoffeeItemValue.text = item.namaNonKopi ?: getString(R.string.not_available_text)
        }

        binding.textViewDateLabel.text = getString(R.string.date_prefix_title)
        binding.textViewDateValue.text = item.tanggal

        // Editable fields
        binding.textViewCatatan.text = getString(R.string.notes_label)
        binding.editTextCatatan.setText(item.catatan)
        binding.editTextCatatan.hint = getString(R.string.notes_hint_optional)
    }

    private fun setupSeekBars() {
        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                when (seekBar?.id) {
                    R.id.seekBarAroma -> binding.textViewAromaValue.text = progress.toString()
                    R.id.seekBarSweetness -> binding.textViewSweetnessValue.text = progress.toString()
                    R.id.seekBarAcidity -> binding.textViewAcidityValue.text = progress.toString()
                    R.id.seekBarBitterness -> binding.textViewBitternessValue.text = progress.toString()
                    R.id.seekBarBody -> binding.textViewBodyValue.text = progress.toString()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

        binding.seekBarAroma.setOnSeekBarChangeListener(listener)
        binding.seekBarSweetness.setOnSeekBarChangeListener(listener)
        binding.seekBarAcidity.setOnSeekBarChangeListener(listener)
        binding.seekBarBitterness.setOnSeekBarChangeListener(listener)
        binding.seekBarBody.setOnSeekBarChangeListener(listener)
    }

    private fun saveEditedData() {
        val updatedCatatan = binding.editTextCatatan.text.toString()

        val updatedItem = originalLoyaltyItem.copy(
            catatan = updatedCatatan,
            aroma = if (originalLoyaltyItem.isCoffee) binding.seekBarAroma.progress else null,
            sweetness = if (originalLoyaltyItem.isCoffee) binding.seekBarSweetness.progress else null,
            acidity = if (originalLoyaltyItem.isCoffee) binding.seekBarAcidity.progress else null,
            bitterness = if (originalLoyaltyItem.isCoffee) binding.seekBarBitterness.progress else null,
            body = if (originalLoyaltyItem.isCoffee) binding.seekBarBody.progress else null
        )

        loyaltyViewModel.updatePurchase(updatedItem)
        Toast.makeText(requireContext(), getString(R.string.loyalty_data_updated_success), Toast.LENGTH_SHORT).show()

        setFragmentResult(REQUEST_KEY_LOYALTY_EDITED, Bundle().apply {
            putBoolean(BUNDLE_KEY_LOYALTY_EDITED, true)
        })

        parentFragmentManager.popBackStack()
        (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}