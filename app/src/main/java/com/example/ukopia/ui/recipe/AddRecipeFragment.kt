package com.example.ukopia.ui.recipe

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.ukopia.R
import com.example.ukopia.data.RecipeItem
import com.example.ukopia.databinding.FragmentAddRecipeBinding
import com.example.ukopia.MainActivity // <<-- TAMBAHKAN INI

class AddRecipeFragment : Fragment() {

    private var _binding: FragmentAddRecipeBinding? = null
    private val binding get() = _binding!!

    private val recipeViewModel: RecipeViewModel by activityViewModels()
    private var methodName: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ▼▼▼ Sembunyikan nav bar ▼▼▼
        (requireActivity() as MainActivity).setBottomNavVisibility(View.GONE)

        methodName = arguments?.getString("METHOD_NAME")

        setupListeners()
        setupGrindSizeSpinner()
        setupTimeInputFormatter()
        setupSuffixFormatters() // Menambahkan formatter untuk sufiks dinamis
    }

    private fun setupListeners() {
        binding.buttonSimpanResep.setOnClickListener {
            saveRecipe()
        }

        binding.buttonAddNewStep.setOnClickListener {
            Toast.makeText(requireContext(), "Fungsi belum diimplementasikan", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Menambahkan TextWatcher ke setiap EditText untuk menambahkan sufiks dinamis.
     */
    private fun setupSuffixFormatters() {
        binding.editTextCoffeeAmount.addTextChangedListener(SuffixTextWatcher(binding.editTextCoffeeAmount, " g"))
        binding.editTextWaterAmount.addTextChangedListener(SuffixTextWatcher(binding.editTextWaterAmount, " ml"))
        binding.editTextHeat.addTextChangedListener(SuffixTextWatcher(binding.editTextHeat, "°C"))
    }

    /**
     * Menyiapkan Spinner untuk pilihan ukuran gilingan.
     */
    private fun setupGrindSizeSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.grind_sizes,
            R.layout.spinner_item_bold_left
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_custom)
            binding.spinnerGrindSize.adapter = adapter
        }
    }

    /**
     * Menambahkan TextWatcher ke editTextProcessTime untuk format MM:SS otomatis.
     */
    private fun setupTimeInputFormatter() {
        val timeEditText = binding.editTextProcessTime
        val textWatcher = object : TextWatcher {
            private var current = ""
            private val timeFormat = "##:##"
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    timeEditText.removeTextChangedListener(this)
                    val cleanString = s.toString().replace("[^\\d]".toRegex(), "")
                    var formatted = ""
                    var index = 0
                    for (i in timeFormat.indices) {
                        if (index < cleanString.length && timeFormat[i] == '#') {
                            formatted += cleanString[index]
                            index++
                        } else if (index < cleanString.length && timeFormat[i] != '#') {
                            formatted += timeFormat[i]
                        }
                    }
                    current = formatted
                    timeEditText.setText(formatted)
                    timeEditText.setSelection(formatted.length)
                    timeEditText.addTextChangedListener(this)
                }
            }
        }
        timeEditText.addTextChangedListener(textWatcher)
    }

    /**
     * Mengambil nilai, memvalidasi, dan menyimpan resep baru.
     */
    private fun saveRecipe() {
        if (methodName == null) {
            Toast.makeText(requireContext(), "Error: Metode seduh tidak diketahui.", Toast.LENGTH_SHORT).show()
            return
        }

        val name = binding.editTextRecipeName.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()

        // Mengambil nilai DAN MENGHAPUS SUFFIX-nya sebelum divalidasi/disimpan
        val coffeeAmountStr = binding.editTextCoffeeAmount.text.toString().removeSuffix(" g").trim()
        val waterAmountStr = binding.editTextWaterAmount.text.toString().removeSuffix(" ml").trim()
        val heatStr = binding.editTextHeat.text.toString().removeSuffix("°C").trim()

        val grindSize = binding.spinnerGrindSize.selectedItem.toString()
        val time = binding.editTextProcessTime.text.toString().trim()

        var isValid = true
        if (name.isEmpty()) {
            binding.editTextRecipeName.error = "Nama Resep wajib diisi!"
            isValid = false
        }
        if (description.isEmpty()) {
            binding.editTextDescription.error = "Deskripsi wajib diisi!"
            isValid = false
        }
        if (coffeeAmountStr.isEmpty()) {
            binding.editTextCoffeeAmount.error = "Jumlah Kopi wajib diisi!"
            isValid = false
        }
        if (waterAmountStr.isEmpty()) {
            binding.editTextWaterAmount.error = "Jumlah Air wajib diisi!"
            isValid = false
        }
        if (heatStr.isEmpty()) {
            binding.editTextHeat.error = "Suhu wajib diisi!"
            isValid = false
        }
        if (time.isEmpty() || time.length < 5) {
            binding.editTextProcessTime.error = "Waktu Proses wajib diisi lengkap!"
            isValid = false
        }

        if (!isValid) {
            Toast.makeText(requireContext(), "Harap lengkapi semua data resep!", Toast.LENGTH_SHORT).show()
            return
        }

        val newRecipe = RecipeItem(
            method = methodName!!,
            name = name,
            description = description,
            waterAmount = "$waterAmountStr ml",
            coffeeAmount = "$coffeeAmountStr g",
            grindSize = grindSize,
            heat = "$heatStr°C",
            time = time,
            isMine = true
        )

        recipeViewModel.addRecipe(newRecipe)
        Toast.makeText(requireContext(), "Resep '$name' berhasil disimpan!", Toast.LENGTH_SHORT).show()

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
        // ▼▼▼ Tampilkan kembali nav bar saat kembali ke fragment RecipeListFragment ▼▼▼
        (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * TextWatcher khusus untuk menambahkan sufiks secara otomatis ke EditText.
 * Contoh: "16" menjadi "16 g".
 */
class SuffixTextWatcher(private val editText: EditText, private val suffix: String) : TextWatcher {
    private var isUpdating = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (isUpdating || s == null) {
            return
        }

        isUpdating = true

        var currentText = s.toString()

        // Jika teks tidak kosong dan tidak diakhiri dengan sufiks, tambahkan sufiks
        if (currentText.isNotEmpty() && !currentText.endsWith(suffix)) {
            // Hapus sufiks lama jika ada (kasus copy-paste)
            if (currentText.contains(suffix)) {
                currentText = currentText.replace(suffix, "")
            }

            val newText = currentText + suffix
            editText.setText(newText)
            editText.setSelection(newText.length - suffix.length)
        }
        // Jika teks hanya berisi sufiks (setelah user menghapus angka), kosongkan
        else if (currentText == suffix) {
            editText.setText("")
        }

        isUpdating = false
    }
}