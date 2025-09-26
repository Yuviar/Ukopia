package com.example.ukopia.ui.loyalty

import android.app.DatePickerDialog
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
import com.example.ukopia.R
import com.example.ukopia.data.LoyaltyItemV2
import com.example.ukopia.databinding.FragmentAddLoyaltyBinding
import com.example.ukopia.databinding.FragmentAddLoyaltyKopiBinding
import com.example.ukopia.databinding.FragmentAddLoyaltyBukanKopiBinding
import java.util.Calendar

class AddLoyaltyFragment : Fragment() {

    private lateinit var binding: FragmentAddLoyaltyBinding
    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddLoyaltyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editTextDate.setOnClickListener {
            showDatePickerDialog()
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

        // Tampilkan form kopi sebagai default saat pertama kali dibuka
        updateButtonState(isCoffee = true)
        loadForm(true)
    }

    private fun updateButtonState(isCoffee: Boolean) {
        val activeBackgroundColor = ContextCompat.getColor(requireContext(), R.color.black)
        val inactiveBackgroundColor = ContextCompat.getColor(requireContext(), R.color.white)
        val activeTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        val inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.black)

        if (isCoffee) {
            binding.buttonKopi.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.black)
            binding.buttonKopi.setTextColor(activeTextColor)
            binding.buttonBukanKopi.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
            binding.buttonBukanKopi.setTextColor(inactiveTextColor)
        } else {
            binding.buttonKopi.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
            binding.buttonKopi.setTextColor(inactiveTextColor)
            binding.buttonBukanKopi.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.black)
            binding.buttonBukanKopi.setTextColor(activeTextColor)
        }
    }

    private fun loadForm(isCoffee: Boolean) {
        val fragment = if (isCoffee) {
            AddLoyaltyKopiFragment()
        } else {
            AddLoyaltyNonKopiFragment()
        }

        childFragmentManager.commit {
            replace(R.id.container_loyalty_form, fragment)
        }
    }

    private fun saveData() {
        val selectedFragment = childFragmentManager.findFragmentById(R.id.container_loyalty_form)
        val tanggal = binding.editTextDate.text.toString()
        val catatan = binding.editTextCatatan.text.toString()

        if (tanggal.isEmpty()) {
            Toast.makeText(requireContext(), "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedFragment is AddLoyaltyKopiFragment) {
            val namaMenu = selectedFragment.getNamaMenu()
            val namaBeans = selectedFragment.getNamaBeans()
            val aroma = selectedFragment.getAroma()
            val sweetness = selectedFragment.getSweetness()
            val acidity = selectedFragment.getAcidity()
            val bitterness = selectedFragment.getBitterness()
            val body = selectedFragment.getBody()

            if (namaMenu.isEmpty()) {
                Toast.makeText(requireContext(), "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show()
                return
            }

            val item = LoyaltyItemV2(
                isCoffee = true,
                namaMenu = namaMenu,
                namaBeans = namaBeans,
                tanggal = tanggal,
                catatan = catatan,
                aroma = aroma,
                sweetness = sweetness,
                acidity = acidity,
                bitterness = bitterness,
                body = body,
                namaNonKopi = null
            )
            loyaltyViewModel.addLoyaltyItemV2(item)
            Toast.makeText(requireContext(), "Data berhasil ditambahkan", Toast.LENGTH_SHORT).show()
        } else if (selectedFragment is AddLoyaltyNonKopiFragment) {
            val namaMenu = selectedFragment.getNamaMenu()
            val namaNonKopi = selectedFragment.getNamaNonKopi()

            if (namaMenu.isEmpty() || namaNonKopi.isEmpty()) {
                Toast.makeText(requireContext(), "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show()
                return
            }

            val item = LoyaltyItemV2(
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
            loyaltyViewModel.addLoyaltyItemV2(item)
            Toast.makeText(requireContext(), "Data berhasil ditambahkan", Toast.LENGTH_SHORT).show()
        }

        parentFragmentManager.popBackStack()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.editTextDate.setText(selectedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeSeekBars() {
        binding.seekBarAroma.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Nilai slider (progress) akan mengikuti range 0-5 yang sudah ditentukan di file XML
                binding.textViewAromaValue.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.seekBarSweetness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Nilai slider (progress) akan mengikuti range 0-5 yang sudah ditentukan di file XML
                binding.textViewSweetnessValue.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.seekBarAcidity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Nilai slider (progress) akan mengikuti range 0-5 yang sudah ditentukan di file XML
                binding.textViewAcidityValue.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.seekBarBitterness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Nilai slider (progress) akan mengikuti range 0-5 yang sudah ditentukan di file XML
                binding.textViewBitternessValue.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.seekBarBody.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Nilai slider (progress) akan mengikuti range 0-5 yang sudah ditentukan di file XML
                binding.textViewBodyValue.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    fun getNamaMenu(): String = _binding?.editNamaKopi?.text.toString() ?: ""
    fun getNamaBeans(): String = _binding?.editTextBeans?.text.toString() ?: ""
    fun getAroma(): Int = _binding?.seekBarAroma?.progress ?: 0
    fun getSweetness(): Int = _binding?.seekBarSweetness?.progress ?: 0
    fun getAcidity(): Int = _binding?.seekBarAcidity?.progress ?: 0
    fun getBitterness(): Int = _binding?.seekBarBitterness?.progress ?: 0
    fun getBody(): Int = _binding?.seekBarBody?.progress ?: 0
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getNamaMenu(): String = _binding?.editTextMenuNonKopi?.text.toString() ?: ""
    fun getNamaNonKopi(): String = _binding?.editTextNamaNonKopi?.text.toString() ?: ""
}
