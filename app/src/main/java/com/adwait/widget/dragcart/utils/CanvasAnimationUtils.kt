package com.adwait.widget.dragcart.utils

import android.view.animation.Interpolator


/**
 * Created by Adwait Abhyankar on 2/8/2019.
 */
class CanvasAnimationUtils() {

    private constructor(duration: Long, start: Float, end: Float, propertyHolder: Map<String, CanvasAnimationHolder>) : this(){
        this.propertyHolder = propertyHolder
        this.value = start
        this.duration = duration
        this.start = start
        this.end = end
    }
    private constructor(interpolator: Interpolator, duration: Long, start: Float, end: Float, propertyHolder: Map<String, CanvasAnimationHolder>) : this(){
        this.propertyHolder = propertyHolder
        this.value = start
        this.interpolator = interpolator
        this.duration = duration
        this.start = start
        this.end = end
    }


    var fraction:Float = 0f

    var value:Float = 0f

    private var interpolator: Interpolator? = null

    private var duration: Long = 0

    private var start: Float = 0.0f

    private var end: Float = 0.0f

    private var propertyHolder:Map<String,CanvasAnimationHolder>? = null

    private fun animate() {
        if (propertyHolder!=null) {
            for (holder in propertyHolder!!.values){
                holder.animate()
            }
        } else {
            if(value<end){
                fraction = interpolator!!.getInterpolation(value/end)
                value += value * fraction
            }
        }
    }

    fun getAnimated(property:String):Pair<Float,Float>{
        return propertyHolder?.get(property)?.getAnimated()!!
    }

    fun getAnimated():Pair<Float,Float>{
        animate()
        return Pair(fraction,value)
    }

    private var listeners: MutableList<Builder.AnimationUpdateListener> = mutableListOf()

    fun addUpdateListener(listener: Builder.AnimationUpdateListener){
        listeners.add(listener)
    }

    class Builder{
        private var interpolator: Interpolator?=null
        private var duration: Long = 0
        private var propertyHolder:MutableMap<String,CanvasAnimationHolder> = mutableMapOf()


        fun withInterpolator(interpolator: Interpolator):Builder = apply{
            this.interpolator = interpolator
        }

        fun withDuration(duration:Long):Builder = apply { this.duration = duration }

        private var startL: Float = 0f
        private var endL: Float = 0f

        fun withInterval(start:Float, end:Float):Builder = apply{
            this.startL = start
            this.endL = end
        }

        fun withProperty(property:String,holder:CanvasAnimationHolder):Builder = apply{
            this.propertyHolder[property] = holder
        }

        fun build():CanvasAnimationUtils{
            return if(interpolator!=null){CanvasAnimationUtils(interpolator!!,duration,startL,endL,propertyHolder)}else{CanvasAnimationUtils(duration,startL,endL,propertyHolder)}
        }

        interface AnimationUpdateListener{
            fun onAnimationUpdate(animator:CanvasAnimationUtils)
        }
    }

    class CanvasAnimationHolder(private val end: Float, private val start: Float, private val interpolator: Interpolator,private val duration:Long) {

        private var value: Float = start

        private var fraction: Float = 0.0f

        fun animate(){
//            if(value<end){
                fraction = Math.abs(end-value)/duration
                value += value * fraction
//            }
        }

        fun getAnimated(): Pair<Float, Float> {
            animate()
            return Pair(fraction,value)
        }
    }
}