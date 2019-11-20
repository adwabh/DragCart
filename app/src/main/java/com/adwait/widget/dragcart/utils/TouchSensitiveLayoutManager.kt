package com.adwait.widget.dragcart.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.support.v4.view.ViewCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator


/**
 * Created by Adwait Abhyankar on 8/28/2019.
 * avabhyankar22@gmail.com
 */
class TouchSensitiveLayoutManager(context: Context, recyclerView: RecyclerView, spanCount: Int, orientation: Int, reverseLayout: Boolean) : GridLayoutManager(context,spanCount,orientation,reverseLayout), View.OnTouchListener{



    private var mScreenHeight: Int
    private var mScreenWidth: Int
    /***
     *
     *val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    mScreenWidth = wm . getDefaultDisplay ().getWidth()
    mScreenHeight = wm . getDefaultDisplay ().getHeight()
    mRecyclerView = recyclerView
     */
    private val mMaxVelocityThreshold: Double = 4500.0
    private val mDragThresholdX: Double? = null
    private val mDragThresholdY: Double? = null
    private val mMinVelocityThreshold: Double = 2000.0
    private val timingThreshold = 100
    private var mVelocityX: Float = 0f
    private var mVelocityY: Float = 0f
    private var mIsUpAction: Boolean? = null
    private var mInitialY: Float = 0f
    private var mInitialX: Float = 0f
    private lateinit var mVelocityTracker: VelocityTracker

     var recyclerView:RecyclerView

    init{
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mScreenWidth = wm.defaultDisplay.width
        mScreenHeight = wm.defaultDisplay.height
        this.recyclerView = recyclerView
    }


    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {

        if (!::mVelocityTracker.isInitialized) {
            mVelocityTracker = VelocityTracker.obtain()
        }
//      add event to velocity tracker
        mVelocityTracker.addMovement(motionEvent)
        when(motionEvent?.action){
            MotionEvent.ACTION_DOWN ->{
                //set initial x and y
                mInitialX = motionEvent.rawX
                mInitialY = motionEvent.rawY
                mIsUpAction = false
                //set boolean is up as false
            }
            MotionEvent.ACTION_UP ->{
                mIsUpAction = true
//                compute velocity with unit of 1000
                mVelocityTracker.computeCurrentVelocity(1000)
                mVelocityX = mVelocityTracker.xVelocity
                mVelocityY = mVelocityTracker.yVelocity
                var velocity = Math.sqrt((mVelocityX * mVelocityX + mVelocityY * mVelocityY).toDouble())
                if((velocity >= mMinVelocityThreshold)
                    || motionEvent.downTime < timingThreshold){
                    releaseCard(view, motionEvent.rawX - mInitialX, motionEvent.rawY - mInitialY, velocity)
                }
            }
            MotionEvent.ACTION_MOVE ->{
//                if after up i.e. isUp true; then set initial x and y
                if (mIsUpAction!!){
                    mInitialX = motionEvent.rawX
                    mInitialY = motionEvent.rawY
                    mIsUpAction = false
                }
//                call onDrag custom event by computing offset x from original to final and similarly for y-offset
                dragCard(view, motionEvent.rawX - mInitialX, motionEvent.rawY - mInitialY)

            }
        }
        return false
    }

    private fun releaseCard(card: View?, rawX: Float, rawY: Float, velocity: Double) {
        //if the dragged card is within the minimum x or y distance threshold for being dragged to cart then animate it to original position and velocity threshold
        if(rawX > mDragThresholdX!!
                || rawY > mDragThresholdY!!
                || (mMaxVelocityThreshold < velocity && velocity >= mMinVelocityThreshold ) ){
            animateCardToCart(card,rawX,rawY,velocity)
        }else{
            animateCardRestored(card,rawX,rawY,velocity)
        }
        //otherwise animate it to cart
    }

    private fun animateCardRestored(card: View?, rawX: Float, rawY: Float, velocity: Double) {
        val oriTransX = card?.translationX
        val oriTransY = card?.translationY
        val oriRotateDeg = card?.rotation
        val oriScaleX = card?.scaleX
        val oriScaleY = card?.getScaleY()
        val oriAlpha = card?.getAlpha()

//        val oriTransZ = ViewCompat.getTranslationZ(card)
//        val targetTransZ = mRecyclerView.getVisibleCardCount() * mCardElevation

        val animator = ValueAnimator.ofFloat(1f, 0f)
        animator.interpolator = OvershootInterpolator()
        animator.addUpdateListener { animation ->
            val offset = animation.animatedValue as Float
            card?.translationX = oriTransX!! * offset
            card.translationY = oriTransY!! * offset
//            ViewCompat.setTranslationZ(card, targetTransZ + (oriTransZ - targetTransZ) * offset)
            card.rotation = oriRotateDeg!! * offset
            card.scaleX = (oriScaleX!! - 1) * offset + 1
            card.scaleY = (oriScaleY!! - 1) * offset + 1
            card.alpha = (oriAlpha!! - 1) * offset + 1
//            dispatchOnDragEvent(card, false, false, oriTransX * offset, oriTransY * offset)
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                ViewCompat.setTranslationZ(recyclerView, -1f)
            }
        })
        animator.duration = 500
        animator.start()
    }

    private fun animateCardToCart(card: View?, rawX: Float, rawY: Float, velocity: Double) {

    }

    private fun dragCard(card: View?, rawX: Float, rawY: Float) {
        card?.translationX = rawX
        card?.translationY = rawY
    }


    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {

        if (itemCount <= 0) {
            removeAndRecycleAllViews(recycler!!)
            return
        }

        super.onLayoutChildren(recycler, state)

        for(i in 0 until recyclerView.childCount){
            var view = recyclerView.getChildAt(i)
            view?.setOnTouchListener(this)
        }
    }
}