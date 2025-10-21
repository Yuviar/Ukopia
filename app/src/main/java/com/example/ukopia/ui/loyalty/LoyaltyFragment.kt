package com.example.ukopia.ui.loyalty

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.SessionManager
import com.example.ukopia.LoginActivity
import com.example.ukopia.adapter.LoyaltyAdapter
import com.example.ukopia.databinding.DialogLoginRequiredBinding
import com.example.ukopia.databinding.FragmentLoyaltyBinding

class LoyaltyFragment : Fragment() {

    private lateinit var binding: FragmentLoyaltyBinding
    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels()

    private lateinit var loyaltyItemAdapter: LoyaltyAdapter

    private var pendingAddLoyaltyAction = false

    private val loginActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (SessionManager.SessionManager.isLoggedIn(requireContext())) {
                loyaltyViewModel.loyaltyUserStatus.value?.let { status ->
                    updateLoyaltyUI(status.totalPoints)
                }
                updateLoyaltyItemsVisibility()

                if (pendingAddLoyaltyAction) {
                    pendingAddLoyaltyAction = false
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container, AddLoyaltyFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }

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

        setupLoyaltyItemsRecyclerView()

        binding.fabAddRecipe.setOnClickListener {
            // --- Animasi Flash Putih ---
            val originalBackgroundTint = ContextCompat.getColor(requireContext(), R.color.black)
            val originalImageTint = ContextCompat.getColor(requireContext(), R.color.white)
            val flashColorBackground = ContextCompat.getColor(requireContext(), R.color.white)
            val flashColorImage = ContextCompat.getColor(requireContext(), R.color.black) // Icon menjadi hitam agar terlihat

            binding.fabAddRecipe.backgroundTintList = ColorStateList.valueOf(flashColorBackground)
            binding.fabAddRecipe.imageTintList = ColorStateList.valueOf(flashColorImage)

            Handler(Looper.getMainLooper()).postDelayed({
                // Pastikan fragment masih melekat sebelum memperbarui UI
                if (isAdded && activity != null) {
                    binding.fabAddRecipe.backgroundTintList = ColorStateList.valueOf(originalBackgroundTint)
                    binding.fabAddRecipe.imageTintList = ColorStateList.valueOf(originalImageTint)
                }
            }, 150) // Durasi flash: 150 milidetik

            // --- Logika Asli Klik ---
            if (SessionManager.SessionManager.isLoggedIn(requireContext())) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, AddLoyaltyFragment())
                    .addToBackStack(null)
                    .commit()
            } else {
                pendingAddLoyaltyAction = true
                showLoginRequiredDialog()
            }
        }

        loyaltyViewModel.loyaltyUserStatus.observe(viewLifecycleOwner) { status ->
            updateLoyaltyUI(status.totalPoints)
            updateLoyaltyItemsVisibility()
        }

        loyaltyViewModel.loyaltyItems.observe(viewLifecycleOwner) { items ->
            loyaltyItemAdapter.submitList(items)
            updateLoyaltyItemsVisibility()
        }
    }

    override fun onResume() {
        super.onResume()
        loyaltyViewModel.loyaltyUserStatus.value?.let { status ->
            updateLoyaltyUI(status.totalPoints)
        }
    }

    private fun updateLoyaltyUI(totalPoints: Int) {
        val (levelName, badgeDrawableId) = loyaltyViewModel.getLoyaltyLevel(requireContext())
        binding.textViewLoyaltyLevel.text = levelName
        binding.imageViewLevelStar.setImageResource(badgeDrawableId)
        binding.imageViewLevelStar.visibility = View.VISIBLE

        binding.textViewLoyaltyPoints.text = getString(R.string.loyalty_points_format, totalPoints)

        binding.progressContainer.visibility = View.VISIBLE
        val (currentVisualProgress, visualProgressMax) = loyaltyViewModel.getVisualStampProgress()
        binding.progressBarStampProgress.max = visualProgressMax
        binding.progressBarStampProgress.progress = currentVisualProgress
        binding.textViewStampProgress.text = loyaltyViewModel.getRewardProgressMessage(requireContext())
    }

    private fun setupLoyaltyItemsRecyclerView() {
        loyaltyItemAdapter = LoyaltyAdapter { item ->
            LoyaltyDetailDialogFragment.newInstance(item).show(parentFragmentManager, "LoyaltyDetailPopup")
        }
        binding.recyclerViewLoyaltyItems.layoutManager =
            LinearLayoutManager(requireContext())
        binding.recyclerViewLoyaltyItems.adapter = loyaltyItemAdapter
    }

    private fun updateLoyaltyItemsVisibility() {
        if (loyaltyViewModel.loyaltyItems.value.isNullOrEmpty()) {
            binding.placeholderContainer.visibility = View.VISIBLE
            binding.recyclerViewLoyaltyItems.visibility = View.GONE
        } else {
            binding.placeholderContainer.visibility = View.GONE
            binding.recyclerViewLoyaltyItems.visibility = View.VISIBLE
        }
    }

    private fun showLoginRequiredDialog() {
        val dialogBinding = DialogLoginRequiredBinding.inflate(layoutInflater)
        val customAlertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.buttonDialogLogin.setOnClickListener {
            loginActivityResultLauncher.launch(Intent(requireContext(), LoginActivity::class.java))
            customAlertDialog.dismiss()
        }

        dialogBinding.buttonDialogCancel.setOnClickListener {
            customAlertDialog.dismiss()
        }

        customAlertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        customAlertDialog.show()
    }
}