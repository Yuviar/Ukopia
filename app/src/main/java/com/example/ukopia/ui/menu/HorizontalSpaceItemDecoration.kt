package com.example.ukopia.utils

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

        if (position < itemCount - 1) {
            outRect.right = horizontalSpaceWidth
        } else {
            outRect.right = 0
        }

        if (position == 0) {
            outRect.left = startPadding
        } else {
            outRect.left = 0
        }

        if (position == itemCount - 1) {
            outRect.right += endPadding
        }
    }
}