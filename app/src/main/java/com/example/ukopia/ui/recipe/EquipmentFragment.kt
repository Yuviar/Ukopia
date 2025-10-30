package com.example.ukopia.ui.equipment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.data.EquipmentItem
import com.example.ukopia.data.SubEquipmentItem // PASTIKAN INI SubEquipment, BUKAN SubEquipmentItem
import com.example.ukopia.databinding.FragmentEquipmentBinding
import com.example.ukopia.ui.recipe.AddRecipeFragment

class EquipmentFragment : Fragment() {

    private var _binding: FragmentEquipmentBinding? = null
    private val binding get() = _binding!!

    private var currentCategoryName: String? = null

    companion object {
        const val ARG_CATEGORY_NAME = "category_name"
        const val REQUEST_KEY_EQUIPMENT_SELECTION = "request_key_equipment_selection"
        const val BUNDLE_KEY_SELECTED_CATEGORY = "bundle_key_selected_category"
        const val BUNDLE_KEY_SELECTED_SUB_EQUIPMENT_NAME = "bundle_key_selected_sub_equipment_name"
        const val BUNDLE_KEY_SELECTED_SUB_EQUIPMENT_ICON = "bundle_key_selected_sub_equipment_icon"

        fun newInstance(categoryName: String? = null): EquipmentFragment {
            val fragment = EquipmentFragment()
            categoryName?.let {
                val args = Bundle()
                args.putString(ARG_CATEGORY_NAME, it)
                fragment.arguments = args
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentCategoryName = arguments?.getString(ARG_CATEGORY_NAME)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEquipmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).setBottomNavVisibility(View.GONE)

        setupHeader()
        setupListeners()
        setupRecyclerView()
    }

    private fun setupHeader() {
        if (currentCategoryName.isNullOrEmpty()) {
            binding.tvHeaderTitleEquipment.text = getString(R.string.my_equipment_title)
        } else {
            binding.tvHeaderTitleEquipment.text = currentCategoryName
        }
    }

    private fun setupListeners() {
        binding.btnBackEquipment.setOnClickListener {
            parentFragmentManager.popBackStack()
            if (currentCategoryName.isNullOrEmpty()) {
                (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)
            }
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerEquipment.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(requireContext(), layoutManager.orientation)

        if (binding.recyclerEquipment.itemDecorationCount > 0) {
            binding.recyclerEquipment.removeItemDecorationAt(0)
        }
        binding.recyclerEquipment.addItemDecoration(dividerItemDecoration)


        if (currentCategoryName.isNullOrEmpty()) {
            setupMainCategories()
        } else {
            setupSubEquipmentList(currentCategoryName!!)
        }
    }

    private fun setupMainCategories() {
        val mainCategories = listOf(
            EquipmentItem(getString(R.string.equipment_category_grinder), getSubEquipmentCount(getString(R.string.equipment_category_grinder))),
            EquipmentItem(getString(R.string.equipment_category_espresso_machine), getSubEquipmentCount(getString(R.string.equipment_category_espresso_machine))),
            EquipmentItem(getString(R.string.equipment_category_portafilter_handle), getSubEquipmentCount(getString(R.string.equipment_category_portafilter_handle))),
            EquipmentItem(getString(R.string.equipment_category_filter), getSubEquipmentCount(getString(R.string.equipment_category_filter))),
            EquipmentItem(getString(R.string.equipment_category_scale), getSubEquipmentCount(getString(R.string.equipment_category_scale))),
            EquipmentItem(getString(R.string.equipment_category_kettle), getSubEquipmentCount(getString(R.string.equipment_category_kettle)))
        )

        binding.recyclerEquipment.adapter = EquipmentAdapter(mainCategories) { categoryName ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, newInstance(categoryName))
                .addToBackStack(categoryName)
                .commit()
        }
    }

    private fun setupSubEquipmentList(categoryName: String) {
        val subEquipmentItems = getSubEquipmentData(categoryName)

        // Pastikan SubEquipmentAdapter menerima List<SubEquipment>
        binding.recyclerEquipment.adapter = SubEquipmentAdapter(
            subEquipmentItems,
            onItemClick = { subEquipment ->
                onSubEquipmentSelected(subEquipment)
            }
        )
        if (binding.recyclerEquipment.itemDecorationCount > 0) {
            binding.recyclerEquipment.removeItemDecorationAt(0)
        }
    }

    private fun getSubEquipmentData(category: String): List<SubEquipmentItem> { // PASTIKAN return type adalah List<SubEquipment>
        return when (category) {
            getString(R.string.equipment_category_grinder) -> listOf(
                SubEquipmentItem("g1", category, "Comandante", "26 clicks", R.drawable.ic_grinder),
                SubEquipmentItem("g2", category, "Comandante", "32 clicks", R.drawable.ic_grinder),
                SubEquipmentItem("g3", category, "Hario", null, R.drawable.ic_grinder),
                SubEquipmentItem("g4", category, "Porlex", null, R.drawable.ic_grinder),
                SubEquipmentItem("g5", category, "Timemore", null, R.drawable.ic_grinder),
                SubEquipmentItem("g6", category, "Grinder Manual", null, R.drawable.ic_grinder),
                SubEquipmentItem("g7", category, "Baratza", null, R.drawable.ic_grinder),
                SubEquipmentItem("g8", category, "Fellow", null, R.drawable.ic_grinder),
                SubEquipmentItem("g9", category, "Mahlkonig", "8.5", R.drawable.ic_grinder),
                SubEquipmentItem("g10", category, "Simonelli", null, R.drawable.ic_grinder),
                SubEquipmentItem("g11", category, "Grinder Automatic", null, R.drawable.ic_grinder)
            )
            getString(R.string.equipment_category_espresso_machine) -> listOf(
                SubEquipmentItem("em1", category, "Breville", null, R.drawable.ic_espresso_machine),
                SubEquipmentItem("em2", category, "La Marzocco", null, R.drawable.ic_espresso_machine),
                SubEquipmentItem("em3", category, "La Pavoni", null, R.drawable.ic_espresso_machine),
                SubEquipmentItem("em4", category, "Sage", null, R.drawable.ic_espresso_machine),
                SubEquipmentItem("em5", category, "Simonelli", null, R.drawable.ic_espresso_machine),
                SubEquipmentItem("em6", category, "Espresso machine", "Lever", R.drawable.ic_espresso_machine),
                SubEquipmentItem("em7", category, "Espresso machine", "Automatic", R.drawable.ic_espresso_machine)
            )
            getString(R.string.equipment_category_portafilter_handle) -> listOf(
                SubEquipmentItem("ph1", category, "Portafilter handle", "Single", R.drawable.ic_portafilter_handle),
                SubEquipmentItem("ph2", category, "Portafilter handle", "Double", R.drawable.ic_portafilter_handle),
                SubEquipmentItem("ph3", category, "Portafilter handle", "Triple", R.drawable.ic_portafilter_handle),
                SubEquipmentItem("ph4", category, "Portafilter handle", "Naked", R.drawable.ic_portafilter_handle)
            )
            getString(R.string.equipment_category_filter) -> listOf(
                SubEquipmentItem("f1", category, "Hario V60", "Paper", R.drawable.ic_filter),
                SubEquipmentItem("f2", category, "Hario V60", "Unbleached Paper", R.drawable.ic_filter),
                SubEquipmentItem("f3", category, "Kalita Wave", "Paper", R.drawable.ic_filter),
                SubEquipmentItem("f4", category, "Aeropress", "Paper", R.drawable.ic_filter),
                SubEquipmentItem("f5", category, "Aeropress", "Metal", R.drawable.ic_filter),
                SubEquipmentItem("f6", category, "Chemex", "Paper", R.drawable.ic_filter),
                SubEquipmentItem("f7", category, "Origami", "Paper", R.drawable.ic_filter),
                SubEquipmentItem("f8", category, "Tricolate", "Paper", R.drawable.ic_filter),
                SubEquipmentItem("f9", category, "Woodneck", "Cloth", R.drawable.ic_filter),
                SubEquipmentItem("f10", category, "Filter", "Cone Paper", R.drawable.ic_filter),
                SubEquipmentItem("f11", category, "Filter", "Flat Cone Paper", R.drawable.ic_filter),
                SubEquipmentItem("f12", category, "Filter", "Wavy Basket Paper", R.drawable.ic_filter)
            )
            getString(R.string.equipment_category_scale) -> listOf(
                SubEquipmentItem("s1", category, "Acaia Lunar", null, R.drawable.ic_scale),
                SubEquipmentItem("s2", category, "Brewista", null, R.drawable.ic_scale),
                SubEquipmentItem("s3", category, "Hario", null, R.drawable.ic_scale),
                SubEquipmentItem("s4", category, "Jennings", null, R.drawable.ic_scale),
                SubEquipmentItem("s5", category, "Timemore", null, R.drawable.ic_scale),
                SubEquipmentItem("s6", category, "Scale", null, R.drawable.ic_scale)
            )
            getString(R.string.equipment_category_kettle) -> listOf(
                SubEquipmentItem("k1", category, "Brewista Artisan", null, R.drawable.ic_kettle),
                SubEquipmentItem("k2", category, "Fellow", null, R.drawable.ic_kettle),
                SubEquipmentItem("k3", category, "Hario", null, R.drawable.ic_kettle),
                SubEquipmentItem("k4", category, "Timemore", null, R.drawable.ic_kettle),
                SubEquipmentItem("k5", category, "Kettle", "Gooseneck", R.drawable.ic_kettle),
                SubEquipmentItem("k6", category, "Kettle", "Classic", R.drawable.ic_kettle)
            )
            else -> emptyList()
        }
    }

    private fun getSubEquipmentCount(category: String): Int {
        return getSubEquipmentData(category).size
    }

    private fun onSubEquipmentSelected(subEquipment: SubEquipmentItem) { // PASTIKAN parameter type adalah SubEquipment
        val selectedText = if (subEquipment.detail.isNullOrEmpty()) {
            subEquipment.name
        } else {
            "${subEquipment.name} (${subEquipment.detail})" // Menggunakan format (detail)
        }
        Toast.makeText(requireContext(), getString(R.string.equipment_selected_toast_format, selectedText), Toast.LENGTH_SHORT).show()

        setFragmentResult(
            REQUEST_KEY_EQUIPMENT_SELECTION,
            Bundle().apply {
                putString(BUNDLE_KEY_SELECTED_CATEGORY, subEquipment.category)
                putString(BUNDLE_KEY_SELECTED_SUB_EQUIPMENT_NAME, selectedText)
                subEquipment.iconResId?.let { // Pastikan iconResId bukan null
                    putInt(BUNDLE_KEY_SELECTED_SUB_EQUIPMENT_ICON, it)
                } ?: run {
                    putInt(BUNDLE_KEY_SELECTED_SUB_EQUIPMENT_ICON, R.drawable.ic_grinder) // Default ikon jika null
                }
            }
        )
        parentFragmentManager.popBackStack(AddRecipeFragment.ADD_RECIPE_FLOW_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}