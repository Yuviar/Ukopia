package com.example.ukopia.ui.recipe

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.ukopia.R
import com.example.ukopia.data.RecipeItem
import com.example.ukopia.databinding.FragmentRecipeListBinding
import com.google.android.material.button.MaterialButton
import com.example.ukopia.MainActivity
import com.example.ukopia.SessionManager
import com.example.ukopia.LoginActivity
import com.example.ukopia.databinding.DialogLoginRequiredBinding
import androidx.appcompat.app.AlertDialog

class RecipeListFragment : Fragment() {

    private var _binding: FragmentRecipeListBinding? = null
    private val binding get() = _binding!!

    private val recipeViewModel: RecipeViewModel by activityViewModels()

    private var selectedMethodName: String? = null
    private var isMyRecipeActive = false

    private var lastViewedAllRecipes: MutableMap<String, RecipeItem> = mutableMapOf()
    private var lastViewedMyRecipes: MutableMap<String, RecipeItem> = mutableMapOf()

    private var currentDisplayedRecipe: RecipeItem? = null

    private var pendingAddRecipeAction = false

    private val loginActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (SessionManager.SessionManager.isLoggedIn(requireContext())) {
                displayRecipesBasedOnFilter()

                if (pendingAddRecipeAction) {
                    pendingAddRecipeAction = false
                    val addFragment = AddRecipeFragment().apply {
                        arguments = Bundle().apply {
                            putString("METHOD_NAME", selectedMethodName)
                        }
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container, addFragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecipeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).setBottomNavVisibility(View.GONE)

        selectedMethodName = arguments?.getString("SELECTED_METHOD_NAME")
        // === Judul header sekarang selalu mendatar, tanpa .replace(" ", "\n") ===
        binding.tvHeaderTitle.text = selectedMethodName?.uppercase() ?: getString(R.string.recipe_list_title)

        setupListeners()

        recipeViewModel.allRecipes.observe(viewLifecycleOwner) {
            val showMyRecipesFromArgs = arguments?.getBoolean("SHOW_MY_RECIPES", false) ?: false
            val specificRecipeTitleFromArgs = arguments?.getString("SPECIFIC_RECIPE_TITLE")

            isMyRecipeActive = showMyRecipesFromArgs

            displayRecipesBasedOnFilter(specificRecipeTitleFromArgs)

            updateButtonStyles(
                if (isMyRecipeActive) binding.buttonResepSaya else binding.buttonSemuaResep,
                if (isMyRecipeActive) binding.buttonSemuaResep else binding.buttonResepSaya
            )

            arguments?.remove("SHOW_MY_RECIPES")
            arguments?.remove("SPECIFIC_RECIPE_TITLE")
        }
    }

    private fun setupListeners() {
        // === Logika Tombol Kembali dengan Flash ===
        binding.btnBack.setOnClickListener {
            val originalTintList = binding.btnBack.imageTintList

            binding.btnBack.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black))

            Handler(Looper.getMainLooper()).postDelayed({
                if (isAdded && activity != null) {
                    binding.btnBack.imageTintList = originalTintList

                    parentFragmentManager.popBackStack()
                    (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)
                }
            }, 150)
        }
        // =========================================

        binding.buttonSemuaResep.setOnClickListener {
            selectedMethodName?.let { method ->
                if (isMyRecipeActive) {
                    currentDisplayedRecipe?.let { lastViewedMyRecipes[method] = it }
                } else {
                    currentDisplayedRecipe?.let { lastViewedAllRecipes[method] = it }
                }
            }

            isMyRecipeActive = false
            displayRecipesBasedOnFilter()
            updateButtonStyles(binding.buttonSemuaResep, binding.buttonResepSaya)
        }

        binding.buttonResepSaya.setOnClickListener {
            selectedMethodName?.let { method ->
                if (isMyRecipeActive) {
                    currentDisplayedRecipe?.let { lastViewedMyRecipes[method] = it }
                } else {
                    currentDisplayedRecipe?.let { lastViewedAllRecipes[method] = it }
                }
            }

            isMyRecipeActive = true
            displayRecipesBasedOnFilter()
            updateButtonStyles(binding.buttonResepSaya, binding.buttonSemuaResep)
        }

