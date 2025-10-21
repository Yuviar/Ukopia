// D:/github_rama/Ukopia/app/src/main/java/com/example/ukopia/ui/home/DetailMenuFragment.kt
package com.example.ukopia.ui.home

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.data.MenuItem
import com.example.ukopia.databinding.FragmentDetailMenuBinding

class DetailMenuFragment : Fragment() {

    private var _binding: FragmentDetailMenuBinding? = null
    private val binding get() = _binding!!

    private var currentMenuItem: MenuItem? = null

    private val averageStarImageViews: MutableList<ImageView> = mutableListOf()
    private val userRatingStarImageViews: MutableList<ImageView> = mutableListOf()

    private var selectedRatingForSubmission: Float = 0f
    private var userSubmittedRating: Float = 0f
    private var userSubmittedComment: String = ""

    private val PREFS_NAME = "UtopiaRatingPrefs"
    private val RATED_KEY_PREFIX = "rated_item_"
    private val USER_RATING_KEY_PREFIX = "user_rating_item_"
    private val USER_COMMENT_KEY_PREFIX = "user_comment_item_"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("DetailMenuFragment", "onViewCreated: Fragment started.")

        (requireActivity() as MainActivity).setBottomNavVisibility(View.GONE)

        averageStarImageViews.add(binding.star1)
        averageStarImageViews.add(binding.star2)
        averageStarImageViews.add(binding.star3)
        averageStarImageViews.add(binding.star4)
        averageStarImageViews.add(binding.star5)

        userRatingStarImageViews.add(binding.userStar1)
        userRatingStarImageViews.add(binding.userStar2)
        userRatingStarImageViews.add(binding.userStar3)
        userRatingStarImageViews.add(binding.userStar4)
        userRatingStarImageViews.add(binding.userStar5)

        currentMenuItem = arguments?.getParcelable(ARG_MENU_ITEM)

