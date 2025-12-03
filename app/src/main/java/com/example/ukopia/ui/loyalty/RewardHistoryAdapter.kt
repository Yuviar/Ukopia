package com.example.ukopia.ui.loyalty

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.R
import com.example.ukopia.data.RewardHistoryItem
import com.example.ukopia.databinding.ItemRewardListBinding

class RewardHistoryAdapter(
    private val onRewardClick: (RewardHistoryItem) -> Unit
) : ListAdapter<RewardHistoryItem, RewardHistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRewardListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemRewardListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RewardHistoryItem) {
            val context = itemView.context

            val localizedRewardName = when {
                item.namaReward.contains("Diskon 10%", ignoreCase = true) || item.namaReward.contains("Discount 10%", ignoreCase = true) -> context.getString(R.string.loyalty_reward_10_percent_discount_title)
                item.namaReward.contains("Free 1 Serve", ignoreCase = true) ||
                        item.namaReward.contains("GRATIS 1 PORSI", ignoreCase = true) ||
                        item.namaReward.contains("Free Manual Brew", ignoreCase = true) -> context.getString(R.string.loyalty_reward_free_serve_title)
                item.namaReward.contains("T-Shirt", ignoreCase = true) || item.namaReward.contains("KAOS", ignoreCase = true) -> context.getString(R.string.loyalty_reward_free_tshirt_title)
                else -> item.namaReward
            }
            binding.tvRewardName.text = localizedRewardName

            val iconResId = when {
                item.namaReward.contains("T-Shirt", ignoreCase = true) || item.namaReward.contains("KAOS", ignoreCase = true) -> R.drawable.ic_tshirt
                item.namaReward.contains("Serve", ignoreCase = true) ||
                        item.namaReward.contains("PORSI", ignoreCase = true) ||
                        item.namaReward.contains("Free Manual Brew", ignoreCase = true) -> R.drawable.ic_coffee_cup
                else -> R.drawable.ic_discount
            }
            binding.ivRewardIcon.setImageResource(iconResId)

            val tanggalDisplay = try {
                item.tanggalDapat.take(10)
            } catch (e: Exception) {
                item.tanggalDapat
            }

            val isUsed = item.statusKlaim.equals("Claimed", ignoreCase = true) || item.statusKlaim.equals("Sudah Dipakai", ignoreCase = true)

            if (isUsed) {
               binding.tvRewardStatus.text = context.getString(R.string.reward_claimed_status).uppercase()
                binding.tvRewardStatus.setBackgroundColor(Color.DKGRAY)
                binding.tvRewardStatus.setTextColor(Color.WHITE)

                binding.tvPointsRequired.text = "${context.getString(R.string.reward_code_prefix)}${item.kodeUnik}\n$tanggalDisplay"

                binding.root.alpha = 0.6f
                binding.root.isEnabled = false
                binding.root.setOnClickListener(null)
            } else {
                binding.tvRewardStatus.text = context.getString(R.string.loyalty_reward_claim_action).uppercase()
                binding.tvRewardStatus.setBackgroundColor(Color.parseColor("#4CAF50")) // Hijau
                binding.tvRewardStatus.setTextColor(Color.WHITE)

                binding.tvPointsRequired.text = "${context.getString(R.string.rewards_available_status)}\n$tanggalDisplay"

                binding.root.alpha = 1.0f
                binding.root.isEnabled = true
                binding.root.setOnClickListener {
                    onRewardClick(item)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<RewardHistoryItem>() {
        override fun areItemsTheSame(oldItem: RewardHistoryItem, newItem: RewardHistoryItem) = oldItem.idRiwayat == newItem.idRiwayat
        override fun areContentsTheSame(oldItem: RewardHistoryItem, newItem: RewardHistoryItem) = oldItem == newItem
    }
}