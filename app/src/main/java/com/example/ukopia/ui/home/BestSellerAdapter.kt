package com.example.ukopia // (Sesuaikan package Anda jika beda)

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ukopia.models.MenuApiItem // <-- IMPORT BARU
import com.google.android.material.imageview.ShapeableImageView
import java.util.Locale

// GANTI NAMA 'layout_bestseller_item' DENGAN NAMA FILE LAYOUT XML ANDA
// (Jika layoutnya sama dengan MenuFragment, Anda bisa pakai R.layout.layout_menu_item)

class BestSellerAdapter(
    private var menuItems: List<MenuApiItem>,
    private val onItemClick: (MenuApiItem) -> Unit
) : RecyclerView.Adapter<BestSellerAdapter.BestSellerViewHolder>() {

    // Asumsi ID view di dalam layout item Anda
    inner class BestSellerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // GANTI ID INI jika di layout Anda namanya beda
        val menuImage: ShapeableImageView = itemView.findViewById(R.id.iv_menu_image)
        val menuTitle: TextView = itemView.findViewById(R.id.tv_menu_title)
        val menuRating: TextView = itemView.findViewById(R.id.tv_menu_rating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestSellerViewHolder {
        // GANTI NAMA LAYOUT INI dengan layout item best seller Anda
        // Saya akan gunakan R.layout.layout_menu_item sebagai contoh
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_menu_item, parent, false)
        return BestSellerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BestSellerViewHolder, position: Int) {
        val item = menuItems[position]

        // Muat gambar dari URL menggunakan Glide
        Glide.with(holder.itemView.context)
            .load(item.gambar_url) // <-- Menggunakan URL
            .placeholder(R.drawable.sample_coffee) // Placeholder
            .into(holder.menuImage)

        holder.menuTitle.text = item.nama_menu

        // Format rating dari Double
        holder.menuRating.text = String.format(Locale.ROOT, "%.1f/5.0", item.average_rating)

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = menuItems.size

    // Fungsi baru untuk update data dari ViewModel
    fun updateData(newItems: List<MenuApiItem>) {
        menuItems = newItems
        notifyDataSetChanged()
    }
}