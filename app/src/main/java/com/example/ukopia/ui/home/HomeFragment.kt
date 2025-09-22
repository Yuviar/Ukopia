package com.example.ukopia.ui.home

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.add
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.R
import com.example.ukopia.data.MenuItem
class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var menuAdapter: MenuAdapter
    private val menuItems= mutableListOf<MenuItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = view.findViewById(R.id.rv_menu_items)
        addSampleData()
        menuAdapter = MenuAdapter(menuItems)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = menuAdapter
        return view
    }

    private fun addSampleData() {
        // Contoh data dummy. Ganti R.drawable.sample dengan gambar Anda yang sebenarnya.
        // Jika Anda belum punya gambar, buat placeholder dengan Vector Asset atau copy gambar ke res/drawable
        // (misal, buat file sample.xml di res/drawable atau gunakan ikon sementara)

        val defaultDescription = "Kopi spesial dengan paduan susu segar dan foam lembut. Cocok dinikmati kapan saja."
        menuItems.add(MenuItem("1", "Coffee Latte", "4,8/5,0", R.drawable.sample_coffee, defaultDescription))
        menuItems.add(MenuItem("2", "Cappuccino", "4,5/5,0", R.drawable.sample_coffee, defaultDescription))
        menuItems.add(MenuItem("3", "Espresso", "4,9/5,0", R.drawable.sample_coffee, defaultDescription))
        menuItems.add(MenuItem("4", "Americano", "4,2/5,0", R.drawable.sample_coffee, defaultDescription))
        menuItems.add(MenuItem("5", "Machiato", "4,7/5,0", R.drawable.sample_coffee, defaultDescription))
        menuItems.add(MenuItem("6", "Cold Brew", "4,6/5,0", R.drawable.sample_coffee, defaultDescription))
    }
}