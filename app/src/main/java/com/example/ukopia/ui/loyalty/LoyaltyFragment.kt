// File: D:/github_rama/Ukopia/app/src/main/java/com/example/ukopia/ui/loyalty/LoyaltyFragment.kt
package com.example.ukopia.ui.loyalty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.SessionManager
import com.example.ukopia.adapter.LoyaltyAdapter
import com.example.ukopia.data.ALL_LOYALTY_REWARDS
import com.example.ukopia.data.LoyaltyReward
import com.example.ukopia.data.LoyaltyUserStatus
import com.example.ukopia.data.getStatus // Import helper status
import com.example.ukopia.databinding.FragmentLoyaltyBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class LoyaltyFragment : Fragment() {

    private var _binding: FragmentLoyaltyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoyaltyViewModel by activityViewModels()
    private lateinit var adapter: LoyaltyAdapter

    // State untuk Pagination Reward (Hanya 2 reward per tampilan)
    private var currentPage = 0
    private val rewardsPerPage = 2

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoyaltyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userName = SessionManager.getUserName(requireContext())
        binding.textViewUserName.text = userName ?: "Guest"

        setupRecyclerView()

        binding.btnRewardHistory.setOnClickListener {
            (activity as? MainActivity)?.navigateToFragment(RewardListFragment())
        }

        // Setup Button Navigasi Reward
        binding.btnPrevReward.setOnClickListener { navigateReward(false) }
        binding.btnNextReward.setOnClickListener { navigateReward(true) }


        viewModel.loyaltyUserStatus.observe(viewLifecycleOwner) { status ->
            binding.textViewLoyaltyPoints.text = getString(R.string.loyalty_points_format, status.totalPoints)
            updateStampCard(status.totalPoints)
            updateRewardDisplay(status.totalPoints, status) // Panggil fungsi utama reward
        }

        viewModel.pendingItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            binding.placeholderContainer.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerViewLoyaltyItems.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        adapter = LoyaltyAdapter { item ->
            val dialog = LoyaltyDetailDialogFragment.newInstance(item)
            dialog.show(parentFragmentManager, "LoyaltyDetailDialog")
        }
        binding.recyclerViewLoyaltyItems.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewLoyaltyItems.adapter = adapter
    }

    // --- Reward Display Logic ---

    // Mengambil 2 card template baru di XML
    private fun getRewardContainers(): List<MaterialCardView> {
        // Mengasumsikan Anda telah mengganti ID statis lama dengan
        // ID: reward_card_1 dan reward_card_2 di XML
        return listOf(
            binding.rewardCard1,
            binding.rewardCard2
        )
    }

    private fun getPaginatedRewards(totalPoints: Int): List<List<LoyaltyReward>> {
        // Logika: Tampilkan rewards yang thresholdnya sampai kelipatan 5 berikutnya dari poin saat ini
        val targetThreshold = ((totalPoints + 5) / 5) * 5
        val filteredRewards = ALL_LOYALTY_REWARDS.filter { it.threshold <= targetThreshold }

        return filteredRewards.chunked(rewardsPerPage)
    }

    private fun updateRewardDisplay(totalPoints: Int, status: LoyaltyUserStatus) {
        val paginatedRewards = getPaginatedRewards(totalPoints)
        val totalPages = paginatedRewards.size

        // Validasi currentPage
        if (currentPage >= totalPages && totalPages > 0) currentPage = totalPages - 1
        if (currentPage < 0) currentPage = 0

        val currentRewards = paginatedRewards.getOrNull(currentPage) ?: emptyList()
        val rewardContainers = getRewardContainers()

        // Sembunyikan semua container terlebih dahulu
        rewardContainers.forEach { it.visibility = View.GONE }

        if (currentRewards.isEmpty()) {
            binding.textViewRewardProgress.text = "0 Rewards"
            binding.btnPrevReward.visibility = View.INVISIBLE
            binding.btnNextReward.visibility = View.INVISIBLE
            return
        }

        // Tampilkan hanya reward untuk halaman saat ini
        currentRewards.forEachIndexed { index, reward ->
            val cardView = rewardContainers[index]
            cardView.visibility = View.VISIBLE

            // --- Mengambil komponen di dalam Card Template ---
            // Menggunakan ID yang disederhanakan: _1 dan _2
            val iconId = resources.getIdentifier("iv_reward_icon_${index + 1}", "id", requireContext().packageName)
            val titleId = resources.getIdentifier("tv_reward_title_${index + 1}", "id", requireContext().packageName)
            val pointsId = resources.getIdentifier("tv_reward_points_${index + 1}", "id", requireContext().packageName)
            val claimBtnId = resources.getIdentifier("btn_claim_status_${index + 1}", "id", requireContext().packageName)

            val tvRewardTitle = cardView.findViewById<TextView>(titleId)
            val ivRewardIcon = cardView.findViewById<ImageView>(iconId)
            val tvRewardPoints = cardView.findViewById<TextView>(pointsId)
            val btnClaim = cardView.findViewById<MaterialButton>(claimBtnId)

            // --- Logic Status ---
            val statusReward = reward.getStatus(totalPoints, status)

            // 1. Update Konten
            tvRewardTitle.text = reward.title
            ivRewardIcon.setImageResource(reward.iconResId)
            tvRewardPoints.text = "${reward.threshold} Points"

            // 2. Update Teks dan Warna Status
            val statusText: String
            val backgroundColor: Int
            val textColor: Int

            when (statusReward) {
                "NOT ACHIEVED" -> {
                    statusText = "NOT ACHIEVED"
                    backgroundColor = ContextCompat.getColor(requireContext(), R.color.light_grey)
                    textColor = ContextCompat.getColor(requireContext(), R.color.black)
                }
                "CLAIMED" -> {
                    statusText = "CLAIMED"
                    backgroundColor = ContextCompat.getColor(requireContext(), R.color.black)
                    textColor = ContextCompat.getColor(requireContext(), R.color.white)
                }
                "NOT YET CLAIMED" -> {
                    statusText = "NOT YET CLAIMED"
                    backgroundColor = ContextCompat.getColor(requireContext(), R.color.black)
                    textColor = ContextCompat.getColor(requireContext(), R.color.white)
                }
                else -> {
                    statusText = "-"
                    backgroundColor = ContextCompat.getColor(requireContext(), R.color.light_grey)
                    textColor = ContextCompat.getColor(requireContext(), R.color.black)
                }
            }

            btnClaim.text = statusText
            btnClaim.setBackgroundColor(backgroundColor)
            btnClaim.setTextColor(textColor)

            // Nonaktifkan tombol (klaim hanya oleh admin)
            btnClaim.setOnClickListener(null)
            btnClaim.isEnabled = false
        }

        // Update Navigasi
        binding.btnPrevReward.visibility = if (currentPage > 0) View.VISIBLE else View.INVISIBLE
        binding.btnNextReward.visibility = if (currentPage < totalPages - 1) View.VISIBLE else View.INVISIBLE

        // Update Indikator Progres
        val start = currentPage * rewardsPerPage + 1
        val end = currentPage * rewardsPerPage + currentRewards.size
        binding.textViewRewardProgress.text = "Rewards $start - $end"
    }

    private fun navigateReward(next: Boolean) {
        val totalPoints = viewModel.loyaltyUserStatus.value?.totalPoints ?: 0
        val totalPages = getPaginatedRewards(totalPoints).size

        if (next && currentPage < totalPages - 1) {
            currentPage++
        } else if (!next && currentPage > 0) {
            currentPage--
        }

        viewModel.loyaltyUserStatus.value?.let { status ->
            updateRewardDisplay(status.totalPoints, status)
        }
    }

    private fun updateStampCard(totalPoints: Int) {
        // ... (Logika Stamp Card tetap sama)
        for (i in 1..10) {
            val bgId = resources.getIdentifier("iv_stamp_background_$i", "id", requireContext().packageName)
            val numId = resources.getIdentifier("tv_stamp_number_$i", "id", requireContext().packageName)
            val checkId = resources.getIdentifier("iv_stamp_checkmark_$i", "id", requireContext().packageName)

            val bgView = binding.root.findViewById<ImageView>(bgId)
            val numView = binding.root.findViewById<TextView>(numId)
            val checkView = binding.root.findViewById<ImageView>(checkId)

            if (bgView != null) {
                if (i <= totalPoints) {
                    bgView.setBackgroundResource(R.drawable.circle_background_white_stroke_black_fill)
                    numView.visibility = View.GONE
                    checkView.visibility = View.VISIBLE
                } else {
                    bgView.setBackgroundResource(R.drawable.reward_circle_background_default)
                    numView.visibility = View.VISIBLE
                    numView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    checkView.visibility = View.GONE
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        // Reset currentPage ke 0 setiap kali kembali ke halaman utama loyalty
        currentPage = 0
        viewModel.refreshLoyaltyData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}