package com.adwait.widget.dragcart.utils

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import com.adwait.widget.dragcart.R


/**
 * Created by Adwait Abhyankar on 11/1/2019.
 * Pitney-Bowes
 * adwait.abhyankar@pb.com
 */
class CartDecoration(private val bitmap: Bitmap, private val anchor: View, private val recyclerView: RecyclerView) : RecyclerView.ItemDecoration() {
    private var cartY: Float = 0.0f
    private var cartX: Float = 0.0f
    private val recyclerDimen: IntArray = intArrayOf(0, 0)
    private val dimen: IntArray = intArrayOf(0,0)
    @SuppressLint("ResourceAsColor")
    private val paint: Paint = Paint().apply {
        color = R.color.colorAccent
    }
    private var finalRadius: Float = 0f
    private val _RADIUS = 300f
    private var cartAnimationFraction: Float = 0f
    private val mInterpolator: Interpolator = LinearInterpolator()


    init {
        anchor.viewTreeObserver.addOnGlobalLayoutListener {
            recyclerView.getLocationOnScreen(recyclerDimen)
            anchor.getLocationOnScreen(dimen)
            cartX = dimen[0].toFloat() - recyclerDimen[0] + anchor.width/2
            cartY = dimen[1].toFloat() - recyclerDimen[1] + anchor.height/2 }
    }
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        drawCart(c,cartAnimationFraction.toDouble())
        Log.i("Animated","drawCalled with $cartAnimationFraction")
    }

    fun onAnimateCartUpdate(animator: ValueAnimator) {
        cartAnimationFraction = animator.animatedFraction
        Log.w("Animated","cartAnimationProgress = $cartAnimationFraction")

    }

    private fun drawCart(canvas: Canvas, animator: Double) {
        val cx= this.cartX// - viewHolder.itemView.left;
        val cy = this.cartY //- viewHolder.itemView.top
        finalRadius = _RADIUS*mInterpolator.getInterpolation(1-animator.toFloat())
        canvas.drawCircle(
                cx,
                cy,
                kotlin.math.min(_RADIUS,finalRadius),
                paint
        )
        if (animator.toFloat()<0.8f) {
            bitmap.let {
                canvas.drawBitmap(it, cx - it.width/2, cy - it.height/2, paint)
            }
        }
    }
}