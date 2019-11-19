package com.adwait.widget.dragcart.utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.support.v4.view.ViewCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import com.adwait.widget.dragcart.R
import kotlin.math.hypot
import com.adwait.widget.dragcart.utils.ViewCaptureUtils.copyViewImageAsBitmap as toBitmap


/**
 * Created by Adwait Abhyankar on 9/25/2019.
 *
 * avabhyankar22@mail.com
 */
class HalfwayItemListHelper(private val recyclerView: RecyclerView, var anchor: View, private val callback: () -> Unit,private val containerView:ViewGroup) : ItemTouchHelper.Callback() {

    private var viewCopy: Bitmap? = null
    private var cartAnimatedFraction: Float = 0f
    private var finalRadius: Float = 0f
    private val mInterpolator = LinearInterpolator()
    var activeCalback:((Boolean)->Unit)? = null
    var scaleUpdater: SampleItemAnimator.UpdateListener? = null

    @SuppressLint("ResourceAsColor")
    private val paint: Paint = Paint().apply{
        color = recyclerView.context.getColor(R.color.colorAccent)
    }
    private val bitmapPaint: Paint = Paint().apply{
        color = recyclerView.context.getColor(android.R.color.white)
    }
    private val bitmap = ModifiedItemListHelper.drawableToBitmap(recyclerView.context.getDrawable(R.drawable.ic_shopping_cart))

    private val _RADIUS: Float = 300f
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
    }

    override fun onMove(recyclerView: RecyclerView, source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        viewCopy = toBitmap(source.itemView)
        return source.itemViewType == target.itemViewType
    }

    override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {

    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        activeCalback?.invoke(isCurrentlyActive)
//        if (isCurrentlyActive) {
//            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//        }
    }

    override fun onChildDrawOver(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        viewHolder?.let {
            if (isCurrentlyActive) {
                if (Build.VERSION.SDK_INT >= 23) {
                    var originalElevation = viewHolder.itemView.getTag(R.id.item_touch_helper_previous_elevation)
                    if (originalElevation == null) {
                        originalElevation = ViewCompat.getElevation(viewHolder.itemView)
                        val newElevation = 1.0f + findMaxElevation(recyclerView, viewHolder.itemView)
                        ViewCompat.setElevation(viewHolder.itemView, newElevation)
                        it.itemView.setTag(R.id.item_touch_helper_previous_elevation, originalElevation)
                    }
                }
                finalX = dX
                finalY = dY
                drawCart(c,viewHolder,recyclerView,getCartThreshold(dX.toDouble(),dY.toDouble(),viewHolder))
                viewCopy.onValid {bitmap-> c.drawBitmap(bitmap,dX + it.itemView.left,dY + it.itemView.top ,paint) }
                bitmap.let { bitmap->c.drawBitmap(bitmap, cartX - bitmap.width/2, cartY - bitmap.height/2, bitmapPaint) }
                it.itemView.translationX = dX
                it.itemView.translationY = dY
            }else {
                if (!toCart) {
                    toCart = if(getCartThreshold(dX.toDouble(),dY.toDouble(),viewHolder) <=1)  {
                        viewCopy.onValid {bitmap-> c.drawBitmap(bitmap,dX + it.itemView.left,dY + it.itemView.top ,paint) }
                        it.itemView.translationX = dX
                        it.itemView.translationY = dY
                        false
                    }else{
                        true
                    }
                }
                drawCart(c,viewHolder,recyclerView,1.0)
                viewCopy.onValid {bitmap-> c.drawBitmap(bitmap,finalX + it.itemView.left,finalY + it.itemView.top ,paint) }
                bitmap.let { bitmap->c.drawBitmap(bitmap, cartX - bitmap.width/2, cartY - bitmap.height/2, bitmapPaint) }
            }
        }
    }

    //TODO: outsource
    private fun getCartThreshold(dX: Double, dY: Double, viewHolder: RecyclerView.ViewHolder): Double {
        return Math.hypot(dX,dY)/Math.hypot(viewHolder.itemView.width.toDouble(),viewHolder.itemView.height.toDouble())
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        if (toCart) {
            prepareAndSetCartInfoFor(viewHolder)
            dispatchPendingAnimateToCart()
            viewCopy?.recycle()
            viewCopy = null
        }else {
            super.clearView(recyclerView, viewHolder)
        }
    }

    //TODO:outsource
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
    fun animateCartParentAlphaRestore(anchor:View ,oldHolder: CartViewHolder){
        ObjectAnimator.ofFloat(anchor,"alpha",anchor.alpha,1.0f).apply {
            duration = 150
            addUpdateListener { anchor.alpha = this.animatedFraction }
            addListener(object:Animator.AnimatorListener{
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

    //TODO:outsource
    fun animateItemScaleInCart(oldHolder: CartViewHolder) {
        val ratio = 0.5f * (oldHolder.itemView.let { hypot(anchor.width.toFloat(), anchor.height.toFloat()) / hypot(it.width.toFloat(), it.height.toFloat()) })
        val scaleXHolder = PropertyValuesHolder.ofFloat("scaleX", 1.0f, ratio)
        val scaleYHolder = PropertyValuesHolder.ofFloat("scaleY", 1.0f, ratio)
        ObjectAnimator.ofPropertyValuesHolder(oldHolder.itemView,scaleXHolder,scaleYHolder).apply {
            duration = 300

            addUpdateListener { scaleUpdater?.let {  it.onAnimateUpdate(this,oldHolder.itemView) } }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {

                }

                override fun onAnimationEnd(p0: Animator?) {
                    animateCartParentAlphaRestore(anchor,oldHolder)
                    scaleUpdater?.animationEndAction?.invoke()
                }

                override fun onAnimationCancel(p0: Animator?) {
                    animateCartParentAlphaRestore(anchor,oldHolder)
                    scaleUpdater?.animationCancelAction?.invoke()
                }

                override fun onAnimationStart(p0: Animator?) {
                    scaleUpdater?.animationStartAction?.invoke()
                }

            })
            start()
        }
    }
    //TODO:outsource
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

    //TODO:outsource
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

    private fun drawCart(canvas: Canvas, viewHolder: RecyclerView.ViewHolder, recyclerView: RecyclerView, animator: ValueAnimator) {
        val cx= this.cartX// - viewHolder.itemView.left;
        val cy = this.cartY //- viewHolder.itemView.top
        canvas.drawCircle(
                cx,
                cy,
                animator.animatedValue as Float,
                paint
        )
        if (animator.animatedFraction<0.8f) {
            bitmap.let {
                canvas.drawBitmap(it, cx - it.width/2, cy - it.height/2, paint)
            }
        }
    }

    private fun drawCart(canvas: Canvas, viewHolder: RecyclerView.ViewHolder, recyclerView: RecyclerView, animator: Double) {
        Log.i("Animated","drawCalled with $animator")
        val cx= this.cartX// - viewHolder.itemView.left;
        val cy = this.cartY //- viewHolder.itemView.top
        finalRadius = _RADIUS*mInterpolator.getInterpolation(animator.toFloat())
        canvas.drawCircle(
                cx,
                cy,
                kotlin.math.min(_RADIUS,finalRadius),
                paint
        )
        anchor.alpha =0f
    }

    fun onAnimateCartUpdate(animator: ValueAnimator) {
        cartAnimatedFraction = animator.animatedFraction
        Log.w("Animated","cartAnimationProgress = $cartAnimatedFraction")
        recyclerView.invalidate()
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    inline fun <T,R> T?.onValid(call:(T)->R){
        this?.let{call.invoke(this)}
    }
}