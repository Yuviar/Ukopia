package com.example.ukopia.ui.recipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.R
import com.example.ukopia.data.SubEquipmentItem // Menggunakan model data SubEquipment

class SelectedEquipmentAdapter(
    private val selectedEquipmentList: MutableList<SubEquipmentItem>,
    private val onDeleteClick: (SubEquipmentItem) -> Unit
) : RecyclerView.Adapter<SelectedEquipmentAdapter.SelectedEquipmentViewHolder>() {

    inner class SelectedEquipmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.iv_selected_equipment_icon)
        val name: TextView = itemView.findViewById(R.id.tv_selected_equipment_name)
        val deleteButton: ImageView = itemView.findViewById(R.id.btn_delete_selected_equipment)

        init {
            deleteButton.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onDeleteClick(selectedEquipmentList[adapterPosition])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedEquipmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_selected_equipment_recipe, parent, false)
        return SelectedEquipmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedEquipmentViewHolder, position: Int) {
        val equipment = selectedEquipmentList[position]
        holder.name.text = equipment.name

        equipment.iconResId?.let {
            holder.icon.setImageResource(it)
            holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.black))
        } ?: run {
            holder.icon.setImageResource(R.drawable.ic_grinder) // Pastikan ikon default ada
            holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.black))
        }
    }

    override fun getItemCount(): Int = selectedEquipmentList.size

    fun removeItem(item: SubEquipmentItem) {
        val index = selectedEquipmentList.indexOf(item)
        if (index != -1) {
            selectedEquipmentList.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}