package com.example.ukopia.ui.loyalty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.data.ALL_LOYALTY_REWARDS
import com.example.ukopia.data.LoyaltyUserStatus
import com.example.ukopia.databinding.FragmentRewardListBinding

class RewardListFragment : Fragment() {

    private lateinit var binding: FragmentRewardListBinding
    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels()
    private lateinit var rewardAdapter: RewardAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRewardListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pastikan bottom navigation terlihat jika diperlukan
        (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)

        setupRecyclerView()
        observeLoyaltyStatus()

        // Setup tombol kembali di header baru
        binding.btnBackRewards.setOnClickListener {
            (activity as? MainActivity)?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        rewardAdapter = RewardAdapter()
        binding.recyclerViewRewards.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = rewardAdapter
        }
    }

    private fun observeLoyaltyStatus() {
        loyaltyViewModel.loyaltyUserStatus.observe(viewLifecycleOwner) { status ->
            val rewardItems = convertLoyaltyStatusToRewardItems(status)
            rewardAdapter.submitList(rewardItems)
            updateEmptyState(rewardItems.isEmpty())
        }
    }

    // Fungsi helper untuk mengkonversi daftar ALL_LOYALTY_REWARDS menjadi RewardItem
    // dengan status klaim dari LoyaltyUserStatus
    private fun convertLoyaltyStatusToRewardItems(status: LoyaltyUserStatus): List<RewardItem> {
        return ALL_LOYALTY_REWARDS
            .filter { loyaltyReward ->
                // HANYA SERTAKAN REWARD YANG POINNYA SUDAH TERCUKUPI
                status.totalPoints >= loyaltyReward.threshold
            }
            .map { loyaltyReward ->
                // Untuk item yang sudah difilter, pointsMet PASTI true
                val pointsMet = true
                val claimedDate = getClaimedDateForReward(status, loyaltyReward.threshold)

                RewardItem(
                    id = loyaltyReward.threshold, // Menggunakan threshold sebagai ID unik
                    name = loyaltyReward.title,
                    pointsRequired = loyaltyReward.threshold,
                    iconResId = loyaltyReward.iconResId,
                    pointsMet = pointsMet, // Sekarang ini akan selalu true karena sudah difilter
                    claimedDate = claimedDate
                )
            }
    }

    // Fungsi helper untuk mendapatkan tanggal klaim dari LoyaltyUserStatus
    private fun getClaimedDateForReward(status: LoyaltyUserStatus, threshold: Int): String? {
        return when (threshold) {
            5 -> status.discount10ClaimDate
            10 -> status.freeServeClaimDate
            15 -> status.discount10Slot15ClaimDate
            20 -> status.freeTshirtClaimDate
            25 -> status.discount10_25ClaimDate
            30 -> status.freeServe_30ClaimDate
            35 -> status.discount10_35ClaimDate
            40 -> status.freeServe_40ClaimDate
            45 -> status.discount10_45ClaimDate
            50 -> status.freeServe_50ClaimDate
            55 -> status.discount10_55ClaimDate
            60 -> status.freeServe_60ClaimDate
            65 -> status.discount10_65ClaimDate
            70 -> status.freeServe_70ClaimDate
            75 -> status.discount10_75ClaimDate
            80 -> status.freeServe_80ClaimDate
            85 -> status.discount10_85ClaimDate
            90 -> status.freeServe_90ClaimDate
            95 -> status.discount10_95ClaimDate
            else -> null
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.recyclerViewRewards.visibility = View.GONE
            binding.emptyStateText.visibility = View.VISIBLE
        } else {
            binding.recyclerViewRewards.visibility = View.VISIBLE
            binding.emptyStateText.visibility = View.GONE
        }
    }
}