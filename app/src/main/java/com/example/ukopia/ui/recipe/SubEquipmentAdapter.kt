package com.example.ukopia.ui.equipment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.ukopia.R
import com.example.ukopia.data.SubEquipmentItem
import com.example.ukopia.databinding.ItemSubEquipmentBinding

class SubEquipmentAdapter(
    private var subEquipmentList: List<SubEquipmentItem>,
    private val onItemClick: (SubEquipmentItem) -> Unit
) : RecyclerView.Adapter<SubEquipmentAdapter.SubEquipmentViewHolder>() {

    fun submitList(newList: List<SubEquipmentItem>) {
        subEquipmentList = newList
        notifyDataSetChanged()
    }

    inner class SubEquipmentViewHolder(private val binding: ItemSubEquipmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SubEquipmentItem) {
            binding.tvSubEquipmentName.text = item.name
            binding.tvSubEquipmentDetail.text = item.detail ?: ""

            binding.ivSubEquipmentIcon.load(item.imageUrl) {
                crossfade(true)
                error(R.drawable.ic_error)
                placeholder(R.drawable.ic_grinder)
            }

            binding.root.setOnClickListener { onItemClick(item) }
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