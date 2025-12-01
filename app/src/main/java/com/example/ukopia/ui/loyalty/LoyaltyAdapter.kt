package com.example.ukopia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.data.LoyaltyItemV2
import com.example.ukopia.databinding.ItemLoyaltyCardBinding

class LoyaltyAdapter(
    private val onItemClick: (LoyaltyItemV2) -> Unit
) : ListAdapter<LoyaltyItemV2, LoyaltyAdapter.LoyaltyViewHolder>(LoyaltyItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoyaltyViewHolder {
        val binding = ItemLoyaltyCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoyaltyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LoyaltyViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class LoyaltyViewHolder(private val binding: ItemLoyaltyCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LoyaltyItemV2, onItemClick: (LoyaltyItemV2) -> Unit) {
            binding.textViewNamaMenu.text = item.namaMenu
            binding.textViewTanggal.text = item.tanggal

            // Tampilkan nama beans (kopi) atau nama menu (non-kopi)
            binding.textViewBeans.text = if(item.isCoffee) item.namaBeans else item.namaMenu

            binding.btnSelengkapnya.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    class LoyaltyItemDiffCallback : DiffUtil.ItemCallback<LoyaltyItemV2>() {
        // Perbaikan: Gunakan idLoyalty (bukan id)
        override fun areItemsTheSame(oldItem: LoyaltyItemV2, newItem: LoyaltyItemV2) = oldItem.idLoyalty == newItem.idLoyalty
        override fun areContentsTheSame(oldItem: LoyaltyItemV2, newItem: LoyaltyItemV2) = oldItem == newItem
    }
}