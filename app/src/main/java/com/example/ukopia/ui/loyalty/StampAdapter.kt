package com.example.ukopia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.R
import com.example.ukopia.databinding.ItemStampBinding

data class StampItem(
    val number: Int,
    val isCollected: Boolean
)

class StampAdapter : ListAdapter<StampItem, StampAdapter.StampViewHolder>(StampDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StampViewHolder {
        val binding = ItemStampBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StampViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StampViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StampViewHolder(private val binding: ItemStampBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StampItem) {
            val context = binding.root.context
            binding.tvStampNumber.text = item.number.toString()

            if (item.isCollected) {
                binding.ivStampBackground.setBackgroundResource(R.drawable.circle_background_white_stroke_black_fill)
                binding.tvStampNumber.visibility = View.GONE
                binding.ivStampCheckmark.visibility = View.VISIBLE
            } else {
                binding.ivStampBackground.setBackgroundResource(R.drawable.reward_circle_background_default)
                binding.tvStampNumber.visibility = View.VISIBLE
                binding.tvStampNumber.setTextColor(ContextCompat.getColor(context, R.color.black))
                binding.ivStampCheckmark.visibility = View.GONE
            }
        }
    }

    class StampDiffCallback : DiffUtil.ItemCallback<StampItem>() {
        override fun areItemsTheSame(oldItem: StampItem, newItem: StampItem) = oldItem.number == newItem.number
        override fun areContentsTheSame(oldItem: StampItem, newItem: StampItem) = oldItem == newItem
    }
}