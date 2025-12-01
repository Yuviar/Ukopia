// File: D:/github_rama/Ukopia/app/src/main/java/com/example/ukopia/ui/loyalty/EditLoyaltyFragment.kt
package com.example.ukopia.ui.loyalty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.ukopia.MainActivity
import com.example.ukopia.data.LoyaltyItemV2
import com.example.ukopia.databinding.FragmentEditLoyaltyBinding // Pastikan package ini benar

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

        item = arguments?.getParcelable("item_edit") ?: return
        setupUI()

        binding.btnSave.setOnClickListener {
            saveChanges()
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupUI() {
        // --- FIELD READ-ONLY (LOCKED) ---
        binding.textViewMenuNameValue.text = item.namaMenu
        binding.textViewDateValue.text = item.tanggal

        binding.textViewMenuNameValue.isEnabled = false
        binding.textViewDateValue.isEnabled = false

        // Ambil nilai lama (jika ada)
        val nilai = item.nilai

        if (item.isCoffee) {
            binding.linearLayoutCoffeeDetails.visibility = View.VISIBLE
            binding.linearLayoutNonCoffeeDetails.visibility = View.GONE

            binding.textViewCoffeeBeanValue.text = item.namaBeans
            binding.textViewCoffeeBeanValue.isEnabled = false

            // Set Slider Values (Default 0 jika null)
            binding.seekBarAroma.progress = nilai?.aroma ?: 0
            binding.seekBarSweetness.progress = nilai?.kemanisan ?: 0
            binding.seekBarAcidity.progress = nilai?.keasaman ?: 0
            binding.seekBarBitterness.progress = nilai?.kepahitan ?: 0
            binding.seekBarBody.progress = nilai?.kekentalan ?: 0
        } else {
            binding.linearLayoutCoffeeDetails.visibility = View.GONE
            binding.linearLayoutNonCoffeeDetails.visibility = View.VISIBLE
            binding.textViewNonCoffeeItemValue.text = item.namaMenu
            binding.textViewNonCoffeeItemValue.isEnabled = false
        }

        // Catatan
        binding.editTextCatatan.setText(nilai?.catatan ?: "")
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
                Toast.makeText(context, "Review Disimpan!", Toast.LENGTH_SHORT).show()
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