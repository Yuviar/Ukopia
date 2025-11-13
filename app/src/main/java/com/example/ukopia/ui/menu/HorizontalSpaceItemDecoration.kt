package com.example.ukopia.utils // Anda bisa menempatkan di package lain jika mau

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalSpaceItemDecoration(
    private val horizontalSpaceWidth: Int,
    private val startPadding: Int,
    private val endPadding: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount ?: 0

        // Set margin kanan untuk semua item kecuali yang terakhir
        if (position < itemCount - 1) {
            outRect.right = horizontalSpaceWidth
        } else {
            // Untuk item terakhir, set margin kanan menjadi 0
            // Padding akhir akan ditambahkan oleh RecyclerView itu sendiri
            outRect.right = 0
        }

        // Set padding awal untuk item pertama
        if (position == 0) {
            outRect.left = startPadding
        } else {
            outRect.left = 0
        }

        // Set padding akhir untuk item terakhir
        if (position == itemCount - 1) {
            outRect.right += endPadding // Menambahkan paddingEnd setelah margin kanan (jika ada)
        }
    }
}