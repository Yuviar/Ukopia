package com.example.ukopia.adapter

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.R
import com.example.ukopia.data.BrewMethod
import com.example.ukopia.databinding.ItemRecipeCardBinding
import android.content.res.ColorStateList

class BrewMethodAdapter(
    private val onItemClick: (BrewMethod) -> Unit
) : ListAdapter<BrewMethod, BrewMethodAdapter.BrewMethodViewHolder>(BrewMethodDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrewMethodViewHolder {
        val binding = ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BrewMethodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BrewMethodViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onItemClick)
    }

    class BrewMethodViewHolder(
        private val binding: ItemRecipeCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BrewMethod, onItemClick: (BrewMethod) -> Unit) {
            binding.textViewRecipeName.text = item.name
            binding.imageViewRecipe.setImageResource(item.imageUrl)

            // --- Animasi Flash untuk Tombol 'Selengkapnya' SAJA ---
            // MENGUBAH BARIS INI: originalButtonBackgroundTint diatur menjadi hitam secara eksplisit
            val originalButtonBackgroundTint = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.black))
            // originalButtonTextColor diatur menjadi hitam secara eksplisit
            val originalButtonTextColor = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.black))

            // Definisikan warna flash (putih untuk background, hitam untuk teks agar kontras)
            val flashBackgroundColor = ContextCompat.getColor(binding.root.context, R.color.white)
            val flashTextColor = ContextCompat.getColor(binding.root.context, R.color.black)

            // Hapus listener dari root view agar hanya tombol yang bisa diklik
            binding.root.setOnClickListener(null) // Penting: Menghapus listener dari keseluruhan card

            binding.btnSelengkapnya.setOnClickListener {
                // Terapkan animasi flash pada *hanya tombol*
                binding.btnSelengkapnya.backgroundTintList = ColorStateList.valueOf(flashBackgroundColor)
                binding.btnSelengkapnya.setTextColor(flashTextColor)

                Handler(Looper.getMainLooper()).postDelayed({
                    // Kembalikan warna background tombol ke hitam (sesuai permintaan)
                    binding.btnSelengkapnya.backgroundTintList = originalButtonBackgroundTint
                    // Kembalikan warna teks tombol ke hitam (sesuai permintaan)
                    binding.btnSelengkapnya.setTextColor(originalButtonTextColor)
                    // Lanjutkan ke aksi klik yang sebenarnya
                    onItemClick(item)
                }, 150) // Durasi flash: 150 milidetik
            }
            // --- Akhir Animasi Flash ---
        }
    }

    class BrewMethodDiffCallback : DiffUtil.ItemCallback<BrewMethod>() {
        override fun areItemsTheSame(oldItem: BrewMethod, newItem: BrewMethod): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: BrewMethod, newItem: BrewMethod): Boolean {
            return oldItem == newItem
        }
    }
}