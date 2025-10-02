package com.example.ukopia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.data.BrewMethod
import com.example.ukopia.databinding.ItemRecipeCardBinding

class BrewMethodAdapter(
    private val onItemClick: (BrewMethod) -> Unit
) : ListAdapter<BrewMethod, BrewMethodAdapter.BrewMethodViewHolder>(BrewMethodDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrewMethodViewHolder {
        val binding = ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BrewMethodViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: BrewMethodViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class BrewMethodViewHolder(
        private val binding: ItemRecipeCardBinding,
        private val onItemClick: (BrewMethod) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BrewMethod) {
            binding.textViewRecipeName.text = item.name
            binding.imageViewRecipe.setImageResource(item.imageUrl)
            // Mengganti listener ke seluruh item, bukan hanya tombol
            binding.root.setOnClickListener {
                onItemClick(item)
            }
            binding.btnSelengkapnya.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    class BrewMethodDiffCallback : DiffUtil.ItemCallback<BrewMethod>() {
        override fun areItemsTheSame(oldItem: BrewMethod, newItem: BrewMethod): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: BrewMethod, newItem: BrewMethod): Boolean {
            return oldItem == newItem
        }
    }
}