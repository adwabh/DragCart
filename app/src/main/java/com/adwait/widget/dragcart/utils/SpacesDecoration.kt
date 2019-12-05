package com.adwait.widget.dragcart.utils

import android.R.attr.top
import android.R.attr.bottom
import android.R.attr.right
import android.R.attr.left
import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View


/**
 * Created by Adwait Abhyankar on 10/28/2019.
 * avabhyankar22@gmail.com
 */

class SpacesDecoration(private val space: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View,
                                parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = space
        outRect.right = space
//        outRect.bottom = space

        // Add top margin only for the first item to avoid double space between items
//        if (parent.getChildLayoutPosition(view) == 0) {
//            outRect.top = space
//        } else {
//            outRect.top = 0
//        }
    }
}
