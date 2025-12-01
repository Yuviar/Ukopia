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
    private var maxReachableRewardPage = 0 // NEW: Tambah variabel ini

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
        // Kita hanya akan mencari reward_card_1 dan reward_card_2 karena hanya itu yang ada di XML
        // Jika Anda ingin lebih banyak, Anda harus menambahkannya di XML
        for (i in 1..rewardsPerPage) { // rewardsPerPage saat ini adalah 2
            val cardId = resources.getIdentifier("reward_card_$i", "id", requireContext().packageName)
            binding.root.findViewById<MaterialCardView>(cardId)?.let {
                containers.add(it)
            } ?: Log.w("LoyaltyFragment", "Reward card with ID 'reward_card_$i' not found in layout. Make sure to define it in XML if rewardsPerPage > 2.")
        }
        return containers
    }

    // MODIFIED: getPaginatedRewards now returns all rewards, chunked
    private fun getPaginatedRewards(): List<List<LoyaltyReward>> {
        return ALL_LOYALTY_REWARDS.chunked(rewardsPerPage)
    }

    private fun updateRewardDisplay(totalPoints: Int, status: LoyaltyUserStatus) {
        val paginatedRewards = getPaginatedRewards() // Get all rewards, chunked
        val totalPages = paginatedRewards.size

        Log.d("LoyaltyFragment", "RewardDisplay: Total Points=$totalPoints, Total Pages=$totalPages, Current Page (before adjust)=$currentPageReward")
        Log.d("LoyaltyFragment", "RewardDisplay: Paginated Rewards Structure: ${paginatedRewards.joinToString { page -> page.joinToString { it.title } }}")

        // --- PENTING: LOGIKA PENYESUAIAN currentPageReward HANYA DI onResume() ---
        // Logika penyesuaian otomatis ini telah dipindahkan ke onResume()
        // agar tidak mengganggu navigasi manual oleh pengguna.
        // --- AKHIR PENTING ---

        val currentRewards = paginatedRewards.getOrNull(currentPageReward) ?: emptyList()
        val allRewardContainers = getRewardContainers() // Ambil semua 2 container

        Log.d("LoyaltyFragment", "RewardDisplay: Current Page (after adjust)=$currentPageReward, Current Rewards count=${currentRewards.size}")
        Log.d("LoyaltyFragment", "RewardDisplay: Current Rewards on page $currentPageReward: ${currentRewards.joinToString { "${it.title} (${it.threshold} pts)" }}")


        // Sembunyikan semua kontainer reward terlebih dahulu
        allRewardContainers.forEach { it.visibility = View.GONE }

        if (currentRewards.isEmpty()) {
            Log.d("LoyaltyFragment", "RewardDisplay: No rewards to display for current page. Check paginatedRewards content.")
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

                    // Mengambil view berdasarkan ID yang sesuai dengan card_1 atau card_2
                    val tvRewardTitle = cardView.findViewById<TextView>(resources.getIdentifier("tv_reward_title_${index + 1}", "id", requireContext().packageName))
                    val ivRewardIcon = cardView.findViewById<ImageView>(resources.getIdentifier("iv_reward_icon_${index + 1}", "id", requireContext().packageName))
                    val tvRewardPoints = cardView.findViewById<TextView>(resources.getIdentifier("tv_reward_points_${index + 1}", "id", requireContext().packageName))
                    val btnClaim = cardView.findViewById<MaterialButton>(resources.getIdentifier("btn_claim_status_${index + 1}", "id", requireContext().packageName))

                    if (tvRewardTitle == null || ivRewardIcon == null || tvRewardPoints == null || btnClaim == null) {
                        Log.e("LoyaltyFragment", "RewardDisplay: One or more views not found for reward card ${index + 1}. Check XML IDs.")
                        return@forEachIndexed // Skip to next reward if views are missing
                    }

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
                        else -> { // Should not happen with current getStatus logic
                            statusText = "-"
                            backgroundColor = ContextCompat.getColor(requireContext(), R.color.light_grey)
                            textColor = ContextCompat.getColor(requireContext(), R.color.black)
                        }
                    }

                    btnClaim.text = statusText
                    btnClaim.setBackgroundColor(backgroundColor)
                    btnClaim.setTextColor(textColor)

                    btnClaim.setOnClickListener(null) // Reward buttons are currently not clickable
                    btnClaim.isEnabled = false // Disable interaction
                } else {
                    Log.w("LoyaltyFragment", "RewardDisplay: Attempted to display reward at index $index, but cardView is null (card_${index+1}). Check XML for correct ID.")
                }
            } else {
                Log.w("LoyaltyFragment", "RewardDisplay: Attempted to display reward at index $index, but only ${rewardsPerPage} containers are shown per page. Reward will not be visible.")
            }
        }

        binding.btnPrevReward.visibility = if (currentPageReward > 0) View.VISIBLE else View.INVISIBLE

        // MODIFIED: Tombol Next Reward terlihat jika ada halaman selanjutnya DAN currentPageReward belum mencapai halaman maksimal yang bisa diakses
        val canGoNextRewardPage = (currentPageReward < totalPages - 1) && (currentPageReward < maxReachableRewardPage)

        binding.btnNextReward.visibility = if (canGoNextRewardPage) View.VISIBLE else View.INVISIBLE

        val start = currentPageReward * rewardsPerPage + 1
        val end = currentPageReward * rewardsPerPage + currentRewards.size
        binding.textViewRewardProgress.text = "Rewards $start - $end"
    }

    private fun navigateReward(next: Boolean) {
        val paginatedRewards = getPaginatedRewards() // Get all rewards, chunked
        val totalPages = paginatedRewards.size
        val totalPoints = viewModel.loyaltyUserStatus.value?.totalPoints ?: 0 // Keep totalPoints for Next logic

        Log.d("LoyaltyFragment", "NavigateReward: current page $currentPageReward, total pages $totalPages, total points $totalPoints, maxReachablePage=$maxReachableRewardPage, next: $next")

        if (next) {
            // Logika untuk tombol Next Reward
            // Hanya izinkan navigasi Next jika ada halaman selanjutnya secara total DAN belum melewati maxReachableRewardPage
            if (currentPageReward < totalPages - 1 && currentPageReward < maxReachableRewardPage) {
                currentPageReward++
                Log.d("LoyaltyFragment", "NavigateReward: moved to page $currentPageReward")
            } else {
                Log.d("LoyaltyFragment", "NavigateReward: Cannot go next. Current page: $currentPageReward, Max pages: $totalPages, Max reachable page: $maxReachableRewardPage")
            }
        } else { // Previous
            // Logika untuk tombol Previous
            // Selalu izinkan navigasi Previous jika tidak di halaman pertama
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

    // --- MODIFIKASI FUNGSI updateStampCard ---
    private fun updateStampCard(totalPoints: Int) {
        val totalStampPages = (maxStamps + stampsPerPage - 1) / stampsPerPage

        // --- MODIFIKASI: Sesuaikan currentPageStamp secara otomatis ---
        // Poin pengguna menentukan halaman maksimum yang bisa dia akses.
        val maxReachableStamp = totalPoints
        val maxReachablePage = if (maxReachableStamp > 0) (maxReachableStamp - 1) / stampsPerPage else 0

        // Jika currentPageStamp saat ini melebihi halaman yang bisa diakses, kembalikan ke halaman maksimal yang bisa diakses.
        // Ini memastikan saat user punya 11 poin, dia langsung di halaman 11-20
        // Atau jika halaman saat ini tidak relevan dengan poin yang baru diupdate
        if (currentPageStamp > maxReachablePage || currentPageStamp >= totalStampPages) {
            currentPageStamp = maxReachablePage
            Log.d("LoyaltyFragment", "StampDisplay: currentPageStamp adjusted to: $currentPageStamp (maxReachablePage: $maxReachablePage)")
        }
        // Pastikan juga currentPageStamp tidak keluar dari batas total halaman yang ada di XML (maxStamps)
        // Dihapus karena sudah dicover oleh `coerceIn` dan `currentPageStamp > maxReachablePage`
        // if (totalStampPages == 0) {
        //     currentPageStamp = 0
        // } else if (currentPageStamp >= totalStampPages) {
        //     currentPageStamp = totalStampPages - 1
        // } else if (currentPageStamp < 0) {
        //     currentPageStamp = 0
        // }
        // --- AKHIR MODIFIKASI: Sesuaikan currentPageStamp secara otomatis ---

        // Loop over the 10 available UI elements (indices 0 to 9, mapping to _1 to _10 in XML)
        for (i in 0 until stampsPerPage) {
            // Calculate the actual stamp number for the current UI slot on the current page
            val stampActualNumber = currentPageStamp * stampsPerPage + i + 1

            // Get resource IDs for the current UI slot (e.g., _1, _2, ..., _10)
            val bgId = resources.getIdentifier("iv_stamp_background_${i + 1}", "id", requireContext().packageName)
            val numId = resources.getIdentifier("tv_stamp_number_${i + 1}", "id", requireContext().packageName)
            val checkId = resources.getIdentifier("iv_stamp_checkmark_${i + 1}", "id", requireContext().packageName)

            val bgView = binding.root.findViewById<ImageView>(bgId)
            val numView = binding.root.findViewById<TextView>(numId)
            val checkView = binding.root.findViewById<ImageView>(checkId)

            if (bgView != null && numView != null && checkView != null) {
                // Only display stamps that are within the overall maxStamps limit
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
                    // Sembunyikan stamp jika nomornya melebihi total maxStamps (misal, di halaman terakhir jika maxStamps bukan kelipatan 10)
                    bgView.visibility = View.GONE
                    numView.visibility = View.GONE
                    checkView.visibility = View.GONE
                }
            } else {
                // Log jika ID stamp tidak ditemukan di XML. Ini terjadi jika XML tidak lengkap (misal, hanya ada stamp 1-5)
                Log.w("LoyaltyFragment", "StampDisplay: Stamp view with ID 'iv_stamp_background_${i + 1}' (or number/checkmark) not found in layout. Make sure to define it in XML for indices 1-10.")
            }
        }

        // Update teks progress stamp
        val startDisplayStamp = currentPageStamp * stampsPerPage + 1
        val endDisplayStamp = (currentPageStamp * stampsPerPage + stampsPerPage).coerceAtMost(maxStamps)
        binding.textViewStampProgress.text = "Stamps $startDisplayStamp - $endDisplayStamp"

        // --- MODIFIKASI LOGIKA VISIBILITAS TOMBOL NAVIGASI STAMP ---
        // Tombol Prev Stamp
        binding.btnPrevStamp.visibility = if (currentPageStamp > 0) View.VISIBLE else View.INVISIBLE

        // Tombol Next Stamp terlihat jika ada halaman selanjutnya SECARA TOTAL
        // DAN jika poin pengguna telah mencapai setidaknya stempel pertama di halaman berikutnya
        val canGoNextPage = (currentPageStamp < totalStampPages - 1) && (totalPoints >= (currentPageStamp + 1) * stampsPerPage)
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

        Log.d("LoyaltyFragment", "NavigateStamp: current page $currentPageStamp, total pages $totalStampPages, total points $totalPoints, next: $next")

        if (next) {
            // Hanya navigasi ke halaman selanjutnya jika ada DAN jika poin pengguna telah mencapai setidaknya stempel pertama di halaman berikutnya
            if (currentPageStamp < totalStampPages - 1 && totalPoints >= (currentPageStamp + 1) * stampsPerPage) {
                currentPageStamp++
                Log.d("LoyaltyFragment", "NavigateStamp: moved to page $currentPageStamp")
            } else {
                Log.d("LoyaltyFragment", "NavigateStamp: Cannot go next. Current page: $currentPageStamp, Max pages: $totalStampPages, Points: $totalPoints, Required for next page: ${(currentPageStamp + 1) * stampsPerPage}")
            }
        } else { // Previous
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
    // --- Akhir fungsi navigasi stamp baru ---

    override fun onResume() {
        super.onResume()
        // Reset currentPageReward dan currentPageStamp ke default yang relevan saat fragment dibuka kembali

        // --- Perbaikan: Logika inisialisasi currentPageReward dipindahkan ke sini ---
        val totalRewardPages = ALL_LOYALTY_REWARDS.chunked(rewardsPerPage).size
        val totalPoints = viewModel.loyaltyUserStatus.value?.totalPoints ?: 0
        val nextRewardIndex = ALL_LOYALTY_REWARDS.indexOfFirst { it.threshold > totalPoints }
        val targetDefaultRewardPage: Int

        if (nextRewardIndex != -1) {
            targetDefaultRewardPage = (nextRewardIndex / rewardsPerPage).coerceIn(0, if (totalRewardPages > 0) totalRewardPages - 1 else 0)
        } else {
            targetDefaultRewardPage = if (totalRewardPages > 0) totalRewardPages - 1 else 0
        }
        currentPageReward = targetDefaultRewardPage // Set nilai default di onResume
        maxReachableRewardPage = targetDefaultRewardPage // NEW: Simpan nilai ini
        Log.d("LoyaltyFragment", "onResume: Initial currentPageReward set to $currentPageReward for totalPoints $totalPoints, maxReachableRewardPage=$maxReachableRewardPage")
        // --- Akhir perbaikan inisialisasi currentPageReward ---


        val maxReachablePage = if (totalPoints > 0) (totalPoints - 1) / stampsPerPage else 0
        currentPageStamp = maxReachablePage.coerceIn(0, (maxStamps + stampsPerPage - 1) / stampsPerPage -1 ) // Langsung ke halaman yang sesuai dengan poin tertinggi
        Log.d("LoyaltyFragment", "onResume: Initial currentPageStamp set to $currentPageStamp for totalPoints $totalPoints")
        viewModel.refreshLoyaltyData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}