package com.example.ukopia.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.R
import com.example.ukopia.databinding.ItemFilterCategoryBinding // Import binding untuk item

class MenuFilterAdapter(
    private val categories: List<String>,
    private val currentSelection: String?,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<MenuFilterAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(private val binding: ItemFilterCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: String, isSelected: Boolean) {
            binding.textViewCategoryName.text = category
            binding.root.setOnClickListener { onItemClick(category) }

            if (isSelected) {
                binding.textViewCategoryName.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                binding.imageViewSelected.visibility = View.VISIBLE
                // Opsional: berikan background atau styling khusus untuk item terpilih
                binding.root.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.light_grey)) // Contoh, pastikan light_grey_translucent ada
            } else {
                binding.textViewCategoryName.setTextColor(ContextCompat.getColor(itemView.context, R.color.black)) // Warna default
                binding.imageViewSelected.visibility = View.GONE
                binding.root.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemFilterCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category, category == currentSelection)
    }

    override fun getItemCount(): Int = categories.size
}