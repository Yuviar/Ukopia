package com.example.ukopia.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.ukopia.R // Pastikan import R
import com.example.ukopia.data.MenuItem

class DetailMenuActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MENU_ITEM = "extra_menu_item"
        const val REQUEST_CODE_SUBMIT_RATING = "request_code_submit_rating"
        const val EXTRA_SUBMITTED_RATING = "extra_submitted_rating"
        const val EXTRA_SUBMITTED_COMMENT = "extra_submitted_comment"
    }

    private var currentMenuItem: MenuItem? = null
    private lateinit var tvUserSubmittedRating: TextView
    private lateinit var tvUserComment: TextView
    private lateinit var starImageViews: List<ImageView>
    private lateinit var userStarImageViews: List<ImageView>

    private val ratingActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val submittedRating = data?.getIntExtra(EXTRA_SUBMITTED_RATING, 0) ?: 0
            val submittedComment = data?.getStringExtra(EXTRA_SUBMITTED_COMMENT) ?: ""

            if (submittedRating > 0) {
                displayUserRating(submittedRating, submittedComment)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_menu)

        currentMenuItem = intent.getParcelableExtra<MenuItem>(EXTRA_MENU_ITEM)

        val menuImage: ImageView = findViewById(R.id.detail_menu_image)
        val menuTitle: TextView = findViewById(R.id.detail_menu_title)
        val menuDescription: TextView = findViewById(R.id.detail_menu_description)
        val btnBack: ImageView = findViewById(R.id.btn_back)
        val btnShare: ImageView = findViewById(R.id.btn_share)
        tvUserSubmittedRating = findViewById(R.id.tv_user_submitted_rating)
        tvUserComment = findViewById(R.id.tv_user_comment)

        starImageViews = listOf(
            findViewById(R.id.star_1), findViewById(R.id.star_2), findViewById(R.id.star_3),
            findViewById(R.id.star_4), findViewById(R.id.star_5)
        )
        userStarImageViews = listOf(
            findViewById(R.id.user_star_1), findViewById(R.id.user_star_2), findViewById(R.id.user_star_3),
            findViewById(R.id.user_star_4), findViewById(R.id.user_star_5)
        )

        currentMenuItem?.let { item ->
            menuImage.setImageResource(item.imageUrl)
            menuTitle.text = item.name
            menuDescription.text = item.description

            setInitialStarRating()

            tvUserSubmittedRating.visibility = View.GONE
            tvUserComment.visibility = View.GONE
            userStarImageViews.forEach { it.visibility = View.GONE }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        btnShare.setOnClickListener {
            currentMenuItem?.let { item -> shareMenuItem(item) }
                ?: run { Toast.makeText(this, getString(R.string.no_menu_to_share_message), Toast.LENGTH_SHORT).show() }
        }
    }

    private fun setInitialStarRating() {
        for (i in starImageViews.indices) {
            val starImageView = starImageViews[i]
            starImageView.setImageResource(R.drawable.ic_star)

            val starIndex = i + 1
            starImageView.setOnClickListener {
                navigateToRatingActivity(starIndex)
            }
        }
        starImageViews.forEach { it.visibility = View.VISIBLE }
    }

    private fun navigateToRatingActivity(initialRating: Int) {
        val intent = Intent(this, RatingActivity::class.java).apply {
            putExtra(RatingActivity.EXTRA_MENU_ITEM, currentMenuItem)
            putExtra(RatingActivity.EXTRA_INITIAL_RATING, initialRating)
        }
        ratingActivityResultLauncher.launch(intent)
    }

    private fun displayUserRating(rating: Int, comment: String) {
        starImageViews.forEach { it.visibility = View.GONE }
        userStarImageViews.forEach { it.visibility = View.VISIBLE }

        for (i in userStarImageViews.indices) {
            if (i < rating) {
                userStarImageViews[i].setImageResource(R.drawable.ic_star_filled)
            } else {
                userStarImageViews[i].setImageResource(R.drawable.ic_star)
            }
        }

        // Menggunakan string resource untuk menampilkan rating
        tvUserSubmittedRating.text = getString(R.string.your_rating_prefix) + rating + getString(R.string.star_suffix)
        tvUserSubmittedRating.visibility = View.VISIBLE

        if (comment.isNotBlank()) {
            // Menggunakan string resource untuk menampilkan komentar
            tvUserComment.text = getString(R.string.comment_prefix) + comment
            tvUserComment.visibility = View.VISIBLE
        } else {
            tvUserComment.visibility = View.GONE
        }
    }

    private fun shareMenuItem(item: MenuItem) {
        // Menggunakan string resource dengan placeholder
        val shareText = getString(
            R.string.share_menu_item_text,
            item.name,
            item.rating,
            item.description
        )
        val shareIntent = Intent().apply{
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        if(shareIntent.resolveActivity(packageManager) != null){
            // Menggunakan string resource untuk chooser title
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via_chooser_title)))
        }else{
            Toast.makeText(this, getString(R.string.no_menu_to_share_message), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}