package com.example.ukopia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.ukopia.R
import com.example.ukopia.data.BrewMethod
import com.example.ukopia.databinding.ItemRecipeCardBinding

class BrewMethodAdapter(
    private val onItemClick: (BrewMethod) -> Unit
) : ListAdapter<BrewMethod, BrewMethodAdapter.BrewMethodViewHolder>(BrewMethodDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrewMethodViewHolder {
        val binding = ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BrewMethodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BrewMethodViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class BrewMethodViewHolder(private val binding: ItemRecipeCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BrewMethod, onItemClick: (BrewMethod) -> Unit) {
            binding.textViewRecipeName.text = item.name.uppercase()

            binding.imageViewRecipe.load(item.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_aeropress)
                error(
                    R.drawable.ic_error)
            }

            // Klik tombol selengkapnya
            binding.btnSelengkapnya.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    class BrewMethodDiffCallback : DiffUtil.ItemCallback<BrewMethod>() {
        override fun areItemsTheSame(oldItem: BrewMethod, newItem: BrewMethod) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: BrewMethod, newItem: BrewMethod) = oldItem == newItem
    }
}