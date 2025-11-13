package com.example.ukopia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.data.MenuItem // Import MenuItem
import com.example.ukopia.R // Import R untuk layout_menu_item

class BestSellerAdapter(
    var itemList: List<MenuItem>,
    private val onItemClick: (MenuItem) -> Unit // Pertahankan listener klik
) : RecyclerView.Adapter<BestSellerAdapter.BestSellerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestSellerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_menu_item, parent, false) // Menggunakan layout_menu_item
        return BestSellerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BestSellerViewHolder, position: Int) {
        val item = itemList[position]
        holder.imageView.setImageResource(item.imageUrl) // Menggunakan imageUrl dari MenuItem
        holder.titleTextView.text = item.name // Menggunakan name dari MenuItem
        holder.ratingPriceTextView.text = item.rating // Menggunakan rating dari MenuItem

        // Pertahankan OnClickListener ke itemView
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class BestSellerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_menu_image) // ID dari layout_menu_item
        val titleTextView: TextView = itemView.findViewById(R.id.tv_menu_title) // ID dari layout_menu_item
        val ratingPriceTextView: TextView = itemView.findViewById(R.id.tv_menu_rating) // ID dari layout_menu_item
    }

    fun updateData(newItems: List<MenuItem>) { // Update method juga pakai MenuItem
        itemList = newItems
        notifyDataSetChanged()
    }
}