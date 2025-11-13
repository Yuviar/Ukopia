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
import com.example.ukopia.R
import com.example.ukopia.data.MenuItem
import com.google.android.material.imageview.ShapeableImageView

class MenuAdapter(private var menuItems: List<MenuItem>, private val onItemClick: (MenuItem) -> Unit) :
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
        holder.menuImage.setImageResource(item.imageUrl)
        holder.menuTitle.text = item.name
        holder.menuRating.text = item.rating

        holder.itemView.setOnClickListener {
            // Ambil warna original dari latar belakang kartu, teks, dan status tint gambar
            val originalCardBackgroundColor = (holder.cardBackground.background as? ColorStateList) ?: ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.context, R.color.white))
            val originalTitleTextColor = holder.menuTitle.textColors
            val originalRatingTextColor = holder.menuRating.textColors

            // Ambil Drawable bintang dan simpan ColorFilter aslinya
            val originalStarDrawable = holder.menuRating.compoundDrawables[0] // drawableStart berada di index 0
            val originalStarColorFilter = originalStarDrawable?.colorFilter

            // Definisikan warna flash menjadi PUTIH SEMUA
            val flashColorBackground = ContextCompat.getColor(holder.itemView.context, R.color.white) // Background flash: putih
            val flashColorText = ContextCompat.getColor(holder.itemView.context, R.color.white)       // Teks flash: putih (akan menghilang sementara)
            val flashColorImageTint = ContextCompat.getColor(holder.itemView.context, R.color.white)  // Tint gambar flash: putih
            val flashColorStarTint = ContextCompat.getColor(holder.itemView.context, R.color.white)   // Tint bintang flash: putih

            // Terapkan warna flash
            holder.cardBackground.backgroundTintList = ColorStateList.valueOf(flashColorBackground)
            holder.menuTitle.setTextColor(flashColorText)
            holder.menuRating.setTextColor(flashColorText)
            holder.menuImage.setColorFilter(flashColorImageTint, PorterDuff.Mode.SRC_IN)

            // Terapkan tint flash pada ikon bintang
            originalStarDrawable?.setColorFilter(flashColorStarTint, PorterDuff.Mode.SRC_IN)
            holder.menuRating.setCompoundDrawablesWithIntrinsicBounds(originalStarDrawable, null, null, null)

            Handler(Looper.getMainLooper()).postDelayed({
                // Kembalikan warna original setelah delay
                holder.cardBackground.backgroundTintList = originalCardBackgroundColor
                holder.menuTitle.setTextColor(originalTitleTextColor)
                holder.menuRating.setTextColor(originalRatingTextColor)
                holder.menuImage.colorFilter = null // Hapus tint dari gambar untuk mengembalikan yang asli

                // Kembalikan ColorFilter asli ke ikon bintang
                originalStarDrawable?.colorFilter = originalStarColorFilter
                holder.menuRating.setCompoundDrawablesWithIntrinsicBounds(originalStarDrawable, null, null, null)

                // Lanjutkan ke onItemClick setelah animasi selesai
                onItemClick(item)
            }, 150) // Durasi animasi flash: 150 milidetik
        }
    }

    override fun getItemCount(): Int = menuItems.size

    fun updateData(newItems: List<MenuItem>) {
        menuItems = newItems
        notifyDataSetChanged()
    }
}