package com.example.ukopia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.data.RecipeItem
import com.example.ukopia.databinding.ItemRecipeCardSmallBinding

class RecipeAdapter(
    private val onItemClick: (RecipeItem) -> Unit
) : ListAdapter<RecipeItem, RecipeAdapter.RecipeViewHolder>(RecipeItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeCardSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RecipeViewHolder(
        private val binding: ItemRecipeCardSmallBinding,
        private val onItemClick: (RecipeItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecipeItem) {
            binding.tvRecipeTitle.text = item.name
            binding.tvRecipeDescription.text = item.description

            binding.tvWaterAmount.text = item.waterAmount
            binding.tvCoffeeAmount.text = item.coffeeAmount
            binding.tvTime.text = item.extractionTime

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    class RecipeItemDiffCallback : DiffUtil.ItemCallback<RecipeItem>() {
        override fun areItemsTheSame(oldItem: RecipeItem, newItem: RecipeItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: RecipeItem, newItem: RecipeItem) = oldItem == newItem
    }
}