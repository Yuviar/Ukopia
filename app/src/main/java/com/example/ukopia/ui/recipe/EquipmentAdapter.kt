package com.example.ukopia.ui.equipment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.data.EquipmentItem
import com.example.ukopia.databinding.ItemEquipmentBinding

class EquipmentAdapter(
    private var equipmentList: List<EquipmentItem>,
    private val onItemClick: (EquipmentItem) -> Unit // Kirim object EquipmentItem lengkap
) : RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder>() {

    // Fungsi update data dari API
    fun submitList(newList: List<EquipmentItem>) {
        equipmentList = newList
        notifyDataSetChanged()
    }

    inner class EquipmentViewHolder(private val binding: ItemEquipmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(equipment: EquipmentItem) {
            binding.tvEquipmentName.text = equipment.name
            binding.tvEquipmentCount.text = equipment.count.toString()

            // Klik item mengirim ID kategori ke fragment selanjutnya
            binding.root.setOnClickListener { onItemClick(equipment) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentViewHolder {
        val binding = ItemEquipmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EquipmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EquipmentViewHolder, position: Int) {
        holder.bind(equipmentList[position])
    }

    override fun getItemCount(): Int = equipmentList.size
}