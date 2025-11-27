// D:/github rama/Ukopia/app/src/main/java/com/example/ukopia/ui/loyalty/RewardAdapter.kt
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

class RewardAdapter : ListAdapter<RewardItem, RewardAdapter.RewardViewHolder>(RewardItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val binding = ItemRewardListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RewardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class RewardViewHolder(private val binding: ItemRewardListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(rewardItem: RewardItem) {
            binding.tvRewardName.text = rewardItem.name
            binding.tvPointsRequired.text = binding.root.context.getString(R.string.reward_points_format_display, rewardItem.pointsRequired)
            binding.ivRewardIcon.setImageResource(rewardItem.iconResId)

            val context = binding.root.context

            // Update reward status text and background based on pointsMet and claimedDate
            if (rewardItem.claimedDate != null) {
                // Status: Sudah Diklaim oleh Admin
                binding.tvRewardStatus.text = context.getString(R.string.reward_status_claimed_date_format, rewardItem.claimedDate)
                binding.tvRewardStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black))
                binding.tvRewardStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else if (rewardItem.pointsMet) {
                // Status: Poin Tercapai, Belum Diklaim oleh Admin
                binding.tvRewardStatus.text = context.getString(R.string.reward_status_not_yet_claimed)
                binding.tvRewardStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black))
                binding.tvRewardStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                // Status: Poin Belum Tercapai
                binding.tvRewardStatus.text = context.getString(R.string.reward_status_not_achieved)
                binding.tvRewardStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black))
                binding.tvRewardStatus.setTextColor(ContextCompat.getColor(context, R.color.white)) // Atau warna hitam agar konsisten dengan warna status lain
            }
        }
    }

    class RewardItemDiffCallback : DiffUtil.ItemCallback<RewardItem>() {
        override fun areItemsTheSame(oldItem: RewardItem, newItem: RewardItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RewardItem, newItem: RewardItem): Boolean {
            return oldItem == newItem
        }
    }
}