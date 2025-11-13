package com.example.ukopia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.R
import com.example.ukopia.databinding.ItemStampBinding

class StampAdapter : RecyclerView.Adapter<StampAdapter.StampViewHolder>() {

    private var totalPoints: Int = 0
    private val totalStamps = 100 // Total stempel yang akan ditampilkan

    fun updatePoints(points: Int) {
        this.totalPoints = points
        notifyDataSetChanged() // Memberi tahu adapter bahwa data berubah
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StampViewHolder {
        val binding = ItemStampBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StampViewHolder(binding)
    }

    override fun getItemCount(): Int = totalStamps

    override fun onBindViewHolder(holder: StampViewHolder, position: Int) {
        val stampNumber = position + 1 // Stempel dimulai dari 1
        holder.bind(stampNumber, stampNumber <= totalPoints)
    }

    class StampViewHolder(private val binding: ItemStampBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(stampNumber: Int, isFilled: Boolean) {
            val context = binding.root.context
            binding.tvStampNumber.text = stampNumber.toString()

            if (isFilled) {
                // Jika terisi: Latar belakang hitam, angka hilang, centang muncul (putih)
                binding.ivStampBackground.background = ContextCompat.getDrawable(context, R.drawable.circle_background_white_stroke_black_fill)
                binding.tvStampNumber.visibility = View.GONE // Sembunyikan angka
                binding.ivStampCheckmark.visibility = View.VISIBLE // Tampilkan centang
            } else {
                // Jika belum terisi: Latar belakang abu-abu, angka muncul (hitam), centang hilang
                binding.ivStampBackground.background = ContextCompat.getDrawable(context, R.drawable.reward_circle_background_default)
                binding.tvStampNumber.visibility = View.VISIBLE // Tampilkan angka
                binding.tvStampNumber.setTextColor(ContextCompat.getColor(context, R.color.black)) // Angka hitam
                binding.ivStampCheckmark.visibility = View.GONE // Sembunyikan centang
            }
        }
    }
}