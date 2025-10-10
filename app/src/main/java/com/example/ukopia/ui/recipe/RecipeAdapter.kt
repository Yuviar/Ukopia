package com.example.ukopia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.data.RecipeItem
import com.example.ukopia.databinding.ItemRecipeCardBinding
import com.example.ukopia.R

class RecipeAdapter(
    private val onItemClick: (RecipeItem) -> Unit
) : ListAdapter<RecipeItem, RecipeAdapter.RecipeViewHolder>(RecipeItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class RecipeViewHolder(
        private val binding: ItemRecipeCardBinding,
        private val onItemClick: (RecipeItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecipeItem) {
            binding.textViewRecipeName.text = item.name
            // Teks tombol "Selengkapnya" sudah ada di XML, jadi tidak perlu diset di sini
            // binding.btnSelengkapnya.text = itemView.context.getString(R.string.more_button_text)

            binding.btnSelengkapnya.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    class RecipeItemDiffCallback : DiffUtil.ItemCallback<RecipeItem>() {
        override fun areItemsTheSame(oldItem: RecipeItem, newItem: RecipeItem): Boolean {
            return oldItem.name == newItem.name && oldItem.description == newItem.description
        }

        override fun areContentsTheSame(oldItem: RecipeItem, newItem: RecipeItem): Boolean {
            return oldItem == newItem
        }
    }
}