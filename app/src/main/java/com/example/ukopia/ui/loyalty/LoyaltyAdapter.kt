package com.example.ukopia.adapter

import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.R
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
            binding.textViewNamaMenu.text = item.namaMenu
            binding.textViewTanggal.text = item.tanggal

            if (item.isCoffee) {
                binding.textViewBeans.text = binding.root.context.getString(R.string.coffee_bean_name_prefix) + (item.namaBeans ?: binding.root.context.getString(R.string.not_available_text))
                binding.textViewBeans.visibility = View.VISIBLE

            } else {
                binding.textViewBeans.text = binding.root.context.getString(R.string.non_coffee_name_prefix) + (item.namaNonKopi ?: binding.root.context.getString(R.string.not_available_text))
                binding.textViewBeans.visibility = View.VISIBLE
            }

            // --- Logika Animasi Flash untuk Tombol SAJA ---
            // Tangkap ColorStateList asli dari backgroundTint tombol
            // (Kita ingin latar belakang tombol kembali ke hitam)
            val originalButtonBackgroundTint = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.black))

            // MENGUBAH BARIS INI: originalButtonTextColor diatur menjadi putih secara eksplisit
            val originalButtonTextColor = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.white))

            // Definisikan warna flash (putih untuk background, hitam untuk teks agar kontras)
            val flashBackgroundColor = ContextCompat.getColor(binding.root.context, R.color.white)
            val flashTextColor = ContextCompat.getColor(binding.root.context, R.color.black)

            binding.btnSelengkapnya.setOnClickListener {
                // Terapkan animasi flash pada *hanya tombol*
                binding.btnSelengkapnya.backgroundTintList = ColorStateList.valueOf(flashBackgroundColor)
                binding.btnSelengkapnya.setTextColor(flashTextColor)

                Handler(Looper.getMainLooper()).postDelayed({
                    // Kembalikan warna background tombol ke hitam (sesuai permintaan)
                    binding.btnSelengkapnya.backgroundTintList = originalButtonBackgroundTint
                    // Kembalikan warna teks tombol ke putih (sesuai permintaan)
                    binding.btnSelengkapnya.setTextColor(originalButtonTextColor)
                    // Lanjutkan ke aksi klik yang sebenarnya
                    onItemClick(item)
                }, 150) // Durasi flash: 150 milidetik
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