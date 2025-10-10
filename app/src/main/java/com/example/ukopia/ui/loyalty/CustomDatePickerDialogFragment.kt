package com.example.ukopia.ui.loyalty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult // Penting: Tambahkan import ini
import com.example.ukopia.R // Pastikan import R
import com.example.ukopia.databinding.FragmentCustomDatePickerBinding // Pastikan ini terdeteksi
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CustomDatePickerDialogFragment : DialogFragment() {

    private var _binding: FragmentCustomDatePickerBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val REQUEST_KEY_DATE_PICKER = "date_picker_request_key"
        const val BUNDLE_KEY_SELECTED_DATE = "selected_date_bundle_key"
        const val BUNDLE_KEY_INITIAL_DATE = "initial_date_bundle_key" // Untuk meneruskan tanggal awal

        fun newInstance(initialDate: String? = null): CustomDatePickerDialogFragment {
            val fragment = CustomDatePickerDialogFragment()
            initialDate?.let {
                val args = Bundle()
                args.putString(BUNDLE_KEY_INITIAL_DATE, it)
                fragment.arguments = args
            }
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
        _binding = FragmentCustomDatePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val datePicker = binding.datePicker
        val btnCancel = binding.btnCancel
        val btnOk = binding.btnOk

        // Atur tanggal awal jika ada di argumen
        val initialDateString = arguments?.getString(BUNDLE_KEY_INITIAL_DATE)
        val calendar = Calendar.getInstance()
        if (initialDateString != null) {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                // Mengatur waktu menjadi tengah malam agar perbandingan tanggal lebih akurat
                calendar.time = sdf.parse(initialDateString) ?: Calendar.getInstance().time
            } catch (e: Exception) {
                e.printStackTrace()
                // Jika parsing gagal, fallback ke tanggal hari ini
                calendar.time = Calendar.getInstance().time
            }
        }
        datePicker.init(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
            null
        )

        btnCancel.setOnClickListener {
            dismiss() // Tutup dialog
        }

        btnOk.setOnClickListener {
            val selectedDay = datePicker.dayOfMonth
            val selectedMonth = datePicker.month // Bulan adalah 0-indexed
            val selectedYear = datePicker.year

            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(selectedYear, selectedMonth, selectedDay)

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = sdf.format(selectedCalendar.time)

            // Kirim hasil kembali ke fragment pemanggil
            setFragmentResult(REQUEST_KEY_DATE_PICKER, Bundle().apply {
                putString(BUNDLE_KEY_SELECTED_DATE, formattedDate)
            })
            dismiss() // Tutup dialog
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}