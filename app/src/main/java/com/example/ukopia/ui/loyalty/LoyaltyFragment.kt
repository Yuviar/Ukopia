package com.example.ukopia.ui.loyalty

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.SessionManager
import com.example.ukopia.adapter.LoyaltyAdapter
import com.example.ukopia.data.ALL_LOYALTY_REWARDS
import com.example.ukopia.data.LoyaltyReward
import com.example.ukopia.data.LoyaltyItemV2
import com.example.ukopia.data.LoyaltyUserStatus
import com.example.ukopia.data.getStatus
import com.example.ukopia.databinding.FragmentLoyaltyBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.util.Locale
import kotlin.math.min

class LoyaltyFragment : Fragment() {

    private var _binding: FragmentLoyaltyBinding? = null
    private val binding get() = _binding!!

    // Menggunakan activityViewModels karena LoyaltyViewModel mungkin perlu dibagikan antar Activity
    // Pastikan ViewModel ini sudah benar dan diinisialisasi melalui Factory jika perlu
    private val viewModel: LoyaltyViewModel by activityViewModels()

    // Menggunakan LoyaltyAdapter yang sudah ada di konteks Anda
    private lateinit var adapter: LoyaltyAdapter

    private var currentPageReward = 0
    private val rewardsPerPage = 2
    private val maxRewards = ALL_LOYALTY_REWARDS.size
    private var maxReachableRewardPage = 0

    private var currentPageStamp = 0
    private val stampsPerPage = 10
    private val maxStamps = 100

    private var allLoyaltyItems: List<LoyaltyItemV2> = emptyList()
    private var currentSearchQuery: String = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoyaltyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userName = SessionManager.getUserName(requireContext())
        binding.textViewUserName.text = userName ?: getString(R.string.greeting_salutation_default)

        setupRecyclerView()
        setupSearchBar()

        binding.btnRewardHistory.setOnClickListener {
            // Pastikan RewardListFragment Anda ada dan sudah benar
            (activity as? MainActivity)?.navigateToFragment(RewardListFragment())
        }

        binding.btnPrevReward.setOnClickListener { navigateReward(false) }
        binding.btnNextReward.setOnClickListener { navigateReward(true) }

        binding.btnPrevStamp.setOnClickListener { navigateStamp(false) }
        binding.btnNextStamp.setOnClickListener { navigateStamp(true) }


        viewModel.loyaltyUserStatus.observe(viewLifecycleOwner) { status ->
            if (status != null) {
                binding.textViewLoyaltyPoints.text = getString(R.string.loyalty_points_format, status.totalPoints)
                updateStampCard(status.totalPoints)
                updateRewardDisplay(status.totalPoints, status)
            } else {
                Log.w("LoyaltyFragment", "LoyaltyUserStatus is null, cannot update UI.")
            }
        }

