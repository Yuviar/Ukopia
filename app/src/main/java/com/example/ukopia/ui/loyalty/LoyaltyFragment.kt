package com.example.ukopia.ui.loyalty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.R
import com.example.ukopia.adapter.LoyaltyAdapter
import com.example.ukopia.data.LoyaltyItemV2
import com.example.ukopia.databinding.FragmentLoyaltyBinding

class LoyaltyFragment : Fragment() {

    private lateinit var binding: FragmentLoyaltyBinding
    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoyaltyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Siapkan adapter dengan click listener untuk navigasi
        val adapter = LoyaltyAdapter { item ->
            // Buat instance LoyaltyDetailFragment dan kirim data
            val detailFragment = LoyaltyDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("loyaltyItem", item)
                }
            }

            // Ganti fragment saat ini dengan halaman detail
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.recyclerViewLoyalty.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewLoyalty.adapter = adapter

        // Gunakan observer untuk mengamati perubahan data dari ViewModel
        loyaltyViewModel.loyaltyItems.observe(viewLifecycleOwner) { items ->
            // Perbarui data di adapter
            adapter.submitList(items)

            // Tampilkan atau sembunyikan pesan jika daftar kosong
            if (items.isEmpty()) {
                binding.imageViewPlaceholder.visibility = View.VISIBLE
                binding.textViewPlaceholder.visibility = View.VISIBLE
                binding.recyclerViewLoyalty.visibility = View.GONE
            } else {
                binding.imageViewPlaceholder.visibility = View.GONE
                binding.textViewPlaceholder.visibility = View.GONE
                binding.recyclerViewLoyalty.visibility = View.VISIBLE
            }
        }
    }
}
