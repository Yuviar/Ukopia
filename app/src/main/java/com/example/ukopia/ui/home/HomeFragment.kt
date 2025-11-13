package com.example.ukopia.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // <-- IMPORT BARU
import androidx.lifecycle.Observer // <-- IMPORT BARU
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.R
import com.example.ukopia.BestSellerAdapter
import com.example.ukopia.databinding.FragmentHomeBinding
import com.example.ukopia.MainActivity
import com.example.ukopia.models.MenuApiItem // <-- IMPORT BARU (ganti dari .data.MenuItem)
import com.example.ukopia.ui.menu.DetailMenuFragment
import com.example.ukopia.UkopiaApplication // <-- IMPORT BARU
import com.example.ukopia.ui.menu.MenuViewModel // <-- IMPORT BARU
import com.example.ukopia.ui.menu.MenuViewModelFactory // <-- IMPORT BARU

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Inisialisasi ViewModel dengan Factory
    private val viewModel: MenuViewModel by viewModels {
        MenuViewModelFactory((requireActivity().application as UkopiaApplication).repository)
    }

    private lateinit var bestSellerAdapter: BestSellerAdapter
    // Hapus: allBestSellerItems

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.setBottomNavVisibility(View.VISIBLE)

        // HAPUS: Logika data dummy
        // val allMenuFromApp = createDummyAllMenuItems()
        // allBestSellerItems = getTopBestSellerItems(allMenuFromApp)

        // Setup RecyclerView
        // PENTING: BestSellerAdapter Anda juga harus diperbarui (lihat di bawah)
        bestSellerAdapter = BestSellerAdapter(emptyList()) { menuItem ->
            // 'menuItem' di sini sekarang adalah MenuApiItem
            // Error Anda akan HILANG
            val detailMenuFragment = DetailMenuFragment.newInstance(menuItem)
            (activity as? MainActivity)?.navigateToFragment(detailMenuFragment)
        }

        binding.bestSellerRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.bestSellerRecyclerView.adapter = bestSellerAdapter

        // Panggil Observer untuk memuat data
        setupObservers()
    }

    // Fungsi BARU untuk mengambil data dari ViewModel (Room/API)
    private fun setupObservers() {
        viewModel.menuItems.observe(viewLifecycleOwner, Observer { menuList ->
            // menuList adalah List<MenuApiItem> dari database
            if (menuList != null) {
                // Filter untuk mendapatkan best seller
                val bestSellerItems = getTopBestSellerItems(menuList)
                // Asumsi adapter Anda punya fungsi updateData (lihat di bawah)
                bestSellerAdapter.updateData(bestSellerItems)
            }
        })
    }

    // HAPUS: createDummyAllMenuItems()

    // MODIFIKASI: Fungsi ini sekarang memfilter List<MenuApiItem>
    private fun getTopBestSellerItems(all: List<MenuApiItem>): List<MenuApiItem> {
        // Mengurutkan berdasarkan 'average_rating' (Double)
        return all.sortedByDescending { it.average_rating }
            .take(5) // Ambil 5 item teratas (atau 2 jika Anda mau)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}