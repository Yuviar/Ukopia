package com.example.ukopia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ukopia.models.MenuApiItem // <-- IMPORT BARU
import com.google.android.material.imageview.ShapeableImageView
import java.util.Locale

class BestSellerAdapter(
    private var menuItems: List<MenuApiItem>,
    private val onItemClick: (MenuApiItem) -> Unit
) : RecyclerView.Adapter<BestSellerAdapter.BestSellerViewHolder>() {

    inner class BestSellerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val menuImage: ShapeableImageView = itemView.findViewById(R.id.iv_menu_image)
        val menuTitle: TextView = itemView.findViewById(R.id.tv_menu_title)
        val menuRating: TextView = itemView.findViewById(R.id.tv_menu_rating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestSellerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_menu_item, parent, false)
        return BestSellerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BestSellerViewHolder, position: Int) {
        val item = menuItems[position]

        Glide.with(holder.itemView.context)
            .load(item.gambar_url)
            .placeholder(R.drawable.sample_coffee)
            .into(holder.menuImage)

        holder.menuTitle.text = item.nama_menu

        holder.menuRating.text = String.format(Locale.ROOT, "%.1f/5.0", item.average_rating)

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = menuItems.size

    fun updateData(newItems: List<MenuApiItem>) {
        menuItems = newItems
        notifyDataSetChanged()
    }
}