// File: D:/github_rama/Ukopia/app/src/main/java/com/example/ukopia/ui/loyalty/LoyaltyFragment.kt
package com.example.ukopia.ui.loyalty

import android.os.Bundle
import android.util.Log
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
import kotlin.math.min

class LoyaltyFragment : Fragment() {

    private var _binding: FragmentLoyaltyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoyaltyViewModel by activityViewModels()
    private lateinit var adapter: LoyaltyAdapter

    // State untuk Pagination Reward
    private var currentPageReward = 0
    private val rewardsPerPage = 2
    private val maxRewards = ALL_LOYALTY_REWARDS.size // Total reward dari daftar ALL_LOYALTY_REWARDS (19)

    // --- State baru untuk Pagination Stamp ---
    private var currentPageStamp = 0
    private val stampsPerPage = 10 // Satu halaman 10 stamp (sesuai grid 5x2)
    private val maxStamps = 100 // Jumlah stamp maksimum yang Anda miliki di XML (1-100)
    // --- Akhir State baru ---


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

        // --- Setup Button Navigasi Stamp ---
        binding.btnPrevStamp.setOnClickListener { navigateStamp(false) }
        binding.btnNextStamp.setOnClickListener { navigateStamp(true) }
        // --- Akhir Setup Button Navigasi Stamp ---


        viewModel.loyaltyUserStatus.observe(viewLifecycleOwner) { status ->
            if (status != null) { // Pastikan status tidak null
                binding.textViewLoyaltyPoints.text = getString(R.string.loyalty_points_format, status.totalPoints)
                updateStampCard(status.totalPoints) // Panggil untuk update stamp card
                updateRewardDisplay(status.totalPoints, status) // Panggil fungsi utama reward
            } else {
                Log.w("LoyaltyFragment", "LoyaltyUserStatus is null, cannot update UI.")
            }
        }

        viewModel.loyaltyItems.observe(viewLifecycleOwner) { items ->
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

    private fun getRewardContainers(): List<MaterialCardView> {
        val containers = mutableListOf<MaterialCardView>()
        // Karena layout statis, kita perlu mengumpulkan semua ID reward_card_X
        for (i in 1..maxRewards) {
            val cardId = resources.getIdentifier("reward_card_$i", "id", requireContext().packageName)
            binding.root.findViewById<MaterialCardView>(cardId)?.let {
                containers.add(it)
            } ?: Log.w("LoyaltyFragment", "Reward card with ID 'reward_card_$i' not found in layout. Make sure to define it in XML if maxRewards > 2.")
        }
        return containers
    }

    private fun getPaginatedRewards(totalPoints: Int): List<List<LoyaltyReward>> {
        // Filter reward yang relevan (misal, yang bisa dicapai atau 10 poin di atas)
        val filteredRewards = ALL_LOYALTY_REWARDS.filter { it.threshold <= totalPoints + 10 }
        return filteredRewards.chunked(rewardsPerPage)
    }

    private fun updateRewardDisplay(totalPoints: Int, status: LoyaltyUserStatus) {
        val paginatedRewards = getPaginatedRewards(totalPoints)
        val totalPages = paginatedRewards.size

        Log.d("LoyaltyFragment", "updateRewardDisplay - Total Points: $totalPoints, Total Pages: $totalPages, Current Page (before adjust): $currentPageReward")

        // --- MODIFIKASI: Sesuaikan currentPageReward secara otomatis saat update ---
        val firstRelevantRewardIndex = ALL_LOYALTY_REWARDS.indexOfFirst { it.threshold <= totalPoints + 10 }
        val targetDefaultRewardPage = if (firstRelevantRewardIndex != -1) firstRelevantRewardIndex / rewardsPerPage else 0
        currentPageReward = targetDefaultRewardPage.coerceIn(0, if (totalPages > 0) totalPages - 1 else 0)
        // --- AKHIR MODIFIKASI: Sesuaikan currentPageReward secara otomatis ---

        // Pastikan juga currentPageReward tidak keluar dari batas total halaman
        if (totalPages == 0) {
            currentPageReward = 0
        } else if (currentPageReward >= totalPages) {
            currentPageReward = totalPages - 1
        } else if (currentPageReward < 0) {
            currentPageReward = 0
        }


        val currentRewards = paginatedRewards.getOrNull(currentPageReward) ?: emptyList()
        val allRewardContainers = getRewardContainers() // Ambil semua 19 container

        Log.d("LoyaltyFragment", "updateRewardDisplay - Current Page (after adjust): $currentPageReward, Current Rewards count: ${currentRewards.size}")

        // Sembunyikan semua kontainer reward terlebih dahulu
        allRewardContainers.forEach { it.visibility = View.GONE }

        if (currentRewards.isEmpty()) {
            Log.d("LoyaltyFragment", "No rewards to display for current page.")
            binding.textViewRewardProgress.text = "0 Rewards"
            binding.btnPrevReward.visibility = View.INVISIBLE
            binding.btnNextReward.visibility = View.INVISIBLE
            return
        }

        currentRewards.forEachIndexed { index, reward ->
            // Gunakan index untuk mengisi card_1 dan card_2
            if (index < rewardsPerPage) { // Pastikan hanya mengisi 2 card per halaman
                val cardView = allRewardContainers.getOrNull(index) // Ambil card_1 atau card_2
                if (cardView != null) {
                    cardView.visibility = View.VISIBLE

                    val tvRewardTitle = cardView.findViewById<TextView>(resources.getIdentifier("tv_reward_title_${index + 1}", "id", requireContext().packageName))
                    val ivRewardIcon = cardView.findViewById<ImageView>(resources.getIdentifier("iv_reward_icon_${index + 1}", "id", requireContext().packageName))
                    val tvRewardPoints = cardView.findViewById<TextView>(resources.getIdentifier("tv_reward_points_${index + 1}", "id", requireContext().packageName))
                    val btnClaim = cardView.findViewById<MaterialButton>(resources.getIdentifier("btn_claim_status_${index + 1}", "id", requireContext().packageName))

                    tvRewardTitle.text = reward.title
                    ivRewardIcon.setImageResource(reward.iconResId)
                    tvRewardPoints.text = getString(R.string.loyalty_points_format, reward.threshold)

                    val statusReward = reward.getStatus(totalPoints, status)

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
                            textColor = ContextCompat.getColor(requireContext(), /* R.color.black, as per original logic, though often 'white' for dark buttons */ R.color.black)
                        }
                    }

                    btnClaim.text = statusText
                    btnClaim.setBackgroundColor(backgroundColor)
                    btnClaim.setTextColor(textColor)

                    btnClaim.setOnClickListener(null)
                    btnClaim.isEnabled = false
                } else {
                    Log.w("LoyaltyFragment", "Attempted to display reward at index $index, but cardView is null (card_${index+1}). Check XML for correct ID.")
                }
            } else {
                Log.w("LoyaltyFragment", "Attempted to display reward at index $index, but only ${rewardsPerPage} containers are shown per page. Reward will not be visible.")
            }
        }