        // Observer utama untuk loyalty items (riwayat order)
        viewModel.loyaltyItems.observe(viewLifecycleOwner) { items ->
            allLoyaltyItems = items // Simpan daftar asli
            displayFilteredLoyaltyItems() // Tampilkan dan atur visibilitas UI
        }
    }

    private fun setupRecyclerView() {
        adapter = LoyaltyAdapter { item ->
            // Pastikan LoyaltyDetailDialogFragment.newInstance menerima LoyaltyItemV2
            val dialog = LoyaltyDetailDialogFragment.newInstance(item)
            dialog.show(parentFragmentManager, "LoyaltyDetailDialog")
        }
        binding.recyclerViewLoyaltyItems.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewLoyaltyItems.adapter = adapter
    }

    private fun setupSearchBar() {
        binding.etSearchLoyaltyHistory.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString()
                binding.ivClearSearchLoyaltyHistory.visibility = if (s.isNullOrBlank()) View.GONE else View.VISIBLE
                displayFilteredLoyaltyItems()
            }
            override fun afterTextChanged(s: Editable?) { }
        })

        binding.ivClearSearchLoyaltyHistory.setOnClickListener {
            binding.etSearchLoyaltyHistory.text?.clear()
        }
    }

    // Mengatur visibilitas search bar, placeholder, dan RecyclerView riwayat loyalty
    private fun displayFilteredLoyaltyItems() {
        var filteredList = allLoyaltyItems

        if (currentSearchQuery.isNotBlank()) {
            val lowerCaseQuery = currentSearchQuery.toLowerCase(Locale.ROOT)
            filteredList = filteredList.filter {
                it.namaMenu.toLowerCase(Locale.ROOT).contains(lowerCaseQuery)
            }
        }

        adapter.submitList(filteredList)

        // Logika untuk menampilkan/menyembunyikan search bar dan placeholder
        if (allLoyaltyItems.isEmpty()) { // Jika daftar loyalty asli (tanpa filter) kosong
            binding.cardSearchLoyalty.visibility = View.GONE // Menggunakan ID yang benar dari XML
            binding.placeholderContainer.visibility = View.VISIBLE
            binding.textViewPlaceholder.text = getString(R.string.no_loyalty_data_placeholder)
            binding.recyclerViewLoyaltyItems.visibility = View.GONE
        } else { // Jika ada data loyalty
            binding.cardSearchLoyalty.visibility = View.VISIBLE // Menggunakan ID yang benar dari XML
            if (filteredList.isEmpty()) { // Jika ada data tapi hasil filter kosong
                binding.placeholderContainer.visibility = View.VISIBLE
                binding.textViewPlaceholder.text = getString(R.string.no_results_found) // Pastikan string ini ada di strings.xml
                binding.recyclerViewLoyaltyItems.visibility = View.GONE
            } else { // Jika ada data dan hasil filter tidak kosong
                binding.placeholderContainer.visibility = View.GONE
                binding.recyclerViewLoyaltyItems.visibility = View.VISIBLE
            }
        }
    }

    private fun getRewardContainers(): List<MaterialCardView> {
        val containers = mutableListOf<MaterialCardView>()
        for (i in 1..rewardsPerPage) {
            val cardId = resources.getIdentifier("reward_card_$i", "id", requireContext().packageName)
            binding.root.findViewById<MaterialCardView>(cardId)?.let {
                containers.add(it)
            } ?: Log.w("LoyaltyFragment", "Reward card with ID 'reward_card_$i' not found in layout. Make sure to define it in XML if rewardsPerPage > 2.")
        }
        return containers
    }

    private fun getPaginatedRewards(): List<List<LoyaltyReward>> {
        return ALL_LOYALTY_REWARDS.chunked(rewardsPerPage)
    }

    private fun updateRewardDisplay(totalPoints: Int, status: LoyaltyUserStatus) {
        val paginatedRewards = getPaginatedRewards()
        val totalPages = paginatedRewards.size

        Log.d("LoyaltyFragment", "RewardDisplay: Total Points=$totalPoints, Total Pages=$totalPages, Current Page (before adjust)=$currentPageReward")
        Log.d("LoyaltyFragment", "RewardDisplay: Paginated Rewards Structure: ${paginatedRewards.joinToString { page -> page.joinToString { getString(it.titleResId) } }}")

        val currentRewards = paginatedRewards.getOrNull(currentPageReward) ?: emptyList()
        val allRewardContainers = getRewardContainers()

        Log.d("LoyaltyFragment", "RewardDisplay: Current Page (after adjust)=$currentPageReward, Current Rewards count=${currentRewards.size}")
        Log.d("LoyaltyFragment", "RewardDisplay: Current Rewards on page $currentPageReward: ${currentRewards.joinToString { "${getString(it.titleResId)} (${it.threshold} pts)" }}")

        allRewardContainers.forEach { it.visibility = View.GONE }

        if (currentRewards.isEmpty()) {
            Log.d("LoyaltyFragment", "RewardDisplay: No rewards to display for current page. Check paginatedRewards content.")
            binding.textViewRewardProgress.text = "0 Rewards"
            binding.btnPrevReward.visibility = View.INVISIBLE
            binding.btnNextReward.visibility = View.INVISIBLE
            return
        }

        currentRewards.forEachIndexed { index, reward ->
            if (index < rewardsPerPage) {
                val cardView = allRewardContainers.getOrNull(index)
                if (cardView != null) {
                    cardView.visibility = View.VISIBLE
                    val tvRewardTitle = cardView.findViewById<TextView>(resources.getIdentifier("tv_reward_title_${index + 1}", "id", requireContext().packageName))
                    val ivRewardIcon = cardView.findViewById<ImageView>(resources.getIdentifier("iv_reward_icon_${index + 1}", "id", requireContext().packageName))
                    val tvRewardPoints = cardView.findViewById<TextView>(resources.getIdentifier("tv_reward_points_${index + 1}", "id", requireContext().packageName))
                    val btnClaim = cardView.findViewById<MaterialButton>(resources.getIdentifier("btn_claim_status_${index + 1}", "id", requireContext().packageName))

                    if (tvRewardTitle == null || ivRewardIcon == null || tvRewardPoints == null || btnClaim == null) {
                        Log.e("LoyaltyFragment", "RewardDisplay: One or more views not found for reward card ${index + 1}. Check XML IDs.")
                        return@forEachIndexed
                    }

                    tvRewardTitle.text = getString(reward.titleResId)
                    ivRewardIcon.setImageResource(reward.iconResId)
                    tvRewardPoints.text = getString(R.string.loyalty_points_needed_format, reward.threshold)

                    val statusReward = reward.getStatus(totalPoints, status)

                    val statusText: String
                    val backgroundColor: Int
                    val textColor: Int

                    when (statusReward) {
                        "NOT ACHIEVED" -> {
                            statusText = getString(R.string.reward_status_not_achieved)
                            backgroundColor = ContextCompat.getColor(requireContext(), R.color.light_grey)
                            textColor = ContextCompat.getColor(requireContext(), R.color.black)
                        }
                        "CLAIMED" -> {
                            statusText = getString(R.string.reward_claimed_status)
                            backgroundColor = ContextCompat.getColor(requireContext(), R.color.black)
                            textColor = ContextCompat.getColor(requireContext(), R.color.white)
                        }
                        "NOT YET CLAIMED" -> {
                            statusText = getString(R.string.reward_status_not_yet_claimed)
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
                    btnClaim.setOnClickListener(null)
                    btnClaim.isEnabled = false
                } else {
                    Log.w("LoyaltyFragment", "RewardDisplay: Attempted to display reward at index $index, but cardView is null (card_${index+1}). Check XML for correct ID.")
                }
            } else {
                Log.w("LoyaltyFragment", "RewardDisplay: Attempted to display reward at index $index, but only ${rewardsPerPage} containers are shown per page. Reward will not be visible.")
            }
        }

        binding.btnPrevReward.visibility = if (currentPageReward > 0) View.VISIBLE else View.INVISIBLE
        val canGoNextRewardPage = (currentPageReward < totalPages - 1) && (currentPageReward < maxReachableRewardPage)
        binding.btnNextReward.visibility = if (canGoNextRewardPage) View.VISIBLE else View.INVISIBLE

        val start = currentPageReward * rewardsPerPage + 1
        val end = currentPageReward * rewardsPerPage + currentRewards.size
        binding.textViewRewardProgress.text = getString(R.string.loyalty_rewards_progress_range_format, start, end)
    }

    private fun navigateReward(next: Boolean) {
        val paginatedRewards = getPaginatedRewards()
        val totalPages = paginatedRewards.size
        val totalPoints = viewModel.loyaltyUserStatus.value?.totalPoints ?: 0

        Log.d("LoyaltyFragment", "NavigateReward: current page $currentPageReward, total pages $totalPages, total points $totalPoints, maxReachablePage=$maxReachableRewardPage, next: $next")

        if (next) {
            if (currentPageReward < totalPages - 1 && currentPageReward < maxReachableRewardPage) {
                currentPageReward++
                Log.d("LoyaltyFragment", "NavigateReward: moved to page $currentPageReward")
            } else {
                Log.d("LoyaltyFragment", "NavigateReward: Cannot go next. Current page: $currentPageReward, Max pages: $totalPages, Max reachable page: $maxReachableRewardPage")
            }
        } else {
            if (currentPageReward > 0) {
                currentPageReward--
                Log.d("LoyaltyFragment", "NavigateReward: moved to page $currentPageReward")
            } else {
                Log.d("LoyaltyFragment", "NavigateReward: Cannot go previous, already on first page.")
            }
        }

        viewModel.loyaltyUserStatus.value?.let { status ->
            updateRewardDisplay(status.totalPoints, status)
        }
    }

    private fun updateStampCard(totalPoints: Int) {
        val totalStampPages = (maxStamps + stampsPerPage - 1) / stampsPerPage

        val maxReachableStamp = totalPoints
        val maxReachablePage = if (maxReachableStamp > 0) (maxReachableStamp - 1) / stampsPerPage else 0

        if (currentPageStamp > maxReachablePage || currentPageStamp >= totalStampPages) {
            currentPageStamp = maxReachablePage
            Log.d("LoyaltyFragment", "StampDisplay: currentPageStamp adjusted to: $currentPageStamp (maxReachablePage: $maxReachablePage)")
        }

        for (i in 0 until stampsPerPage) {
            val stampActualNumber = currentPageStamp * stampsPerPage + i + 1
            val bgId = resources.getIdentifier("iv_stamp_background_${i + 1}", "id", requireContext().packageName)
            val numId = resources.getIdentifier("tv_stamp_number_${i + 1}", "id", requireContext().packageName)
            val checkId = resources.getIdentifier("iv_stamp_checkmark_${i + 1}", "id", requireContext().packageName)

            val bgView = binding.root.findViewById<ImageView>(bgId)
            val numView = binding.root.findViewById<TextView>(numId)
            val checkView = binding.root.findViewById<ImageView>(checkId)

            if (bgView != null && numView != null && checkView != null) {
                if (stampActualNumber <= maxStamps) {
                    bgView.visibility = View.VISIBLE
                    numView.visibility = View.VISIBLE
                    checkView.visibility = View.VISIBLE

                    if (stampActualNumber <= totalPoints) {
                        bgView.setBackgroundResource(R.drawable.circle_background_white_stroke_black_fill)
                        numView.visibility = View.GONE
                        checkView.visibility = View.VISIBLE
                    } else {
                        bgView.setBackgroundResource(R.drawable.reward_circle_background_default)
                        numView.visibility = View.VISIBLE
                        numView.text = stampActualNumber.toString()
                        numView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                        checkView.visibility = View.GONE
                    }
                } else {
                    bgView.visibility = View.GONE
                    numView.visibility = View.GONE
                    checkView.visibility = View.GONE
                }
            } else {
                Log.w("LoyaltyFragment", "StampDisplay: Stamp view with ID 'iv_stamp_background_${i + 1}' (or number/checkmark) not found in layout. Make sure to define it in XML for indices 1-10.")
            }
        }

        val startDisplayStamp = currentPageStamp * stampsPerPage + 1
        val endDisplayStamp = (currentPageStamp * stampsPerPage + stampsPerPage).coerceAtMost(maxStamps)
        binding.textViewStampProgress.text = getString(R.string.loyalty_stamp_progress_format, startDisplayStamp, endDisplayStamp)

        binding.btnPrevStamp.visibility = if (currentPageStamp > 0) View.VISIBLE else View.INVISIBLE
        val canGoNextPage = (currentPageStamp < totalStampPages - 1) && (totalPoints >= (currentPageStamp + 1) * stampsPerPage)
        binding.btnNextStamp.visibility = if (canGoNextPage) View.VISIBLE else View.INVISIBLE
        if (totalStampPages <= 1) {
            binding.btnPrevStamp.visibility = View.INVISIBLE
            binding.btnNextStamp.visibility = View.INVISIBLE
        }
    }

    private fun navigateStamp(next: Boolean) {
        val totalStampPages = (maxStamps + stampsPerPage - 1) / stampsPerPage
        val totalPoints = viewModel.loyaltyUserStatus.value?.totalPoints ?: 0

        Log.d("LoyaltyFragment", "NavigateStamp: current page $currentPageStamp, total pages $totalStampPages, total points $totalPoints, next: $next")

        if (next) {
            if (currentPageStamp < totalStampPages - 1 && totalPoints >= (currentPageStamp + 1) * stampsPerPage) {
                currentPageStamp++
                Log.d("LoyaltyFragment", "NavigateStamp: moved to page $currentPageStamp")
            } else {
                Log.d("LoyaltyFragment", "NavigateStamp: Cannot go next. Current page: $currentPageStamp, Max pages: $totalStampPages, Points: $totalPoints, Required for next page: ${(currentPageStamp + 1) * stampsPerPage}")
            }
        } else {
            if (currentPageStamp > 0) {
                currentPageStamp--
                Log.d("LoyaltyFragment", "NavigateStamp: moved to page $currentPageStamp")
            } else {
                Log.d("LoyaltyFragment", "NavigateStamp: Cannot go previous, already on first page.")
            }
        }

        viewModel.loyaltyUserStatus.value?.let { status ->
            updateStampCard(status.totalPoints)
        }
    }

    override fun onResume() {
        super.onResume()

        val totalRewardPages = ALL_LOYALTY_REWARDS.chunked(rewardsPerPage).size
        val totalPoints = viewModel.loyaltyUserStatus.value?.totalPoints ?: 0
        val nextRewardIndex = ALL_LOYALTY_REWARDS.indexOfFirst { it.threshold > totalPoints }
        val targetDefaultRewardPage: Int

        if (nextRewardIndex != -1) {
            targetDefaultRewardPage = (nextRewardIndex / rewardsPerPage).coerceIn(0, if (totalRewardPages > 0) totalRewardPages - 1 else 0)
        } else {
            targetDefaultRewardPage = if (totalRewardPages > 0) totalRewardPages - 1 else 0
        }
        currentPageReward = targetDefaultRewardPage
        maxReachableRewardPage = targetDefaultRewardPage
        Log.d("LoyaltyFragment", "onResume: Initial currentPageReward set to $currentPageReward for totalPoints $totalPoints, maxReachableRewardPage=$maxReachableRewardPage")


        val maxReachablePage = if (totalPoints > 0) (totalPoints - 1) / stampsPerPage else 0
        currentPageStamp = maxReachablePage.coerceIn(0, (maxStamps + stampsPerPage - 1) / stampsPerPage -1 )
        Log.d("LoyaltyFragment", "onResume: Initial currentPageStamp set to $currentPageStamp for totalPoints $totalPoints")
        viewModel.refreshLoyaltyData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}