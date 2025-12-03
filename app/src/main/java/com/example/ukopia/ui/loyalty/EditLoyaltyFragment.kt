package com.example.ukopia.ui.loyalty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.data.LoyaltyItemV2
import com.example.ukopia.databinding.FragmentEditLoyaltyBinding

class EditLoyaltyFragment : Fragment() {

    private var _binding: FragmentEditLoyaltyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoyaltyViewModel by activityViewModels()
    private lateinit var item: LoyaltyItemV2

    companion object {
        fun newInstance(item: LoyaltyItemV2): EditLoyaltyFragment {
            val fragment = EditLoyaltyFragment()
            fragment.arguments = Bundle().apply {
                putParcelable("item_edit", item)
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditLoyaltyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.setBottomNavVisibility(View.GONE)

        item = arguments?.getParcelable("item_edit") ?: run {
            Toast.makeText(context, "Item loyalty tidak ditemukan.", Toast.LENGTH_SHORT).show() // String ini bisa jadi string resource
            parentFragmentManager.popBackStack()
            return
        }

        setupUI()
        setupSeekbarListeners()

        binding.btnSave.setOnClickListener {
            saveChanges()
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupUI() {
        binding.tvHeaderTitle.text = getString(R.string.edit_loyalty_title) // Menggunakan string resource

        binding.textViewMenuNameLabel.text = getString(R.string.menu_name_label)
        binding.textViewMenuNameValue.text = item.namaMenu
        binding.textViewMenuNameValue.isEnabled = false

        binding.textViewDateLabel.text = getString(R.string.date_prefix_title)
        binding.textViewDateValue.text = item.tanggal
        binding.textViewDateValue.isEnabled = false

        binding.textViewCatatan.text = getString(R.string.notes_label) // Menggunakan string resource
        binding.editTextCatatan.hint = getString(R.string.notes_hint_optional) // Menggunakan string resource


        val nilai = item.nilai

        if (item.isCoffee) {
            binding.linearLayoutCoffeeDetails.visibility = View.VISIBLE
            binding.linearLayoutNonCoffeeDetails.visibility = View.GONE

            binding.textViewCoffeeBeanLabel.text = getString(R.string.coffee_bean_name_label)
            binding.textViewCoffeeBeanValue.text = item.namaBeans
            binding.textViewCoffeeBeanValue.isEnabled = false

            binding.layoutAroma.findViewById<TextView>(R.id.label_aroma_seekbar).text = getString(R.string.aroma_label) // Perlu ID label di XML
            binding.layoutSweetness.findViewById<TextView>(R.id.label_sweetness_seekbar).text = getString(R.string.sweetness_label)
            binding.layoutAcidity.findViewById<TextView>(R.id.label_acidity_seekbar).text = getString(R.string.acidity_label)
            binding.layoutBitterness.findViewById<TextView>(R.id.label_bitterness_seekbar).text = getString(R.string.bitterness_label)
            binding.layoutBody.findViewById<TextView>(R.id.label_body_seekbar).text = getString(R.string.body_label)


            binding.seekBarAroma.progress = nilai?.aroma ?: 0
            binding.seekBarSweetness.progress = nilai?.kemanisan ?: 0
            binding.seekBarAcidity.progress = nilai?.keasaman ?: 0
            binding.seekBarBitterness.progress = nilai?.kepahitan ?: 0
            binding.seekBarBody.progress = nilai?.kekentalan ?: 0

            binding.textViewAromaValue.text = (nilai?.aroma ?: 0).toString()
            binding.textViewSweetnessValue.text = (nilai?.kemanisan ?: 0).toString()
            binding.textViewAcidityValue.text = (nilai?.keasaman ?: 0).toString()
            binding.textViewBitternessValue.text = (nilai?.kepahitan ?: 0).toString()
            binding.textViewBodyValue.text = (nilai?.kekentalan ?: 0).toString()

        } else {
            binding.linearLayoutCoffeeDetails.visibility = View.GONE
            binding.linearLayoutNonCoffeeDetails.visibility = View.VISIBLE
            binding.textViewNonCoffeeItemLabel.text = getString(R.string.non_coffee_item_name_label)
            binding.textViewNonCoffeeItemValue.text = item.namaMenu
            binding.textViewNonCoffeeItemValue.isEnabled = false
        }

        binding.editTextCatatan.setText(nilai?.catatan ?: "")
    }

    private fun setupSeekbarListeners() {
        setupSingleSeekbarListener(binding.seekBarAroma, binding.textViewAromaValue)
        setupSingleSeekbarListener(binding.seekBarSweetness, binding.textViewSweetnessValue)
        setupSingleSeekbarListener(binding.seekBarAcidity, binding.textViewAcidityValue)
        setupSingleSeekbarListener(binding.seekBarBitterness, binding.textViewBitternessValue)
        setupSingleSeekbarListener(binding.seekBarBody, binding.textViewBodyValue)
    }

    private fun setupSingleSeekbarListener(seekBar: SeekBar, textView: TextView) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textView.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun saveChanges() {
        viewModel.submitReview(
            item = item,
            catatan = binding.editTextCatatan.text.toString(),
            aroma = binding.seekBarAroma.progress,
            sweetness = binding.seekBarSweetness.progress,
            acidity = binding.seekBarAcidity.progress,
            bitterness = binding.seekBarBitterness.progress,
            body = binding.seekBarBody.progress,
            onSuccess = {
                Toast.makeText(context, getString(R.string.loyalty_data_updated_success), Toast.LENGTH_SHORT).show() // Menggunakan string resource
                parentFragmentManager.popBackStack()
            },
            onError = { msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as? MainActivity)?.setBottomNavVisibility(View.VISIBLE)
    }
}