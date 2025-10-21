package com.example.ukopia.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.R
import com.example.ukopia.databinding.ItemHorizontalFilterCategoryBinding

class HorizontalFilterAdapter(
    private val categories: List<String>,
    private var currentSelection: String,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<HorizontalFilterAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(private val binding: ItemHorizontalFilterCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: String, isSelected: Boolean) {
            val button = binding.buttonCategory
            button.text = category
            button.setOnClickListener {
                val oldSelection = currentSelection
                currentSelection = category
                onItemClick(category)
                // Perbarui tampilan item lama dan item baru yang terpilih
                notifyItemChanged(categories.indexOf(oldSelection))
                notifyItemChanged(adapterPosition)
            }

            if (isSelected) {
                button.setBackgroundTintList(ContextCompat.getColorStateList(itemView.context, R.color.black))
                button.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
            } else {
                button.setBackgroundTintList(ContextCompat.getColorStateList(itemView.context, R.color.white))
                button.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemHorizontalFilterCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category, category == currentSelection)
    }

    override fun getItemCount(): Int = categories.size

    fun updateSelection(newSelection: String) {
        val oldSelectionIndex = categories.indexOf(currentSelection)
        val newSelectionIndex = categories.indexOf(newSelection)
        currentSelection = newSelection
        if (oldSelectionIndex != -1) notifyItemChanged(oldSelectionIndex)
        if (newSelectionIndex != -1) notifyItemChanged(newSelectionIndex)
    }
}