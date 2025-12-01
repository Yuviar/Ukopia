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
            binding.tvRewardName.text = item.namaReward

            // Icon Mapping Sederhana
            val iconResId = when {
                item.namaReward.contains("T-Shirt", ignoreCase = true) -> R.drawable.ic_tshirt
                item.namaReward.contains("Serve", ignoreCase = true) -> R.drawable.ic_coffee_cup
                else -> R.drawable.ic_discount
            }
            binding.ivRewardIcon.setImageResource(iconResId)

            // Format Tanggal: Ambil tanggalnya saja (YYYY-MM-DD)
            val tanggalDisplay = try {
                item.tanggalDapat.take(10)
            } catch (e: Exception) {
                item.tanggalDapat
            }

            // Cek Status Klaim
            val isUsed = item.statusKlaim.equals("Claimed", ignoreCase = true)

            if (isUsed) {
                // STATUS: SUDAH DIPAKAI (DISABLE)
                // Kotak status hanya berisi teks status
                binding.tvRewardStatus.text = "CLAIMED"
                binding.tvRewardStatus.setBackgroundColor(Color.DKGRAY)
                binding.tvRewardStatus.setTextColor(Color.WHITE)

                // Tanggal ditaruh di bawah kode (menggunakan \n)
                binding.tvPointsRequired.text = "Code: ${item.kodeUnik}\n$tanggalDisplay"

                // Efek visual disable
                binding.root.alpha = 0.6f
                binding.root.isEnabled = false
                binding.root.setOnClickListener(null)
            } else {
                // STATUS: BELUM DIPAKAI (ENABLE)
                // Kotak status hanya berisi teks status
                binding.tvRewardStatus.text = "TAP TO USE"
                binding.tvRewardStatus.setBackgroundColor(Color.parseColor("#4CAF50")) // Hijau
                binding.tvRewardStatus.setTextColor(Color.WHITE)

                // Tanggal ditaruh di bawah keterangan
                binding.tvPointsRequired.text = "Rewards Available\n$tanggalDisplay"

                // Efek visual enable
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