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
import android.widget.ImageView
import android.widget.TextView
// Hapus import androidx.activity.result.contract.ActivityResultContracts
// Hapus import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.SessionManager
import com.example.ukopia.adapter.LoyaltyAdapter
import com.example.ukopia.data.LoyaltyUserStatus
// Hapus import com.example.ukopia.databinding.DialogLoginRequiredBinding
import com.example.ukopia.databinding.FragmentLoyaltyBinding
// Hapus import com.example.ukopia.ui.auth.LoginActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlin.math.min
import kotlin.math.ceil

class LoyaltyFragment : Fragment() {

    private lateinit var binding: FragmentLoyaltyBinding
    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels()

    private lateinit var loyaltyItemAdapter: LoyaltyAdapter
    // Hapus: private var pendingAddLoyaltyAction = false

    // State untuk navigasi stempel
    private var currentStampPage = 0
    private val stampsPerPage = 10 // 5 mendatar x 2 ke bawah = 10 stempel per tampilan

    // Daftar untuk menampung referensi ke UI stempel
    private val stampBackgrounds = mutableListOf<ImageView>()
    private val stampNumbers = mutableListOf<TextView>()
    private val stampCheckmarks = mutableListOf<ImageView>()

    // NEW: State untuk navigasi rewards
    private var currentRewardPage = 0
    private val rewardsPerPage = 2 // Menampilkan 2 kartu reward per halaman
    private val allRewardCards = mutableListOf<MaterialCardView>() // Daftar semua kartu reward

    // NEW: Daftar threshold poin untuk rewards, HARUS SESUAI URUTAN allRewardCards dan LoyaltyUserStatus
    private val rewardThresholds = listOf(
        5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95
    )
    private val totalRewards = rewardThresholds.size // Total reward yang ada
    private val totalRewardPages = ceil(totalRewards.toDouble() / rewardsPerPage).toInt()

    // Hapus: loginActivityResultLauncher as it's no longer needed for adding loyalty
    // private val loginActivityResultLauncher = registerForActivityResult(...)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hapus: setFragmentResultListener for AddLoyaltyFragment.REQUEST_KEY_LOYALTY_ADDED
        /*
        setFragmentResultListener(AddLoyaltyFragment.REQUEST_KEY_LOYALTY_ADDED) { _, bundle ->
            if (bundle.getBoolean(AddLoyaltyFragment.BUNDLE_KEY_LOYALTY_ADDED, false)) {
                Handler(Looper.getMainLooper()).postDelayed({
                    if (isAdded) {
                        // Panggil updateLoyaltyUI untuk memuat ulang data dan menyesuaikan halaman
                        loyaltyViewModel.loyaltyUserStatus.value?.let { status ->
                            updateLoyaltyUI(status, true) // forceRecalculatePages = true
                        }
                        updateLoyaltyItemsVisibility()
                    }
                }, 200)
            }
        }
        */

        // NEW: Listener for loyalty item edits
        setFragmentResultListener(EditLoyaltyFragment.REQUEST_KEY_LOYALTY_EDITED) { _, bundle ->
            if (bundle.getBoolean(EditLoyaltyFragment.BUNDLE_KEY_LOYALTY_EDITED, false)) {
                Handler(Looper.getMainLooper()).postDelayed({
                    if (isAdded) {
                        loyaltyViewModel.refreshLoyaltyItems() // Refresh data from ViewModel
                        loyaltyViewModel.loyaltyUserStatus.value?.let { status ->
                            updateLoyaltyUI(status, true) // forceRecalculatePages = true
                        }
                        updateLoyaltyItemsVisibility()
                    }
                }, 200)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoyaltyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)

        setupLoyaltyItemsRecyclerView()
        initializeStampViews()
        initializeRewardViews()

        // Hapus: binding.fabAddRecipe.setOnClickListener { handleFabClick() }

        binding.btnRewardHistory.setOnClickListener {
            (activity as? MainActivity)?.navigateToFragment(RewardListFragment())
        }

        loyaltyViewModel.loyaltyUserStatus.observe(viewLifecycleOwner) { status ->
            updateLoyaltyUI(status, true) // forceRecalculatePages = true
        }

        loyaltyViewModel.loyaltyItems.observe(viewLifecycleOwner) { items ->
            loyaltyItemAdapter.submitList(items)
            updateLoyaltyItemsVisibility()
        }
    }

