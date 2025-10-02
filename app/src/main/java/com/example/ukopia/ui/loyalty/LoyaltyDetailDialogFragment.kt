package com.example.ukopia.ui.loyalty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.ukopia.data.LoyaltyItemV2
import com.example.ukopia.databinding.FragmentLoyaltyDetailDialogBinding

class LoyaltyDetailDialogFragment : DialogFragment() {

    private var _binding: FragmentLoyaltyDetailDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Menggunakan View Binding untuk mengembang layout dialog
        _binding = FragmentLoyaltyDetailDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val item = arguments?.getParcelable<LoyaltyItemV2>(ARG_ITEM)

        item?.let {
            // Mengatur header (nama menu)
            binding.textViewDialogNamaMenu.text = it.namaMenu.uppercase()

            // Mengatur data umum
            binding.textViewDialogTanggal.text = "Tanggal: ${it.tanggal}"

            // Mengatur komentar
            if (it.catatan.isNullOrBlank()) {
                binding.textViewDialogCatatan.visibility = View.GONE
            } else {
                binding.textViewDialogCatatan.text = "Catatan: ${it.catatan}"
                binding.textViewDialogCatatan.visibility = View.VISIBLE
            }

            // Mengisi data berdasarkan jenis item
            if (it.isCoffee) {
                binding.linearLayoutDialogKopi.visibility = View.VISIBLE
                binding.linearLayoutDialogNonKopi.visibility = View.GONE

                // Data Biji Kopi
                binding.textViewDialogNamaBeans.text = "Biji Kopi: ${it.namaBeans ?: "-"}"

                // Data Profil Rasa
                binding.textViewDetailAroma.text = "Aroma: ${it.aroma ?: 0}"
                binding.textViewDetailSweetness.text = "Sweetness: ${it.sweetness ?: 0}"
                binding.textViewDetailAcidity.text = "Acidity: ${it.acidity ?: 0}"
                binding.textViewDetailBitterness.text = "Bitterness: ${it.bitterness ?: 0}"
                binding.textViewDetailBody.text = "Body: ${it.body ?: 0}"
            } else {
                binding.linearLayoutDialogKopi.visibility = View.GONE
                binding.linearLayoutDialogNonKopi.visibility = View.VISIBLE

                // Data Non-Kopi
                binding.textViewDialogNamaNonKopi.text = "Nama: ${it.namaNonKopi ?: "Tidak ada"}"
            }
        }

        // Tutup dialog saat tombol diklik
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
        // Mengatur lebar dialog agar mengisi sebagian besar layar
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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
