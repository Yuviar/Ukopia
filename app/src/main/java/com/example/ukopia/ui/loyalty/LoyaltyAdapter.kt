package com.example.ukopia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.R // Pastikan import R
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
        val item = getItem(position)
        holder.bind(item, onItemClick)
    }

    class LoyaltyViewHolder(private val binding: ItemLoyaltyCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LoyaltyItemV2, onItemClick: (LoyaltyItemV2) -> Unit) {
            // Selalu tampilkan nama menu dan tanggal dengan benar
            binding.textViewNamaMenu.text = item.namaMenu
            binding.textViewTanggal.text = item.tanggal

            if (item.isCoffee) {
                // Tampilkan info beans untuk item kopi
                // Menggunakan string resource untuk prefix
                binding.textViewBeans.text = binding.root.context.getString(R.string.coffee_bean_name_prefix) + (item.namaBeans ?: binding.root.context.getString(R.string.not_available_text))
                binding.textViewBeans.visibility = View.VISIBLE

            } else {
                // Sembunyikan dan sesuaikan teks untuk item non-kopi
                // Menggunakan string resource untuk prefix
                binding.textViewBeans.text = binding.root.context.getString(R.string.non_coffee_name_prefix) + (item.namaNonKopi ?: binding.root.context.getString(R.string.not_available_text))
                binding.textViewBeans.visibility = View.VISIBLE
            }

            // Teks tombol "More" sudah di XML, tapi jika perlu diset dari kode:
            // binding.btnSelengkapnya.text = binding.root.context.getString(R.string.more_button_text)

            binding.btnSelengkapnya.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    class LoyaltyItemDiffCallback : DiffUtil.ItemCallback<LoyaltyItemV2>() {
        override fun areItemsTheSame(oldItem: LoyaltyItemV2, newItem: LoyaltyItemV2): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: LoyaltyItemV2, newItem: LoyaltyItemV2): Boolean {
            return oldItem == newItem
        }
    }
}