package com.example.ukopia.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ukopia.R
import com.example.ukopia.data.MenuItem // Import kelas MenuItem Anda

class DetailMenuActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MENU_ITEM = "extra_menu_item"
    }
    private var currentMenuItem: MenuItem? = null

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

        // Set data ke Views
        currentMenuItem?.let { item ->
            menuImage.setImageResource(item.imageUrl)
            menuTitle.text = item.name
            menuDescription.text = item.description
            setStarRating(item.rating)
        }

        // Opsional: Menambahkan tombol back di ActionBar jika ada
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "" // Kosongkan title agar tidak menutupi tampilan
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        btnShare.setOnClickListener {
            currentMenuItem?.let { item ->
                shareMenuItem(item)
            } ?: run {
                Toast.makeText(this, "Tidak ada menu untuk dibagikan!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fungsi untuk mengatur tampilan bintang rating
    private fun setStarRating(ratingText: String) {
        val ratingValue = ratingText.substringBefore("/").replace(",", ".").toDoubleOrNull() ?: 0.0
        val filledStars = (ratingValue / 1.0).toInt() // Misalnya, 4.8/5.0 -> 4 bintang terisi

        val starIds = listOf(R.id.star_1, R.id.star_2, R.id.star_3, R.id.star_4, R.id.star_5)

        for (i in starIds.indices) {
            val starImageView: ImageView = findViewById(starIds[i])
            if (i < filledStars) {
//                starImageView.setImageResource(R.drawable.ic_star_filled)
                starImageView.setImageResource(R.drawable.ic_star)
            } else {
                starImageView.setImageResource(R.drawable.ic_star)
//                starImageView.setImageResource(R.drawable.ic_star_empty)
            }
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
            Toast.makeText(this, "Tidak ada aplikasi yang tersedia untuk berbagi!", Toast.LENGTH_SHORT).show()
        }
    }

    // Mengaktifkan fungsionalitas tombol back pada ActionBar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}