package com.example.ukopia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.R
import com.example.ukopia.databinding.ItemStampBinding // <-- Sesuaikan jika nama file layout Anda adalah item_stamp_card.xml

// --- Tambahkan data class ini ---
data class StampItem(
    val number: Int,
    val isCollected: Boolean
)
// --- Akhir data class ---

class StampAdapter : ListAdapter<StampItem, StampAdapter.StampViewHolder>(StampDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StampViewHolder {
        // Gunakan ItemStampBinding yang sudah ada di proyek Anda
        // Sesuaikan dengan nama layout file Anda, misal ItemStampCardBinding jika nama layoutnya item_stamp_card.xml
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
                // Jika terisi: Latar belakang hitam, angka hilang, centang muncul (putih)
                binding.ivStampBackground.setBackgroundResource(R.drawable.circle_background_white_stroke_black_fill)
                binding.tvStampNumber.visibility = View.GONE // Sembunyikan angka
                binding.ivStampCheckmark.visibility = View.VISIBLE // Tampilkan centang
            } else {
                // Jika belum terisi: Latar belakang abu-abu, angka muncul (hitam), centang hilang
                binding.ivStampBackground.setBackgroundResource(R.drawable.reward_circle_background_default)
                binding.tvStampNumber.visibility = View.VISIBLE // Tampilkan angka
                binding.tvStampNumber.setTextColor(ContextCompat.getColor(context, R.color.black)) // Angka hitam
                binding.ivStampCheckmark.visibility = View.GONE // Sembunyikan centang
            }
        }
    }

    class StampDiffCallback : DiffUtil.ItemCallback<StampItem>() {
        override fun areItemsTheSame(oldItem: StampItem, newItem: StampItem) = oldItem.number == newItem.number
        override fun areContentsTheSame(oldItem: StampItem, newItem: StampItem) = oldItem == newItem
    }
}