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

class RecipeDetailEquipmentAdapter(private val equipmentList: List<SubEquipmentItem>) :
    RecyclerView.Adapter<RecipeDetailEquipmentAdapter.EquipmentViewHolder>() {

    inner class EquipmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.iv_equipment_icon) // PASTIKAN ID INI SESUAI DENGAN item_recipe_detail_equipment.xml
        val name: TextView = itemView.findViewById(R.id.tv_equipment_name) // PASTIKAN ID INI SESUAI DENGAN item_recipe_detail_equipment.xml
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe_detail_equipment, parent, false)
        return EquipmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: EquipmentViewHolder, position: Int) {
        val equipment = equipmentList[position]
        holder.name.text = equipment.name

        equipment.iconResId?.let {
            holder.icon.setImageResource(it)
            holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.black))
        } ?: run {
            holder.icon.setImageResource(R.drawable.ic_grinder) // Pastikan ikon default ada
            holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.black))
        }
    }

    override fun getItemCount(): Int = equipmentList.size
}