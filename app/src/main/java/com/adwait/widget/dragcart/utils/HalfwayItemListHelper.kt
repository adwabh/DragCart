package com.adwait.widget.dragcart.utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Canvas
import android.os.Build
import android.support.v4.view.ViewCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import com.adwait.widget.dragcart.R
import kotlinx.android.extensions.LayoutContainer
import kotlin.math.hypot


/**
 * Created by Adwait Abhyankar on 9/25/2019.
 * Pitney-Bowes
 * adwait.abhyankar@pb.com
 */
class HalfwayItemListHelper(private val recyclerView: RecyclerView, var anchor: View, private val callback: () -> Unit,private val containerView:ViewGroup) : ItemTouchHelper.Callback() {

    private var toCart: Boolean = false
    private val DRAG_THREASHHOLD: Long = 300
    private var count: Int = 0
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
        /*val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        val swipeFlags = 0
        return makeMovementFlags(dragFlags, swipeFlags)*/
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
            }else {
                if (!toCart) {
                    toCart = if(Math.hypot(dX.toDouble(),dY.toDouble())/Math.hypot(viewHolder.itemView.width.toDouble(),viewHolder.itemView.height.toDouble())<=1)  {
                        viewHolder.itemView.translationX = dX
                        viewHolder.itemView.translationY = dY
                        false
                    }else{
                        true
                    }
                }
            }
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        if (toCart) {
            prepareAndSetCartInfoFor(viewHolder)
            dispatchPendingAnimateToCart()
        }else {
            super.clearView(recyclerView, viewHolder)
        }
    }

    private fun dispatchPendingAnimateToCart() {
        var holder = mAddedToCart[0]
        recyclerView.adapter?.notifyItemChanged(holder.adapterPosition)
        Log.e("Animate","dispatched")

    }

    //TODO:outsource
    private fun prepareAndSetCartInfoFor(viewHolder: RecyclerView.ViewHolder) {
        Log.d("Animated",mAddedToCart.toString())
        if (!mAddedToCart.contains(viewHolder)) {
            if (viewHolder is CartViewHolder) {
                viewHolder.let {
                    it.cartInfo = MoveToCartInfo(this.finalX,
                            this.finalY,
                            this.cartX - it.itemView.left - it.itemView.width/2,
                            this.cartY - it.itemView.top - it.itemView.height/2,
                            it.itemView.left, it.itemView.top, it.itemView.width, it.itemView.height)
                    mAddedToCart.add(it)
                    Log.e("Animated",it.cartInfo.toString())
                }
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


    //TODO:outsource
    fun animateCart(oldHolder: CartViewHolder) {
        val ratio = 0.5f * (oldHolder.itemView.let { hypot(anchor.width.toFloat(), anchor.height.toFloat()) / hypot(it.width.toFloat(), it.height.toFloat()) })
        val scaleXHolder = PropertyValuesHolder.ofFloat("scaleX", 1.0f, ratio)
        val scaleYHolder = PropertyValuesHolder.ofFloat("scaleY", 1.0f, ratio)
        ObjectAnimator.ofPropertyValuesHolder(oldHolder.itemView,scaleXHolder,scaleYHolder).apply {
            duration = 300
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {

                }

                override fun onAnimationEnd(p0: Animator?) {
                    animateCartCount(oldHolder)
                }

                override fun onAnimationCancel(p0: Animator?) {
                    animateCartCount(oldHolder)
                }

                override fun onAnimationStart(p0: Animator?) {

                }

            })
            start()
        }
    }

    private fun animateCartCount(oldHolder: CartViewHolder) {
        val scaleX = PropertyValuesHolder.ofFloat("scaleX", 0.0f, 1.1f)
        val scaleY = PropertyValuesHolder.ofFloat("scaleY",0.0f,1.1f)
        ObjectAnimator.ofPropertyValuesHolder(containerView,scaleX,scaleY).apply {
            duration = 300
            addListener(object :Animator.AnimatorListener{
                override fun onAnimationRepeat(p0: Animator?) {

                }

                override fun onAnimationEnd(p0: Animator?) {
                    containerView.visibility = View.VISIBLE
                    removeAnimated(oldHolder)
                    callback()
                    toCart = false
                }

                override fun onAnimationCancel(p0: Animator?) {
                    containerView.visibility = View.VISIBLE
                    removeAnimated(oldHolder)
                    callback()
                    toCart = false
                }

                override fun onAnimationStart(p0: Animator?) {

                }
            })
            startDelay = 50
            start()
        }
    }

    //TODO:outsource
    fun removeAnimated(oldHolder: CartViewHolder) {
        mAddedToCart.remove(oldHolder)
        oldHolder.animateRestore()
        ObjectAnimator.ofFloat(oldHolder.itemView,"alpha",0.0f,1.0f).apply{
            duration = 600
            start()
        }
    }

    private fun CartViewHolder.animateRestore(){
        this.itemView.apply {
            if (Build.VERSION.SDK_INT >= 21) {
                val tag = getTag(android.support.v7.recyclerview.R.id.item_touch_helper_previous_elevation)
                if (tag != null && tag is Float) {
                    ViewCompat.setElevation(this, tag)
                }
                setTag(android.support.v7.recyclerview.R.id.item_touch_helper_previous_elevation, null as Any?)
            }
            alpha = 0.0f
            scaleX = 1.0f
            scaleY = 1.0f
            translationX = 0.0f
            translationY = 0.0f
        }
    }
}