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
import com.google.android.material.card.MaterialCardView // Import MaterialCardView
import kotlin.math.min
import kotlin.math.ceil // Untuk perhitungan total halaman

class LoyaltyFragment : Fragment() {

    private lateinit var binding: FragmentLoyaltyBinding
    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels()

    private lateinit var loyaltyItemAdapter: LoyaltyAdapter
    private var pendingAddLoyaltyAction = false

    // State untuk navigasi stempel
    private var currentStampPage = 0
    private val stampsPerPage = 10 // 5 mendatar x 2 ke bawah = 10 stempel per tampilan
    private val totalPossibleStamps = 100 // Total stempel yang bisa didapatkan
    private val totalStampPages = totalPossibleStamps / stampsPerPage // 100 / 10 = 10 halaman

    // Daftar untuk menampung referensi ke UI stempel
    private val stampBackgrounds = mutableListOf<ImageView>()
    private val stampNumbers = mutableListOf<TextView>()
    private val stampCheckmarks = mutableListOf<ImageView>()

    // NEW: State untuk navigasi rewards
    private var currentRewardPage = 0
    private val rewardsPerPage = 2 // Menampilkan 2 kartu reward per halaman
    private val allRewardCards = mutableListOf<MaterialCardView>() // Daftar semua kartu reward
    private val totalRewards = 9 // Total reward yang ada (5,10,15,20,25,30,35,40,100)
    private val totalRewardPages = ceil(totalRewards.toDouble() / rewardsPerPage).toInt()

    // NEW: Daftar threshold poin untuk rewards, harus sesuai urutan allRewardCards
    private val rewardThresholds = listOf(5, 10, 15, 20, 25, 30, 35, 40, 100)


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
            if (currentStampPage < totalStampPages - 1) {
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
        // updateStampCardDisplay() TIDAK DIPANGGIL DI SINI lagi, akan dipanggil di updateLoyaltyUI
    }

    // NEW: Inisialisasi view rewards dan tombol navigasinya
    private fun initializeRewardViews() {
        allRewardCards.clear()

        // Ambil semua MaterialCardView rewards dari layout dan simpan dalam list
        allRewardCards.add(binding.cardRewardDisc10)
        allRewardCards.add(binding.cardRewardFreeServe)
        allRewardCards.add(binding.cardRewardDisc102)
        allRewardCards.add(binding.cardRewardTshirt)
        allRewardCards.add(binding.cardRewardDisc1025)
        allRewardCards.add(binding.cardRewardFreeServe30)
        allRewardCards.add(binding.cardRewardDisc1035)
        allRewardCards.add(binding.cardRewardFreeServe40)
        allRewardCards.add(binding.cardRewardGrinder)

        binding.btnNextReward.setOnClickListener {
            if (currentRewardPage < totalRewardPages - 1) {
                currentRewardPage++
                updateRewardCardDisplay()
                loyaltyViewModel.loyaltyUserStatus.value?.let { updateRewardsSection(it) } // Perbarui status tombol untuk kartu baru
            }
        }

        binding.btnPrevReward.setOnClickListener {
            if (currentRewardPage > 0) {
                currentRewardPage--
                updateRewardCardDisplay()
                loyaltyViewModel.loyaltyUserStatus.value?.let { updateRewardsSection(it) } // Perbarui status tombol untuk kartu baru
            }
        }
        // updateRewardCardDisplay() TIDAK DIPANGGIL DI SINI lagi, akan dipanggil di updateLoyaltyUI
    }


