package com.example.ukopia.ui.equipment

import android.view.LayoutInflater
import android.view.View // Pastikan ini diimpor untuk View.GONE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.R
import com.example.ukopia.data.SubEquipmentItem // PASTIKAN INI SubEquipment, BUKAN SubEquipmentItem
import com.example.ukopia.databinding.ItemSubEquipmentBinding

class SubEquipmentAdapter(
    private val subEquipmentList: List<SubEquipmentItem>, // PASTIKAN List<SubEquipment>
    private val onItemClick: (SubEquipmentItem) -> Unit
) : RecyclerView.Adapter<SubEquipmentAdapter.SubEquipmentViewHolder>() {

    inner class SubEquipmentViewHolder(private val binding: ItemSubEquipmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(subEquipment: SubEquipmentItem) { // PASTIKAN subEquipment: SubEquipment
            binding.tvSubEquipmentName.text = subEquipment.name
            binding.tvSubEquipmentDetail.text = subEquipment.detail ?: "" // Show detail if exists
            binding.tvSubEquipmentDetail.visibility = if (subEquipment.detail.isNullOrEmpty()) View.GONE else View.VISIBLE

            subEquipment.iconResId?.let {
                binding.ivSubEquipmentIcon.setImageResource(it)
                binding.ivSubEquipmentIcon.visibility = View.VISIBLE
                binding.ivSubEquipmentIcon.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.black)) // Tambah tinting
            } ?: run {
                binding.ivSubEquipmentIcon.setImageResource(R.drawable.ic_grinder) // Default ikon
                binding.ivSubEquipmentIcon.visibility = View.VISIBLE
                binding.ivSubEquipmentIcon.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.black)) // Tambah tinting
            }

            binding.root.setOnClickListener { onItemClick(subEquipment) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubEquipmentViewHolder {
        val binding = ItemSubEquipmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubEquipmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubEquipmentViewHolder, position: Int) {
        holder.bind(subEquipmentList[position])
    }

    override fun getItemCount(): Int = subEquipmentList.size
}