// D:/github_rama/Ukopia/app/src/main/java/com/example/ukopia/ui/home/RatingFragment.kt
package com.example.ukopia.ui.home

import android.content.Context
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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult // Import ini
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.data.MenuItem
import com.example.ukopia.databinding.FragmentRatingBinding
import java.util.Locale

class RatingFragment : Fragment() {

    private var _binding: FragmentRatingBinding? = null
    private val binding get() = _binding!!

    private var currentMenuItem: MenuItem? = null
    private var selectedRating: Float = 0f // Untuk menyimpan rating yang dipilih

    private val starImageViews: MutableList<ImageView> = mutableListOf()

    // Key untuk SharedPreferences (untuk menyimpan rating per user dan status sudah merating)
    private val PREFS_NAME = "UtopiaRatingPrefs"
    private val RATED_KEY_PREFIX = "rated_item_"
    private val USER_RATING_KEY_PREFIX = "user_rating_item_"
    private val USER_COMMENT_KEY_PREFIX = "user_comment_item_"
    private val ITEM_TOTAL_RATING_KEY_PREFIX = "item_total_rating_"
    private val ITEM_RATING_COUNT_KEY_PREFIX = "item_rating_count_"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRatingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("RatingFragment", "onViewCreated: Fragment started.")

        (requireActivity() as MainActivity).setBottomNavVisibility(View.GONE)

        currentMenuItem = arguments?.getParcelable(ARG_MENU_ITEM)

        starImageViews.add(binding.star1)
        starImageViews.add(binding.star2)
        starImageViews.add(binding.star3)
        starImageViews.add(binding.star4)
        starImageViews.add(binding.star5)

        val initialRatingFromArgs = arguments?.getFloat(ARG_INITIAL_RATING, 0f) ?: 0f
        selectedRating = initialRatingFromArgs
        updateStarUI(selectedRating, starImageViews) // Menggunakan fungsi yang diperbarui
        Log.d("RatingFragment", "Initial rating from args: $initialRatingFromArgs, selectedRating: $selectedRating")

