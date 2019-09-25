package com.adwait.widget.dragcart.utils

import android.graphics.Canvas
import android.os.Build
import android.support.v4.view.ViewCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import com.adwait.widget.dragcart.R


/**
 * Created by Adwait Abhyankar on 9/25/2019.
 * Pitney-Bowes
 * adwait.abhyankar@pb.com
 */
class HalfwayItemListHelper : ItemTouchHelper.Callback() {

    private val mAddedToCart = mutableListOf<CartViewHolder>()
    private var finalX: Float = 0f
    private var finalY: Float = 0f

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return if (recyclerView.layoutManager is GridLayoutManager) {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            val swipeFlags = 0
            makeMovementFlags(dragFlags, swipeFlags)
        } else {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
            makeMovementFlags(dragFlags, swipeFlags)
        }
    }

    override fun onMove(recyclerView: RecyclerView, source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return source.itemViewType == target.itemViewType
    }

    override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {

    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
//        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        if (Build.VERSION.SDK_INT >= 21 && isCurrentlyActive) {
            val originalElevation = viewHolder.itemView.getTag(R.id.item_touch_helper_previous_elevation)
            if (originalElevation == null) {
                val originalElevation = ViewCompat.getElevation(viewHolder.itemView)
                val newElevation = 1.0f + findMaxElevation(recyclerView, viewHolder.itemView)
                ViewCompat.setElevation(viewHolder.itemView, newElevation)
                viewHolder.itemView.setTag(R.id.item_touch_helper_previous_elevation, originalElevation)
            }
        }

        if (isCurrentlyActive) {
            viewHolder.itemView.translationX = dX
            viewHolder.itemView.translationY = dY
            finalX = dX
            finalY = dY
        } else {
            prepareAndSetCartInfoFor(viewHolder)
        }
    }

    private fun prepareAndSetCartInfoFor(viewHolder: RecyclerView.ViewHolder) {
        mAddedToCart.add(viewHolder as CartViewHolder)

    }

    private fun findMaxElevation(recyclerView: RecyclerView, itemView: View): Float {
        val childCount = recyclerView.childCount
        var max = 0.0f

        for (i in 0 until childCount) {
            val child = recyclerView.getChildAt(i)
            if (child !== itemView) {
                val elevation = ViewCompat.getElevation(child)
                if (elevation > max) {
                    max = elevation
                }
            }
        }

        return max
    }
}