package com.example.ukopia.ui.home

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.ukopia.R
import com.example.ukopia.BestSellerAdapter
import com.example.ukopia.databinding.FragmentHomeBinding
import com.example.ukopia.MainActivity
import com.example.ukopia.models.MenuApiItem
import com.example.ukopia.ui.menu.DetailMenuFragment
import com.example.ukopia.ui.menu.MenuViewModelFactory
import com.example.ukopia.UkopiaApplication
import com.example.ukopia.ui.menu.MenuViewModel
import com.example.ukopia.ui.loyalty.LoyaltyViewModel
import com.example.ukopia.SessionManager
import com.example.ukopia.models.ApiClient
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MenuViewModel by activityViewModels {
        MenuViewModelFactory((requireActivity().application as UkopiaApplication).repository)
    }

    private val loyaltyViewModel: LoyaltyViewModel by activityViewModels()
    private lateinit var bestSellerAdapter: BestSellerAdapter

    private var currentStampPage = 0
    private val stampsPerPage = 10
    private val maxStamps = 100
    private val stampBackgrounds = mutableListOf<ImageView>()
    private val stampNumbers = mutableListOf<TextView>()
    private val stampCheckmarks = mutableListOf<ImageView>()
    private val TAG = "HomeFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.setBottomNavVisibility(View.VISIBLE)
        viewModel.loadPromo()

        setupBestSellerRecyclerView()
        setupObservers()
        setupStampCardSection()

        loadPromoBanner()
    }

    private fun setupBestSellerRecyclerView() {
        bestSellerAdapter = BestSellerAdapter(emptyList()) { menuItem ->
            val detailMenuFragment = DetailMenuFragment.newInstance(menuItem)
            (activity as? MainActivity)?.navigateToFragment(detailMenuFragment)
        }
        binding.bestSellerRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.bestSellerRecyclerView.adapter = bestSellerAdapter
    }

