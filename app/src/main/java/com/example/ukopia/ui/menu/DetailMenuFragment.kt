package com.example.ukopia.ui.menu

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.SessionManager
import com.example.ukopia.UkopiaApplication
import com.example.ukopia.databinding.DialogLoginRequiredBinding
import com.example.ukopia.databinding.FragmentDetailMenuBinding
import com.example.ukopia.models.MenuApiItem
import com.example.ukopia.models.ReviewApiItem
import com.example.ukopia.ui.auth.LoginActivity
import java.util.Locale
import android.util.Log

class DetailMenuFragment : Fragment() {

    private var _binding: FragmentDetailMenuBinding? = null
    private val binding get() = _binding!!

    private var currentMenuItem: MenuApiItem? = null
    private var currentUserReview: ReviewApiItem? = null

    private val viewModel: MenuViewModel by viewModels {
        MenuViewModelFactory((requireActivity().application as UkopiaApplication).repository) // PASTIKAN MENGGUNAKAN menuRepository
    }

    private lateinit var reviewAdapter: ReviewAdapter

    private val averageStarImageViews: MutableList<ImageView> = mutableListOf()
    private val userStarImageViews: MutableList<ImageView> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("DetailMenuFragment", "onViewCreated: Fragment started")
        (requireActivity() as MainActivity).setBottomNavVisibility(View.GONE)

