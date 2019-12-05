package com.adwait.widget.dragcart.utils

import android.graphics.Canvas
import android.graphics.Rect
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.View

/**
 * Created by arif on 22/12/17.
 */

class CharacterItemDecoration(private val offset: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

        view.layoutParams as RecyclerView.LayoutParams

        with(outRect) { set(offset,offset,offset,offset) }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        Log.d("Animated", "onDrawOverCalled")
    }
}