    private fun updateUserInfo() {
        val userName = SessionManager.getUserName(requireContext())
        binding.textViewUserName.text = userName ?: "Guest"
    }

    private fun initializeStampViews() {
        stampBackgrounds.clear()
        stampNumbers.clear()
        stampCheckmarks.clear()

        for (i in 1..stampsPerPage) {
            val backgroundId = resources.getIdentifier("iv_stamp_background_$i", "id", requireContext().packageName)
            val numberId = resources.getIdentifier("tv_stamp_number_$i", "id", requireContext().packageName)
            val checkmarkId = resources.getIdentifier("iv_stamp_checkmark_$i", "id", requireContext().packageName)

            binding.root.findViewById<ImageView>(backgroundId)?.let { stampBackgrounds.add(it) }
            binding.root.findViewById<TextView>(numberId)?.let { stampNumbers.add(it) }
            binding.root.findViewById<ImageView>(checkmarkId)?.let { stampCheckmarks.add(it) }
        }

        binding.btnNextStamp.setOnClickListener {
            val totalPoints = loyaltyViewModel.loyaltyUserStatus.value?.totalPoints ?: 0
            // Menggunakan ceil untuk mendapatkan jumlah halaman yang benar.
            // Jika ada 10 poin, ceil(10/10) = 1, jadi maxPage = 0.
            // Jika ada 11 poin, ceil(11/10) = 2, jadi maxPage = 1.
            val maxStampPage = if (totalPoints > 0) ceil(totalPoints.toDouble() / stampsPerPage).toInt() - 1 else 0

            if (currentStampPage < maxStampPage) {
                currentStampPage++
                updateStampCardDisplay()
            }
        }

        binding.btnPrevStamp.setOnClickListener {
            if (currentStampPage > 0) {
                currentStampPage--
                updateStampCardDisplay()
            }
        }
    }

    private fun initializeRewardViews() {
        allRewardCards.clear()

        allRewardCards.add(binding.cardRewardDisc10)
        allRewardCards.add(binding.cardRewardFreeServe)
        allRewardCards.add(binding.cardRewardDisc102)
        allRewardCards.add(binding.cardRewardTshirt)
        allRewardCards.add(binding.cardRewardDisc1025)
        allRewardCards.add(binding.cardRewardFreeServe30)
        allRewardCards.add(binding.cardRewardDisc1035)
        allRewardCards.add(binding.cardRewardFreeServe40)
        allRewardCards.add(binding.cardRewardDisc1045)
        allRewardCards.add(binding.cardRewardFreeServe50)
        allRewardCards.add(binding.cardRewardDisc1055)
        allRewardCards.add(binding.cardRewardFreeServe60)
        allRewardCards.add(binding.cardRewardDisc1065)
        allRewardCards.add(binding.cardRewardFreeServe70)
        allRewardCards.add(binding.cardRewardDisc1075)
        allRewardCards.add(binding.cardRewardFreeServe80)
        allRewardCards.add(binding.cardRewardDisc1085)
        allRewardCards.add(binding.cardRewardFreeServe90)
        allRewardCards.add(binding.cardRewardDisc1095)

        binding.btnNextReward.setOnClickListener {
            val status = loyaltyViewModel.loyaltyUserStatus.value ?: LoyaltyUserStatus()

            // Calculate maxPageForNextButton correctly.
            // This should allow scrolling through all defined reward cards.
            val maxPageForNavigation = (totalRewards - 1) / rewardsPerPage

            if (currentRewardPage < maxPageForNavigation) {
                currentRewardPage++
                updateRewardCardDisplay()
                updateRewardsSection(status)
            }
        }

        binding.btnPrevReward.setOnClickListener {
            if (currentRewardPage > 0) {
                currentRewardPage--
                updateRewardCardDisplay()
                loyaltyViewModel.loyaltyUserStatus.value?.let { updateRewardsSection(it) }
            }
        }
    }

