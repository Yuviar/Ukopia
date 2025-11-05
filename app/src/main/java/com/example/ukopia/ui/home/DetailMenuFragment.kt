package com.example.ukopia.ui.home

import android.app.Activity
import android.content.Context
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.SessionManager
import com.example.ukopia.data.MenuItem
import com.example.ukopia.databinding.DialogLoginRequiredBinding
import com.example.ukopia.databinding.FragmentDetailMenuBinding
import com.example.ukopia.ui.auth.LoginActivity
import java.util.Locale

class DetailMenuFragment : Fragment() {

    private var _binding: FragmentDetailMenuBinding? = null
    private val binding get() = _binding!!
    private var currentMenuItem: MenuItem? = null

    private var pendingRateAction = false
    private var hasRatingChanged = false

    // SharedPreferences Keys
    private val PREFS_NAME = "UtopiaRatingPrefs"
    private val RATED_KEY_PREFIX = "rated_item_"
    private val USER_RATING_KEY_PREFIX = "user_rating_item_"
    private val USER_COMMENT_KEY_PREFIX = "user_comment_item_"

    private val averageStarImageViews: MutableList<ImageView> = mutableListOf()
    private val userStarImageViews: MutableList<ImageView> = mutableListOf()

    private val loginActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && SessionManager.isLoggedIn(requireContext())) {
            if (pendingRateAction) {
                pendingRateAction = false
                currentMenuItem?.let { navigateToRatingFragment(it) }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).setBottomNavVisibility(View.GONE)

        averageStarImageViews.addAll(listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5))
        userStarImageViews.addAll(listOf(binding.userStar1, binding.userStar2, binding.userStar3, binding.userStar4, binding.userStar5))

        currentMenuItem = arguments?.getParcelable(ARG_MENU_ITEM)

        currentMenuItem?.let { menuItem ->
            setupInitialUI(menuItem)
            setupListeners(menuItem)
            setupFragmentResultListener()
        } ?: parentFragmentManager.popBackStack()
    }

    private fun setupInitialUI(menuItem: MenuItem) {
        binding.detailMenuTitle.text = menuItem.name
        binding.detailMenuImage.setImageResource(menuItem.imageUrl)
        binding.detailMenuDescription.text = menuItem.description

        val averageRatingValue = menuItem.rating.substringBefore('/').toFloatOrNull() ?: 0f
        binding.tvAverageRatingText.text = getString(R.string.average_rating_prefix) + " " + menuItem.rating
        displayStarsWithProgress(averageRatingValue, averageStarImageViews)

        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hasRated = prefs.getBoolean(RATED_KEY_PREFIX + menuItem.id, false)
        if (hasRated) {
            val userRating = prefs.getFloat(USER_RATING_KEY_PREFIX + menuItem.id, 0f)
            val userComment = prefs.getString(USER_COMMENT_KEY_PREFIX + menuItem.id, "") ?: ""
            showUserRating(userRating, userComment)
        }
    }

    private fun setupListeners(menuItem: MenuItem) {
        // 1. Tombol Back dengan Animasi Flash
        binding.btnBack.setOnClickListener {
            // Asumsi ikon asli tombol back adalah hitam, dan kita ingin mengembalikannya ke hitam
            val targetTint = ContextCompat.getColorStateList(requireContext(), R.color.black)
            val flashTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)) // Flash ikon menjadi putih

            binding.btnBack.imageTintList = flashTint

            Handler(Looper.getMainLooper()).postDelayed({
                binding.btnBack.imageTintList = targetTint // Kembalikan ke warna ikon hitam
                // Aksi asli: kirim hasil dan pop fragment
                sendResultToHomeFragment()
                parentFragmentManager.popBackStack()
            }, 150) // Durasi animasi flash: 150 milidetik
        }

        // 2. Tombol Rate dengan Animasi Flash (Flash dari Black/White ke White/Black dan kembali ke Black/White)
        binding.btnRateThisMenu.setOnClickListener {
            // Definisikan warna TARGET yang diinginkan setelah flash (Black Background, White Text)
            val targetBackgroundTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black))
            val targetTextColors = ContextCompat.getColorStateList(requireContext(), R.color.white)

            // Definisikan warna saat flash terjadi (White Background, Black Text)
            val flashColorBackground = ContextCompat.getColor(requireContext(), R.color.white)
            val flashColorText = ContextCompat.getColor(requireContext(), R.color.black)

            // Terapkan warna flash
            binding.btnRateThisMenu.backgroundTintList = ColorStateList.valueOf(flashColorBackground)
            binding.btnRateThisMenu.setTextColor(flashColorText)

            // Setelah delay, kembalikan ke warna target yang diinginkan
            Handler(Looper.getMainLooper()).postDelayed({
                binding.btnRateThisMenu.backgroundTintList = targetBackgroundTint
                binding.btnRateThisMenu.setTextColor(targetTextColors)

                // Aksi asli: cek login dan navigasi ke halaman rating
                if (SessionManager.isLoggedIn(requireContext())) {
                    navigateToRatingFragment(menuItem)
                } else {
                    pendingRateAction = true
                    showLoginRequiredDialog()
                }
            }, 150) // Durasi animasi flash: 150 milidetik
        }

        // 3. Tombol Share dengan Fungsionalitas Intent
        binding.btnShare.setOnClickListener {
            val shareText = "Check out this menu on Ukopia: ${menuItem.name}!\n\n${menuItem.description}"

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain" // Tipe MIME untuk teks biasa
            }

            // Membuat chooser agar pengguna bisa memilih aplikasi untuk berbagi
            val chooser = Intent.createChooser(shareIntent, getString(R.string.share_menu_via))
            // Pastikan ada aplikasi yang bisa menangani intent ini
            if (shareIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(chooser)
            } else {
                Toast.makeText(requireContext(), getString(R.string.no_app_to_share), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener("ratingResult", viewLifecycleOwner) { _, bundle ->
            val updatedMenuItem = bundle.getParcelable<MenuItem>("updatedMenuItem")
            val newUserRating = bundle.getFloat("newUserRating", 0f)
            val newUserComment = bundle.getString("newUserComment", "")

            updatedMenuItem?.let {
                hasRatingChanged = true
                currentMenuItem = it
                val newAverageRating = it.rating.substringBefore('/').toFloatOrNull() ?: 0f
                binding.tvAverageRatingText.text = getString(R.string.average_rating_prefix) + " " + it.rating
                displayStarsWithProgress(newAverageRating, averageStarImageViews)
                showUserRating(newUserRating, newUserComment)
            }
        }
    }

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

    private fun displayStarsWithProgress(rating: Float, starViews: List<ImageView>) {
        for (i in starViews.indices) {
            val starDrawable = starViews[i].drawable as LayerDrawable
            val clipDrawable = starDrawable.findDrawableByLayerId(R.id.clip_star_item) as ClipDrawable
            val level = when {
                (rating - i) >= 1f -> 10000 // Bintang penuh (100% level)
                (rating - i) > 0f -> ((rating - i) * 10000).toInt() // Bintang sebagian
                else -> 0 // Bintang kosong (0% level)
            }
            clipDrawable.level = level
        }
    }

    private fun navigateToRatingFragment(menuItem: MenuItem) {
        val ratingFragment = RatingFragment.newInstance(menuItem)
        (requireActivity() as MainActivity).navigateToFragment(ratingFragment)
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
        dialogBinding.buttonDialogCancel.setOnClickListener { dialog.dismiss() }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun sendResultToHomeFragment() {
        if (hasRatingChanged) {
            currentMenuItem?.let {
                val resultBundle = Bundle().apply {
                    putParcelable("updatedMenuItem", it)
                }
                parentFragmentManager.setFragmentResult("detailResult", resultBundle)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sendResultToHomeFragment() // Pastikan hasil dikirim saat fragment dihancurkan
        _binding = null
    }

    companion object {
        const val ARG_MENU_ITEM = "menu_item"
        fun newInstance(menuItem: MenuItem): DetailMenuFragment {
            return DetailMenuFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_MENU_ITEM, menuItem)
                }
            }
        }
    }
}