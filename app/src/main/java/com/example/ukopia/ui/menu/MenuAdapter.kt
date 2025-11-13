package com.example.ukopia.ui.menu

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // IMPORT GLIDE
import com.example.ukopia.R
import com.example.ukopia.models.MenuApiItem // GANTI
import com.google.android.material.imageview.ShapeableImageView
import java.util.Locale

class MenuAdapter(private var menuItems: List<MenuApiItem>, private val onItemClick: (MenuApiItem) -> Unit) :
    RecyclerView.Adapter<MenuAdapter.MenuItemViewHolder>() {

    inner class MenuItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val menuImage: ShapeableImageView = itemView.findViewById(R.id.iv_menu_image)
        val menuTitle: TextView = itemView.findViewById(R.id.tv_menu_title)
        val menuRating: TextView = itemView.findViewById(R.id.tv_menu_rating)
        val cardBackground: View = itemView.findViewById(R.id.card_view_background)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_menu_item, parent, false)
        return MenuItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        val item = menuItems[position]

        // --- PERUBAHAN ---
        // Muat gambar dari URL menggunakan Glide
        Glide.with(holder.itemView.context)
            .load(item.gambar_url) // Ambil dari URL
            .placeholder(R.drawable.sample_coffee) // Gambar placeholder
            .error(R.drawable.sample_coffee) // Gambar jika error
            .into(holder.menuImage)

        holder.menuTitle.text = item.nama_menu

        // Format rating dari API
        holder.menuRating.text = String.format(Locale.ROOT, "%.1f/5.0", item.average_rating)
        // --- AKHIR PERUBAHAN ---

        holder.itemView.setOnClickListener {
            // ... (Logika animasi flash Anda tetap sama) ...
            Handler(Looper.getMainLooper()).postDelayed({
                // ... (Logika animasi flash Anda tetap sama) ...
                onItemClick(item)
            }, 150)
        }
    }

    override fun getItemCount(): Int = menuItems.size

    fun updateData(newItems: List<MenuApiItem>) {
        menuItems = newItems
        notifyDataSetChanged() // Anda bisa ganti dengan DiffUtil nanti
    }
}