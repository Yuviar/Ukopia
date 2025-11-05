package com.example.ukopia.ui.home

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.data.MenuItem
import com.example.ukopia.databinding.FragmentRatingBinding
import android.content.res.ColorStateList
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.LayerDrawable
import java.util.Locale

class RatingFragment : Fragment() {

    private var _binding: FragmentRatingBinding? = null
    private val binding get() = _binding!!

    private var currentMenuItem: MenuItem? = null
    private var selectedRating: Float = 0f
    private val starImageViews: MutableList<ImageView> = mutableListOf()

    // Key untuk SharedPreferences (untuk menyimpan rating)
    private val PREFS_NAME = "UtopiaRatingPrefs"
    private val RATED_KEY_PREFIX = "rated_item_"
    private val USER_RATING_KEY_PREFIX = "user_rating_item_"
    private val USER_COMMENT_KEY_PREFIX = "user_comment_item_"
    private val ITEM_TOTAL_RATING_KEY_PREFIX = "item_total_rating_"
    private val ITEM_RATING_COUNT_KEY_PREFIX = "item_rating_count_"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRatingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).setBottomNavVisibility(View.GONE)

        currentMenuItem = arguments?.getParcelable(ARG_MENU_ITEM)

        starImageViews.addAll(listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5))

        currentMenuItem?.let { menuItem ->
            binding.tvRatingMenuTitle.text = getString(R.string.rate_for_prefix) + " " + menuItem.name

            starImageViews.forEachIndexed { index, starView ->
                starView.setOnClickListener {
                    selectedRating = (index + 1).toFloat()
                    updateStarSelectionUI(selectedRating)
                }
            }

            binding.btnSubmitRating.setOnClickListener {
                // ... (animasi tombol seperti di kode Anda)
                submitRating(menuItem)
            }
        } ?: run {
            Toast.makeText(requireContext(), "Menu item data not found!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    private fun updateStarSelectionUI(rating: Float) {
        for (i in starImageViews.indices) {
            val starDrawable = starImageViews[i].drawable as LayerDrawable
            val clipDrawable = starDrawable.findDrawableByLayerId(R.id.clip_star_item) as ClipDrawable
            // Untuk seleksi, kita buat bintang penuh atau kosong saja
            clipDrawable.level = if ((i + 1) <= rating) 10000 else 0
        }
    }

    private fun submitRating(menuItem: MenuItem) {
        if (selectedRating == 0f) {
            Toast.makeText(requireContext(), getString(R.string.error_no_rating_selected), Toast.LENGTH_SHORT).show()
            return
        }
        val comment = binding.editTextKomentar.text.toString().trim()
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Ambil data rating yang ada
        var totalRating = prefs.getFloat(ITEM_TOTAL_RATING_KEY_PREFIX + menuItem.id, 0f)
        var ratingCount = prefs.getInt(ITEM_RATING_COUNT_KEY_PREFIX + menuItem.id, 0)

        // Cek apakah user pernah merating item ini sebelumnya
        val hasUserRatedBefore = prefs.getBoolean(RATED_KEY_PREFIX + menuItem.id, false)
        if (!hasUserRatedBefore) {
            totalRating += selectedRating
            ratingCount += 1
        } else {
            // Jika user merating ulang, kita update rata-ratanya
            // (logika lebih kompleks bisa ditambahkan di sini, untuk simple kita anggap rating pertama)
        }

        val newAverageRating = if (ratingCount > 0) totalRating / ratingCount else 0f

        // Simpan semua data baru
        editor.putFloat(ITEM_TOTAL_RATING_KEY_PREFIX + menuItem.id, totalRating)
        editor.putInt(ITEM_RATING_COUNT_KEY_PREFIX + menuItem.id, ratingCount)
        editor.putBoolean(RATED_KEY_PREFIX + menuItem.id, true) // Tandai user sudah merating
        editor.putFloat(USER_RATING_KEY_PREFIX + menuItem.id, selectedRating)
        editor.putString(USER_COMMENT_KEY_PREFIX + menuItem.id, comment)
        editor.apply()

        // Update objek menuItem untuk dikirim kembali
        menuItem.rating = String.format(Locale.ROOT, "%.1f/5.0", newAverageRating)

        // Kirim hasil kembali ke DetailMenuFragment
        val resultBundle = Bundle().apply {
            putParcelable("updatedMenuItem", menuItem)
            putFloat("newUserRating", selectedRating)
            putString("newUserComment", comment)
        }
        parentFragmentManager.setFragmentResult("ratingResult", resultBundle)

        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_MENU_ITEM = "menu_item_for_rating"
        fun newInstance(menuItem: MenuItem): RatingFragment {
            return RatingFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_MENU_ITEM, menuItem)
                }
            }
        }
    }
}