        currentMenuItem?.let { menuItem ->
            Log.d("DetailMenuFragment", "onViewCreated: MenuItem data received: ${menuItem.name}, initial rating: ${menuItem.rating}")
            binding.detailMenuTitle.text = menuItem.name
            binding.detailMenuImage.setImageResource(menuItem.imageUrl)
            binding.detailMenuDescription.text = menuItem.description

            val initialAverageRatingValue = menuItem.rating.substringBefore('/').toFloatOrNull() ?: 0f
            binding.tvAverageRatingText.text = getString(R.string.average_rating_prefix) + menuItem.rating
            displayStarsWithProgress(initialAverageRatingValue, averageStarImageViews) // Menggunakan fungsi baru
            Log.d("DetailMenuFragment", "onViewCreated: Average rating displayed: ${menuItem.rating}, value: $initialAverageRatingValue")

            selectedRatingForSubmission = initialAverageRatingValue

            val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val hasUserRated = prefs.getBoolean(RATED_KEY_PREFIX + menuItem.id, false)
            userSubmittedRating = prefs.getFloat(USER_RATING_KEY_PREFIX + menuItem.id, 0f)
            userSubmittedComment = prefs.getString(USER_COMMENT_KEY_PREFIX + menuItem.id, "") ?: ""
            Log.d("DetailMenuFragment", "onViewCreated: User rated status: $hasUserRated, submitted rating: $userSubmittedRating, comment: $userSubmittedComment")

            updateUIAfterRating(menuItem.id, hasUserRated, userSubmittedRating, userSubmittedComment)

            setupListeners(menuItem)
            setupFragmentResultListener()
        } ?: run {
            Log.e("DetailMenuFragment", "onViewCreated: MenuItem data not found in arguments!")
            Toast.makeText(requireContext(), "Menu item data not found!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    private fun updateUIAfterRating(itemId: String?, hasUserRated: Boolean, userRating: Float, userComment: String) {
        if (hasUserRated && userRating > 0f) {
            binding.btnSubmitRating.visibility = View.GONE
            binding.tvUserSubmittedRating.text = getString(R.string.your_rating_prefix) + String.format("%.1f", userRating) + getString(R.string.star_suffix)
            binding.tvUserSubmittedRating.visibility = View.VISIBLE
            displayStarsWithProgress(userRating, userRatingStarImageViews) // Memanggil fungsi baru untuk bintang personal
            binding.userRatingStarsContainer.visibility = View.VISIBLE

            if (userComment.isNotBlank()) {
                binding.tvUserComment.text = getString(R.string.comment_prefix) + userComment
                binding.tvUserComment.visibility = View.VISIBLE
            } else {
                binding.tvUserComment.visibility = View.GONE
            }
            Log.d("DetailMenuFragment", "UI updated: User has rated. Button hidden, user rating/comment shown.")
        } else {
            binding.btnSubmitRating.visibility = View.VISIBLE
            binding.tvUserSubmittedRating.visibility = View.GONE
            binding.tvUserComment.visibility = View.GONE
            binding.userRatingStarsContainer.visibility = View.GONE
            Log.d("DetailMenuFragment", "UI updated: User has not rated. Button visible.")
        }
    }

    private fun setupListeners(menuItem: MenuItem) {
        binding.btnBack.setOnClickListener {
            Log.d("DetailMenuFragment", "btnBack clicked.")
            parentFragmentManager.popBackStack()
        }

        binding.btnShare.setOnClickListener {
            Log.d("DetailMenuFragment", "btnShare clicked.")
            shareMenuItem(menuItem)
        }

        binding.cardRatingStars.findViewById<Button>(R.id.btn_submit_rating)?.setOnClickListener {
            Log.d("DetailMenuFragment", "Rate button clicked for: ${menuItem.name}")

            val rateButton = it as Button
            val originalButtonBackgroundTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black))
            val originalButtonTextColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))

            val flashColorBackground = ContextCompat.getColor(requireContext(), R.color.white)
            val flashColorText = ContextCompat.getColor(requireContext(), R.color.black)

            rateButton.backgroundTintList = ColorStateList.valueOf(flashColorBackground)
            rateButton.setTextColor(flashColorText)

            Handler(Looper.getMainLooper()).postDelayed({
                if (isAdded && activity != null && _binding != null) {
                    rateButton.backgroundTintList = originalButtonBackgroundTint
                    rateButton.setTextColor(originalButtonTextColor)
                    Log.d("DetailMenuFragment", "Animation finished, navigating to RatingFragment.")
                    val initialRatingForFragment = currentMenuItem?.rating?.substringBefore('/')?.toFloatOrNull() ?: 0f
                    navigateToRatingFragment(menuItem, initialRatingForFragment)
                } else {
                    Log.w("DetailMenuFragment", "Fragment not added/activity null when navigating to RatingFragment after delay.")
                }
            }, 150)
        } ?: run {
            Log.e("DetailMenuFragment", "Rate button (R.id.btn_submit_rating) not found in card_rating_stars!")
        }
    }

    // Fungsi untuk menampilkan bintang (rata-rata atau personal) dengan progres pecahan
    private fun displayStarsWithProgress(rating: Float, starViews: List<ImageView>) {
        Log.d("DetailMenuFragment", "displayStarsWithProgress called with rating: $rating for ${starViews.size} stars")
        for (i in starViews.indices) {
            // Pastikan setiap ImageView memiliki drawable baru agar ClipDrawable dapat diatur secara independen
            starViews[i].setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.star_rating_progress))

            val starDrawable = starViews[i].drawable as? LayerDrawable
            val clipDrawable = starDrawable?.findDrawableByLayerId(R.id.clip_star_item) as? ClipDrawable

            if (clipDrawable == null) {
                Log.e("DetailMenuFragment", "ClipDrawable not found for star at index $i. Check star_rating_progress.xml and ImageView setup.")
                continue
            }

            val currentStarIndex = i.toFloat()
            val remainingRating = rating - currentStarIndex

            when {
                remainingRating >= 1f -> { // Bintang penuh
                    clipDrawable.level = 10000
                    Log.d("DetailMenuFragment", "Star ${i+1}: Full (level 10000)")
                }
                remainingRating > 0f -> { // Bintang sebagian
                    val level = (remainingRating * 10000).toInt()
                    clipDrawable.level = level
                    Log.d("DetailMenuFragment", "Star ${i+1}: Partial (${String.format("%.1f", remainingRating * 100)}%) level: $level")
                }
                else -> { // Bintang kosong
                    clipDrawable.level = 0
                    Log.d("DetailMenuFragment", "Star ${i+1}: Empty (level 0)")
                }
            }
            starViews[i].invalidateDrawable(clipDrawable) // Pastikan drawable di-redraw
        }
    }

    private fun shareMenuItem(menuItem: MenuItem) {
        val shareText = getString(
            R.string.share_menu_item_text,
            menuItem.name,
            menuItem.rating,
            menuItem.description
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        if (shareIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via_chooser_title)))
            Log.d("DetailMenuFragment", "Share Intent launched for ${menuItem.name}")
        } else {
            Toast.makeText(requireContext(), getString(R.string.no_menu_to_share_message), Toast.LENGTH_SHORT).show()
            Log.w("DetailMenuFragment", "No app to handle share Intent.")
        }
    }

    private fun navigateToRatingFragment(menuItem: MenuItem, initialRating: Float) {
        Log.d("DetailMenuFragment", "navigateToRatingFragment called for: ${menuItem.name} with initial rating: $initialRating")
        val ratingFragment = RatingFragment.newInstance(currentMenuItem!!, initialRating)
        (requireActivity() as MainActivity).navigateToFragment(ratingFragment)
    }

    private fun setupFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener("requestKeyRatingSubmit", viewLifecycleOwner) { requestKey, bundle ->
            if (requestKey == "requestKeyRatingSubmit") {
                val ratedMenuItemId = bundle.getString("ratedMenuItemId")
                val newRating = bundle.getFloat("newRating")
                val newComment = bundle.getString("newComment") ?: ""
                val updatedMenuItemFromRating = bundle.getParcelable<MenuItem>("updatedMenuItem")

                Log.d("DetailMenuFragment", "Received result for item ID: $ratedMenuItemId")
                Log.d("DetailMenuFragment", "New submitted rating: $newRating, comment: $newComment")
                Log.d("DetailMenuFragment", "Updated MenuItem from RatingFragment: ${updatedMenuItemFromRating?.name}, Rating: ${updatedMenuItemFromRating?.rating}")

                if (ratedMenuItemId == currentMenuItem?.id && updatedMenuItemFromRating != null) {
                    currentMenuItem = updatedMenuItemFromRating
                    userSubmittedRating = newRating
                    userSubmittedComment = newComment

                    currentMenuItem?.let { item ->
                        val currentAverageRatingValue = item.rating.substringBefore('/').toFloatOrNull() ?: 0f
                        binding.tvAverageRatingText.text = getString(R.string.average_rating_prefix) + item.rating
                        displayStarsWithProgress(currentAverageRatingValue, averageStarImageViews) // Gunakan fungsi baru
                        Log.d("DetailMenuFragment", "ResultListener: Average rating updated to: ${item.rating}, value: $currentAverageRatingValue")
                    }

                    updateUIAfterRating(ratedMenuItemId, true, userSubmittedRating, userSubmittedComment)

                    selectedRatingForSubmission = 0f
                    Log.d("DetailMenuFragment", "ResultListener: UI updated after successful rating submission.")
                } else {
                    Log.e("DetailMenuFragment", "ResultListener: Failed to update UI: MenuItem ID mismatch or updatedMenuItemFromRating is null.")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("DetailMenuFragment", "onDestroyView: Fragment destroyed.")
    }

    companion object {
        const val ARG_MENU_ITEM = "menu_item"

        fun newInstance(menuItem: MenuItem): DetailMenuFragment {
            val fragment = DetailMenuFragment()
            val args = Bundle()
            args.putParcelable(ARG_MENU_ITEM, menuItem)
            fragment.arguments = args
            return fragment
        }
    }
}
