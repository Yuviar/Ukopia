package com.example.ukopia.ui.loyalty

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.ukopia.MainActivity
import com.example.ukopia.data.LoyaltyItemV2
import com.example.ukopia.databinding.FragmentLoyaltyDetailDialogBinding

class LoyaltyDetailDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentLoyaltyDetailDialogBinding
    private var item: LoyaltyItemV2? = null

    companion object {
        fun newInstance(item: LoyaltyItemV2): LoyaltyDetailDialogFragment {
            val fragment = LoyaltyDetailDialogFragment()
            fragment.arguments = Bundle().apply {
                putParcelable("loyalty_item", item)
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoyaltyDetailDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        item = arguments?.getParcelable("loyalty_item")

        item?.let { data ->
            binding.textViewDialogNamaMenu.text = data.namaMenu
            binding.textViewDialogTanggal.text = data.tanggal

            // Isi Catatan
            binding.textViewDialogCatatan.text = if (data.nilai?.catatan.isNullOrEmpty()) {
                "Catatan: -"
            } else {
                "Catatan: ${data.nilai?.catatan}"
            }

            if (data.isCoffee) {
                binding.linearLayoutDialogKopi.visibility = View.VISIBLE
                binding.linearLayoutDialogNonKopi.visibility = View.GONE

                binding.textViewDialogNamaBeans.text = data.namaBeans ?: data.namaMenu

                // Ambil nilai review
                val nilai = data.nilai
                binding.textViewDetailAroma.text = "Aroma: ${nilai?.aroma ?: 0}"
                binding.textViewDetailSweetness.text = "Sweetness: ${nilai?.kemanisan ?: 0}"
                binding.textViewDetailAcidity.text = "Acidity: ${nilai?.keasaman ?: 0}"
                binding.textViewDetailBitterness.text = "Bitterness: ${nilai?.kepahitan ?: 0}"
                binding.textViewDetailBody.text = "Body: ${nilai?.kekentalan ?: 0}"

            } else {
                binding.linearLayoutDialogKopi.visibility = View.GONE
                binding.linearLayoutDialogNonKopi.visibility = View.VISIBLE
                binding.textViewDialogNamaNonKopi.text = data.namaMenu
                // Hapus data taste profil untuk non-kopi
                binding.textViewTasteProfil.visibility = View.GONE
            }

            // --- MODIFIKASI DIMULAI DI SINI ---
            binding.buttonDialogEdit.setOnClickListener {
                dismiss() // <<< Tambahkan baris ini untuk menutup dialog sebelum navigasi
                val editFragment = EditLoyaltyFragment.newInstance(data)
                (activity as? MainActivity)?.navigateToFragment(editFragment)
            }
            // --- MODIFIKASI BERAKHIR DI SINI ---
        }

        binding.buttonDialogClose.setOnClickListener { dismiss() }
    }
}