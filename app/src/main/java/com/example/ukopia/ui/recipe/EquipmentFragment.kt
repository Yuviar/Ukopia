package com.example.ukopia.ui.equipment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.databinding.FragmentEquipmentBinding
import com.example.ukopia.ui.recipe.AddRecipeFragment
import com.example.ukopia.ui.recipe.RecipeViewModel

class EquipmentFragment : Fragment() {

    private var _binding: FragmentEquipmentBinding? = null
    private val binding get() = _binding!!

    private val recipeViewModel: RecipeViewModel by activityViewModels()

    private var currentCategoryId: Int = 0
    private var currentCategoryName: String? = null

    companion object {
        const val ARG_CATEGORY_ID = "category_id"
        const val ARG_CATEGORY_NAME = "category_name"

        const val REQUEST_KEY_EQUIPMENT_SELECTION = "request_key_equipment_selection"
        const val BUNDLE_KEY_SELECTED_CATEGORY = "bundle_key_selected_category"
        const val BUNDLE_KEY_SELECTED_SUB_EQUIPMENT_NAME = "bundle_key_selected_sub_equipment_name"
        const val BUNDLE_KEY_EQUIPMENT_ID = "BUNDLE_KEY_EQUIPMENT_ID"
        const val BUNDLE_KEY_EQUIPMENT_IMAGE_URL = "BUNDLE_KEY_EQUIPMENT_IMAGE_URL"

        fun newInstance(catId: Int = 0, catName: String? = null): EquipmentFragment {
            val fragment = EquipmentFragment()
            if (catId > 0) {
                val args = Bundle()
                args.putInt(ARG_CATEGORY_ID, catId)
                args.putString(ARG_CATEGORY_NAME, catName)
                fragment.arguments = args
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentCategoryId = it.getInt(ARG_CATEGORY_ID, 0)
            currentCategoryName = it.getString(ARG_CATEGORY_NAME)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEquipmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).setBottomNavVisibility(View.GONE)

        setupHeader()
        setupListeners()
        setupRecyclerView()

        if (currentCategoryId == 0) {
            recipeViewModel.loadEquipmentCategories()
            recipeViewModel.equipmentCategories.observe(viewLifecycleOwner) { list ->
                (binding.recyclerEquipment.adapter as? EquipmentAdapter)?.submitList(list)
            }
        } else {
            recipeViewModel.loadToolsByCategory(currentCategoryId)
            recipeViewModel.subEquipmentList.observe(viewLifecycleOwner) { list ->
                (binding.recyclerEquipment.adapter as? SubEquipmentAdapter)?.submitList(list)
            }
        }
    }

    private fun setupHeader() {
        binding.tvHeaderTitleEquipment.text = currentCategoryName ?: getString(R.string.my_equipment_title)
    }

    private fun setupListeners() {
        binding.btnBackEquipment.setOnClickListener {
            parentFragmentManager.popBackStack()
            if (currentCategoryId == 0) {
                (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)
            }
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerEquipment.layoutManager = layoutManager
        binding.recyclerEquipment.addItemDecoration(DividerItemDecoration(requireContext(), layoutManager.orientation))

        if (currentCategoryId == 0) {
            binding.recyclerEquipment.adapter = EquipmentAdapter(emptyList()) { category ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, newInstance(category.id, category.name))
                    .addToBackStack(null)
                    .commit()
            }
        } else {
            binding.recyclerEquipment.adapter = SubEquipmentAdapter(emptyList()) { subItem ->
                setFragmentResult(REQUEST_KEY_EQUIPMENT_SELECTION, Bundle().apply {
                    putString(BUNDLE_KEY_SELECTED_CATEGORY, currentCategoryName)
                    putString(BUNDLE_KEY_SELECTED_SUB_EQUIPMENT_NAME, subItem.name)
                    putInt(BUNDLE_KEY_EQUIPMENT_ID, subItem.id)
                    putString(BUNDLE_KEY_EQUIPMENT_IMAGE_URL, subItem.imageUrl)
                })

                parentFragmentManager.popBackStack(AddRecipeFragment.ADD_RECIPE_FLOW_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)

                Toast.makeText(requireContext(), "Selected: ${subItem.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}