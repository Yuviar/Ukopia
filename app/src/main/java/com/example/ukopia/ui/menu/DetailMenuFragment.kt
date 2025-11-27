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
import com.example.ukopia.SessionManager // Menggunakan SessionManager (object) Anda
import com.example.ukopia.UkopiaApplication // Untuk ViewModel Factory
import com.example.ukopia.databinding.DialogLoginRequiredBinding
import com.example.ukopia.databinding.FragmentDetailMenuBinding
import com.example.ukopia.models.MenuApiItem // Model BARU
import com.example.ukopia.models.ReviewApiItem // Model BARU
import com.example.ukopia.ui.auth.LoginActivity
import java.util.Locale

class DetailMenuFragment : Fragment() {

    private var _binding: FragmentDetailMenuBinding? = null
    private val binding get() = _binding!!

    // Menggunakan model data BARU
    private var currentMenuItem: MenuApiItem? = null
    private var currentUserReview: ReviewApiItem? = null // Untuk menyimpan ulasan user

    // Inisialisasi ViewModel BARU dengan Factory
    private val viewModel: MenuViewModel by viewModels {
        MenuViewModelFactory((requireActivity().application as UkopiaApplication).repository)
    }

    private lateinit var reviewAdapter: ReviewAdapter // Adapter BARU untuk ulasan

    private val averageStarImageViews: MutableList<ImageView> = mutableListOf()
    private val userStarImageViews: MutableList<ImageView> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).setBottomNavVisibility(View.GONE)

        averageStarImageViews.addAll(listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5))
        userStarImageViews.addAll(listOf(binding.userStar1, binding.userStar2, binding.userStar3, binding.userStar4, binding.userStar5))

        // Setup adapter ulasan BARU
        reviewAdapter = ReviewAdapter(emptyList())
        binding.rvReviews.adapter = reviewAdapter

        // Ambil argumen dengan model BARU
        currentMenuItem = arguments?.getParcelable(ARG_MENU_ITEM)

        currentMenuItem?.let { menuItem ->
            setupInitialUI(menuItem)
            setupListeners(menuItem)
            setupObservers() // Panggil setupObservers BARU

            // Panggil ViewModel untuk ambil detail (ulasan) dari API
            viewModel.fetchMenuDetails(menuItem.id_menu, SessionManager.getUid(requireContext()))
        } ?: parentFragmentManager.popBackStack()
    }

    private fun setupInitialUI(menuItem: MenuApiItem) {
        binding.detailMenuTitle.text = menuItem.nama_menu
        binding.detailMenuDescription.text = menuItem.deskripsi

        // Muat gambar dari URL (String) dengan Glide
        Glide.with(this)
            .load(menuItem.gambar_url)
            .placeholder(R.drawable.sample_coffee)
            .into(binding.detailMenuImage)

        // Ambil rating dari (Double)
        val averageRatingValue = menuItem.average_rating.toFloat()
        binding.tvAverageRatingText.text = getString(R.string.average_rating_prefix) + " " +
                String.format(Locale.ROOT, "%.1f/5.0", averageRatingValue)
        displayStarsWithProgress(averageRatingValue, averageStarImageViews)
    }

    private fun setupListeners(menuItem: MenuApiItem) {
        // 1. Tombol Back (Animasi tetap, Hapus sendResult)
        binding.btnBack.setOnClickListener {
            val targetTint = ContextCompat.getColorStateList(requireContext(), R.color.black)
            val flashTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
            binding.btnBack.imageTintList = flashTint
            Handler(Looper.getMainLooper()).postDelayed({
                binding.btnBack.imageTintList = targetTint
                parentFragmentManager.popBackStack()
            }, 150)
        }

        // 2. Tombol Rate
        binding.btnRateThisMenu.setOnClickListener {
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
                    navigateToRatingFragment(menuItem, currentUserReview)
                } else {
                    showLoginRequiredDialog()
                }
            }, 150)
        }

        // 3. Tombol Share (Logika tidak berubah)
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
    }

    // FUNGSI BARU: Mengamati LiveData dari ViewModel
    private fun setupObservers() {
        // Observer untuk daftar ulasan (dari pengguna lain)
        viewModel.reviews.observe(viewLifecycleOwner, Observer { reviews ->

            // ==========================================================
            // PERBAIKAN LOGIKA DI SINI
            // ==========================================================
            // HAPUS FILTER! ViewModel sudah melakukannya.
            reviewAdapter.updateData(reviews)
            // ==========================================================
        })

        viewModel.userReview.observe(viewLifecycleOwner, Observer { userReview ->
            currentUserReview = userReview // Simpan ulasan user
            if (userReview != null) {
                // Tampilkan jika user sudah pernah mengulas
                showUserRating(userReview.rating.toFloat(), userReview.komentar)
            } else {
                // Sembunyikan jika user belum mengulas
                binding.userRatingContainer.visibility = View.GONE
                binding.btnRateThisMenu.text = getString(R.string.rate_button_text)
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            // Anda bisa tambahkan progress bar di sini
        })
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
            }
        })
    }

    // Fungsi ini tidak berubah, hanya dipanggil dari observer
    private fun showUserRating(rating: Float, comment: String) {
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

    // Fungsi ini tidak berubah
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

    // MODIFIKASI: Kirim MenuApiItem DAN ReviewApiItem
    private fun navigateToRatingFragment(menuItem: MenuApiItem, existingReview: ReviewApiItem?) {
        val ratingFragment = RatingFragment.newInstance(menuItem, existingReview)
        (requireActivity() as MainActivity).navigateToFragment(ratingFragment)
    }

    // MODIFIKASI: Hapus penggunaan ActivityResultLauncher
    private fun showLoginRequiredDialog() {
        val dialogBinding = DialogLoginRequiredBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        dialogBinding.buttonDialogLogin.setOnClickListener {
            // Langsung buka LoginActivity
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
    }

    companion object {
        const val ARG_MENU_ITEM = "menu_item"
        // MODIFIKASI: Terima MenuApiItem
        fun newInstance(menuItem: MenuApiItem): DetailMenuFragment {
            return DetailMenuFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_MENU_ITEM, menuItem)
                }
            }
        }
    }
}