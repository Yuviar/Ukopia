package com.example.ukopia.ui.akun

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.R
import com.example.ukopia.databinding.ItemLanguageOptionBinding
import androidx.core.content.ContextCompat

class LanguageOptionAdapter(
    private val languages: List<LanguageChooserDialogFragment.LanguageItem>,
    private val currentSelectionCode: String,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<LanguageOptionAdapter.LanguageViewHolder>() {

    inner class LanguageViewHolder(private val binding: ItemLanguageOptionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(languageItem: LanguageChooserDialogFragment.LanguageItem, isSelected: Boolean) {
            binding.textViewLanguageName.text = languageItem.name
            binding.root.setOnClickListener { onItemClick(languageItem.code) }

            if (isSelected) {
                binding.textViewLanguageName.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                binding.imageViewSelected.visibility = View.VISIBLE
            } else {
                binding.textViewLanguageName.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                binding.imageViewSelected.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val languageItem = languages[position]
        holder.bind(languageItem, languageItem.code == currentSelectionCode)
    }

    override fun getItemCount(): Int = languages.size
}