    private fun initializeRewardButtons() {
        // Set listener untuk tombol reward
        binding.btnClaimDisc10.setOnClickListener { handleRewardClaim(5) }
        binding.btnClaimFreeServe.setOnClickListener { handleRewardClaim(10) }
        binding.btnClaimDisc102.setOnClickListener { handleRewardClaim(15) }
        binding.btnClaimTshirt.setOnClickListener { handleRewardClaim(20) }
        binding.btnClaimGrinder.setOnClickListener { handleRewardClaim(100) }
        binding.btnClaimDisc1025.setOnClickListener { handleRewardClaim(25) }
        binding.btnClaimFreeServe30.setOnClickListener { handleRewardClaim(30) }
        binding.btnClaimDisc1035.setOnClickListener { handleRewardClaim(35) }
        binding.btnClaimFreeServe40.setOnClickListener { handleRewardClaim(40) }
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

            if (stampActualNumber > totalPossibleStamps) {
                stampBackgrounds[i].visibility = View.INVISIBLE
                stampNumbers[i].visibility = View.INVISIBLE
                stampCheckmarks[i].visibility = View.INVISIBLE
                continue
            } else {
                stampBackgrounds[i].visibility = View.VISIBLE
            }

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
        val startStamp = currentStampPage * stampsPerPage + 1
        val endStamp = (currentStampPage + 1) * stampsPerPage

        val actualEndStamp = minOf(endStamp, totalPossibleStamps)

        binding.textViewStampProgress.text = getString(R.string.loyalty_stamp_progress_format, startStamp, actualEndStamp)

        binding.btnPrevStamp.visibility = if (currentStampPage == 0) View.INVISIBLE else View.VISIBLE
        binding.btnNextStamp.visibility = if (currentStampPage == totalStampPages - 1) View.INVISIBLE else View.VISIBLE
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
        val startIndex = currentRewardPage * rewardsPerPage
        val endIndex = min((currentRewardPage + 1) * rewardsPerPage, allRewardCards.size)

        if (allRewardCards.isEmpty() || rewardThresholds.isEmpty() || startIndex >= rewardThresholds.size) {
            binding.textViewRewardProgress.text = "" // Atau placeholder seperti "No Rewards"
            binding.btnPrevReward.visibility = View.INVISIBLE
            binding.btnNextReward.visibility = View.INVISIBLE
            return
        }

        val firstRewardPoints = rewardThresholds[startIndex]

        // Handle the last page where there might be only one item
        if (endIndex - 1 >= rewardThresholds.size) { // Should not happen with minOf, but as a safeguard
            binding.textViewRewardProgress.text = getString(R.string.loyalty_rewards_progress_single_format, firstRewardPoints)
        } else if (startIndex == endIndex - 1) { // Only one item on this page
            binding.textViewRewardProgress.text = getString(R.string.loyalty_rewards_progress_single_format, firstRewardPoints)
        } else {
            val lastRewardPoints = rewardThresholds[endIndex - 1]
            binding.textViewRewardProgress.text = getString(R.string.loyalty_rewards_progress_range_format, firstRewardPoints, lastRewardPoints)
        }

        binding.btnPrevReward.visibility = if (currentRewardPage == 0) View.INVISIBLE else View.VISIBLE
        binding.btnNextReward.visibility = if (currentRewardPage == totalRewardPages - 1) View.INVISIBLE else View.VISIBLE
    }

    // FUNGSI INI DIMODIFIKASI untuk menghitung halaman default
    private fun updateLoyaltyUI(status: LoyaltyUserStatus, forceRecalculatePages: Boolean = false) {
        updateUserInfo()
        binding.textViewLoyaltyPoints.text = getString(R.string.loyalty_points_format, status.totalPoints)

        if (forceRecalculatePages) {
            // Hitung halaman stempel default
            currentStampPage = if (status.totalPoints > 0) {
                (status.totalPoints - 1) / stampsPerPage
            } else {
                0
            }
            // Pastikan tidak melebihi total halaman
            currentStampPage = min(currentStampPage, totalStampPages - 1)

            // Hitung halaman reward default
            var firstUnmetRewardIndex = rewardThresholds.indexOfFirst { it > status.totalPoints }
            currentRewardPage = if (firstUnmetRewardIndex == -1 || status.totalPoints >= rewardThresholds.last()) {
                // Jika semua reward sudah terpenuhi atau user punya poin cukup untuk reward terakhir, tampilkan halaman terakhir
                totalRewardPages - 1
            } else {
                // Tampilkan halaman yang berisi reward pertama yang belum terpenuhi
                firstUnmetRewardIndex / rewardsPerPage
            }
            // Pastikan tidak melebihi total halaman
            currentRewardPage = min(currentRewardPage, totalRewardPages - 1)
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
                R.id.card_reward_grinder -> updateRewardButtonState(binding.btnClaimGrinder, binding.tvRewardPointsGrinder, status.totalPoints, 100, status.isCoffeeGrinderClaimed)
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

            100 -> if (!currentStatus.isCoffeeGrinderClaimed) {
                loyaltyViewModel.claimCoffeeGrinder()
                showToast(getString(R.string.loyalty_reward_coffee_grinder_short))
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