    private fun updateStampCardDisplay() {
        val totalPoints = loyaltyViewModel.loyaltyUserStatus.value?.totalPoints ?: 0
        val startIndex = currentStampPage * stampsPerPage

        if (stampBackgrounds.isEmpty() || stampNumbers.isEmpty() || stampCheckmarks.isEmpty()) {
            return
        }

        for (i in 0 until stampsPerPage) {
            val stampActualNumber = startIndex + i + 1

            stampBackgrounds[i].visibility = View.VISIBLE

            stampNumbers[i].text = stampActualNumber.toString()

            if (stampActualNumber <= totalPoints) {
                stampBackgrounds[i].background = ContextCompat.getDrawable(requireContext(), R.drawable.circle_background_white_stroke_black_fill)
                stampNumbers[i].visibility = View.GONE
                stampCheckmarks[i].visibility = View.VISIBLE
            } else {
                stampBackgrounds[i].background = ContextCompat.getDrawable(requireContext(), R.drawable.reward_circle_background_default)
                stampNumbers[i].visibility = View.VISIBLE
                stampNumbers[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                stampCheckmarks[i].visibility = View.GONE
            }
        }
        updateStampNavigationIndicator()
    }

    private fun updateStampNavigationIndicator() {
        val totalPoints = loyaltyViewModel.loyaltyUserStatus.value?.totalPoints ?: 0
        val startStamp = currentStampPage * stampsPerPage + 1
        val endStamp = min((currentStampPage + 1) * stampsPerPage, 100) // Batasi hingga 100 jika max stamps

        // Calculate maxPageForNextButton correctly based on total possible stamps
        val maxPageForNextButton = if (totalPoints == 0) 0 else (ceil(totalPoints.toDouble() / stampsPerPage).toInt() - 1).coerceAtLeast(0)

        binding.textViewStampProgress.text = getString(R.string.loyalty_stamp_progress_format, startStamp, endStamp)

        binding.btnPrevStamp.visibility = if (currentStampPage == 0) View.INVISIBLE else View.VISIBLE
        binding.btnNextStamp.visibility = if (currentStampPage >= maxPageForNextButton) View.INVISIBLE else View.VISIBLE
    }

    private fun updateRewardCardDisplay() {
        val startIndex = currentRewardPage * rewardsPerPage
        val endIndex = min(startIndex + rewardsPerPage, allRewardCards.size)

        for (i in allRewardCards.indices) {
            allRewardCards[i].visibility = View.GONE
        }

        for (i in startIndex until endIndex) {
            allRewardCards[i].visibility = View.VISIBLE
        }

        updateRewardNavigationIndicator()
    }

    private fun updateRewardNavigationIndicator() {
        // val status = loyaltyViewModel.loyaltyUserStatus.value ?: LoyaltyUserStatus() // No need for status here
        val startIndex = currentRewardPage * rewardsPerPage
        val endIndex = min((currentRewardPage + 1) * rewardsPerPage, allRewardCards.size)

        if (allRewardCards.isEmpty() || rewardThresholds.isEmpty()) {
            binding.textViewRewardProgress.text = ""
            binding.btnPrevReward.visibility = View.INVISIBLE
            binding.btnNextReward.visibility = View.INVISIBLE
            return
        }

        val firstRewardPoints = rewardThresholds.getOrNull(startIndex) ?: 0

        val textProgress = if (startIndex == endIndex - 1) {
            getString(R.string.loyalty_rewards_progress_single_format, firstRewardPoints)
        } else {
            val lastRewardPoints = rewardThresholds.getOrNull(endIndex - 1) ?: rewardThresholds.last()
            getString(R.string.loyalty_rewards_progress_range_format, firstRewardPoints, lastRewardPoints)
        }
        binding.textViewRewardProgress.text = textProgress

        binding.btnPrevReward.visibility = if (currentRewardPage == 0) View.INVISIBLE else View.VISIBLE

        // Max page for next button is the last possible page, considering total available rewards
        val maxPageForNextButton = (totalRewards - 1) / rewardsPerPage
        binding.btnNextReward.visibility = if (currentRewardPage >= maxPageForNextButton) View.INVISIBLE else View.VISIBLE
    }

    private fun updateLoyaltyUI(status: LoyaltyUserStatus, forceRecalculatePages: Boolean = false) {
        updateUserInfo()
        binding.textViewLoyaltyPoints.text = getString(R.string.loyalty_points_format, status.totalPoints)

        if (forceRecalculatePages) {
            currentStampPage = if (status.totalPoints > 0) {
                (status.totalPoints - 1) / stampsPerPage
            } else {
                0
            }

            // Adjust currentRewardPage to show relevant rewards, typically the first page or the page containing current points
            var targetRewardIndex = rewardThresholds.indexOfFirst { it > status.totalPoints }
            if (targetRewardIndex == -1) { // If all rewards are achieved or none are defined, show the last available page of rewards
                targetRewardIndex = (totalRewards - 1).coerceAtLeast(0) // Ensure it's not -1 if totalRewards is 0
            }
            currentRewardPage = if (targetRewardIndex >= 0) {
                (targetRewardIndex / rewardsPerPage).coerceAtLeast(0)
            } else {
                0
            }
        }

        updateStampCardDisplay()
        updateRewardCardDisplay()
        updateRewardsSection(status)
    }

    private fun updateRewardClaimStatusDisplay(
        button: MaterialButton,
        pointsTextView: TextView,
        currentPoints: Int,
        threshold: Int,
        claimDate: String?
    ) {
        val context = button.context

        pointsTextView.text = context.getString(R.string.reward_points_format_display, threshold)

        button.isEnabled = false // User tidak dapat mengklaim secara langsung

        if (claimDate != null) {
            // Status: Sudah Diklaim (Latar belakang hitam, teks putih)
            button.text = context.getString(R.string.reward_status_claimed_date_format, claimDate)
            button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black))
            button.setTextColor(ContextCompat.getColor(context, R.color.white))
        } else if (currentPoints >= threshold) {
            // Status: Tercapai, Belum Diklaim (Latar belakang hitam, teks putih)
            button.text = context.getString(R.string.reward_status_not_yet_claimed)
            button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black))
            button.setTextColor(ContextCompat.getColor(context, R.color.white))
        } else {
            // Status: Belum Tercapai (Latar belakang hitam, teks putih)
            button.text = context.getString(R.string.reward_status_not_achieved)
            button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black))
            button.setTextColor(ContextCompat.getColor(context, R.color.white))
        }
    }

    private fun updateRewardsSection(status: LoyaltyUserStatus) {
        val startIndex = currentRewardPage * rewardsPerPage
        val endIndex = min(startIndex + rewardsPerPage, allRewardCards.size)

        for (i in startIndex until endIndex) {
            val rewardCard = allRewardCards[i]
            when (rewardCard.id) {
                R.id.card_reward_disc_10 -> updateRewardClaimStatusDisplay(binding.btnClaimDisc10, binding.tvRewardPointsDisc10, status.totalPoints, 5, status.discount10ClaimDate)
                R.id.card_reward_free_serve -> updateRewardClaimStatusDisplay(binding.btnClaimFreeServe, binding.tvRewardPointsFreeServe, status.totalPoints, 10, status.freeServeClaimDate)
                R.id.card_reward_disc_10_2 -> updateRewardClaimStatusDisplay(binding.btnClaimDisc102, binding.tvRewardPointsDisc102, status.totalPoints, 15, status.discount10Slot15ClaimDate)
                R.id.card_reward_tshirt -> updateRewardClaimStatusDisplay(binding.btnClaimTshirt, binding.tvRewardPointsTshirt, status.totalPoints, 20, status.freeTshirtClaimDate)
                R.id.card_reward_disc_10_25 -> updateRewardClaimStatusDisplay(binding.btnClaimDisc1025, binding.tvRewardPointsDisc1025, status.totalPoints, 25, status.discount10_25ClaimDate)
                R.id.card_reward_free_serve_30 -> updateRewardClaimStatusDisplay(binding.btnClaimFreeServe30, binding.tvRewardPointsFreeServe30, status.totalPoints, 30, status.freeServe_30ClaimDate)
                R.id.card_reward_disc_10_35 -> updateRewardClaimStatusDisplay(binding.btnClaimDisc1035, binding.tvRewardPointsDisc1035, status.totalPoints, 35, status.discount10_35ClaimDate)
                R.id.card_reward_free_serve_40 -> updateRewardClaimStatusDisplay(binding.btnClaimFreeServe40, binding.tvRewardPointsFreeServe40, status.totalPoints, 40, status.freeServe_40ClaimDate)
                R.id.card_reward_disc_10_45 -> updateRewardClaimStatusDisplay(binding.btnClaimDisc1045, binding.tvRewardPointsDisc1045, status.totalPoints, 45, status.discount10_45ClaimDate)
                R.id.card_reward_free_serve_50 -> updateRewardClaimStatusDisplay(binding.btnClaimFreeServe50, binding.tvRewardPointsFreeServe50, status.totalPoints, 50, status.freeServe_50ClaimDate)
                R.id.card_reward_disc_10_55 -> updateRewardClaimStatusDisplay(binding.btnClaimDisc1055, binding.tvRewardPointsDisc1055, status.totalPoints, 55, status.discount10_55ClaimDate)
                R.id.card_reward_free_serve_60 -> updateRewardClaimStatusDisplay(binding.btnClaimFreeServe60, binding.tvRewardPointsFreeServe60, status.totalPoints, 60, status.freeServe_60ClaimDate)
                R.id.card_reward_disc_10_65 -> updateRewardClaimStatusDisplay(binding.btnClaimDisc1065, binding.tvRewardPointsDisc1065, status.totalPoints, 65, status.discount10_65ClaimDate)
                R.id.card_reward_free_serve_70 -> updateRewardClaimStatusDisplay(binding.btnClaimFreeServe70, binding.tvRewardPointsFreeServe70, status.totalPoints, 70, status.freeServe_70ClaimDate)
                R.id.card_reward_disc_10_75 -> updateRewardClaimStatusDisplay(binding.btnClaimDisc1075, binding.tvRewardPointsDisc1075, status.totalPoints, 75, status.discount10_75ClaimDate)
                R.id.card_reward_free_serve_80 -> updateRewardClaimStatusDisplay(binding.btnClaimFreeServe80, binding.tvRewardPointsFreeServe80, status.totalPoints, 80, status.freeServe_80ClaimDate)
                R.id.card_reward_disc_10_85 -> updateRewardClaimStatusDisplay(binding.btnClaimDisc1085, binding.tvRewardPointsDisc1085, status.totalPoints, 85, status.discount10_85ClaimDate)
                R.id.card_reward_free_serve_90 -> updateRewardClaimStatusDisplay(binding.btnClaimFreeServe90, binding.tvRewardPointsFreeServe90, status.totalPoints, 90, status.freeServe_90ClaimDate)
                R.id.card_reward_disc_10_95 -> updateRewardClaimStatusDisplay(binding.btnClaimDisc1095, binding.tvRewardPointsDisc1095, status.totalPoints, 95, status.discount10_95ClaimDate)
            }
        }
    }

    // Hapus: handleFabClick()

    private fun setupLoyaltyItemsRecyclerView() {
        loyaltyItemAdapter = LoyaltyAdapter { item ->
            LoyaltyDetailDialogFragment.newInstance(item).show(parentFragmentManager, "LoyaltyDetailPopup")
        }
        binding.recyclerViewLoyaltyItems.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewLoyaltyItems.adapter = loyaltyItemAdapter
    }
    private fun updateLoyaltyItemsVisibility() {
        val hasItems = loyaltyItemAdapter.currentList.isNotEmpty()
        binding.recyclerViewLoyaltyItems.visibility = if (hasItems) View.VISIBLE else View.GONE
        binding.placeholderContainer.visibility = if (hasItems) View.GONE else View.VISIBLE
    }
    // Hapus: showLoginRequiredDialog() as it was only for add loyalty
}