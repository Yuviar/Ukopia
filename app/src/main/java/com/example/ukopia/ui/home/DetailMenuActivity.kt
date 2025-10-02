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
import com.example.ukopia.R
import com.example.ukopia.data.MenuItem

class DetailMenuActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MENU_ITEM = "extra_menu_item"
        // Key untuk menerima hasil rating
        const val REQUEST_CODE_SUBMIT_RATING = "request_code_submit_rating"
        const val EXTRA_SUBMITTED_RATING = "extra_submitted_rating"
        const val EXTRA_SUBMITTED_COMMENT = "extra_submitted_comment" // Konstan baru untuk komentar
    }

    private var currentMenuItem: MenuItem? = null
    private lateinit var tvUserSubmittedRating: TextView
    private lateinit var tvUserComment: TextView // TextView baru untuk komentar
    private lateinit var starImageViews: List<ImageView> // Daftar 5 bintang rata-rata
    private lateinit var userStarImageViews: List<ImageView> // Daftar 5 bintang rating pengguna

    // Activity Result Launcher untuk menerima hasil dari RatingActivity
    private val ratingActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val submittedRating = data?.getIntExtra(EXTRA_SUBMITTED_RATING, 0) ?: 0
            val submittedComment = data?.getStringExtra(EXTRA_SUBMITTED_COMMENT) ?: "" // Menerima komentar

            // Tampilkan rating baru yang dikirim oleh pengguna
            if (submittedRating > 0) {
                displayUserRating(submittedRating, submittedComment)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_menu)

        // Mengambil data MenuItem dari Intent
        currentMenuItem = intent.getParcelableExtra<MenuItem>(EXTRA_MENU_ITEM)

        // Inisialisasi Views
        val menuImage: ImageView = findViewById(R.id.detail_menu_image)
        val menuTitle: TextView = findViewById(R.id.detail_menu_title)
        val menuDescription: TextView = findViewById(R.id.detail_menu_description)
        val btnBack: ImageView = findViewById(R.id.btn_back)
        val btnShare: ImageView = findViewById(R.id.btn_share)
        tvUserSubmittedRating = findViewById(R.id.tv_user_submitted_rating)
        tvUserComment = findViewById(R.id.tv_user_comment) // Inisialisasi TextView komentar

        // Bintang Rata-rata (Average)
        starImageViews = listOf(
            findViewById(R.id.star_1), findViewById(R.id.star_2), findViewById(R.id.star_3),
            findViewById(R.id.star_4), findViewById(R.id.star_5)
        )
        // Bintang Rating Pengguna
        userStarImageViews = listOf(
            findViewById(R.id.user_star_1), findViewById(R.id.user_star_2), findViewById(R.id.user_star_3),
            findViewById(R.id.user_star_4), findViewById(R.id.user_star_5)
        )

        // Set data ke Views
        currentMenuItem?.let { item ->
            menuImage.setImageResource(item.imageUrl)
            menuTitle.text = item.name
            menuDescription.text = item.description

            // Mengatur tampilan bintang rata-rata dan click listener
            setInitialStarRating()

            // Sembunyikan semua elemen rating pengguna secara default
            tvUserSubmittedRating.visibility = View.GONE
            tvUserComment.visibility = View.GONE
            userStarImageViews.forEach { it.visibility = View.GONE }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        btnShare.setOnClickListener {
            currentMenuItem?.let { item -> shareMenuItem(item) }
                ?: run { Toast.makeText(this, "Tidak ada menu untuk dibagikan!", Toast.LENGTH_SHORT).show() }
        }
    }

    // Mengatur Tampilan Awal Bintang (Hanya Ikon Kosong + Click Listener)
    private fun setInitialStarRating() {
        for (i in starImageViews.indices) {
            val starImageView = starImageViews[i]

            // Atur tampilan bintang menjadi IKON KOSONG (tanpa mengisi warna)
            starImageView.setImageResource(R.drawable.ic_star)

            // Tambahkan Click Listener ke setiap bintang
            val starIndex = i + 1
            starImageView.setOnClickListener {
                navigateToRatingActivity(starIndex)
            }
        }

        // Pastikan bintang rata-rata terlihat saat pertama kali dimuat
        starImageViews.forEach { it.visibility = View.VISIBLE }
    }

    // Fungsi untuk meluncurkan RatingActivity menggunakan Activity Result Launcher
    private fun navigateToRatingActivity(initialRating: Int) {
        val intent = Intent(this, RatingActivity::class.java).apply {
            putExtra(RatingActivity.EXTRA_MENU_ITEM, currentMenuItem)
            putExtra(RatingActivity.EXTRA_INITIAL_RATING, initialRating)
        }
        ratingActivityResultLauncher.launch(intent)
    }

    // Fungsi baru untuk menampilkan rating dan komentar yang dikirim pengguna
    private fun displayUserRating(rating: Int, comment: String) {
        // 1. Sembunyikan bintang rata-rata yang ada
        starImageViews.forEach { it.visibility = View.GONE }

        // 2. Tampilkan bintang rating pengguna
        userStarImageViews.forEach { it.visibility = View.VISIBLE }

        // 3. Isi warna bintang rating pengguna
        for (i in userStarImageViews.indices) {
            if (i < rating) {
                userStarImageViews[i].setImageResource(R.drawable.ic_star_filled)
            } else {
                userStarImageViews[i].setImageResource(R.drawable.ic_star)
            }
        }

        // 4. Tampilkan TextView rating pengguna
        tvUserSubmittedRating.text = "Rating Anda: ${rating} Bintang"
        tvUserSubmittedRating.visibility = View.VISIBLE

        // 5. Tampilkan komentar (jika ada)
        if (comment.isNotBlank()) {
            tvUserComment.text = "Komentar: $comment"
            tvUserComment.visibility = View.VISIBLE
        } else {
            tvUserComment.visibility = View.GONE
        }
    }

    private fun shareMenuItem(item: MenuItem) {
        val shareText = "Yuk coba ${item.name} di Ukopia!\n" +
                "Rating: ${item.rating}\n" +
                "Deskripsi: ${item.description}\n" +
                "#Ukopia #CoffeeLover"
        val shareIntent = Intent().apply{
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        if(shareIntent.resolveActivity(packageManager) != null){
            startActivity(Intent.createChooser(shareIntent, "Bagikan melalui:"))
        }else{
            Toast.makeText(this, "Tidak ada menu untuk dibagikan!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