        binding.btnPrevReward.visibility = if (currentPageReward > 0) View.VISIBLE else View.INVISIBLE
        binding.btnNextReward.visibility = if (currentPageReward < totalPages - 1) View.VISIBLE else View.INVISIBLE

        val start = currentPageReward * rewardsPerPage + 1
        val end = currentPageReward * rewardsPerPage + currentRewards.size
        binding.textViewRewardProgress.text = "Rewards $start - $end"
    }

    private fun navigateReward(next: Boolean) {
        val totalPoints = viewModel.loyaltyUserStatus.value?.totalPoints ?: 0
        val totalPages = getPaginatedRewards(totalPoints).size

        if (next && currentPageReward < totalPages - 1) {
            currentPageReward++
        } else if (!next && currentPageReward > 0) {
            currentPageReward--
        }

        viewModel.loyaltyUserStatus.value?.let { status ->
            updateRewardDisplay(status.totalPoints, status)
        }
    }

    // --- MODIFIKASI FUNGSI updateStampCard ---
    private fun updateStampCard(totalPoints: Int) {
        val totalStampPages = (maxStamps + stampsPerPage - 1) / stampsPerPage

        // --- MODIFIKASI: Sesuaikan currentPageStamp secara otomatis ---
        // Poin pengguna menentukan halaman maksimum yang bisa dia akses.
        val maxReachableStamp = totalPoints
        val maxReachablePage = if (maxReachableStamp > 0) (maxReachableStamp - 1) / stampsPerPage else 0

        // Jika currentPageStamp saat ini melebihi halaman yang bisa diakses, kembalikan ke halaman maksimal yang bisa diakses.
        // Ini memastikan saat user punya 11 poin, dia langsung di halaman 11-20
        if (currentPageStamp > maxReachablePage) {
            currentPageStamp = maxReachablePage
        }
        // Pastikan juga currentPageStamp tidak keluar dari batas total halaman yang ada di XML (maxStamps)
        if (totalStampPages == 0) {
            currentPageStamp = 0
        } else if (currentPageStamp >= totalStampPages) {
            currentPageStamp = totalStampPages - 1
        } else if (currentPageStamp < 0) {
            currentPageStamp = 0
        }
        // --- AKHIR MODIFIKASI: Sesuaikan currentPageStamp secara otomatis ---

        val startStampIndex = currentPageStamp * stampsPerPage + 1
        val endStampIndex = (currentPageStamp + 1) * stampsPerPage

        for (i in 1..maxStamps) {
            val bgId = resources.getIdentifier("iv_stamp_background_$i", "id", requireContext().packageName)
            val numId = resources.getIdentifier("tv_stamp_number_$i", "id", requireContext().packageName)
            val checkId = resources.getIdentifier("iv_stamp_checkmark_$i", "id", requireContext().packageName)

            val bgView = binding.root.findViewById<ImageView>(bgId)
            val numView = binding.root.findViewById<TextView>(numId)
            val checkView = binding.root.findViewById<ImageView>(checkId)

            if (bgView != null) {
                // Tentukan apakah stamp ini harus ditampilkan di halaman saat ini
                if (i in startStampIndex..endStampIndex) {
                    bgView.visibility = View.VISIBLE
                    numView?.visibility = View.VISIBLE
                    checkView?.visibility = View.VISIBLE

                    if (i <= totalPoints) {
                        bgView.setBackgroundResource(R.drawable.circle_background_white_stroke_black_fill)
                        numView?.visibility = View.GONE
                        checkView?.visibility = View.VISIBLE
                    } else {
                        bgView.setBackgroundResource(R.drawable.reward_circle_background_default)
                        numView?.visibility = View.VISIBLE
                        numView?.text = i.toString()
                        numView?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                        checkView?.visibility = View.GONE
                    }
                } else {
                    // Sembunyikan stamp yang tidak termasuk di halaman saat ini
                    bgView.visibility = View.GONE
                    numView?.visibility = View.GONE
                    checkView?.visibility = View.GONE
                }
            } else {
                // Log jika ID stamp tidak ditemukan di XML. Ini terjadi jika maxStamps > 10
                // dan XML hanya memiliki ID stamp 1-10.
                if (i > 10) { // Hanya log untuk stamp di luar rentang 1-10 yang ada di XML
                    Log.w("LoyaltyFragment", "Stamp view with ID 'iv_stamp_background_$i' not found in layout. Make sure to define it in XML if maxStamps > 10.")
                }
            }
        }

        // Update teks progress stamp
        val displayEndStamp = min(endStampIndex, maxStamps)
        binding.textViewStampProgress.text = "Stamps $startStampIndex - $displayEndStamp"

        // --- MODIFIKASI LOGIKA VISIBILITAS TOMBOL NAVIGASI STAMP ---
        // Tombol Prev Stamp
        binding.btnPrevStamp.visibility = if (currentPageStamp > 0) View.VISIBLE else View.INVISIBLE

        // Tombol Next Stamp terlihat jika ada halaman selanjutnya DAN user memiliki poin untuk setidaknya 1 stamp di halaman selanjutnya
        val canGoNextPage = (currentPageStamp < totalStampPages - 1) && (currentPageStamp < maxReachablePage)
        binding.btnNextStamp.visibility = if (canGoNextPage) View.VISIBLE else View.INVISIBLE

        // Jika hanya ada satu halaman total, sembunyikan kedua tombol navigasi
        if (totalStampPages <= 1) {
            binding.btnPrevStamp.visibility = View.INVISIBLE
            binding.btnNextStamp.visibility = View.INVISIBLE
        }
        // --- AKHIR MODIFIKASI LOGIKA VISIBILITAS TOMBOL NAVIGASI STAMP ---
    }

    // --- Tambahkan fungsi navigasi stamp baru ---
    private fun navigateStamp(next: Boolean) {
        val totalStampPages = (maxStamps + stampsPerPage - 1) / stampsPerPage
        val totalPoints = viewModel.loyaltyUserStatus.value?.totalPoints ?: 0
        val maxReachableStamp = totalPoints
        val maxReachablePage = if (maxReachableStamp > 0) (maxReachableStamp - 1) / stampsPerPage else 0

        if (next) {
            // Hanya navigasi ke halaman selanjutnya jika ada dan jika user memiliki poin yang cukup
            if (currentPageStamp < totalStampPages - 1 && currentPageStamp < maxReachablePage) {
                currentPageStamp++
            }
        } else { // Previous
            if (currentPageStamp > 0) {
                currentPageStamp--
            }
        }

        viewModel.loyaltyUserStatus.value?.let { status ->
            updateStampCard(status.totalPoints)
        }
    }
    // --- Akhir fungsi navigasi stamp baru ---

    override fun onResume() {
        super.onResume()
        currentPageReward = 0
        // --- MODIFIKASI: Default currentPageStamp agar sesuai dengan poin pengguna saat resume ---
        val totalPoints = viewModel.loyaltyUserStatus.value?.totalPoints ?: 0
        val maxReachablePage = if (totalPoints > 0) (totalPoints - 1) / stampsPerPage else 0
        currentPageStamp = maxReachablePage // Langsung ke halaman yang sesuai dengan poin tertinggi
        // --- AKHIR MODIFIKASI ---
        viewModel.refreshLoyaltyData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}