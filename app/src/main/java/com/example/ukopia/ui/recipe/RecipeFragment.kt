package com.example.ukopia.ui.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.R
import com.example.ukopia.adapter.RecipeAdapter
import com.example.ukopia.data.RecipeItem

class RecipeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Menggunakan R.layout.fragment_recipe untuk mengembang layout
        return inflater.inflate(R.layout.fragment_resep, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Contoh data resep
        val recipeItems = listOf(
            RecipeItem("AEROPRESS", "url_gambar_aeropress"),
            RecipeItem("DELTER PRESS", "url_gambar_delter_press"),
            RecipeItem("FRENCH PRESS", "url_gambar_french_press"),
            RecipeItem("CHERMEX", "url_gambar_chermax")
        )

        // Menggunakan findViewById untuk mendapatkan referensi RecyclerView
        val recyclerViewRecipe = view.findViewById<RecyclerView>(R.id.recyclerViewRecipe)

        // Memastikan recyclerViewRecipe tidak null sebelum digunakan
        recyclerViewRecipe?.let {
            // Mengatur adapter dan layout manager
            val adapter = RecipeAdapter(recipeItems)
            it.layoutManager = LinearLayoutManager(requireContext())
            it.adapter = adapter
        }
    }
}
