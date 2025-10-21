package com.example.ukopia.ui.loyalty

import android.os.Bundle
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
import androidx.fragment.app.commit
import androidx.fragment.app.setFragmentResultListener
import com.example.ukopia.R
import com.example.ukopia.data.LoyaltyItemV2
import com.example.ukopia.databinding.FragmentAddLoyaltyBinding
import com.example.ukopia.databinding.FragmentAddLoyaltyKopiBinding
import com.example.ukopia.databinding.FragmentAddLoyaltyBukanKopiBinding
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.ukopia.MainActivity

class AddLoyaltyFragment : Fragment() {

    private lateinit var binding: FragmentAddLoyaltyBinding
    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels()

    private var isCoffeeSelected = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddLoyaltyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).setBottomNavVisibility(View.GONE)

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

        binding.buttonKopi.setOnClickListener {
            updateButtonState(isCoffee = true)
            loadForm(true)
        }

        binding.buttonBukanKopi.setOnClickListener {
            updateButtonState(isCoffee = false)
            loadForm(false)
        }

        binding.btnSelesai.setOnClickListener {
            saveData()
        }

        binding.buttonKopi.text = getString(R.string.coffee_button_text)
        binding.buttonBukanKopi.text = getString(R.string.non_coffee_button_text)
        binding.btnSelesai.text = getString(R.string.add_button_text)
        binding.tvHeaderTitle.text = getString(R.string.add_loyalty_title)
        binding.textViewTanggal.text = getString(R.string.date_prefix_title)
        binding.editTextDate.hint = getString(R.string.date_hint)
        binding.textViewCatatan.text = getString(R.string.notes_label)
        binding.editTextCatatan.hint = getString(R.string.notes_hint_optional)


        updateButtonState(isCoffee = true)
        loadForm(true)
    }

    private fun updateButtonState(isCoffee: Boolean) {
        val context = requireContext()
        val activeColor = ContextCompat.getColor(context, R.color.white)
        val inactiveColor = ContextCompat.getColor(context, R.color.black)

        if (isCoffee) {
            binding.buttonKopi.setBackgroundTintList(
                ContextCompat.getColorStateList(
                    context,
                    R.color.black
                )
            )
            binding.buttonKopi.setTextColor(activeColor)
            binding.buttonBukanKopi.setBackgroundTintList(
                ContextCompat.getColorStateList(
                    context,
                    R.color.white
                )
            )
            binding.buttonBukanKopi.setTextColor(inactiveColor)
        } else {
            binding.buttonKopi.setBackgroundTintList(
                ContextCompat.getColorStateList(
                    context,
                    R.color.white
                )
            )
            binding.buttonKopi.setTextColor(inactiveColor)
            binding.buttonBukanKopi.setBackgroundTintList(
                ContextCompat.getColorStateList(
                    context,
                    R.color.black
                )
            )
            binding.buttonBukanKopi.setTextColor(activeColor)
        }
    }

    private fun loadForm(isCoffee: Boolean) {
        val fragment = if (isCoffee) {
            AddLoyaltyKopiFragment()
        } else {
            AddLoyaltyNonKopiFragment()
        }

        isCoffeeSelected = isCoffee

        childFragmentManager.commit {
            replace(R.id.container_loyalty_form, fragment)
        }
    }

    private fun saveData() {
        val selectedFragment = childFragmentManager.findFragmentById(R.id.container_loyalty_form)
        val tanggal = binding.editTextDate.text.toString()
        val catatan = binding.editTextCatatan.text.toString()

        var isValid = true
        var firstErrorView: View? = null

        binding.editTextDate.error = null
        if (tanggal.isEmpty()) {
            binding.editTextDate.error = getString(R.string.error_date_required)
            firstErrorView = binding.editTextDate
            isValid = false
        }

        var item: LoyaltyItemV2? = null

        if (selectedFragment is AddLoyaltyKopiFragment) {
            val namaMenu = selectedFragment.getNamaMenu()
            val namaBeans = selectedFragment.getNamaBeans()

            if (namaMenu.isEmpty()) {
                selectedFragment.setNamaMenuError(getString(R.string.error_menu_name_required))
                if (firstErrorView == null) firstErrorView = selectedFragment.getNamaMenuEditText()
                isValid = false
            }
            if (namaBeans.isEmpty()) {
                selectedFragment.setNamaBeansError(getString(R.string.error_coffee_bean_name_required))
                if (firstErrorView == null) firstErrorView = selectedFragment.getNamaBeansEditText()
                isValid = false
            }

            if (isValid) {
                item = LoyaltyItemV2(
                    isCoffee = true,
                    namaMenu = namaMenu,
                    namaBeans = namaBeans,
                    tanggal = tanggal,
                    catatan = catatan,
                    aroma = selectedFragment.getAroma(),
                    sweetness = selectedFragment.getSweetness(),
                    acidity = selectedFragment.getAcidity(),
                    bitterness = selectedFragment.getBitterness(),
                    body = selectedFragment.getBody(),
                    namaNonKopi = null,
                )
            }
        } else if (selectedFragment is AddLoyaltyNonKopiFragment) {
            val namaMenu = selectedFragment.getNamaMenu()
            val namaNonKopi = selectedFragment.getNamaNonKopi()

            if (namaMenu.isEmpty()) {
                selectedFragment.setNamaMenuError(getString(R.string.error_menu_required))
                if (firstErrorView == null) firstErrorView = selectedFragment.getNamaMenuEditText()
                isValid = false
            }
            if (namaNonKopi.isEmpty()) {
                selectedFragment.setNamaNonKopiError(getString(R.string.error_name_required))
                if (firstErrorView == null) firstErrorView =
                    selectedFragment.getNamaNonKopiEditText()
                isValid = false
            }

            if (isValid) {
                item = LoyaltyItemV2(
                    isCoffee = false,
                    namaMenu = namaMenu,
                    namaNonKopi = namaNonKopi,
                    tanggal = tanggal,
                    catatan = catatan,
                    namaBeans = null,
                    aroma = null,
                    sweetness = null,
                    acidity = null,
                    bitterness = null,
                    body = null
                )
            }
        }

        if (!isValid) {
            firstErrorView?.requestFocus()
            binding.layoutAddLoyalty.post {
                if (firstErrorView != null) {
                    binding.layoutAddLoyalty.smoothScrollTo(0, firstErrorView.top)
                }
            }
        } else if (item != null) {
            loyaltyViewModel.addPurchase(item)
            Toast.makeText(
                requireContext(),
                getString(R.string.loyalty_data_added_success),
                Toast.LENGTH_SHORT
            ).show()
            parentFragmentManager.popBackStack()
            (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)
        }
    }

    private fun showCustomDatePickerDialog() {
        val initialDate = binding.editTextDate.text.toString().ifEmpty { null }
        val customDatePickerDialog = CustomDatePickerDialogFragment.newInstance(initialDate)
        customDatePickerDialog.show(childFragmentManager, "CUSTOM_DATE_PICKER")
    }

    class AddLoyaltyKopiFragment : Fragment() {
        private var _binding: FragmentAddLoyaltyKopiBinding? = null
        private val binding get() = _binding!!

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentAddLoyaltyKopiBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            initializeSeekBars()
            binding.editNamaKopi.hint = getString(R.string.menu_hint)
            binding.editTextBeans.hint = getString(R.string.bean_hint)
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
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

        private fun initializeSeekBars() {
            val listener = object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    when (seekBar?.id) {
                        R.id.seekBarAroma -> binding.textViewAromaValue.text = progress.toString()
                        R.id.seekBarSweetness -> binding.textViewSweetnessValue.text =
                            progress.toString()

                        R.id.seekBarAcidity -> binding.textViewAcidityValue.text =
                            progress.toString()

                        R.id.seekBarBitterness -> binding.textViewBitternessValue.text =
                            progress.toString()

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

        fun getNamaMenu(): String = _binding?.editNamaKopi?.text.toString() ?: ""
        fun getNamaBeans(): String = _binding?.editTextBeans?.text.toString() ?: ""
        fun getAroma(): Int = _binding?.seekBarAroma?.progress ?: 0
        fun getSweetness(): Int = _binding?.seekBarSweetness?.progress ?: 0
        fun getAcidity(): Int = _binding?.seekBarAcidity?.progress ?: 0
        fun getBitterness(): Int = _binding?.seekBarBitterness?.progress ?: 0
        fun getBody(): Int = _binding?.seekBarBody?.progress ?: 0

        fun getNamaMenuEditText(): EditText? = _binding?.editNamaKopi
        fun getNamaBeansEditText(): EditText? = _binding?.editTextBeans

        fun setNamaMenuError(error: String?) {
            binding.editNamaKopi.error = error
        }

        fun setNamaBeansError(error: String?) {
            binding.editTextBeans.error = error
        }
    }

    class AddLoyaltyNonKopiFragment : Fragment() {
        private var _binding: FragmentAddLoyaltyBukanKopiBinding? = null
        private val binding get() = _binding!!

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentAddLoyaltyBukanKopiBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            binding.editTextMenuNonKopi.hint = getString(R.string.menu_hint)
            binding.editTextNamaNonKopi.hint = getString(R.string.name_hint)
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

        fun getNamaMenu(): String = _binding?.editTextMenuNonKopi?.text.toString() ?: ""
        fun getNamaNonKopi(): String = _binding?.editTextNamaNonKopi?.text.toString() ?: ""

        fun getNamaMenuEditText(): EditText? = _binding?.editTextMenuNonKopi
        fun getNamaNonKopiEditText(): EditText? = _binding?.editTextNamaNonKopi

        fun setNamaMenuError(error: String?) {
            binding.editTextMenuNonKopi.error = error
        }

        fun setNamaNonKopiError(error: String?) {
            binding.editTextNamaNonKopi.error = error
        }
    }
}