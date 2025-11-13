package com.example.ukopia.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.R
import com.example.ukopia.BestSellerAdapter
import com.example.ukopia.data.MenuItem
import com.example.ukopia.databinding.FragmentHomeBinding
import com.example.ukopia.MainActivity // Import MainActivity
import com.example.ukopia.ui.menu.DetailMenuFragment // Pertahankan ini

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var bestSellerAdapter: BestSellerAdapter
    private var allBestSellerItems: List<MenuItem> = emptyList() // Daftar lengkap item best seller

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tampilkan BottomNavigationView saat HomeFragment ditampilkan
        (activity as? MainActivity)?.setBottomNavVisibility(View.VISIBLE)

        // Inisialisasi daftar semua item menu, lalu pilih yang terlaris (disimulasikan di sini)
        val allMenuFromApp = createDummyAllMenuItems() // Ambil daftar lengkap dari metode simulasi
        allBestSellerItems = getTopBestSellerItems(allMenuFromApp) // Pilih 2 item terlaris

        // Setup RecyclerView untuk Best Seller Menu
        // BEST SELLER ITEM CLICK LOGIC - PERTAHANKAN INI
        bestSellerAdapter = BestSellerAdapter(allBestSellerItems) { menuItem ->
            val detailMenuFragment = DetailMenuFragment.newInstance(menuItem)
            (activity as? MainActivity)?.navigateToFragment(detailMenuFragment)
        }

        binding.bestSellerRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.bestSellerRecyclerView.adapter = bestSellerAdapter

        // Hapus logika search bar dari sini
        // Hapus listener klik untuk ikon settings dan account dari sini (karena ikon sudah dihapus dari layout)
        // Catatan: Navigasi ke AkunFragment sekarang sepenuhnya melalui Bottom Navigation Bar
    }

    // Metode setupSearchBar() dihapus
    /*
    private fun setupSearchBar() {
        // ... logika search bar yang sudah dihapus ...
    }
    */

    // Metode filterBestSellerItems() dihapus
    /*
    private fun filterBestSellerItems(query: String) {
        // ... logika filter yang sudah dihapus ...
    }
    */

    private fun createDummyAllMenuItems(): List<MenuItem> {
        val context = requireContext()
        // Asumsi string resources seperti category_black_coffee didefinisikan di strings.xml
        return listOf(
            MenuItem("1", "Espresso", "4.8/5.0", R.drawable.sample_coffee, "Ekstraksi kopi pekat, dasar dari semua minuman kopi.", context.getString(R.string.category_black_coffee)),
            MenuItem("2", "Long Black", "4.5/5.0", R.drawable.sample_coffee, "Air panas dicampur dengan espresso untuk rasa yang kuat.", context.getString(R.string.category_black_coffee)),
            MenuItem("3", "Red Eye", "4.2/5.0", R.drawable.sample_coffee, "Kopi drip dengan satu shot espresso tambahan.", context.getString(R.string.category_black_coffee)),
            MenuItem("4", "Black Eye", "4.3/5.0", R.drawable.sample_coffee, "Kopi drip dengan dua shot espresso tambahan.", context.getString(R.string.category_black_coffee)),
            MenuItem("5", "Dead Eye", "4.4/5.0", R.drawable.sample_coffee, "Kopi drip dengan tiga shot espresso tambahan.", context.getString(R.string.category_black_coffee)),
            MenuItem("6", "Americano", "4.6/5.0", R.drawable.sample_coffee, "Espresso dengan tambahan air panas.", context.getString(R.string.category_black_coffee)),
            MenuItem("7", "Flat White", "4.7/5.0", R.drawable.sample_coffee, "Espresso dengan susu steamed yang lembut, tanpa foam tebal.", context.getString(R.string.category_white_coffee)),
            MenuItem("8", "Cappuccino", "4.6/5.0", R.drawable.sample_coffee, "Kopi klasik dengan foam susu tebal dan taburan bubuk cokelat.", context.getString(R.string.category_white_coffee)),
            MenuItem("9", "Latte", "4.7/5.0", R.drawable.sample_coffee, "Espresso dengan susu steamed dan lapisan microfoam.", context.getString(R.string.category_white_coffee)),
            MenuItem("10", "Piccolo", "4.4/5.0", R.drawable.sample_coffee, "Mini latte dengan ristretto.", context.getString(R.string.category_white_coffee)),
            MenuItem("11", "Magic", "4.5/5.0", R.drawable.sample_coffee, "Double ristretto dengan susu steamed.", context.getString(R.string.category_white_coffee)),
            MenuItem("12", "Macchiato", "4.3/5.0", R.drawable.sample_coffee, "Espresso dengan sedikit foam susu di atasnya.", context.getString(R.string.category_white_coffee)),
            MenuItem("13", "Matcha", "4.7/5.0", R.drawable.sample_coffee, "Minuman teh hijau Jepang yang disajikan dingin atau panas.", context.getString(R.string.category_non_coffee)),
            MenuItem("14", "Coklat", "4.8/5.0", R.drawable.sample_coffee, "Minuman cokelat kaya rasa, bisa panas atau dingin.", context.getString(R.string.category_non_coffee)),
            MenuItem("15", "Chamomile", "4.5/5.0", R.drawable.sample_coffee, "Teh herbal yang dikenal menenangkan.", context.getString(R.string.category_artisan_tea)),
            MenuItem("16", "Rose", "4.6/5.0", R.drawable.sample_coffee, "Teh mawar dengan aroma harum.", context.getString(R.string.category_artisan_tea)),
            MenuItem("17", "Vanilla", "4.4/5.0", R.drawable.sample_coffee, "Teh dengan sentuhan rasa vanila yang lembut.", context.getString(R.string.category_artisan_tea)),
            MenuItem("18", "Blueberry Pancake", "4.9/5.0", R.drawable.sample_coffee, "Susu dengan perpaduan rasa blueberry dan pancake.", context.getString(R.string.category_flavoured_milk)),
            MenuItem("19", "Strawberry", "4.7/5.0", R.drawable.sample_coffee, "Susu dengan rasa strawberry alami.", context.getString(R.string.category_flavoured_milk)),
            MenuItem("20", "Hazelnut", "4.8/5.0", R.drawable.sample_coffee, "Susu dengan aroma dan rasa hazelnut yang kaya.", context.getString(R.string.category_flavoured_milk)),
            MenuItem("21", "Pistachio", "4.6/5.0", R.drawable.sample_coffee, "Susu dengan rasa pistachio yang unik dan gurih.", context.getString(R.string.category_flavoured_milk)),
            MenuItem("22", "Vanilla", "4.5/5.0", R.drawable.sample_coffee, "Susu dengan rasa vanila klasik.", context.getString(R.string.category_flavoured_milk)),
            MenuItem("23", "Butterscotch", "4.7/5.0", R.drawable.sample_coffee, "Susu dengan rasa butterscotch yang manis dan creamy.", context.getString(R.string.category_flavoured_milk))
        )
    }

    private fun getTopBestSellerItems(all: List<MenuItem>): List<MenuItem> {
        return all.sortedByDescending { it.rating.substringBefore("/").toFloatOrNull() ?: 0f }
            .take(2) // Mengambil tepat 2 item teratas
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}