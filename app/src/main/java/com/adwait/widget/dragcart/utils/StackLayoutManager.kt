package com.adwait.widget.dragcart.utils

import android.content.Context
import android.graphics.Rect
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager



class StackLayoutManager(context: Context, private val mRecyclerView: RecyclerView) : RecyclerView.LayoutManager() {
    private val mScreenWidth: Int
    private val mScreenHeight: Int
    private val mDragThresholdX: Int
    private val mDragThresholdY: Int

    private val mCardOffset = 10
    private val mCardElevation = 10
    private val mVisibleCardCount = 3


    init {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mScreenWidth = wm.defaultDisplay.width
        mScreenHeight = wm.defaultDisplay.height
        mDragThresholdX = mScreenWidth / 3
        mDragThresholdY = mScreenHeight / 3
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        if (itemCount <= 0) {
            removeAndRecycleAllViews(recycler!!)
            return
        }

        // remove all attached child views
        detachAndScrapAttachedViews(recycler!!)

        // re-layout
        onLayoutNudge(recycler, state)
    }

    private fun onLayoutNudge(recycler: RecyclerView.Recycler, state: RecyclerView.State?) {
        if (itemCount > 0) {
            // calculate the validate areas that all the visible cards cover.
            var left = 0
            var top = 0
            // in order to compat lower version, card must be added from last to first,
            // and if all the cards are added at every time this method invoked, there may
            // be some performance problems happened, such as OOM crash, long time wasted to
            // attached cards,and less smoothly when scroll etc.So to avoid these potential
            // problems, only necessary cards will be attached to improve performance
            //            int maxAttachChildrenCount = Math.min(mRecyclerView.getVisibleCardCount(), getItemCount() - 1);
            val maxAttachChildrenCount = Math.min(mVisibleCardCount, itemCount - 1)
            for (i in maxAttachChildrenCount downTo 0) {
                val child_i = recycler.getViewForPosition(i)
                addView(child_i)
                measureChildWithMargins(child_i, 0, 0)
                val childWidth = getDecoratedMeasuredWidth(child_i)
                val childHeight = getDecoratedMeasuredHeight(child_i)
                if (i == maxAttachChildrenCount) {
                    val childWidthWithTotalOffset = childWidth + mCardOffset * (mVisibleCardCount - 1)
                    val childHeightWithTotalOffset = childHeight + mCardOffset * (mVisibleCardCount - 1)
                    val parentWExcludePadding = width - paddingLeft - paddingRight
                    val parentHExcludePadding = height - paddingTop - paddingBottom
                    val params = child_i.layoutParams as ViewGroup.MarginLayoutParams
                    val childMarginHorizOffset = params.leftMargin - params.rightMargin
                    val childMarginVertOffset = params.topMargin - params.bottomMargin
                    left = (parentWExcludePadding - childWidthWithTotalOffset) / 2 + paddingLeft + childMarginHorizOffset
                    top = (parentHExcludePadding - childHeightWithTotalOffset) / 2 + paddingTop + childMarginVertOffset
                }
                // remove decorator area
                val childRect = Rect()
                calculateItemDecorationsForChild(child_i, childRect)
                val left_i: Int
                val top_i: Int
                if (i <= mVisibleCardCount - 1) {
                    left_i = left + mCardOffset * i
                    top_i = top + mCardOffset * (mVisibleCardCount - 1 - i)
                    // set elevations for all the visible children
                    ViewCompat.setTranslationZ(child_i, (mCardElevation * (mVisibleCardCount - i)).toFloat())
                } else {
                    left_i = left + mCardOffset * (mVisibleCardCount - 1)
                    top_i = top
                    ViewCompat.setTranslationZ(child_i, 0f)
                }
                // reset card
                child_i.translationY = 0f
                child_i.translationX = 0f
                child_i.alpha = 1f
                child_i.rotation = 0f
                child_i.scaleX = 1f
                child_i.scaleY = 1f
                layoutDecorated(child_i, left_i, top_i, left_i + childWidth, top_i + childHeight)
            }
        }
    }


    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT)
    }

    // refresh other visible cards' positions when dragging
    fun refreshOtherVisibleCardsPosition(factor: Float) {
        if (itemCount > 1) {
            val cardOffset = mCardOffset
            // count of all the attached cards
            val maxAttachChildrenCount = Math.min(mVisibleCardCount, itemCount - 1)
            // count of all the cards required refreshing
            val totalRefreshingCount = Math.min(itemCount - 1, mVisibleCardCount)
            for (i in 1..totalRefreshingCount) {
                val childPosition = maxAttachChildrenCount - i
                val child = getChildAt(childPosition)
                // trans x y. if the visible cards count is three, for example,
                // here we only need to handle translation x and y for the other two visible cards at position 1 and 2
                if (i < totalRefreshingCount) {
                    if (child != null) {
                        child.translationX = -cardOffset * factor
                        child.translationY = cardOffset * factor
                    }
                }

                // it is different for handling transZ compared to transX Y.
                // we just need to handle more than one card.

                // calculate the current card ori elevation
                val current = mVisibleCardCount - i
                val oriElevation = mCardElevation * current
                // update
                val currentElevation = (oriElevation + mCardElevation * factor).toInt()
                if (child != null) {
                    ViewCompat.setTranslationZ(child, currentElevation.toFloat())
                }
            }
        }
    }

    fun refreshOtherVisibleCardsPosition(offset_x: Float, offset_y: Float) {
        var factor = (Math.sqrt((offset_x * offset_x + offset_y * offset_y).toDouble()) / Math.sqrt((mDragThresholdX * mDragThresholdX + mDragThresholdY * mDragThresholdY).toDouble())).toFloat()
        factor = Math.min(factor, 1f)
        refreshOtherVisibleCardsPosition(factor)
    }


}
