package com.adwait.widget.dragcart.utils

import android.animation.Animator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator


/**
 * Created by Adwait Abhyankar on 2/20/2019.
 * http://blog.trsquarelab.com/2015/12/creating-custom-animation-in.html
 *  https://android.googlesource.com/platform/frameworks/support/+/c110be5/v7/recyclerview/src/android/support/v7/widget/DefaultItemAnimator.java
 */
class SampleItemAnimator(root: ViewGroup,updateTarget:UpdateListener): SimpleItemAnimator() {
    interface UpdateListener {
        fun onAnimateUpdate(animator:ValueAnimator)
    }

    private val fabX: Float = 300f
    private val fabY: Float = 500f
    private val root: ViewGroup = root
    private val updateTarget = updateTarget
    private lateinit var currentAnimator: ValueAnimator

    companion object {
        private const val TAG ="ANIMATION"
    }

    override fun animateAdd(p0: RecyclerView.ViewHolder?): Boolean {
        return false
    }

    override fun runPendingAnimations() {

    }

    override fun animateMove(p0: RecyclerView.ViewHolder?, p1: Int, p2: Int, p3: Int, p4: Int): Boolean {
        return false
    }

    override fun animateChange(holder: RecyclerView.ViewHolder?, newHolder: RecyclerView.ViewHolder?, fromLeft: Int, fromTop: Int, toLeft: Int, toTop: Int): Boolean {

        var xHolder = PropertyValuesHolder.ofInt("x", 0, 300)
        var yHolder = PropertyValuesHolder.ofInt("y", 0, 500)
        currentAnimator = ValueAnimator.ofPropertyValuesHolder(xHolder,yHolder)
                .apply {
                duration = moveDuration
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { updateTarget.onAnimateUpdate(this) }
                    addListener(object:Animator.AnimatorListener{
                        override fun onAnimationRepeat(p0: Animator?) {

                        }

                        override fun onAnimationEnd(p0: Animator?) {
                            dispatchMoveFinished(holder!!)
                        }

                        override fun onAnimationCancel(p0: Animator?) {
                            dispatchMoveFinished(holder!!)
                        }

                        override fun onAnimationStart(p0: Animator?) {
                            dispatchMoveStarting(holder!!)
                        }
                    })
                start()}
        return false
    }

    override fun isRunning(): Boolean {
        return if(::currentAnimator.isInitialized){
            currentAnimator.isRunning
        }else{
            false
        }
    }

    override fun endAnimation(p0: RecyclerView.ViewHolder) {

    }

    override fun animateRemove(p0: RecyclerView.ViewHolder?): Boolean {
        return false
    }

    override fun endAnimations() {

    }


}