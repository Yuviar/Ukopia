package com.example.ukopia.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukopia.R
// <<-- UBAH IMPORT BINDING INI -->>
import com.example.ukopia.databinding.FragmentMenuFilterDialogBinding
import androidx.core.content.ContextCompat

class MenuFilterDialogFragment : DialogFragment() {

    // <<-- UBAH TIPE BINDING INI -->>
    private var _binding: FragmentMenuFilterDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var filterCategories: Array<String>
    private var currentSelection: String? = null
    private var listener: OnCategorySelectedListener? = null

    interface OnCategorySelectedListener {
        fun onCategorySelected(category: String)
    }

    companion object {
        private const val ARG_CATEGORIES = "categories"
        private const val ARG_CURRENT_SELECTION = "current_selection"

        fun newInstance(categories: Array<String>, currentSelection: String): MenuFilterDialogFragment {
            val fragment = MenuFilterDialogFragment()
            val args = Bundle().apply {
                putStringArray(ARG_CATEGORIES, categories)
                putString(ARG_CURRENT_SELECTION, currentSelection)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (targetFragment is OnCategorySelectedListener) {
            listener = targetFragment as OnCategorySelectedListener
        } else {
            throw RuntimeException("$context must implement OnCategorySelectedListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            filterCategories = it.getStringArray(ARG_CATEGORIES) ?: arrayOf()
            currentSelection = it.getString(ARG_CURRENT_SELECTION)
        }
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // <<-- UBAH INFLATE KE LAYOUT YANG BENAR -->>
        _binding = FragmentMenuFilterDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        val adapter = MenuFilterAdapter(filterCategories.toList(), currentSelection) { selectedCategory ->
            listener?.onCategorySelected(selectedCategory)
            dismiss()
        }

        binding.recyclerViewFilterCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFilterCategories.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}