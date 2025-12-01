// File: D:/github_rama/Ukopia/app/src/main/java/com/example/ukopia/ui/loyalty/RewardListFragment.kt
package com.example.ukopia.ui.loyalty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.MainActivity
import com.example.ukopia.data.ALL_LOYALTY_REWARDS
import com.example.ukopia.data.LoyaltyUserStatus
import com.example.ukopia.data.getStatus // Import helper status
import com.example.ukopia.databinding.FragmentRewardListBinding

class RewardListFragment : Fragment() {

    private var _binding: FragmentRewardListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoyaltyViewModel by activityViewModels()
    private val adapter = RewardAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRewardListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.setBottomNavVisibility(View.GONE)

        binding.recyclerViewRewards.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewRewards.adapter = adapter

        binding.btnBackRewards.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        viewModel.loyaltyUserStatus.observe(viewLifecycleOwner) { status ->
            status?.let {
                // Filter hanya reward yang sudah tercapai (Poin >= Threshold)
                val achievedRewards = generateAchievedRewards(it.totalPoints, it)
                adapter.submitList(achievedRewards)
                binding.emptyStateText.visibility = if (achievedRewards.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    /**
     * Menggunakan daftar statis dan filter hanya reward yang poinnya sudah tercapai.
     */
    private fun generateAchievedRewards(points: Int, status: LoyaltyUserStatus): List<RewardItem> {
        return ALL_LOYALTY_REWARDS
            .filter { it.threshold <= points } // Filter hanya yang sudah tercapai
            .map { reward ->
                val statusText = reward.getStatus(points, status)

                // Ambil tanggal klaim dari helper jika statusnya CLAIMED
                val claimDate = if (statusText == "CLAIMED") getClaimDate(reward.threshold, status) else null

                RewardItem(
                    id = reward.threshold,
                    name = reward.title,
                    pointsRequired = reward.threshold,
                    iconResId = reward.iconResId,
                    pointsMet = true,
                    claimedDate = claimDate
                )
            }.reversed() // Tampilkan reward terbaru di atas
    }

    /**
     * Mengambil tanggal klaim dari properti yang sesuai di LoyaltyUserStatus.
     */
    private fun getClaimDate(threshold: Int, status: LoyaltyUserStatus): String? {
        return when(threshold) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as? MainActivity)?.setBottomNavVisibility(View.VISIBLE)
    }
}