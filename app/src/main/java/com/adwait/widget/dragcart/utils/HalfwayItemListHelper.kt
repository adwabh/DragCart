package com.adwait.widget.dragcart.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.support.v4.view.ViewCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import com.adwait.widget.dragcart.R


/**
 * Created by Adwait Abhyankar on 9/25/2019.
 * Pitney-Bowes
 * adwait.abhyankar@pb.com
 */
class HalfwayItemListHelper(private val recyclerView: RecyclerView, var anchor: View) : ItemTouchHelper.Callback() {

    private val recyclerDimen = intArrayOf(0,0)
    private val dimen = intArrayOf(0,0)
    private var cartX: Float = 0f
    private var cartY: Float = 0f
    private val mAddedToCart = mutableListOf<CartViewHolder>()
    private var finalX: Float = 0f
    private var finalY: Float = 0f

    private var globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        recyclerView.getLocationOnScreen(recyclerDimen)
        anchor.getLocationOnScreen(dimen)
        cartX = dimen[0].toFloat() - recyclerDimen[0] + anchor.width/2
        cartY = dimen[1].toFloat() - recyclerDimen[1] + anchor.height/2
    }

    init{
        anchor.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

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
        if (isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun onChildDrawOver(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        viewHolder?.let {
            if (Build.VERSION.SDK_INT >= 23 && isCurrentlyActive) {
                var originalElevation = viewHolder.itemView.getTag(R.id.item_touch_helper_previous_elevation)
                if (originalElevation == null) {
                    originalElevation = ViewCompat.getElevation(viewHolder.itemView)
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
            }// else {
//                prepareAndSetCartInfoFor(viewHolder)
//            }
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        prepareAndSetCartInfoFor(viewHolder)
        dispatchPendingAnimateToCart()
//        super.clearView(recyclerView, viewHolder)
    }

    private fun dispatchPendingAnimateToCart() {
        var holder = mAddedToCart[0]
        recyclerView.adapter?.notifyItemChanged(holder.adapterPosition)
        Log.e("Animate","dispatched")

    }

    private fun prepareAndSetCartInfoFor(viewHolder: RecyclerView.ViewHolder) {
        Log.d("Animated",mAddedToCart.toString())
        if (!mAddedToCart.contains(viewHolder)) {

            if (viewHolder is CartViewHolder) {
                viewHolder.cartInfo = MoveToCartInfo(this.finalX,
                        this.finalY,
                        this.cartX - viewHolder.itemView.left,
                        this.cartY - viewHolder.itemView.top,
                        viewHolder.itemView.left, viewHolder.itemView.top, viewHolder.itemView.width, viewHolder.itemView.height)
                mAddedToCart.add(viewHolder)
                Log.e("Animated",viewHolder.cartInfo.toString())
            }
        }else{
            Log.e("Animated","already added to cart")
        }
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

    fun removeAnimated(oldHolder: CartViewHolder) {
        mAddedToCart.remove(oldHolder)
        super.clearView(recyclerView,oldHolder)
    }
}