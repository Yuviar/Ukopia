package com.example.ukopia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.data.RecipeStep
import com.example.ukopia.databinding.ItemRecipeStepBinding

class RecipeStepAdapter(private val onItemClick: (RecipeStep) -> Unit) :
    ListAdapter<RecipeStep, RecipeStepAdapter.RecipeStepViewHolder>(RecipeStepDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeStepViewHolder {
        val binding = ItemRecipeStepBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeStepViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: RecipeStepViewHolder, position: Int) {
        val step = getItem(position)
        holder.bind(step)
    }

    class RecipeStepViewHolder(
        private val binding: ItemRecipeStepBinding,
        private val onItemClick: (RecipeStep) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(step: RecipeStep) {
            binding.textViewStepTitle.text = step.title
            binding.imageViewStepIcon.setImageResource(step.iconResId)

            binding.root.setOnClickListener {
                onItemClick(step)
            }
        }
    }

    class RecipeStepDiffCallback : DiffUtil.ItemCallback<RecipeStep>() {
        override fun areItemsTheSame(oldItem: RecipeStep, newItem: RecipeStep): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RecipeStep, newItem: RecipeStep): Boolean {
            return oldItem == newItem
        }
    }
}