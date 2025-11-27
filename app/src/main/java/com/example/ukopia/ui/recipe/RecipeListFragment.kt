package com.example.ukopia.ui.recipe

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.MainActivity
import com.example.ukopia.R
import com.example.ukopia.SessionManager
import com.example.ukopia.adapter.RecipeAdapter
import com.example.ukopia.data.RecipeItem
import com.example.ukopia.databinding.DialogLoginRequiredBinding
import com.example.ukopia.databinding.FragmentRecipeListBinding
import com.example.ukopia.ui.auth.LoginActivity
import com.google.android.material.button.MaterialButton

class RecipeListFragment : Fragment() {

    private var _binding: FragmentRecipeListBinding? = null
    private val binding get() = _binding!!

    private val recipeViewModel: RecipeViewModel by activityViewModels()
    private lateinit var recipeAdapter: RecipeAdapter

    private var methodId: Int = 0
    private var methodName: String? = null
    private var isMyRecipeActive = false

    private val loginActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadData()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecipeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).setBottomNavVisibility(View.GONE)

        // 1. Ambil Argument
        methodId = arguments?.getInt("ID_METODE") ?: 0
        methodName = arguments?.getString("SELECTED_METHOD_NAME")

        binding.tvHeaderTitle.text = methodName?.uppercase() ?: "RECIPES"

        // 2. Setup RecyclerView
        setupAdapter()

        // 3. Setup Listeners
        setupListeners()

        // 4. Observe Data
        recipeViewModel.allRecipes.observe(viewLifecycleOwner) { recipes ->
            recipeAdapter.submitList(recipes)

            // Bisa tambah logic menampilkan "Data Kosong" jika recipes.isEmpty()
        }

        recipeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // 5. Load Data
        loadData()
    }

    private fun setupAdapter() {
        recipeAdapter = RecipeAdapter { recipe ->
            // Klik item -> Buka Detail Fragment
            navigateToRecipeDetail(recipe)
        }
        binding.recyclerViewRecipeList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recipeAdapter
        }
    }

    private fun loadData() {
        val type = if (isMyRecipeActive) "my" else "all"
        val uid = SessionManager.getUid(requireContext())

        recipeViewModel.loadRecipes(methodId, type, uid)

        updateButtonStyles(
            if (isMyRecipeActive) binding.buttonResepSaya else binding.buttonSemuaResep,
            if (isMyRecipeActive) binding.buttonSemuaResep else binding.buttonResepSaya
        )
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
            (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)
        }

        binding.buttonSemuaResep.setOnClickListener {
            if (isMyRecipeActive) {
                isMyRecipeActive = false
                loadData()
            }
        }

        binding.buttonResepSaya.setOnClickListener {
            if (!isMyRecipeActive) {
                isMyRecipeActive = true
                loadData()
            }
        }

        binding.fabAddRecipe.setOnClickListener {
            if (SessionManager.isLoggedIn(requireContext())) {
                val addFragment = AddRecipeFragment().apply {
                    arguments = Bundle().apply {
                        putInt("ID_METODE", methodId)
                        putString("METHOD_NAME", methodName)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, addFragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                showLoginRequiredDialog()
            }
        }
    }

    private fun navigateToRecipeDetail(recipe: RecipeItem) {
        val detailFragment = RecipeDetailFragment().apply {
            arguments = Bundle().apply {
                putInt("ID_RESEP", recipe.id.toInt())
                putParcelable("RECIPE_ITEM", recipe)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showLoginRequiredDialog() {
        val dialogBinding = DialogLoginRequiredBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.buttonDialogLogin.setOnClickListener {
            loginActivityResultLauncher.launch(Intent(requireContext(), LoginActivity::class.java))
            dialog.dismiss()
        }
        dialogBinding.buttonDialogCancel.setOnClickListener { dialog.dismiss() }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun updateButtonStyles(active: MaterialButton, inactive: MaterialButton) {
        context?.let {
            active.setBackgroundColor(ContextCompat.getColor(it, R.color.black))
            active.setTextColor(ContextCompat.getColor(it, R.color.white))
            inactive.setBackgroundColor(ContextCompat.getColor(it, R.color.white))
            inactive.setTextColor(ContextCompat.getColor(it, R.color.black))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}