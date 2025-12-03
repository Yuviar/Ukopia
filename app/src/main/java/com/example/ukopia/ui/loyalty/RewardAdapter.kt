package com.example.ukopia.ui.loyalty

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.R
import com.example.ukopia.databinding.ItemRewardListBinding

class RewardAdapter : ListAdapter<RewardItem, RewardAdapter.RewardViewHolder>(RewardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val binding = ItemRewardListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RewardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RewardViewHolder(private val binding: ItemRewardListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RewardItem) {
            binding.tvRewardName.text = item.name
            binding.tvPointsRequired.text = "Requires ${item.pointsRequired} Points"
            binding.ivRewardIcon.setImageResource(item.iconResId) // Icon sudah benar

            val context = binding.root.context

            val statusText: String
            val backgroundColor: Int
            val textColor: Int

            if (item.claimedDate != null) {
                statusText = "CLAIMED: ${item.claimedDate}"
                backgroundColor = ContextCompat.getColor(context, R.color.black)
                textColor = ContextCompat.getColor(context, R.color.white)
            } else {
                statusText = "NOT YET CLAIMED"
                backgroundColor = ContextCompat.getColor(context, R.color.black)
                textColor = ContextCompat.getColor(context, R.color.white)
            }

            binding.tvRewardStatus.text = statusText
            binding.tvRewardStatus.backgroundTintList = ColorStateList.valueOf(backgroundColor)
            binding.tvRewardStatus.setTextColor(textColor)
        }
    }

    class RewardDiffCallback : DiffUtil.ItemCallback<RewardItem>() {
        override fun areItemsTheSame(oldItem: RewardItem, newItem: RewardItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: RewardItem, newItem: RewardItem) = oldItem == newItem
    }
}