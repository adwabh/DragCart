package com.adwait.widget.dragcart.utils

import android.animation.Animator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.View
import com.adwait.widget.dragcart.R
import android.graphics.Bitmap
import android.view.ViewTreeObserver


/**
 * Created by Adwait Abhyankar on 9/16/2019.
 * avabhyankar22@gmail.com
 */
class ModifiedItemListHelper(private val context: Context, private val anchor: View, private val color: Int = R.color.colorAccent, cartDrawable: Int, recyclerView: RecyclerView) : ItemTouchHelper.Callback() {


    private lateinit var mCartAnim: ValueAnimator
    private var mCartHolder: RecyclerView.ViewHolder? = null
    private var cartX: Float = 0f
    private var cartY: Float = 0f
    private val paint: Paint = Paint()

    private  var drawable: Drawable = context.getDrawable(cartDrawable)!!
    private var bitmap = drawableToBitmap(drawable)
    private var finalX: Float = 0f
    private var finalY: Float = 0f
    private val mCartAnimations: MutableMap<RecyclerView.ViewHolder,ValueAnimator> = mutableMapOf()
    private val dimen = intArrayOf(0,0)
    private val recyclerDimen = intArrayOf(0,0)
    private var translateLocation: FloatArray

    private var globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        recyclerView.getLocationOnScreen(recyclerDimen)
        anchor.getLocationOnScreen(dimen)
        cartX = dimen[0].toFloat() - recyclerDimen[0] + anchor.width/2
        cartY = dimen[1].toFloat() - recyclerDimen[1] + anchor.height/2
    }

    init{
        anchor.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        paint.color = context.resources.getColor(color,null)
        translateLocation = floatArrayOf(0f, 0f)
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if (Build.VERSION.SDK_INT >= 21 && isCurrentlyActive) {
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
        } else {
            if(isSelected(viewHolder)){
                viewHolder.itemView.translationX = mCartAnimations[viewHolder]!!.getAnimatedValue("translateX") as Float
                viewHolder.itemView.translationY = mCartAnimations[viewHolder]!!.getAnimatedValue("translateY") as Float
            }else{
                calculateTranslateArray(translateLocation,cartX - viewHolder.itemView.left,cartY - viewHolder.itemView.top,viewHolder.itemView.width,viewHolder.itemView.height)
                select(recyclerView,viewHolder,actionState,finalX, translateLocation[0],finalY, translateLocation[1])
//                select(recyclerView,viewHolder,actionState,finalX, cartX - viewHolder.itemView.left,finalY, cartY - viewHolder.itemView.top)
            }
        }

    }

    private fun calculateTranslateArray(translateLocation: FloatArray, centerX: Float, centerY: Float, width: Int, height: Int) {
        translateLocation[0] = centerX - width/2
        translateLocation[1] = centerY - height/2
    }

    override fun onChildDrawOver(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if(isCurrentlyActive){
            animateCartExpansionFor(c, recyclerView, viewHolder!!, actionState,dX,dY)
        }
        if (isSelected(viewHolder!!)) {
            var animatedFraction = mCartAnimations[viewHolder]!!.animatedFraction
            drawCart(c, viewHolder, recyclerView, animatedFraction)
            if(animatedFraction>=0.8f){
                viewHolder.itemView.scaleX = (1-animatedFraction)
                viewHolder.itemView.scaleY = (1-animatedFraction)
            }
            animateAnchorAlpha(anchor,animatedFraction)
        }
    }

    //region utility methods

    private fun animateCartExpansionFor(canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, actionState: Int, dX: Float, dY: Float) {
        if (Math.sqrt(Math.pow(dX.toDouble(),2.0) + Math.pow(dY.toDouble(),2.0)) >= getMoveThreshold(viewHolder)) {
            if (!isCartAnimating()) {
                var animationType: Byte = 2
                if (actionState == 2) {
                    animationType = 8
                }
                mCartHolder = viewHolder
                mCartAnim = ValueAnimator.ofFloat(0f,1f)
                mCartAnim.duration = (getAnimationDuration(recyclerView,animationType.toInt(),finalX,finalY)).toLong()
                mCartAnim.start()
            }else{
                animateAnchorAlpha(anchor,1-mCartAnim.animatedFraction)
                drawCart(canvas, viewHolder, recyclerView, 1-mCartAnim.animatedFraction)
            }
        }
    }

    private fun animateAnchorAlpha(anchor: View, alpha: Float) {
        anchor.alpha = alpha
    }

    private fun isCartAnimating(): Boolean {
        return mCartHolder!=null
    }


    private fun drawCart(canvas: Canvas, viewHolder: RecyclerView.ViewHolder, recyclerView: RecyclerView, animatedFraction: Float) {
        calculateTranslateArray(translateLocation,cartX - recyclerView.left - recyclerView.scrollX ,cartY - recyclerView.top - recyclerView.scrollY,viewHolder.itemView.width,viewHolder.itemView.height)
        canvas.drawCircle(translateLocation[0] , translateLocation[1] ,(1 - animatedFraction) * radius,paint)
        if (animatedFraction<0.8f) {
            canvas.drawBitmap(bitmap, translateLocation[0] - bitmap.width/2, translateLocation[1] - bitmap.height/2, paint)
        }
    }

    private fun select(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, actionState: Int, startX: Float, endX: Float, startY: Float, endY: Float) {
        var va = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat("translateX",startX,endX), PropertyValuesHolder.ofFloat("translateY",startY,endY))
        var animationType: Byte = 2
        if (actionState == 2) {
            animationType = 8
        }
        va.duration = getAnimationDuration(recyclerView,animationType.toInt(),finalX,finalY)
        va.start()
        va.addListener(object:Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                viewHolder.itemView.translationX = 0f
                viewHolder.itemView.translationY = 0f
                viewHolder.itemView.scaleX = 1f
                viewHolder.itemView.scaleY = 1f
                viewHolder.setIsRecyclable(true)
                mCartAnimations.remove(viewHolder)
                mCartHolder = null
            }

            override fun onAnimationCancel(p0: Animator?) {
                viewHolder.itemView.translationX = 0f
                viewHolder.itemView.translationY = 0f
                viewHolder.itemView.scaleX = 1f
                viewHolder.itemView.scaleY = 1f
                viewHolder.setIsRecyclable(true)
                mCartAnimations.remove(viewHolder)
                mCartHolder = null
            }

            override fun onAnimationStart(p0: Animator?) {
                viewHolder.setIsRecyclable(false)
            }
        })
        va.start()
        mCartAnimations[viewHolder] = va
    }

    private fun isSelected(viewHolder: RecyclerView.ViewHolder): Boolean {
        return mCartAnimations.containsKey(viewHolder)
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

    //endregion utility methods

    companion object {

        private const val radius: Float = 120f


        fun drawableToBitmap(drawable: Drawable): Bitmap {

            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }

            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            return bitmap
        }
    }
}