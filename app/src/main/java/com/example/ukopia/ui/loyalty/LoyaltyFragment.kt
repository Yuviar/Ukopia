package com.example.ukopia.ui.loyalty

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout // BARU: Import ini
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
import com.example.ukopia.ui.auth.LoginActivity
import com.example.ukopia.adapter.LoyaltyAdapter
import com.example.ukopia.databinding.DialogLoginRequiredBinding
import com.example.ukopia.databinding.FragmentLoyaltyBinding
import android.content.res.ColorStateList

class LoyaltyFragment : Fragment() {

    private lateinit var binding: FragmentLoyaltyBinding
    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels()

    private lateinit var loyaltyItemAdapter: LoyaltyAdapter
    private var pendingAddLoyaltyAction = false

    private val regularStampImageViews = mutableListOf<ImageView>()

    // Variabel untuk FrameLayout, TextView, dan ImageView di reward circles
    private lateinit var containerRewardDisc10: FrameLayout
    private lateinit var tvRewardDisc10Text: TextView
    private lateinit var ivRewardDisc10Checkmark: ImageView

    private lateinit var containerRewardFreeServe: FrameLayout
    private lateinit var tvRewardFreeServeText: TextView
    private lateinit var ivRewardFreeServeCheckmark: ImageView

    private lateinit var containerRewardDisc10_2: FrameLayout
    private lateinit var tvRewardDisc10_2Text: TextView
    private lateinit var ivRewardDisc10_2Checkmark: ImageView

    private lateinit var containerRewardFreeTshirt: FrameLayout
    private lateinit var tvRewardFreeTshirtText: TextView
    private lateinit var ivRewardFreeTshirtCheckmark: ImageView


