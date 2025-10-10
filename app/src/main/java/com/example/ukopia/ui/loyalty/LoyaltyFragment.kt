package com.example.ukopia.ui.loyalty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.R // Pastikan import R
import com.example.ukopia.adapter.LoyaltyAdapter
import com.example.ukopia.data.LoyaltyItemV2
import com.example.ukopia.databinding.FragmentLoyaltyBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.ukopia.MainActivity

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

        (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)

        val adapter = LoyaltyAdapter { item ->
            LoyaltyDetailDialogFragment.newInstance(item).show(parentFragmentManager, "LoyaltyDetailPopup")
        }

        binding.recyclerViewLoyalty.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewLoyalty.adapter = adapter

        // Teks contentDescription fab_add_recipe_to_loyalty sudah di XML
        val fabAddData: FloatingActionButton = view.findViewById(R.id.fab_add_recipe)
        fabAddData.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, AddLoyaltyFragment())
                .addToBackStack(null)
                .commit()
        }

        loyaltyViewModel.loyaltyItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)

            if (items.isEmpty()) {
                binding.placeholderContainer.visibility = View.VISIBLE
                binding.recyclerViewLoyalty.visibility = View.GONE
                // Menggunakan string resource untuk placeholder
                binding.textViewPlaceholder.text = getString(R.string.no_loyalty_data_placeholder)
            } else {
                binding.placeholderContainer.visibility = View.GONE
                binding.recyclerViewLoyalty.visibility = View.VISIBLE
            }
        }
    }
}