        binding.fabAddRecipe.setOnClickListener {
            // --- Animasi Flash Putih ---
            // Tangkap ColorStateList asli dari FAB
            val originalBackgroundTint = binding.fabAddRecipe.backgroundTintList
            val originalImageTint = binding.fabAddRecipe.imageTintList

            // Definisikan warna flash (putih untuk background, hitam untuk ikon agar kontras)
            val flashColorBackground = ContextCompat.getColor(requireContext(), R.color.white)
            val flashColorImage = ContextCompat.getColor(requireContext(), R.color.black)

            binding.fabAddRecipe.backgroundTintList = ColorStateList.valueOf(flashColorBackground)
            binding.fabAddRecipe.imageTintList = ColorStateList.valueOf(flashColorImage)

            Handler(Looper.getMainLooper()).postDelayed({
                // Pastikan fragment masih melekat sebelum memperbarui UI
                if (isAdded && activity != null && _binding != null) {
                    // Kembalikan ke tint asli yang ditangkap
                    binding.fabAddRecipe.backgroundTintList = originalBackgroundTint
                    binding.fabAddRecipe.imageTintList = originalImageTint
                }
            }, 150) // Durasi flash: 150 milidetik

            // --- Logika Asli Klik ---
            if (SessionManager.SessionManager.isLoggedIn(requireContext())) {
                val addFragment = AddRecipeFragment().apply {
                    arguments = Bundle().apply {
                        putString("METHOD_NAME", selectedMethodName)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, addFragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                pendingAddRecipeAction = true
                showLoginRequiredDialog()
            }
        }

        // --- Logika Animasi Flash untuk Card Resep saat diklik ---
        binding.cardViewRecipeInfoContainer.setOnClickListener { // Menggunakan cardViewRecipeInfoContainer sebagai clickable area utama
            currentDisplayedRecipe?.let { recipe ->
                val originalCardBackgroundColor = ContextCompat.getColor(binding.root.context, R.color.white)
                val flashColorCard = ContextCompat.getColor(binding.root.context, R.color.secondary) // Menggunakan secondary sebagai warna flash (abu-abu terang)

                binding.cardViewRecipeInfoContainer.setCardBackgroundColor(flashColorCard)

                Handler(Looper.getMainLooper()).postDelayed({
                    if (isAdded && activity != null && _binding != null) {
                        binding.cardViewRecipeInfoContainer.setCardBackgroundColor(originalCardBackgroundColor)
                        navigateToRecipeDetail(recipe)
                    }
                }, 150) // Durasi flash: 150 milidetik
            }
        }
        // --- Akhir Logika Animasi Flash untuk Card Resep ---
    }

    private fun showLoginRequiredDialog() {
        val dialogBinding = DialogLoginRequiredBinding.inflate(layoutInflater)
        val customAlertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.buttonDialogLogin.setOnClickListener {
            loginActivityResultLauncher.launch(Intent(requireContext(), LoginActivity::class.java))
            customAlertDialog.dismiss()
        }

        dialogBinding.buttonDialogCancel.setOnClickListener {
            customAlertDialog.dismiss()
        }

        customAlertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        customAlertDialog.show()
    }

    private fun displayRecipesBasedOnFilter(specificRecipeTitle: String? = null) {
        val allRecipesForMethod = recipeViewModel.allRecipes.value
            ?.filter { it.method.equals(selectedMethodName, ignoreCase = true) }
            ?.sortedBy { it.name }
            ?: emptyList()

        val recipesForCurrentFilter: List<RecipeItem> = if (isMyRecipeActive) {
            allRecipesForMethod.filter { it.isMine }
        } else {
            allRecipesForMethod.filter { !it.isMine }
        }

        var recipeToDisplay: RecipeItem? = null

        if (specificRecipeTitle != null) {
            recipeToDisplay = recipesForCurrentFilter.firstOrNull { it.name.equals(specificRecipeTitle, ignoreCase = true) }
        }

        if (recipeToDisplay == null && selectedMethodName != null) {
            recipeToDisplay = if (isMyRecipeActive) {
                lastViewedMyRecipes[selectedMethodName]?.takeIf { recipesForCurrentFilter.contains(it) }
            } else {
                lastViewedAllRecipes[selectedMethodName]?.takeIf { recipesForCurrentFilter.contains(it) }
            }
        }

        if (recipeToDisplay == null && currentDisplayedRecipe != null && recipesForCurrentFilter.any { it.name == currentDisplayedRecipe!!.name }) {
            recipeToDisplay = currentDisplayedRecipe
        }


        if (recipeToDisplay == null) {
            recipeToDisplay = recipesForCurrentFilter.firstOrNull()
        }

        currentDisplayedRecipe = recipeToDisplay
        updateHeaderAndRecipeDetails(currentDisplayedRecipe)
    }


    private fun updateHeaderAndRecipeDetails(recipe: RecipeItem?) {
        recipe?.let {
            binding.tvRecipeTitle.text = it.name
            binding.tvRecipeDescription.text = it.description
            binding.tvWaterAmount.text = it.waterAmount
            binding.tvCoffeeAmount.text = it.coffeeAmount
            binding.tvTime.text = it.extractionTime // Mengganti it.time menjadi it.extractionTime

            binding.tvRecipeDescription.visibility = View.VISIBLE
            binding.layoutWaterAmount.visibility = View.VISIBLE
            binding.layoutCoffeeAmount.visibility = View.VISIBLE
            binding.layoutTimer.visibility = View.VISIBLE

        } ?: run {
            if (isMyRecipeActive) {
                binding.tvRecipeTitle.text = getString(R.string.no_my_recipes_added_yet)
                binding.tvRecipeDescription.visibility = View.GONE
                binding.layoutWaterAmount.visibility = View.GONE
                binding.layoutCoffeeAmount.visibility = View.GONE
                binding.layoutTimer.visibility = View.GONE
            } else {
                binding.tvRecipeTitle.text = getString(R.string.no_recipes_available)
                binding.tvRecipeDescription.text = getString(R.string.add_new_recipes_prompt)
                binding.tvRecipeDescription.visibility = View.VISIBLE

                binding.tvWaterAmount.text = "-";
                binding.tvCoffeeAmount.text = "-";
                binding.tvTime.text = "-";
                binding.layoutWaterAmount.visibility = View.GONE
                binding.layoutCoffeeAmount.visibility = View.GONE
                binding.layoutTimer.visibility = View.GONE
            }
        }
    }

    private fun navigateToRecipeDetail(recipe: RecipeItem) {
        val detailFragment = RecipeDetailFragment().apply {
            arguments = Bundle().apply {
                putParcelable("RECIPE_ITEM", recipe)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun updateButtonStyles(activeButton: MaterialButton, inactiveButton: MaterialButton) {
        context?.let {
            val black = ContextCompat.getColor(it, R.color.black)
            val white = ContextCompat.getColor(it, R.color.white)
            activeButton.setBackgroundColor(black)
            activeButton.setTextColor(white)
            inactiveButton.setBackgroundColor(white)
            inactiveButton.setTextColor(black)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}