    private val loginActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && SessionManager.isLoggedIn(requireContext())) {
            loyaltyViewModel.loyaltyUserStatus.value?.let { updateLoyaltyUI(it.totalPoints) }
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
                        loyaltyViewModel.loyaltyUserStatus.value?.let { updateLoyaltyUI(it.totalPoints) }
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
        initializeViews()

        binding.fabAddRecipe.setOnClickListener { handleFabClick() }

        loyaltyViewModel.loyaltyUserStatus.observe(viewLifecycleOwner) { status ->
            view.post { updateLoyaltyUI(status.totalPoints) }
        }

        loyaltyViewModel.loyaltyItems.observe(viewLifecycleOwner) { items ->
            loyaltyItemAdapter.submitList(items)
            updateLoyaltyItemsVisibility()
        }
    }

    private fun initializeViews() {
        regularStampImageViews.clear()
        regularStampImageViews.addAll(listOf(
            binding.ivStamp1, binding.ivStamp2, binding.ivStamp3, binding.ivStamp4,
            binding.ivStamp6, binding.ivStamp7, binding.ivStamp8, binding.ivStamp9
        ))

        // Inisialisasi referensi untuk FrameLayout, TextView, dan ImageView reward circles
        containerRewardDisc10 = binding.containerRewardDisc10Circle
        tvRewardDisc10Text = binding.tvRewardDisc10Text
        ivRewardDisc10Checkmark = binding.ivRewardDisc10Checkmark

        containerRewardFreeServe = binding.containerRewardFreeServeCircle
        tvRewardFreeServeText = binding.tvRewardFreeServeText
        ivRewardFreeServeCheckmark = binding.ivRewardFreeServeCheckmark

        containerRewardDisc10_2 = binding.containerRewardDisc102Circle
        tvRewardDisc10_2Text = binding.tvRewardDisc102Text
        ivRewardDisc10_2Checkmark = binding.ivRewardDisc102Checkmark

        containerRewardFreeTshirt = binding.containerRewardFreeTshirtCircle
        tvRewardFreeTshirtText = binding.tvRewardFreeTshirtText
        ivRewardFreeTshirtCheckmark = binding.ivRewardFreeTshirtCheckmark

        // Setup listeners untuk container reward circles
        containerRewardDisc10.setOnClickListener { handleRewardClaim(5) }
        containerRewardFreeServe.setOnClickListener { handleRewardClaim(10) }
        containerRewardDisc10_2.setOnClickListener { handleRewardClaim(15) }
        containerRewardFreeTshirt.setOnClickListener { handleRewardClaim(20) }
    }

    private fun updateLoyaltyUI(totalPoints: Int) {
        binding.textViewLoyaltyPoints.text = getString(R.string.loyalty_points_format, totalPoints)
        val status = loyaltyViewModel.loyaltyUserStatus.value ?: return

        // Level 1: Stamps 1-10
        updateRegularStamps(0, 4, 1, totalPoints) // Stamps 1-4
        updateRewardCircle(
            containerRewardDisc10, tvRewardDisc10Text, ivRewardDisc10Checkmark,
            totalPoints, 5, status.isDiscount10Claimed, R.string.loyalty_reward_10_percent_discount_short
        )
        updateRegularStamps(4, 8, 2, totalPoints) // Stamps 6-9 (index 4-7 dari list, tapi representasi 6-9)
        updateRewardCircle(
            containerRewardFreeServe, tvRewardFreeServeText, ivRewardFreeServeCheckmark,
            totalPoints, 10, status.isFreeServeClaimed, R.string.loyalty_reward_free_serve_short
        )

        // Level 2 (Progressive Unlocking): Stamps 11-20
        if (status.isFreeServeClaimed) {
            binding.row3Container.visibility = View.VISIBLE
            binding.row4Container.visibility = View.VISIBLE

            // Pastikan list regularStampImageViews diperbarui dengan stamp baru
            // Perlu clear dan add ulang agar tidak duplikat jika dipanggil berkali-kali
            if (regularStampImageViews.size == 8) { // Jika hanya berisi stamp level 1
                regularStampImageViews.addAll(listOf(
                    binding.ivStamp11, binding.ivStamp12, binding.ivStamp13, binding.ivStamp14,
                    binding.ivStamp16, binding.ivStamp17, binding.ivStamp18, binding.ivStamp19
                ))
            }


            updateRegularStamps(8, 12, 3, totalPoints) // Stamps 11-14 (index 8-11 dari list)
            updateRewardCircle(
                containerRewardDisc10_2, tvRewardDisc10_2Text, ivRewardDisc10_2Checkmark,
                totalPoints, 15, status.isDiscount10Slot15Claimed, R.string.loyalty_reward_10_percent_discount_short
            )
            updateRegularStamps(12, 16, 4, totalPoints) // Stamps 16-19 (index 12-15 dari list)
            updateRewardCircle(
                containerRewardFreeTshirt, tvRewardFreeTshirtText, ivRewardFreeTshirtCheckmark,
                totalPoints, 20, status.isFreeTshirtClaimed, R.string.loyalty_reward_free_tshirt_short
            )

        } else {
            binding.row3Container.visibility = View.GONE
            binding.row4Container.visibility = View.GONE
            // Jika rewards level 2 tidak aktif, pastikan stamp ImageView dari level 2 tidak ada di list aktif
            // Jika rewards level 2 dinonaktifkan, hapus elemen stamp level 2 dari list
            if (regularStampImageViews.size > 8) {
                regularStampImageViews.subList(8, regularStampImageViews.size).clear()
            }
        }
    }

    private fun updateRegularStamps(startIdx: Int, endIdx: Int, pointOffset: Int, totalPoints: Int) {
        for (i in startIdx until endIdx) {
            val imageView = regularStampImageViews[i]
            val actualPointIndex = i + pointOffset
            if (actualPointIndex <= totalPoints) {
                imageView.background = ContextCompat.getDrawable(requireContext(), R.drawable.circle_background_white_stroke_black_fill)
                imageView.setImageResource(R.drawable.ic_checkmark) // Menggunakan ic_checkmark
                imageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white)) // Centang putih
            } else {
                imageView.background = ContextCompat.getDrawable(requireContext(), R.drawable.reward_circle_background_default)
                imageView.setImageResource(0) // Tidak ada gambar
                imageView.setColorFilter(null) // Hapus filter warna
            }
        }
    }

    // Fungsi baru untuk memperbarui tampilan lingkaran hadiah
    private fun updateRewardCircle(
        container: FrameLayout,
        textView: TextView,
        imageViewCheckmark: ImageView,
        currentPoints: Int,
        threshold: Int,
        isClaimed: Boolean,
        defaultTextResId: Int
    ) {
        val defaultBg = ContextCompat.getDrawable(requireContext(), R.drawable.reward_circle_background_default)
        val claimedBg = ContextCompat.getDrawable(requireContext(), R.drawable.circle_background_white_stroke_black_fill)

        if (isClaimed) {
            container.background = claimedBg // Latar belakang hitam dengan border putih
            imageViewCheckmark.setImageResource(R.drawable.ic_checkmark)
            imageViewCheckmark.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white)) // Centang putih
            imageViewCheckmark.visibility = View.VISIBLE
            textView.visibility = View.GONE // Sembunyikan teks
        } else if (currentPoints >= threshold) {
            container.background = defaultBg // Latar belakang putih dengan border hitam
            textView.text = getString(R.string.loyalty_reward_claim_action) // Teks "CLAIM"
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            textView.visibility = View.VISIBLE
            imageViewCheckmark.visibility = View.GONE // Sembunyikan centang
            imageViewCheckmark.setColorFilter(null) // Hapus filter warna
        } else {
            container.background = defaultBg // Latar belakang putih dengan border hitam
            textView.text = getString(defaultTextResId) // Teks "DISC 10%", dll.
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            textView.visibility = View.VISIBLE
            imageViewCheckmark.visibility = View.GONE // Sembunyikan centang
            imageViewCheckmark.setColorFilter(null) // Hapus filter warna
        }
        // Pastikan gravitasi selalu di tengah untuk teks
        textView.gravity = Gravity.CENTER
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