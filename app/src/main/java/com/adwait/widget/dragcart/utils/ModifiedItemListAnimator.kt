package com.adwait.widget.dragcart.utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ViewGroup


/**
 * Created by Adwait Abhyankar on 9/25/2019.
 * Pitney-Bowes
 * adwait.abhyankar@pb.com
 */
class ModifiedItemListAnimator(root: ViewGroup, private val itemListHelper:HalfwayItemListHelper, updateTarget: UpdateListener) : SampleItemAnimator(root, updateTarget) {
    override fun animateChange(oldHolder: RecyclerView.ViewHolder, newHolder: RecyclerView.ViewHolder?, fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
        var cartHolder = oldHolder as CartViewHolder
        return animateToCartImpl(cartHolder.cartInfo,oldHolder,newHolder, itemListHelper)
    }


    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
        var canReuse = super.canReuseUpdatedViewHolder(viewHolder);
        Log.d("Animated","canReuse = $canReuse")
        return true
    }
    private fun animateToCartImpl(changeInfo: MoveToCartInfo, oldHolder: CartViewHolder, newHolder: RecyclerView.ViewHolder?,itemListHelper: HalfwayItemListHelper): Boolean {
            Log.d("Animated", "old=$oldHolder, new=$newHolder")

            val xHolder = PropertyValuesHolder.ofFloat("translationX",changeInfo.lastX,changeInfo.cartX)
            val yHolder = PropertyValuesHolder.ofFloat("translationY",changeInfo.lastY,changeInfo.cartY)
            var valueAnimator = ObjectAnimator.ofPropertyValuesHolder(oldHolder.itemView,xHolder,yHolder)
            valueAnimator.apply {
                duration = 1000
                addUpdateListener {
                    updateTarget.onAnimateUpdate(this)
                    Log.e("Animated", "x=${this.getAnimatedValue("translationX")}, y=${this.getAnimatedValue("translationY")}")
                }
                addListener(object: Animator.AnimatorListener{
                    override fun onAnimationRepeat(p0: Animator?) {
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        dispatchFinishedWhenDone()
                        itemListHelper.removeAnimated(oldHolder)
                        Log.e("Animated", "onAnimationEnd")
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                        dispatchFinishedWhenDone()
                        itemListHelper.removeAnimated(oldHolder)
                        Log.e("Animated", "onAnimationCancel")
                    }

                    override fun onAnimationStart(p0: Animator?) {
                        dispatchChangeStarting(oldHolder, true)
                        Log.e("Animated", "onAnimationStart")

                    }
                })
                start()
            }
        return true
    }
}