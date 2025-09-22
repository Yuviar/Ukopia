package com.example.ukopia.ui.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.R
import com.example.ukopia.data.MenuItem
import com.google.android.material.imageview.ShapeableImageView

class MenuAdapter(private val menuItems: List<MenuItem>) :
    RecyclerView.Adapter<MenuAdapter.MenuItemViewHolder>() {

    // Inner class untuk menampung referensi View dari layout_menu_item.xml
    inner class MenuItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val menuImage: ShapeableImageView = itemView.findViewById(R.id.iv_menu_image)
        val menuTitle: TextView = itemView.findViewById(R.id.tv_menu_title)
        val menuRating: TextView = itemView.findViewById(R.id.tv_menu_rating)
        // Ikon bintang tidak perlu di-referensi jika tidak berubah per item
    }

    // Dipanggil saat RecyclerView membutuhkan ViewHolder baru
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_menu_item, parent, false) // Inflate layout kartu kita
        return MenuItemViewHolder(view)
    }

    // Dipanggil untuk menampilkan data pada ViewHolder tertentu
    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        val item = menuItems[position]
        holder.menuImage.setImageResource(item.imageUrl) // Set gambar
        holder.menuTitle.text = item.name // Set judul
        holder.menuRating.text = item.rating // Set rating

        holder.itemView.setOnClickListener {
            // Lakukan sesuatu saat item diklik
            // Toast.makeText(holder.itemView.context, "Klik ${item.name}", Toast.LENGTH_SHORT).show()
            val context = holder.itemView.context
            val intent = Intent(context, DetailMenuActivity::class.java)
            intent.putExtra(DetailMenuActivity.EXTRA_MENU_ITEM, item)
            context.startActivity(intent)
        }
    }

    // Mengembalikan jumlah total item dalam daftar
    override fun getItemCount(): Int = menuItems.size
}