private fun loadPromoBanner() {
    lifecycleScope.launch {
        try {
            val response = ApiClient.instance.getLatestPromo()

            _binding?.let { bind ->
                if (response.isSuccessful && response.body()?.success == true) {
                    val promoData = response.body()

                    if (_binding == null) {
                        Log.w(TAG, "loadPromoBanner: View destroyed, skipping UI update for promo success.")
                        return@launch
                    }

                    if (promoData?.hasPromo == true && !promoData.imageUrl.isNullOrEmpty()) {
                        bind.promoCardView.visibility = View.VISIBLE

                        bind.promoImage.load(promoData.imageUrl) {
                        binding.promoCardView.visibility = View.VISIBLE
                        binding.promoImage.load(promoData.imageUrl) {
                            crossfade(true)
                            error(R.drawable.ic_error)
                        }
                    } }else {
                        bind.promoCardView.visibility = View.GONE
                    }
                } else {
                    bind.promoCardView.visibility = View.GONE
                    if (_binding == null) {
                        Log.w(TAG, "loadPromoBanner: View destroyed, skipping UI update for promo failure.")
                        return@launch
                    }
                    binding.promoCardView.visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal load promo: ${e.message}")
            if (_binding == null) {
                Log.w(TAG, "loadPromoBanner: View destroyed, skipping UI update for promo exception.")
                return@launch
            }
            binding.promoCardView.visibility = View.GONE
        }
    }
}


    private fun setupObservers() {
        viewModel.menuItems.observe(viewLifecycleOwner) { menuList ->
            if (_binding == null) {
                Log.w(TAG, "setupObservers: View destroyed, skipping menu items update.")
                return@observe
            }
            if (menuList != null) {
                val bestSellerItems = getTopBestSellerItems(menuList)
                bestSellerAdapter.updateData(bestSellerItems)
            }
        }

        loyaltyViewModel.loyaltyUserStatus.observe(viewLifecycleOwner) { status ->
            if (_binding == null) {
                Log.w(TAG, "loyaltyUserStatus observer: View destroyed, skipping UI update.")
                return@observe
            }

            val isLoggedIn = SessionManager.isLoggedIn(requireContext())
            val userName = SessionManager.getUserName(requireContext())

            if (isLoggedIn && !userName.isNullOrEmpty()) {
                binding.textViewUserName.text = getString(R.string.welcome_format, userName)
            } else {
                binding.textViewUserName.text = getString(R.string.greeting_salutation_default)
            }

            if (isLoggedIn) {
                binding.textViewLoyaltyPoints.visibility = View.VISIBLE
                binding.tvHomeStampCardTitle.visibility = View.VISIBLE
                binding.stampCardView.visibility = View.VISIBLE

                binding.textViewLoyaltyPoints.text = getString(R.string.loyalty_points_format, status.totalPoints)
                updateStampCardDisplay(status.totalPoints)
                updateStampNavigationIndicator(status.totalPoints)
            } else {
                binding.textViewLoyaltyPoints.visibility = View.GONE
                binding.tvHomeStampCardTitle.visibility = View.GONE
                binding.stampCardView.visibility = View.GONE
            }
        }
        viewModel.promoData.observe(viewLifecycleOwner) { promoData ->
            _binding?.let { bind ->
                if (promoData?.hasPromo == true && !promoData.imageUrl.isNullOrEmpty()) {
                    bind.promoCardView.visibility = View.VISIBLE
                    bind.promoImage.load(promoData.imageUrl) {
                        crossfade(true)
                        error(R.drawable.ic_error)
                    }
                } else {
                    bind.promoCardView.visibility = View.GONE
                }
            }
        }
    }

    private fun getTopBestSellerItems(all: List<MenuApiItem>): List<MenuApiItem> {
        return all.sortedByDescending { it.average_rating }.take(2)
    }

    private fun setupStampCardSection() {
        Log.d(TAG, "setupStampCardSection called.")
        initializeStampViews()

        binding.btnNextStamp.setOnClickListener {
            if (_binding == null) {
                Log.w(TAG, "btnNextStamp click: View destroyed, skipping action.")
                return@setOnClickListener
            }

            val totalPoints = loyaltyViewModel.loyaltyUserStatus.value?.totalPoints ?: 0
            val maxStampPageBasedOnPoints = if (totalPoints == 0) 0 else (totalPoints - 1) / stampsPerPage

            if (currentStampPage < maxStampPageBasedOnPoints) {
                currentStampPage++
                updateStampCardDisplay(totalPoints)
                updateStampNavigationIndicator(totalPoints)
            }
        }

        binding.btnPrevStamp.setOnClickListener {
            if (_binding == null) {
                Log.w(TAG, "btnPrevStamp click: View destroyed, skipping action.")
                return@setOnClickListener
            }

            if (currentStampPage > 0) {
                currentStampPage--
                updateStampCardDisplay(loyaltyViewModel.loyaltyUserStatus.value?.totalPoints ?: 0)
                updateStampNavigationIndicator(loyaltyViewModel.loyaltyUserStatus.value?.totalPoints ?: 0)
            }
        }
    }

    private fun initializeStampViews() {
        stampBackgrounds.clear()
        stampNumbers.clear()
        stampCheckmarks.clear()
        Log.d(TAG, "initializeStampViews: Cleared previous lists.")

        if (_binding == null) {
            Log.w(TAG, "initializeStampViews: _binding is null, stopping initialization.")
            return
        }

        for (i in 1..stampsPerPage) {
            val backgroundId = resources.getIdentifier("iv_stamp_background_$i", "id", requireContext().packageName)
            val numberId = resources.getIdentifier("tv_stamp_number_$i", "id", requireContext().packageName)
            val checkmarkId = resources.getIdentifier("iv_stamp_checkmark_$i", "id", requireContext().packageName)

            Log.d(TAG, "Searching for: iv_stamp_background_$i (ID=$backgroundId), tv_stamp_number_$i (ID=$numberId), iv_stamp_checkmark_$i (ID=$checkmarkId)")

            val bg = binding.root.findViewById<ImageView>(backgroundId)
            val num = binding.root.findViewById<TextView>(numberId)
            val check = binding.root.findViewById<ImageView>(checkmarkId)

            if (bg != null) {
                stampBackgrounds.add(bg)
                Log.d(TAG, "Added background $i.")
            } else {
                Log.w(TAG, "ImageView iv_stamp_background_$i not found!")
            }
            if (num != null) {
                stampNumbers.add(num)
                Log.d(TAG, "Added number $i.")
            } else {
                Log.w(TAG, "TextView tv_stamp_number_$i not found!")
            }
            if (check != null) {
                stampCheckmarks.add(check)
                Log.d(TAG, "Added checkmark $i.")
            } else {
                Log.w(TAG, "ImageView iv_stamp_checkmark_$i not found!")
            }
        }
        Log.d(TAG, "initializeStampViews: Finished. Lists sizes -> Backgrounds: ${stampBackgrounds.size}, Numbers: ${stampNumbers.size}, Checkmarks: ${stampCheckmarks.size}")
    }

    private fun updateStampCardDisplay(totalPoints: Int) {
        Log.d(TAG, "updateStampCardDisplay called with totalPoints: $totalPoints, currentPage: $currentStampPage")
        val startIndex = currentStampPage * stampsPerPage

        if (stampBackgrounds.isEmpty() || stampNumbers.isEmpty() || stampCheckmarks.isEmpty()) {
            Log.e(TAG, "updateStampCardDisplay: Stamp view lists are empty. Cannot update display.")
            return
        }
        if (_binding == null) {
            Log.w(TAG, "updateStampCardDisplay: View destroyed, skipping UI update.")
            return
        }

        Log.d(TAG, "updateStampCardDisplay: Lists are populated. Sizes: bg=${stampBackgrounds.size}, num=${stampNumbers.size}, check=${stampCheckmarks.size}")


        for (i in 0 until stampsPerPage) {
            val stampActualNumber = startIndex + i + 1
            if (stampActualNumber <= maxStamps) {
                if (i < stampBackgrounds.size) {
                    stampBackgrounds[i].visibility = View.VISIBLE
                    stampNumbers[i].text = stampActualNumber.toString()

                    if (stampActualNumber <= totalPoints) {
                        stampBackgrounds[i].background = ContextCompat.getDrawable(requireContext(), R.drawable.circle_background_white_stroke_black_fill)
                        stampNumbers[i].visibility = View.GONE
                        stampCheckmarks[i].visibility = View.VISIBLE
                        Log.d(TAG, "Stamp $stampActualNumber: FILLED")
                    } else {
                        stampBackgrounds[i].background = ContextCompat.getDrawable(requireContext(), R.drawable.reward_circle_background_default)
                        stampNumbers[i].visibility = View.VISIBLE
                        stampNumbers[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                        stampCheckmarks[i].visibility = View.GONE
                        Log.d(TAG, "Stamp $stampActualNumber: EMPTY")
                    }
                } else {
                    Log.w(TAG, "Skipping stamp index $i because stampBackgrounds.size (${stampBackgrounds.size}) is too small.")
                }
            } else {
                if (i < stampBackgrounds.size) {
                    stampBackgrounds[i].visibility = View.GONE
                    stampNumbers[i].visibility = View.GONE
                    stampCheckmarks[i].visibility = View.GONE
                }
            }
        }
    }

    private fun updateStampNavigationIndicator(totalPoints: Int) {
        if (_binding == null) {
            Log.w(TAG, "updateStampNavigationIndicator: View destroyed, skipping UI update.")
            return
        }

        val startStamp = currentStampPage * stampsPerPage + 1
        val endStamp = (currentStampPage * stampsPerPage + stampsPerPage).coerceAtMost(maxStamps)

        val totalStampPages = (maxStamps + stampsPerPage - 1) / stampsPerPage
        val maxPageForNextButton = if (totalPoints == 0) 0 else (totalPoints - 1) / stampsPerPage

        binding.textViewStampProgress.text = getString(R.string.loyalty_stamp_progress_format, startStamp, endStamp)

        binding.btnPrevStamp.visibility = if (currentStampPage == 0) View.INVISIBLE else View.VISIBLE

        val canGoNextPage = (currentStampPage < totalStampPages - 1) && (currentStampPage < maxPageForNextButton)
        binding.btnNextStamp.visibility = if (canGoNextPage) View.VISIBLE else View.INVISIBLE

        if (totalStampPages <= 1) {
            binding.btnPrevStamp.visibility = View.INVISIBLE
            binding.btnNextStamp.visibility = View.INVISIBLE
        }

        Log.d(TAG, "Navigation indicator updated: Current Page $currentStampPage, Max Reachable Page $maxPageForNextButton, Total Stamp Pages $totalStampPages. Prev:${binding.btnPrevStamp.visibility}, Next:${binding.btnNextStamp.visibility}")
    }

    override fun onResume() {
        super.onResume()
        if (SessionManager.isLoggedIn(requireContext())) {
            if (_binding == null) {
                Log.w(TAG, "onResume: View destroyed, skipping loyalty refresh UI update.")
                return
            }

            val totalPoints = loyaltyViewModel.loyaltyUserStatus.value?.totalPoints ?: 0
            val totalStampPages = (maxStamps + stampsPerPage - 1) / stampsPerPage
            val maxReachablePage = if (totalPoints > 0) (totalPoints - 1) / stampsPerPage else 0

            currentStampPage = maxReachablePage.coerceIn(0, if (totalStampPages > 0) totalStampPages - 1 else 0)
            loyaltyViewModel.refreshLoyaltyData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}