        currentMenuItem?.let { menuItem ->
            binding.tvRatingMenuTitle.text = getString(R.string.rate_for_prefix) + menuItem.name

            starImageViews.forEachIndexed { index, starView ->
                starView.setOnClickListener {
                    selectedRating = (index + 1).toFloat()
                    updateStarUI(selectedRating, starImageViews) // Menggunakan fungsi yang diperbarui
                    Log.d("RatingFragment", "Star ${index + 1} clicked. Selected for submission: $selectedRating")
                }
            }

            binding.btnSubmitRating.setOnClickListener {
                Log.d("RatingFragment", "Submit button clicked.")
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
                        Log.d("RatingFragment", "Animation finished, submitting rating.")
                        submitRating(menuItem)
                    } else {
                        Log.w("RatingFragment", "Fragment not added/activity null when submitting rating after delay.")
                    }
                }, 150)
            }
        } ?: run {
            Log.e("RatingFragment", "MenuItem data not found for rating!")
            Toast.makeText(requireContext(), "Menu item data not found for rating!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    // Fungsi untuk menampilkan bintang (rata-rata atau personal) dengan progres pecahan
    private fun updateStarUI(rating: Float, starViews: List<ImageView>) {
        Log.d("RatingFragment", "updateStarUI called with rating: $rating for ${starViews.size} stars")
        for (i in starViews.indices) {
            val starDrawable = starViews[i].drawable as? LayerDrawable
            val clipDrawable = starDrawable?.findDrawableByLayerId(R.id.clip_star_item) as? ClipDrawable

            if (clipDrawable == null) {
                Log.e("RatingFragment", "ClipDrawable not found for star at index $i. Check star_rating_progress.xml and ImageView setup.")
                continue
            }

            val currentStarIndex = i.toFloat()
            val remainingRating = rating - currentStarIndex

            when {
                remainingRating >= 1f -> { // Bintang penuh
                    clipDrawable.level = 10000
                    Log.d("RatingFragment", "Star ${i+1}: Full (level 10000)")
                }
                remainingRating > 0f -> { // Bintang sebagian
                    val level = (remainingRating * 10000).toInt()
                    clipDrawable.level = level
                    Log.d("RatingFragment", "Star ${i+1}: Partial (${String.format("%.1f", remainingRating * 100)}%) level: $level")
                }
                else -> { // Bintang kosong
                    clipDrawable.level = 0
                    Log.d("RatingFragment", "Star ${i+1}: Empty (level 0)")
                }
            }
            starViews[i].invalidateDrawable(clipDrawable) // Pastikan drawable di-redraw
        }
    }

    private fun submitRating(menuItem: MenuItem) {
        val comment = binding.editTextKomentar.text.toString().trim()

        if (selectedRating == 0f) {
            Toast.makeText(requireContext(), getString(R.string.error_no_rating_selected), Toast.LENGTH_SHORT).show()
            Log.w("RatingFragment", "No rating selected.")
            return
        }

        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        var totalRating = prefs.getFloat(ITEM_TOTAL_RATING_KEY_PREFIX + menuItem.id, 0f)
        var ratingCount = prefs.getInt(ITEM_RATING_COUNT_KEY_PREFIX + menuItem.id, 0)

        val hasUserRated = prefs.getBoolean(RATED_KEY_PREFIX + menuItem.id, false)

        if (!hasUserRated) {
            totalRating += selectedRating
            ratingCount += 1
            editor.putBoolean(RATED_KEY_PREFIX + menuItem.id, true) // Tandai user sudah merating
            Log.d("RatingFragment", "First time rating. New totalRating: $totalRating, new ratingCount: $ratingCount")
        } else {
            // Jika user sudah merating sebelumnya, kita tidak mengubah rata-rata global lagi
            // Hanya rating spesifik user yang diupdate jika dia merating ulang dalam sesi ini
            Log.d("RatingFragment", "User has rated before for item ${menuItem.id}. Only updating user's specific rating/comment.")
        }

        val newAverageRating = if (ratingCount > 0) totalRating / ratingCount else selectedRating // Jika belum ada rating, rating pertama jadi rata-rata
        menuItem.rating = String.format(Locale.ROOT, "%.1f/5.0", newAverageRating)

        editor.putFloat(ITEM_TOTAL_RATING_KEY_PREFIX + menuItem.id, totalRating)
        editor.putInt(ITEM_RATING_COUNT_KEY_PREFIX + menuItem.id, ratingCount)
        editor.putFloat(USER_RATING_KEY_PREFIX + menuItem.id, selectedRating)
        editor.putString(USER_COMMENT_KEY_PREFIX + menuItem.id, comment)
        editor.apply()
        Log.d("RatingFragment", "SharedPreferences updated. newAverageRating: $newAverageRating, userRating: $selectedRating, userComment: $comment")

        Toast.makeText(
            requireContext(),
            "Rating for ${menuItem.name}: ${selectedRating} stars, Comment: $comment",
            Toast.LENGTH_LONG
        ).show()

        val resultBundle = Bundle().apply {
            putString("ratedMenuItemId", menuItem.id)
            putFloat("newRating", selectedRating)
            putString("newComment", comment)
            putParcelable("updatedMenuItem", menuItem)
        }
        parentFragmentManager.setFragmentResult("requestKeyRatingSubmit", resultBundle)
        Log.d("RatingFragment", "Result sent back to DetailMenuFragment. Item ID: ${menuItem.id}, Rating: $selectedRating, Comment: $comment")

        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("RatingFragment", "onDestroyView: Fragment destroyed.")
    }

    companion object {
        const val ARG_MENU_ITEM = "menu_item_for_rating"
        const val ARG_INITIAL_RATING = "initial_rating_for_rating_fragment"

        fun newInstance(menuItem: MenuItem, initialRating: Float): RatingFragment {
            val fragment = RatingFragment()
            val args = Bundle()
            args.putParcelable(ARG_MENU_ITEM, menuItem)
            args.putFloat(ARG_INITIAL_RATING, initialRating)
            fragment.arguments = args
            return fragment
        }
    }
}