        averageStarImageViews.addAll(listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5))
        userStarImageViews.addAll(listOf(binding.userStar1, binding.userStar2, binding.userStar3, binding.userStar4, binding.userStar5))

        reviewAdapter = ReviewAdapter(emptyList())
        binding.rvReviews.adapter = reviewAdapter

        val menuId = arguments?.getParcelable<MenuApiItem>(ARG_MENU_ITEM)?.id_menu

        if (menuId != null) {
            Log.d("DetailMenuFragment", "onViewCreated: Menu ID found: $menuId")
            setupListeners()
            setupObservers()
            setLoadingState(true)
            viewModel.fetchMenuDetails(menuId, SessionManager.getUid(requireContext()))
        } else {
            Log.e("DetailMenuFragment", "onViewCreated: Menu item ID not found in arguments!")
            Toast.makeText(requireContext(), "Menu item ID not found!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupInitialUI(menuItem: MenuApiItem) {
        currentMenuItem = menuItem
        Log.d("DetailMenuFragment", "setupInitialUI: currentMenuItem updated to: ${menuItem.nama_menu}")

        binding.detailMenuTitle.text = menuItem.nama_menu
        binding.detailMenuDescription.text = menuItem.deskripsi

        Glide.with(this)
            .load(menuItem.gambar_url)
            .placeholder(R.drawable.sample_coffee)
            .error(R.drawable.sample_coffee)
            .into(binding.detailMenuImage)

        val averageRatingValue = menuItem.average_rating.toFloat()
        binding.tvAverageRatingText.text = getString(R.string.average_rating_prefix) + " " + String.format(Locale.ROOT, "%.1f/5.0", averageRatingValue)
        displayStarsWithProgress(averageRatingValue, averageStarImageViews)

        binding.btnShare.setOnClickListener {
            val shareText = "Check out this menu on Ukopia: ${menuItem.nama_menu}!\n\n${menuItem.deskripsi}"
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            val chooser = Intent.createChooser(shareIntent, getString(R.string.share_menu_via))
            if (shareIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(chooser)
            } else {
                Toast.makeText(requireContext(), getString(R.string.no_app_to_share), Toast.LENGTH_SHORT).show()
            }
        }
        setLoadingState(false)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            val targetTint = ContextCompat.getColorStateList(requireContext(), R.color.black)
            val flashTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
            binding.btnBack.imageTintList = flashTint
            Handler(Looper.getMainLooper()).postDelayed({
                binding.btnBack.imageTintList = targetTint
                parentFragmentManager.popBackStack()
            }, 150)
        }

        binding.btnRateThisMenu.setOnClickListener {
            Log.d("DetailMenuFragment", "btnRateThisMenu clicked. currentMenuItem: $currentMenuItem")
            val targetBackgroundTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black))
            val targetTextColors = ContextCompat.getColorStateList(requireContext(), R.color.white)
            val flashColorBackground = ContextCompat.getColor(requireContext(), R.color.white)
            val flashColorText = ContextCompat.getColor(requireContext(), R.color.black)

            binding.btnRateThisMenu.backgroundTintList = ColorStateList.valueOf(flashColorBackground)
            binding.btnRateThisMenu.setTextColor(flashColorText)

            Handler(Looper.getMainLooper()).postDelayed({
                binding.btnRateThisMenu.backgroundTintList = targetBackgroundTint
                binding.btnRateThisMenu.setTextColor(targetTextColors)

                if (SessionManager.isLoggedIn(requireContext())) {
                    currentMenuItem?.let {
                        Log.d("DetailMenuFragment", "Navigating to RatingFragment for menu: ${it.nama_menu}")
                        navigateToRatingFragment(it, currentUserReview)
                    } ?: run {
                        Log.w("DetailMenuFragment", "currentMenuItem is null when Rate button clicked. Button enabled: ${binding.btnRateThisMenu.isEnabled}")
                        Toast.makeText(requireContext(), "Menu item data not loaded yet. Please wait.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d("DetailMenuFragment", "User not logged in, showing login dialog.")
                    showLoginRequiredDialog()
                }
            }, 150)
        }
    }

    private fun setupObservers() {
        Log.d("DetailMenuFragment", "setupObservers called.")
        viewModel.currentDetailMenuItem.observe(viewLifecycleOwner, Observer { menuItem ->
            Log.d("DetailMenuFragment", "currentDetailMenuItem LiveData observed. Value: ${menuItem?.nama_menu ?: "null"}")
            menuItem?.let {
                setupInitialUI(it)
            } ?: run {
                Log.w("DetailMenuFragment", "currentDetailMenuItem LiveData returned null value.")
                setLoadingState(false)
            }
        })

        viewModel.reviews.observe(viewLifecycleOwner, Observer { reviews ->
            Log.d("DetailMenuFragment", "reviews LiveData observed. Count: ${reviews.size}")
            reviewAdapter.updateData(reviews)
        })

        viewModel.userReview.observe(viewLifecycleOwner, Observer { userReview ->
            Log.d("DetailMenuFragment", "userReview LiveData observed. Value: ${userReview?.rating ?: "null"}")
            currentUserReview = userReview
            if (userReview != null) {
                showUserRating(userReview.rating.toFloat(), userReview.komentar)
            } else {
                binding.userRatingContainer.visibility = View.GONE
                binding.btnRateThisMenu.text = getString(R.string.rate_button_text)
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            Log.d("DetailMenuFragment", "isLoading LiveData observed. Value: $isLoading")
            setLoadingState(isLoading)
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Log.e("DetailMenuFragment", "errorMessage LiveData observed. Error: $it")
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
                setLoadingState(false)
            }
        })
    }

    private fun setLoadingState(isLoading: Boolean) {
        Log.d("DetailMenuFragment", "setLoadingState: isLoading=$isLoading, btnRateThisMenu.isEnabled=${!isLoading}")
        binding.btnRateThisMenu.isEnabled = !isLoading // Nonaktifkan tombol saat loading

        if (isLoading) {
            binding.btnRateThisMenu.text = "Loading..."
        } else {
            if (currentUserReview != null) {
                binding.btnRateThisMenu.text = getString(R.string.update_your_rating)
            } else {
                binding.btnRateThisMenu.text = getString(R.string.rate_button_text)
            }
            if (viewModel.errorMessage.value == null) {
                binding.btnRateThisMenu.isEnabled = true
            }
        }
    }

    private fun showUserRating(rating: Float, comment: String) {
        Log.d("DetailMenuFragment", "showUserRating: Displaying user rating $rating with comment: '$comment'")
        binding.userRatingContainer.visibility = View.VISIBLE
        displayStarsWithProgress(rating, userStarImageViews)
        if (comment.isNotBlank()) {
            binding.tvUserComment.visibility = View.VISIBLE
            binding.tvUserComment.text = "\"$comment\""
        } else {
            binding.tvUserComment.visibility = View.GONE
        }
        binding.btnRateThisMenu.text = getString(R.string.update_your_rating)
    }

    private fun displayStarsWithProgress(rating: Float, starViews: List<ImageView>) {
        for (i in starViews.indices) {
            val starDrawable = starViews[i].drawable as LayerDrawable
            val clipDrawable = starDrawable.findDrawableByLayerId(R.id.clip_star_item) as ClipDrawable
            val level = when {
                (rating - i) >= 1f -> 10000
                (rating - i) > 0f -> ((rating - i) * 10000).toInt()
                else -> 0
            }
            clipDrawable.level = level
        }
    }

    private fun navigateToRatingFragment(menuItem: MenuApiItem, existingReview: ReviewApiItem?) {
        val ratingFragment = RatingFragment.newInstance(menuItem, existingReview)
        (requireActivity() as MainActivity).navigateToFragment(ratingFragment)
    }

    private fun showLoginRequiredDialog() {
        val dialogBinding = DialogLoginRequiredBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        dialogBinding.buttonDialogLogin.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            dialog.dismiss()
        }
        dialogBinding.buttonDialogCancel.setOnClickListener { dialog.dismiss() }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("DetailMenuFragment", "onDestroyView: Fragment destroyed")
    }

    companion object {
        const val ARG_MENU_ITEM = "menu_item"
        fun newInstance(menuItem: MenuApiItem): DetailMenuFragment {
            return DetailMenuFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_MENU_ITEM, menuItem)
                }
            }
        }
    }
}