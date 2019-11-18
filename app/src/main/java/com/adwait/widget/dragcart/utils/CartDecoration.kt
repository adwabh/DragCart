package com.adwait.widget.dragcart.utils

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import com.adwait.widget.dragcart.R
import kotlin.math.hypot
import android.graphics.*


/**
 * Created by Adwait Abhyankar on 11/1/2019.
 * avabhyankar22@gmail.com
 */
class CartDecoration(private val bitmap: Bitmap, private val anchor: View, private val recyclerView: RecyclerView) : RecyclerView.ItemDecoration() {
    private val duffMode: PorterDuffXfermode = PorterDuffXfermode(PorterDuff.Mode.XOR)
    private val scalePaint: Paint = Paint().apply { xfermode = duffMode }
    private lateinit var newRect: Rect
    private lateinit var oldRect: Rect

    init{
        recyclerView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)//Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN) })
    }
    var currentlyActive:Boolean = false

    private var clearCanvas: Boolean = false
    private var scaleStarted: Boolean = false
    private var imageCopy: Bitmap? = null
    private var currentScaleX: Float = 0f
    private var currentScaleY: Float = 0f
    private val destPaint: Paint = Paint().apply {
        color = anchor.context.getColor(R.color.colorAccent)
//        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
    }
    private var itemView: View? = null
    private val sourcePaint: Paint = Paint().apply {
        color = anchor.context.getColor(R.color.colorAccent)
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
    }
    private var currentLeft: Float = 0f
    private var currentTop: Float = 0f
    private var anchor_dimen: Double = 0.0

    private var cartY: Float = 0.0f
    private var cartX: Float = 0.0f
    private val recyclerDimen: IntArray = intArrayOf(0, 0)
    private val dimen: IntArray = intArrayOf(0,0)
    @SuppressLint("ResourceAsColor")
    private val paint: Paint = Paint().apply {
        color = recyclerView.context.getColor(R.color.colorAccent)
    }
    private var finalRadius: Float = 0f
    private val _RADIUS = 300f
    private var baseBitmap:Lazy<Bitmap> = lazy {  Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)}
    private val scratch:Lazy<Canvas> = lazy{Canvas(baseBitmap.value)}
    private var cartAnimationFraction: Float = 0f
    private val mInterpolator: Interpolator = LinearInterpolator()


    init {
        anchor.viewTreeObserver.addOnGlobalLayoutListener {
            recyclerView.getLocationOnScreen(recyclerDimen)
            anchor.getLocationOnScreen(dimen)
            anchor_dimen = with(anchor){ hypot(this.width.toDouble(),this.height.toDouble())/2 }
            cartX = dimen[0].toFloat() - recyclerDimen[0] + anchor.width/2
            cartY = dimen[1].toFloat() - recyclerDimen[1] + anchor.height/2
        }
    }
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        if (itemView!=null && !currentlyActive) { drawCart(c, recyclerView, cartAnimationFraction.toDouble(), itemView!!) }
        Log.i("Animated","drawCalled with $cartAnimationFraction")
    }

    private fun drawCart(canvas: Canvas, recyclerView: RecyclerView, animator: Double, itemView: View) {
        val cx= this.cartX// - viewHolder.itemView.left;
        val cy = this.cartY //- viewHolder.itemView.top
        finalRadius = _RADIUS*mInterpolator.getInterpolation(1-animator.toFloat())

        with(canvas) {

            if (clearCanvas) {
                Log.w("Animated","restore canvas")
                clearCanvas = false
                return
            }
                drawCircle(
                        cx,
                        cy,
                        kotlin.math.max(anchor_dimen.toFloat(),finalRadius),
                        paint)
                imageCopy?.let {
                    if (!::oldRect.isInitialized) {
                        oldRect = Rect(currentLeft.toInt(),currentTop.toInt(), (currentLeft+it.getScaledWidth(canvas)).toInt(), (currentTop+it.getScaledHeight(canvas)).toInt())

                    }
                    if (scaleStarted) {
//                        drawRect(newRect,scalePaint)
                        drawBitmap(it,null,newRect,paint)
                        Log.w("Animated","old rect = $oldRect, new rect = $newRect")
//                        oldRect = newRect
                    }else{
                        if(it.isRecycled){
                            imageCopy = itemView.toBitmap()
                        }
                        it.let { checked->
                            drawBitmap(checked, currentLeft + itemView.left, currentTop + itemView.top, paint)
                        }
                    }
                }
//                drawCircle(
//                        cx,
//                        cy,
//                        kotlin.math.max(anchor_dimen.toFloat(),finalRadius),
//                        sourcePaint)

                bitmap.let {
                    canvas.drawBitmap(it, cx - it.width/2, cy - it.height/2, paint)
                }

        }
    }

    fun reset() {
        cartAnimationFraction = 0f
    }

    fun onAnimateCartUpdate(animator: ValueAnimator,itemView:View) {
        Log.w("Animated","cartAnimationProgress = $cartAnimationFraction")
        currentLeft = animator.getAnimatedValue("translationX") as Float
        currentTop = animator.getAnimatedValue("translationY") as Float
        this.itemView = itemView.apply {
            if(imageCopy==null){
                imageCopy = itemView.toBitmap()
            }
        }
        recyclerView.invalidate()
    }

    fun onItemScaleUpdate(animator: ValueAnimator,itemView: View){
        cartAnimationFraction = animator.animatedFraction
        with(itemView) {
            currentScaleX = width*animator.getAnimatedValue("scaleX") as Float
            currentScaleY = height*animator.getAnimatedValue("scaleY") as Float
            newRect = Rect((cartX - (currentScaleX/2)).toInt(), kotlin.math.abs((cartY - (currentScaleY/2))).toInt(),(cartX + (currentScaleX/2)).toInt(),(cartY + (currentScaleY/2)).toInt())
        Log.w("Animated","cartDecor rect = ($left,$top,$right,$bottom)")
        }
        recyclerView.invalidate()
    }

    public fun View.toBitmap():Bitmap{
        return ViewCaptureUtils.copyViewImageAsBitmap(this)
    }

    fun startScale(){
        scaleStarted = true
    }

    fun endScale() {
        scaleStarted = false
        imageCopy?.recycle()
        imageCopy = null
        itemView?.let {
            currentScaleY = it.height.toFloat()
            currentScaleX = it.width.toFloat()
        }
        itemView = null
        clearCanvas = true
        reset()
    }

    fun cancelScale() {
        endScale()
    }

    inline fun <R> Bitmap?.onValid(call:(Bitmap)->R){
        this?.let{call.invoke(this)}
    }

}