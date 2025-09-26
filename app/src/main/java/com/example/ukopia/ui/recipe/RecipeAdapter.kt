package com.example.ukopia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.R
import com.example.ukopia.data.RecipeItem

/**
 * Adapter untuk RecyclerView yang menampilkan daftar resep.
 *
 * @param items Daftar RecipeItem yang akan ditampilkan.
 */
class RecipeAdapter(private val items: List<RecipeItem>) :
    RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    /**
     * ViewHolder yang memegang referensi ke setiap elemen UI di dalam item_recipe_card.xml.
     */

    /**
     * Dipanggil saat RecyclerView membutuhkan ViewHolder baru.
     * Mengembang (inflate) layout item_recipe_card.xml.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_card, parent, false)
        return RecipeViewHolder(view)
    }

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.textViewRecipeName)
    }
    /**
     * Mengikat data ke elemen UI di dalam ViewHolder.
     */
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.nama
        // Ganti baris ini dengan kode untuk memuat gambar
        // holder.image.setImageResource(...) atau menggunakan library seperti Glide/Picasso
    }

    /**
     * Mengembalikan jumlah total item dalam daftar.
     */
    override fun getItemCount(): Int = items.size
}
