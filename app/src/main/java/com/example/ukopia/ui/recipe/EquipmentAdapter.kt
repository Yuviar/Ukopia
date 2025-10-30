// D:/github_rama/Ukopia/app/src/main/java/com/example/ukopia/ui/equipment/EquipmentAdapter.kt
package com.example.ukopia.ui.equipment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.R
import com.example.ukopia.data.EquipmentItem
import com.example.ukopia.databinding.ItemEquipmentBinding

class EquipmentAdapter(
    private val equipmentList: List<EquipmentItem>,
    private val onItemClick: (String) -> Unit // Mengubah tipe parameter menjadi String (nama kategori)
) : RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder>() {

    inner class EquipmentViewHolder(private val binding: ItemEquipmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(equipment: EquipmentItem) {
            binding.tvEquipmentName.text = equipment.name
            binding.tvEquipmentCount.text = equipment.count.toString()
            binding.root.setOnClickListener { onItemClick(equipment.name) } // Melewatkan nama kategori
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