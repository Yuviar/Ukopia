package com.example.ukopia.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ukopia.R // Pastikan import R
import com.example.ukopia.data.MenuItem

class RatingActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MENU_ITEM = "extra_menu_item"
        const val EXTRA_INITIAL_RATING = "extra_initial_rating"
        const val EXTRA_SUBMITTED_RATING = "extra_submitted_rating"
        const val EXTRA_SUBMITTED_COMMENT = "extra_submitted_comment"
    }

    private var selectedRating = 0
    private lateinit var starImageViews: List<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)

        val menuItem = intent.getParcelableExtra<MenuItem>(EXTRA_MENU_ITEM)
        val initialRating = intent.getIntExtra(EXTRA_INITIAL_RATING, 0)

        val tvTitle: TextView = findViewById(R.id.tv_rating_menu_title)
        val btnSubmit: Button = findViewById(R.id.btn_submit_rating)
        val etKomentar: EditText = findViewById(R.id.editTextKomentar)

        // Menggunakan string resource untuk judul
        tvTitle.text = getString(R.string.rate_for_prefix) + (menuItem?.name ?: getString(R.string.menu_placeholder))
        // Hint untuk EditText komentar sudah di XML

        starImageViews = listOf(
            findViewById(R.id.star_1), findViewById(R.id.star_2), findViewById(R.id.star_3),
            findViewById(R.id.star_4), findViewById(R.id.star_5)
        )

        if (initialRating > 0) {
            updateStarRating(initialRating)
        }

        starImageViews.forEachIndexed { index, star ->
            star.setOnClickListener {
                updateStarRating(index + 1)
            }
        }

        btnSubmit.setOnClickListener {
            if (selectedRating == 0) {
                // Menggunakan string resource untuk error
                Toast.makeText(this, getString(R.string.error_no_rating_selected), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val commentText = etKomentar.text.toString()

            val resultIntent = Intent().apply {
                putExtra(EXTRA_SUBMITTED_RATING, selectedRating)
                putExtra(EXTRA_SUBMITTED_COMMENT, commentText)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun updateStarRating(rating: Int) {
        selectedRating = rating
        for (i in starImageViews.indices) {
            if (i < rating) {
                starImageViews[i].setImageResource(R.drawable.ic_star_filled)
            } else {
                starImageViews[i].setImageResource(R.drawable.ic_star)
            }
        }
    }
}