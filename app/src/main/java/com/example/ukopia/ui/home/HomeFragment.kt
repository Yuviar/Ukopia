package com.example.ukopia.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels // NEW IMPORT for LoyaltyViewModel
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.R
import com.example.ukopia.BestSellerAdapter
import com.example.ukopia.databinding.FragmentHomeBinding
import com.example.ukopia.MainActivity
import com.example.ukopia.models.MenuApiItem
import com.example.ukopia.ui.menu.DetailMenuFragment
import com.example.ukopia.UkopiaApplication
import com.example.ukopia.ui.menu.MenuViewModel
import com.example.ukopia.ui.menu.MenuViewModelFactory
// import com.example.ukopia.data.LoyaltyUserStatus // No direct import needed if using ViewModel and SessionManager
import com.example.ukopia.ui.loyalty.LoyaltyViewModel // NEW IMPORT
import com.example.ukopia.SessionManager // NEW IMPORT

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Inisialisasi ViewModel dengan Factory
    private val viewModel: MenuViewModel by viewModels {
        MenuViewModelFactory((requireActivity().application as UkopiaApplication).repository)
    }

    // NEW: Loyalty ViewModel for stamp card and header points
    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels() // Share ViewModel with LoyaltyFragment

    private lateinit var bestSellerAdapter: BestSellerAdapter

    // NEW: Stamp Card State variables
    private var currentStampPage = 0
    private val stampsPerPage = 10 // 5 mendatar x 2 ke bawah = 10 stempel per tampilan

    private val stampBackgrounds = mutableListOf<ImageView>()
    private val stampNumbers = mutableListOf<TextView>()
    private val stampCheckmarks = mutableListOf<ImageView>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.setBottomNavVisibility(View.VISIBLE)

        // Setup RecyclerView for Best Seller
        bestSellerAdapter = BestSellerAdapter(emptyList()) { menuItem ->
            val detailMenuFragment = DetailMenuFragment.newInstance(menuItem)
            (activity as? MainActivity)?.navigateToFragment(detailMenuFragment)
        }

        binding.bestSellerRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.bestSellerRecyclerView.adapter = bestSellerAdapter

        // Call Observer to load data for Best Seller and Stamp Card
        setupObservers()

        // Setup Stamp Card Section
        setupStampCardSection()
    }

    // Fungsi untuk Best Seller dan Header/Stamp Card data
    private fun setupObservers() {
        viewModel.menuItems.observe(viewLifecycleOwner, Observer { menuList ->
            if (menuList != null) {
                val bestSellerItems = getTopBestSellerItems(menuList)
                bestSellerAdapter.updateData(bestSellerItems)
            }
        })

        // NEW: Observe loyaltyUserStatus for header and stamp card update
        loyaltyViewModel.loyaltyUserStatus.observe(viewLifecycleOwner) { status ->
            // Update User Info Header
            val userName = SessionManager.getUserName(requireContext())
            binding.textViewUserName.text = getString(R.string.welcome_format, userName ?: "Guest")
            binding.textViewLoyaltyPoints.text = getString(R.string.loyalty_points_format, status.totalPoints)

            // Update Stamp Card Display
            updateStampCardDisplay(status.totalPoints)
        }
    }

    private fun getTopBestSellerItems(all: List<MenuApiItem>): List<MenuApiItem> {
        return all.sortedByDescending { it.average_rating }
            .take(5)
    }

    // Functions for Stamp Card
    private fun setupStampCardSection() {
        initializeStampViews() // Find and store references to stamp UI elements

        binding.btnNextStamp.setOnClickListener {
            val totalPoints = loyaltyViewModel.loyaltyUserStatus.value?.totalPoints ?: 0
            val maxStampPageBasedOnPoints = if (totalPoints == 0) 0 else (totalPoints - 1) / stampsPerPage

            if (currentStampPage < maxStampPageBasedOnPoints) { // Allow navigation up to the page containing the last earned stamp
                currentStampPage++
                updateStampCardDisplay(totalPoints)
            }
        }

        binding.btnPrevStamp.setOnClickListener {
            if (currentStampPage > 0) {
                currentStampPage--
                updateStampCardDisplay(loyaltyViewModel.loyaltyUserStatus.value?.totalPoints ?: 0)
            }
        }

        // Initial display update based on current points
        updateStampCardDisplay(loyaltyViewModel.loyaltyUserStatus.value?.totalPoints ?: 0)
    }

    private fun initializeStampViews() {
        stampBackgrounds.clear()
        stampNumbers.clear()
        stampCheckmarks.clear()

        for (i in 1..stampsPerPage) {
            val backgroundId = resources.getIdentifier("iv_stamp_background_$i", "id", requireContext().packageName)
            val numberId = resources.getIdentifier("tv_stamp_number_$i", "id", requireContext().packageName)
            val checkmarkId = resources.getIdentifier("iv_stamp_checkmark_$i", "id", requireContext().packageName)

            // Ensure these views are found within the current binding root.
            binding.root.findViewById<ImageView>(backgroundId)?.let { stampBackgrounds.add(it) }
            binding.root.findViewById<TextView>(numberId)?.let { stampNumbers.add(it) }
            binding.root.findViewById<ImageView>(checkmarkId)?.let { stampCheckmarks.add(it) }
        }
    }

    // Fungsi untuk memperbarui tampilan 10 stempel yang sedang ditampilkan
    private fun updateStampCardDisplay(totalPoints: Int) {
        val startIndex = currentStampPage * stampsPerPage

        if (stampBackgrounds.isEmpty() || stampNumbers.isEmpty() || stampCheckmarks.isEmpty()) {
            return
        }

        for (i in 0 until stampsPerPage) {
            val stampActualNumber = startIndex + i + 1

            // All stamps on the current page should be visible.
            // But only stamps with number <= totalPoints will be "stamped".
            if (i < stampBackgrounds.size) { // Add bounds check
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
        }
        updateStampNavigationIndicator(totalPoints)
    }

    private fun updateStampNavigationIndicator(totalPoints: Int) {
        val startStamp = currentStampPage * stampsPerPage + 1
        val endStamp = (currentStampPage + 1) * stampsPerPage

        val maxPageForNextButton = if (totalPoints == 0) 0 else (totalPoints - 1) / stampsPerPage

        binding.textViewStampProgress.text = getString(R.string.loyalty_stamp_progress_format, startStamp, endStamp)

        binding.btnPrevStamp.visibility = if (currentStampPage == 0) View.INVISIBLE else View.VISIBLE
        // Next button is visible if current page is not the last page that contains earned stamps
        binding.btnNextStamp.visibility = if (currentStampPage >= maxPageForNextButton) View.INVISIBLE else View.VISIBLE
    }
    // END: Functions for Stamp Card


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}