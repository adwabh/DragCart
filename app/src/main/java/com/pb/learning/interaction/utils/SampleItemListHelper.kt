package com.pb.learning.interaction.utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.graphics.*
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.*
import android.widget.ImageView
import kotlinx.android.synthetic.main.fragment_detail.view.*


/**
 * Created by Adwait Abhyankar on 1/21/2019.
 */
class SampleItemListHelper(private val root: ViewGroup?) : ItemTouchHelper.Callback() {

    var lastX:Float = 0f
    var lastY:Float = 0f
    override fun getMovementFlags(recyclerView: RecyclerView, holder: RecyclerView.ViewHolder): Int {
        return if (recyclerView.layoutManager is GridLayoutManager) {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            val swipeFlags = 0
            ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
        } else {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
            ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
        }
    }

    override fun onMove(p0: RecyclerView, source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return source.itemViewType == target.itemViewType
    }

    override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {

    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

    }

    private val fabX: Float = 300F
    private val fabY: Float = 500F

    private var isAnimating: Boolean = false
    private lateinit var viewParams: ViewParams
    private lateinit var currentAnimator: ValueAnimator

    private val animationListener: Animator.AnimatorListener = object:Animator.AnimatorListener{
        override fun onAnimationRepeat(p0: Animator?) {

        }

        override fun onAnimationEnd(p0: Animator?) {
            isAnimating = false
//            viewHolder?.itemView?.visibility = VISIBLE
        }

        override fun onAnimationCancel(p0: Animator?) {
            isAnimating = false
//            viewHolder?.itemView?.visibility = VISIBLE
        }

        override fun onAnimationStart(p0: Animator?) {
            isAnimating = true
        }
    }

    private var animUtil: CanvasAnimationUtils? = null

    override fun onChildDrawOver(canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        val top = viewHolder?.itemView?.top?:0
        val bottom = viewHolder?.itemView?.bottom?:0
        val left = viewHolder?.itemView?.left?:0
        val right = viewHolder?.itemView?.right?:0
        val rect = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
        var copy = ViewCaptureUtils.copyViewImageAsBitmap(viewHolder?.itemView!!)

        if(isCurrentlyActive){
//                viewHolder.itemView.visibility = INVISIBLE
                drawEnclosed(canvas, viewHolder, copy, dX, dY)
                lastX = dX
                lastY = dY
        }else{
//            var radius = Math.hypot((left - dX).toDouble(), (top - dY).toDouble())
//            canvas.drawCircle(fabX,fabY, radius.toFloat(), Paint(Color.RED))

            if(animUtil==null) {
                animUtil = CanvasAnimationUtils.Builder()
                        .withProperty("x", CanvasAnimationUtils.CanvasAnimationHolder(fabX, viewParams.xPos, AccelerateDecelerateInterpolator(), 300))
                        .withProperty("y", CanvasAnimationUtils.CanvasAnimationHolder(fabX, viewParams.xPos, AccelerateDecelerateInterpolator(), 300))
                        .build()

            }


/*            if (!isAnimating) {
                var xholder = PropertyValuesHolder.ofFloat("x",viewParams.xPos,fabX)
                var yholder = PropertyValuesHolder.ofFloat("y",viewParams.yPos,fabY)
                currentAnimator = ValueAnimator.ofPropertyValuesHolder(xholder,yholder).apply {
                    duration = 300
                    addListener(animationListener)
                    start()
                    addUpdateListener {
                        drawEnclosed(canvas, viewHolder, copy, currentAnimator.getAnimatedValue("x") as Float, currentAnimator.getAnimatedValue("y") as Float)
                    }

                }
*/
//            }
            val (fractionX,valueX) = this.animUtil!!.getAnimated("x")
            val (fractionY,valueY) = this.animUtil!!.getAnimated("y")
            drawEnclosed(canvas,viewHolder,copy,valueX,valueY)

        }
    }

    private fun drawEnclosed(canvas: Canvas, viewHolder: RecyclerView.ViewHolder, copy: Bitmap, dX: Float, dY: Float) {
        val paint = Paint()
        paint.color = Color.WHITE
        val radius = Math.min(viewHolder.itemView.width,viewHolder.itemView.height)//Math.hypot((viewHolder.itemView.left - dX).toDouble(), (viewHolder.itemView.top - dY).toDouble())

        val clipped = ImageUtils.GetBitmapClippedCircle(copy, radius.toInt())
        val xPos = viewHolder.itemView.left + dX
        val yPos = viewHolder.itemView.top + dY
        val height = clipped.height
        val width = clipped.width
        viewParams = ViewParams(clipped,xPos,yPos,
                height.toFloat(),
                width.toFloat())
        canvas.drawBitmap(clipped,xPos,yPos, paint)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        currentAnimator.cancel()
        super.clearView(recyclerView, viewHolder)
    }

    /*override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        val image = ImageView(recyclerView.context)
        image.setImageBitmap(viewParams.bitmap)
        image.apply {
            layoutParams = ViewGroup.LayoutParams(viewHolder.itemView.height,viewHolder.itemView.width)
            layoutParams.height = viewParams.height.toInt()
            layoutParams.width = viewParams.width.toInt()
            x = viewParams.xPos
            y = viewParams.yPos - viewHolder.itemView.top
//            translationX = -viewParams.xPos
//            translationY = -viewParams.yPos

//            setImageBitmap(Bitmap.createScaledBitmap(viewParams.bitmap,viewParams.width.toInt(),viewParams.height.toInt(),false))
        }
        root?.addView(image).run {animateToDestinationIfNecessary(image,viewHolder)}
    }*/

    private fun animateToDestinationIfNecessary(image: ImageView,viewHolder: RecyclerView.ViewHolder) {
        var xholder:PropertyValuesHolder = PropertyValuesHolder.ofFloat("x",image.x,fabX)
        var yholder:PropertyValuesHolder = PropertyValuesHolder.ofFloat("y",image.y,fabY)
        var animator:ObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(image,xholder,yholder)
        animator.apply {
            addListener(object: Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {

                }

                override fun onAnimationEnd(p0: Animator?) {
                    viewHolder.itemView.visibility = VISIBLE
                    root?.removeView(image)
                }

                override fun onAnimationCancel(p0: Animator?) {
                    root?.removeView(image)
                    viewHolder.itemView.visibility = VISIBLE
                }

                override fun onAnimationStart(p0: Animator?) {

                }

            })
            duration = 5000
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    data class ViewParams(val bitmap:Bitmap,val xPos: Float,val yPos:Float,val height:Float,val width:Float)
}