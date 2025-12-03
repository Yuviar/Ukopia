package com.example.ukopia.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.SessionManager
import com.example.ukopia.UkopiaApplication
import com.example.ukopia.databinding.FragmentRatingBinding
import com.example.ukopia.models.MenuApiItem
import com.example.ukopia.models.ReviewApiItem
import com.example.ukopia.models.ReviewPostRequest
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.LayerDrawable
import java.util.Locale

class RatingFragment : Fragment() {

    private var _binding: FragmentRatingBinding? = null
    private val binding get() = _binding!!

    private var currentMenuItem: MenuApiItem? = null
    private var existingReview: ReviewApiItem? = null

    private var selectedRating: Float = 0f
    private val starImageViews: MutableList<ImageView> = mutableListOf()

    private val viewModel: MenuViewModel by viewModels {
        MenuViewModelFactory((requireActivity().application as UkopiaApplication).repository)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRatingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).setBottomNavVisibility(View.GONE)

        currentMenuItem = arguments?.getParcelable(ARG_MENU_ITEM)
        existingReview = arguments?.getParcelable(ARG_EXISTING_REVIEW)

        starImageViews.addAll(listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5))

        currentMenuItem?.let { menuItem ->
            binding.tvRatingMenuTitle.text = getString(R.string.rate_for_prefix) + " " + menuItem.nama_menu

            existingReview?.let {
                selectedRating = it.rating.toFloat()
                updateStarSelectionUI(selectedRating)
                binding.editTextKomentar.setText(it.komentar)
            }

            starImageViews.forEachIndexed { index, starView ->
                starView.setOnClickListener {
                    selectedRating = (index + 1).toFloat()
                    updateStarSelectionUI(selectedRating)
                }
            }

            binding.btnSubmitRating.setOnClickListener {
                submitRating(menuItem)
            }
        } ?: run {
            Toast.makeText(requireContext(), "Menu item data not found!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }

        setupObservers()
    }

    private fun setupObservers() {
        viewModel.reviewPostSuccess.observe(viewLifecycleOwner, Observer { isSuccess ->
            if (isSuccess) {
                Toast.makeText(requireContext(), "Ulasan berhasil disimpan", Toast.LENGTH_SHORT).show()
                viewModel.resetReviewPostStatus() // Reset status
                parentFragmentManager.popBackStack() // Kembali ke Detail
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Toast.makeText(requireContext(), "Gagal: $it", Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.btnSubmitRating.isEnabled = !isLoading
            binding.btnSubmitRating.text = if(isLoading) "Loading..." else getString(R.string.submit_rating_button_text)
        })
    }
    private fun updateStarSelectionUI(rating: Float) {
        for (i in starImageViews.indices) {
            val starDrawable = starImageViews[i].drawable as LayerDrawable
            val clipDrawable = starDrawable.findDrawableByLayerId(R.id.clip_star_item) as ClipDrawable
            clipDrawable.level = if ((i + 1) <= rating) 10000 else 0
        }
    }

    private fun submitRating(menuItem: MenuApiItem) {
        if (selectedRating == 0f) {
            Toast.makeText(requireContext(), getString(R.string.error_no_rating_selected), Toast.LENGTH_SHORT).show()
            return
        }

        if (!SessionManager.isLoggedIn(requireContext())) {
            Toast.makeText(requireContext(), "Anda harus login", Toast.LENGTH_SHORT).show()
            return
        }

        val comment = binding.editTextKomentar.text.toString().trim()
        val uid = SessionManager.getUid(requireContext()) // Ambil UID dari SessionManager

        val request = ReviewPostRequest(
            id_menu = menuItem.id_menu,
            uid_akun = uid,
            rating = selectedRating,
            komentar = comment
        )

        viewModel.submitReview(request)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_MENU_ITEM = "menu_item_for_rating"
        const val ARG_EXISTING_REVIEW = "existing_review"

        fun newInstance(menuItem: MenuApiItem, existingReview: ReviewApiItem?): RatingFragment {
            return RatingFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_MENU_ITEM, menuItem)
                    putParcelable(ARG_EXISTING_REVIEW, existingReview)
                }
            }
        }
    }
}