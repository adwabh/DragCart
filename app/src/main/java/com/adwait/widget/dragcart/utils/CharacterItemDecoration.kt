package com.adwait.widget.dragcart.utils

import android.graphics.Canvas
import android.graphics.Rect
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View

/**
 * Created by arif on 22/12/17.
 */

class CharacterItemDecoration(private val offset: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

        val layoutParams = view.layoutParams as GridLayoutManager.LayoutParams

//        if (layoutParams.spanIndex % 2 == 0) {
//
//            outRect.top = offset
//            outRect.left = offset
//            outRect.right = offset / 2
//
//        } else {
//
//            outRect.top = offset
//            outRect.right = offset
//            outRect.left = offset / 2
//
//        }

        with(outRect) {
            when(layoutParams.spanIndex){
                0-> {
                    top = offset
                    left = offset
                    right = offset / 2
                }
                layoutParams.spanSize-1->{
                    top = offset
                    left = offset / 2
                    right = offset
                }
                else->{
                    top = offset
                    left = offset / 2
                    right = offset / 2
                }
            }
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        Log.d("Animated", "onDrawOverCalled")
    }
}
