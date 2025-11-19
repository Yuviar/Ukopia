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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import com.example.ukopia.databinding.DialogLoginRequiredBinding
import com.example.ukopia.databinding.FragmentLoyaltyBinding
import com.example.ukopia.ui.auth.LoginActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlin.math.min
import kotlin.math.ceil

class LoyaltyFragment : Fragment() {

    private lateinit var binding: FragmentLoyaltyBinding
    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels()

    private lateinit var loyaltyItemAdapter: LoyaltyAdapter
    private var pendingAddLoyaltyAction = false

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


    private val loginActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && SessionManager.isLoggedIn(requireContext())) {
            updateUserInfo()
            // Panggil updateLoyaltyUI untuk memuat ulang data dan menyesuaikan halaman
            loyaltyViewModel.loyaltyUserStatus.value?.let { status ->
                updateLoyaltyUI(status, true) // forceRecalculatePages = true
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoyaltyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)

        // Hanya inisialisasi tampilan statis dan listener
        setupLoyaltyItemsRecyclerView()
        initializeStampViews()
        initializeRewardViews()
        initializeRewardButtons()

        binding.fabAddRecipe.setOnClickListener { handleFabClick() }

        // Observe loyaltyUserStatus untuk update UI dan menghitung halaman awal
        loyaltyViewModel.loyaltyUserStatus.observe(viewLifecycleOwner) { status ->
            // Perbarui UI dan hitung halaman secara default saat status berubah/dimuat
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

            stampBackgrounds.add(binding.root.findViewById(backgroundId))
            stampNumbers.add(binding.root.findViewById(numberId))
            stampCheckmarks.add(binding.root.findViewById(checkmarkId))
        }

        binding.btnNextStamp.setOnClickListener {
            val totalPoints = loyaltyViewModel.loyaltyUserStatus.value?.totalPoints ?: 0
            val maxStampPageBasedOnPoints = if (totalPoints == 0) 0 else (totalPoints - 1) / stampsPerPage
            val currentMaxPossiblePage = if (totalPoints % stampsPerPage == 0 && totalPoints > 0) totalPoints / stampsPerPage else totalPoints / stampsPerPage // This effectively allows seeing the page where the next stamp would be, or the page with points

            if (currentStampPage < currentMaxPossiblePage) {
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

    // NEW: Inisialisasi view rewards dan tombol navigasinya
    private fun initializeRewardViews() {
        allRewardCards.clear()

        // Ambil semua MaterialCardView rewards dari layout dan simpan dalam list
        allRewardCards.add(binding.cardRewardDisc10)
        allRewardCards.add(binding.cardRewardFreeServe)
        allRewardCards.add(binding.cardRewardDisc102)
        allRewardCards.add(binding.cardRewardTshirt) // T-shirt is only at 20 points
        allRewardCards.add(binding.cardRewardDisc1025)
        allRewardCards.add(binding.cardRewardFreeServe30)
        allRewardCards.add(binding.cardRewardDisc1035)
        allRewardCards.add(binding.cardRewardFreeServe40)
        allRewardCards.add(binding.cardRewardDisc1045)
        allRewardCards.add(binding.cardRewardFreeServe50)
        allRewardCards.add(binding.cardRewardDisc1055)
        allRewardCards.add(binding.cardRewardFreeServe60) // Changed to Free Serve at 60 points
        allRewardCards.add(binding.cardRewardDisc1065)
        allRewardCards.add(binding.cardRewardFreeServe70)
        allRewardCards.add(binding.cardRewardDisc1075)
        allRewardCards.add(binding.cardRewardFreeServe80)
        allRewardCards.add(binding.cardRewardDisc1085)
        allRewardCards.add(binding.cardRewardFreeServe90)
        allRewardCards.add(binding.cardRewardDisc1095)

        binding.btnNextReward.setOnClickListener {
            val status = loyaltyViewModel.loyaltyUserStatus.value ?: LoyaltyUserStatus()
            val highestAchievableRewardIndex = rewardThresholds.indexOfLast { it <= status.totalPoints }
            val highestVisibleRewardIndex = if (highestAchievableRewardIndex == -1 && totalRewards > 0) {
                0 // If no rewards achieved, show the first page
            } else if (highestAchievableRewardIndex == -1) {
                -1 // No rewards defined
            } else if (highestAchievableRewardIndex < totalRewards - 1) {
                highestAchievableRewardIndex + 1 // Show up to the next unachieved reward
            } else {
                totalRewards - 1 // Show up to the last defined reward
            }
            val maxRewardPageForNavigation = if (highestVisibleRewardIndex != -1) {
                highestVisibleRewardIndex / rewardsPerPage
            } else {
                0
            }

            if (currentRewardPage < maxRewardPageForNavigation) {
                currentRewardPage++
                updateRewardCardDisplay()
                updateRewardsSection(status) // Perbarui status tombol untuk kartu baru
            }
        }

        binding.btnPrevReward.setOnClickListener {
            if (currentRewardPage > 0) {
                currentRewardPage--
                updateRewardCardDisplay()
                loyaltyViewModel.loyaltyUserStatus.value?.let { updateRewardsSection(it) } // Perbarui status tombol untuk kartu baru
            }
        }
    }


    private fun initializeRewardButtons() {
        // Set listener untuk tombol reward
        binding.btnClaimDisc10.setOnClickListener { handleRewardClaim(5) }
        binding.btnClaimFreeServe.setOnClickListener { handleRewardClaim(10) }
        binding.btnClaimDisc102.setOnClickListener { handleRewardClaim(15) }
        binding.btnClaimTshirt.setOnClickListener { handleRewardClaim(20) }
        binding.btnClaimDisc1025.setOnClickListener { handleRewardClaim(25) }
        binding.btnClaimFreeServe30.setOnClickListener { handleRewardClaim(30) }
        binding.btnClaimDisc1035.setOnClickListener { handleRewardClaim(35) }
        binding.btnClaimFreeServe40.setOnClickListener { handleRewardClaim(40) }
        binding.btnClaimDisc1045.setOnClickListener { handleRewardClaim(45) }
        binding.btnClaimFreeServe50.setOnClickListener { handleRewardClaim(50) }
        binding.btnClaimDisc1055.setOnClickListener { handleRewardClaim(55) }
        binding.btnClaimFreeServe60.setOnClickListener { handleRewardClaim(60) } // Updated for Free Serve at 60 points
        binding.btnClaimDisc1065.setOnClickListener { handleRewardClaim(65) }
        binding.btnClaimFreeServe70.setOnClickListener { handleRewardClaim(70) }
        binding.btnClaimDisc1075.setOnClickListener { handleRewardClaim(75) }
        binding.btnClaimFreeServe80.setOnClickListener { handleRewardClaim(80) }
        binding.btnClaimDisc1085.setOnClickListener { handleRewardClaim(85) }
        binding.btnClaimFreeServe90.setOnClickListener { handleRewardClaim(90) }
        binding.btnClaimDisc1095.setOnClickListener { handleRewardClaim(95) }
    }


    // Fungsi untuk memperbarui tampilan 10 stempel yang sedang ditampilkan
    private fun updateStampCardDisplay() {
        val totalPoints = loyaltyViewModel.loyaltyUserStatus.value?.totalPoints ?: 0
        val startIndex = currentStampPage * stampsPerPage

        if (stampBackgrounds.isEmpty() || stampNumbers.isEmpty() || stampCheckmarks.isEmpty()) {
            return
        }

        for (i in 0 until stampsPerPage) {
            val stampActualNumber = startIndex + i + 1

            // All stamps on the current page should be visible, regardless of totalPoints.
            // But only stamps with number <= totalPoints will be "stamped".
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
        val endStamp = (currentStampPage + 1) * stampsPerPage

        val maxPageForNextButton = if (totalPoints == 0) 0 else (totalPoints - 1) / stampsPerPage

        // Progress text should show the range of stamps on the current page.
        // It should still show 1-10 even if user only has 3 points.
        binding.textViewStampProgress.text = getString(R.string.loyalty_stamp_progress_format, startStamp, endStamp)

        binding.btnPrevStamp.visibility = if (currentStampPage == 0) View.INVISIBLE else View.VISIBLE
        // Next button is visible if current page is not the last page that contains earned stamps
        // Or if the current page itself contains the last earned stamp but there are more stamps that can be earned (i.e., totalPoints is not 0 and current page is not the page containing totalPoints yet)
        binding.btnNextStamp.visibility = if (currentStampPage >= maxPageForNextButton) View.INVISIBLE else View.VISIBLE
    }

    // NEW: Fungsi untuk memperbarui tampilan kartu reward yang sedang ditampilkan
    private fun updateRewardCardDisplay() {
        val startIndex = currentRewardPage * rewardsPerPage // Indeks awal reward untuk halaman ini
        val endIndex = min(startIndex + rewardsPerPage, allRewardCards.size) // Indeks akhir reward (eksklusif)

        for (i in allRewardCards.indices) {
            // Sembunyikan semua kartu reward terlebih dahulu
            allRewardCards[i].visibility = View.GONE
        }

        // Tampilkan hanya kartu reward untuk halaman saat ini
        for (i in startIndex until endIndex) {
            allRewardCards[i].visibility = View.VISIBLE
        }

        updateRewardNavigationIndicator() // Perbarui indikator navigasi setelah display diupdate
    }

    // NEW: Fungsi untuk memperbarui teks dan visibilitas tombol navigasi rewards
    private fun updateRewardNavigationIndicator() {
        val status = loyaltyViewModel.loyaltyUserStatus.value ?: LoyaltyUserStatus()
        val startIndex = currentRewardPage * rewardsPerPage
        val endIndex = min((currentRewardPage + 1) * rewardsPerPage, allRewardCards.size)

        if (allRewardCards.isEmpty() || rewardThresholds.isEmpty()) {
            binding.textViewRewardProgress.text = "" // Atau placeholder seperti "No Rewards"
            binding.btnPrevReward.visibility = View.INVISIBLE
            binding.btnNextReward.visibility = View.INVISIBLE
            return
        }

        val firstRewardPoints = rewardThresholds.getOrNull(startIndex) ?: 0

        val textProgress = if (startIndex == endIndex - 1) { // Only one item on this page
            getString(R.string.loyalty_rewards_progress_single_format, firstRewardPoints)
        } else {
            val lastRewardPoints = rewardThresholds.getOrNull(endIndex - 1) ?: rewardThresholds.last()
            getString(R.string.loyalty_rewards_progress_range_format, firstRewardPoints, lastRewardPoints)
        }
        binding.textViewRewardProgress.text = textProgress

        binding.btnPrevReward.visibility = if (currentRewardPage == 0) View.INVISIBLE else View.VISIBLE

        // Determine the maximum page the user can navigate to.
        // This should be the page containing the first unachieved reward, or the last page if all achieved.
        val highestAchievableRewardIndex = rewardThresholds.indexOfLast { it <= status.totalPoints }
        val maxPageForNextButton = if (highestAchievableRewardIndex == -1) {
            0 // If no rewards achieved, only page 0 is accessible
        } else if (highestAchievableRewardIndex < totalRewards - 1) {
            (highestAchievableRewardIndex + 1) / rewardsPerPage // Allow to see the page with the next unachieved reward
        } else {
            (totalRewards - 1) / rewardsPerPage // All rewards achieved, show last page
        }

        binding.btnNextReward.visibility = if (currentRewardPage >= maxPageForNextButton) View.INVISIBLE else View.VISIBLE
    }


    // FUNGSI INI DIMODIFIKASI untuk menghitung halaman default
    private fun updateLoyaltyUI(status: LoyaltyUserStatus, forceRecalculatePages: Boolean = false) {
        updateUserInfo()
        binding.textViewLoyaltyPoints.text = getString(R.string.loyalty_points_format, status.totalPoints)

        if (forceRecalculatePages) {
            // Hitung halaman stempel default: pindah ke halaman yang berisi stempel terakhir yang diperoleh.
            currentStampPage = if (status.totalPoints > 0) {
                (status.totalPoints - 1) / stampsPerPage
            } else {
                0
            }

            // Hitung halaman reward default: pindah ke halaman yang berisi reward pertama yang belum terpenuhi,
            // atau halaman terakhir jika semua reward sudah terpenuhi.
            var targetRewardIndex = rewardThresholds.indexOfFirst { it > status.totalPoints }
            if (targetRewardIndex == -1) { // If all rewards are claimed/achieved
                targetRewardIndex = rewardThresholds.lastIndex
            }
            currentRewardPage = if (targetRewardIndex != -1) {
                (targetRewardIndex / rewardsPerPage).coerceAtLeast(0)
            } else {
                0 // Fallback if no rewards are defined
            }
        }

        // Sekarang perbarui tampilan berdasarkan halaman yang dihitung atau yang sedang aktif
        updateStampCardDisplay()
        updateRewardCardDisplay()
        updateRewardsSection(status) // Ini akan memperbarui status tombol klaim untuk reward yang saat ini terlihat
    }

    private fun updateRewardsSection(status: LoyaltyUserStatus) {
        // Hanya perbarui status tombol untuk kartu reward yang saat ini terlihat
        val startIndex = currentRewardPage * rewardsPerPage
        val endIndex = min(startIndex + rewardsPerPage, allRewardCards.size)

        for (i in startIndex until endIndex) {
            val rewardCard = allRewardCards[i]
            when (rewardCard.id) {
                R.id.card_reward_disc_10 -> updateRewardButtonState(binding.btnClaimDisc10, binding.tvRewardPointsDisc10, status.totalPoints, 5, status.isDiscount10Claimed)
                R.id.card_reward_free_serve -> updateRewardButtonState(binding.btnClaimFreeServe, binding.tvRewardPointsFreeServe, status.totalPoints, 10, status.isFreeServeClaimed)
                R.id.card_reward_disc_10_2 -> updateRewardButtonState(binding.btnClaimDisc102, binding.tvRewardPointsDisc102, status.totalPoints, 15, status.isDiscount10Slot15Claimed)
                R.id.card_reward_tshirt -> updateRewardButtonState(binding.btnClaimTshirt, binding.tvRewardPointsTshirt, status.totalPoints, 20, status.isFreeTshirtClaimed)
                R.id.card_reward_disc_10_25 -> updateRewardButtonState(binding.btnClaimDisc1025, binding.tvRewardPointsDisc1025, status.totalPoints, 25, status.isDiscount10_25Claimed)
                R.id.card_reward_free_serve_30 -> updateRewardButtonState(binding.btnClaimFreeServe30, binding.tvRewardPointsFreeServe30, status.totalPoints, 30, status.isFreeServe_30Claimed)
                R.id.card_reward_disc_10_35 -> updateRewardButtonState(binding.btnClaimDisc1035, binding.tvRewardPointsDisc1035, status.totalPoints, 35, status.isDiscount10_35Claimed)
                R.id.card_reward_free_serve_40 -> updateRewardButtonState(binding.btnClaimFreeServe40, binding.tvRewardPointsFreeServe40, status.totalPoints, 40, status.isFreeServe_40Claimed)
                R.id.card_reward_disc_10_45 -> updateRewardButtonState(binding.btnClaimDisc1045, binding.tvRewardPointsDisc1045, status.totalPoints, 45, status.isDiscount10_45Claimed)
                R.id.card_reward_free_serve_50 -> updateRewardButtonState(binding.btnClaimFreeServe50, binding.tvRewardPointsFreeServe50, status.totalPoints, 50, status.isFreeServe_50Claimed)
                R.id.card_reward_disc_10_55 -> updateRewardButtonState(binding.btnClaimDisc1055, binding.tvRewardPointsDisc1055, status.totalPoints, 55, status.isDiscount10_55Claimed)
                R.id.card_reward_free_serve_60 -> updateRewardButtonState(binding.btnClaimFreeServe60, binding.tvRewardPointsFreeServe60, status.totalPoints, 60, status.isFreeServe_60Claimed) // Updated
                R.id.card_reward_disc_10_65 -> updateRewardButtonState(binding.btnClaimDisc1065, binding.tvRewardPointsDisc1065, status.totalPoints, 65, status.isDiscount10_65Claimed)
                R.id.card_reward_free_serve_70 -> updateRewardButtonState(binding.btnClaimFreeServe70, binding.tvRewardPointsFreeServe70, status.totalPoints, 70, status.isFreeServe_70Claimed)
                R.id.card_reward_disc_10_75 -> updateRewardButtonState(binding.btnClaimDisc1075, binding.tvRewardPointsDisc1075, status.totalPoints, 75, status.isDiscount10_75Claimed)
                R.id.card_reward_free_serve_80 -> updateRewardButtonState(binding.btnClaimFreeServe80, binding.tvRewardPointsFreeServe80, status.totalPoints, 80, status.isFreeServe_80Claimed)
                R.id.card_reward_disc_10_85 -> updateRewardButtonState(binding.btnClaimDisc1085, binding.tvRewardPointsDisc1085, status.totalPoints, 85, status.isDiscount10_85Claimed)
                R.id.card_reward_free_serve_90 -> updateRewardButtonState(binding.btnClaimFreeServe90, binding.tvRewardPointsFreeServe90, status.totalPoints, 90, status.isFreeServe_90Claimed)
                R.id.card_reward_disc_10_95 -> updateRewardButtonState(binding.btnClaimDisc1095, binding.tvRewardPointsDisc1095, status.totalPoints, 95, status.isDiscount10_95Claimed)
            }
        }
    }

    private fun updateRewardButtonState(
        button: MaterialButton,
        pointsTextView: TextView,
        currentPoints: Int,
        threshold: Int,
        isClaimed: Boolean
    ) {
        when {
            isClaimed -> {
                pointsTextView.visibility = View.VISIBLE
                pointsTextView.text = getString(R.string.loyalty_points_format, threshold)
                button.text = getString(R.string.reward_claimed_status)
                button.isEnabled = false
                button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.grey))
            }
            currentPoints >= threshold -> {
                pointsTextView.visibility = View.VISIBLE
                pointsTextView.text = getString(R.string.loyalty_points_format, threshold)
                button.text = getString(R.string.loyalty_reward_claim_action)
                button.isEnabled = true
                button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black))
            }
            else -> {
                pointsTextView.visibility = View.GONE
                button.text = getString(R.string.loyalty_points_needed_format, threshold)
                button.isEnabled = false
                button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.grey))
            }
        }
    }

    private fun handleRewardClaim(threshold: Int) {
        val currentStatus = loyaltyViewModel.loyaltyUserStatus.value ?: return
        if (currentStatus.totalPoints < threshold) {
            Toast.makeText(requireContext(), getString(R.string.not_enough_stamps), Toast.LENGTH_SHORT).show()
            return
        }

        when (threshold) {
            5 -> if (!currentStatus.isDiscount10Claimed) {
                loyaltyViewModel.claimDiscount10()
                showToast(getString(R.string.loyalty_reward_10_percent_discount_short))
            } else showAlreadyClaimedToast()

            10 -> if (!currentStatus.isFreeServeClaimed) {
                loyaltyViewModel.claimFreeServe()
                showToast(getString(R.string.loyalty_reward_free_serve_short))
            } else showAlreadyClaimedToast()

            15 -> if (!currentStatus.isDiscount10Slot15Claimed) {
                loyaltyViewModel.claimDiscount10Slot15()
                showToast(getString(R.string.loyalty_reward_10_percent_discount_short))
            } else showAlreadyClaimedToast()

            20 -> if (!currentStatus.isFreeTshirtClaimed) {
                loyaltyViewModel.claimFreeTshirt()
                showToast(getString(R.string.loyalty_reward_free_tshirt_short))
            } else showAlreadyClaimedToast()

            25 -> if (!currentStatus.isDiscount10_25Claimed) {
                loyaltyViewModel.claimDiscount10_25()
                showToast(getString(R.string.loyalty_reward_10_percent_discount_short))
            } else showAlreadyClaimedToast()

            30 -> if (!currentStatus.isFreeServe_30Claimed) {
                loyaltyViewModel.claimFreeServe_30()
                showToast(getString(R.string.loyalty_reward_free_serve_short))
            } else showAlreadyClaimedToast()

            35 -> if (!currentStatus.isDiscount10_35Claimed) {
                loyaltyViewModel.claimDiscount10_35()
                showToast(getString(R.string.loyalty_reward_10_percent_discount_short))
            } else showAlreadyClaimedToast()

            40 -> if (!currentStatus.isFreeServe_40Claimed) {
                loyaltyViewModel.claimFreeServe_40()
                showToast(getString(R.string.loyalty_reward_free_serve_short))
            } else showAlreadyClaimedToast()

            45 -> if (!currentStatus.isDiscount10_45Claimed) {
                loyaltyViewModel.claimDiscount10_45()
                showToast(getString(R.string.loyalty_reward_10_percent_discount_short))
            } else showAlreadyClaimedToast()

            50 -> if (!currentStatus.isFreeServe_50Claimed) {
                loyaltyViewModel.claimFreeServe_50()
                showToast(getString(R.string.loyalty_reward_free_serve_short))
            } else showAlreadyClaimedToast()

            55 -> if (!currentStatus.isDiscount10_55Claimed) {
                loyaltyViewModel.claimDiscount10_55()
                showToast(getString(R.string.loyalty_reward_10_percent_discount_short))
            } else showAlreadyClaimedToast()

            60 -> if (!currentStatus.isFreeServe_60Claimed) { // Updated to Free Serve
                loyaltyViewModel.claimFreeServe_60()
                showToast(getString(R.string.loyalty_reward_free_serve_short))
            } else showAlreadyClaimedToast()

            65 -> if (!currentStatus.isDiscount10_65Claimed) {
                loyaltyViewModel.claimDiscount10_65()
                showToast(getString(R.string.loyalty_reward_10_percent_discount_short))
            } else showAlreadyClaimedToast()

            70 -> if (!currentStatus.isFreeServe_70Claimed) {
                loyaltyViewModel.claimFreeServe_70()
                showToast(getString(R.string.loyalty_reward_free_serve_short))
            } else showAlreadyClaimedToast()

            75 -> if (!currentStatus.isDiscount10_75Claimed) {
                loyaltyViewModel.claimDiscount10_75()
                showToast(getString(R.string.loyalty_reward_10_percent_discount_short))
            } else showAlreadyClaimedToast()

            80 -> if (!currentStatus.isFreeServe_80Claimed) {
                loyaltyViewModel.claimFreeServe_80()
                showToast(getString(R.string.loyalty_reward_free_serve_short))
            } else showAlreadyClaimedToast()

            85 -> if (!currentStatus.isDiscount10_85Claimed) {
                loyaltyViewModel.claimDiscount10_85()
                showToast(getString(R.string.loyalty_reward_10_percent_discount_short))
            } else showAlreadyClaimedToast()

            90 -> if (!currentStatus.isFreeServe_90Claimed) {
                loyaltyViewModel.claimFreeServe_90()
                showToast(getString(R.string.loyalty_reward_free_serve_short))
            } else showAlreadyClaimedToast()

            95 -> if (!currentStatus.isDiscount10_95Claimed) {
                loyaltyViewModel.claimDiscount10_95()
                showToast(getString(R.string.loyalty_reward_10_percent_discount_short))
            } else showAlreadyClaimedToast()
        }
    }

    private fun showToast(rewardName: String) {
        Toast.makeText(requireContext(), getString(R.string.reward_claimed_toast_format, rewardName), Toast.LENGTH_SHORT).show()
    }
    private fun showAlreadyClaimedToast() {
        Toast.makeText(requireContext(), getString(R.string.reward_already_claimed), Toast.LENGTH_SHORT).show()
    }

    private fun handleFabClick() {
        val originalBackgroundTint = ContextCompat.getColor(requireContext(), R.color.black)
        val originalImageTint = ContextCompat.getColor(requireContext(), R.color.white)
        val flashColorBackground = ContextCompat.getColor(requireContext(), R.color.white)
        val flashColorImage = ContextCompat.getColor(requireContext(), R.color.black)

        binding.fabAddRecipe.backgroundTintList = ColorStateList.valueOf(flashColorBackground)
        binding.fabAddRecipe.imageTintList = ColorStateList.valueOf(flashColorImage)

        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded && activity != null) {
                binding.fabAddRecipe.backgroundTintList = ColorStateList.valueOf(originalBackgroundTint)
                binding.fabAddRecipe.imageTintList = ColorStateList.valueOf(originalImageTint)

                if (SessionManager.isLoggedIn(requireContext())) {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container, AddLoyaltyFragment())
                        .addToBackStack(null)
                        .commit()
                } else {
                    pendingAddLoyaltyAction = true
                    showLoginRequiredDialog()
                }
            }
        }, 150)
    }

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
    private fun showLoginRequiredDialog() {
        val dialogBinding = DialogLoginRequiredBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        dialogBinding.buttonDialogLogin.setOnClickListener {
            loginActivityResultLauncher.launch(Intent(requireContext(), LoginActivity::class.java))
            dialog.dismiss()
        }
        dialogBinding.buttonDialogCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
}