package com.example.ukopia.ui.loyalty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.ukopia.data.LoyaltyItemV2
import com.example.ukopia.databinding.FragmentLoyaltyDetailBinding

class LoyaltyDetailFragment : Fragment() {

    private lateinit var binding: FragmentLoyaltyDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoyaltyDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loyaltyItem = arguments?.getParcelable<LoyaltyItemV2>("loyaltyItem")

        loyaltyItem?.let {
            // Tampilkan data umum
            binding.textViewNamaMenuDetail.text = it.namaMenu
            binding.textViewTanggalDetail.text = it.tanggal
            if (it.catatan != null) {
                binding.textViewCatatanDetail.text = it.catatan
                binding.textViewCatatanDetail.visibility = View.VISIBLE
            } else {
                binding.textViewCatatanDetail.visibility = View.GONE
            }

            // Tampilkan data berdasarkan jenis item
            if (it.isCoffee) {
                binding.coffeeLayout.visibility = View.VISIBLE
                binding.nonCoffeeLayout.visibility = View.GONE

                binding.textViewNamaBeansDetail.text = "Biji Kopi: ${it.namaBeans}"

                binding.textViewAromaDetail.text = "Aroma: ${it.aroma}"
                binding.textViewSweetnessDetail.text = "Kemanisan: ${it.sweetness}"
                binding.textViewAcidityDetail.text = "Keasaman: ${it.acidity}"
                binding.textViewBitternessDetail.text = "Kepahitan: ${it.bitterness}"
                binding.textViewBodyDetail.text = "Kekentalan (Body): ${it.body}"
            } else {
                binding.coffeeLayout.visibility = View.GONE
                binding.nonCoffeeLayout.visibility = View.VISIBLE

                binding.textViewNamaNonKopiDetail.text = "Nama: ${it.namaNonKopi}"
            }
        }
    }
}
