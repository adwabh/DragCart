package com.adwait.widget.dragcart.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.support.v7.widget.CardView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageView
import com.adwait.widget.dragcart.R


/**
 * Created by Adwait Abhyankar on 1/4/2019.
 */
object ViewCaptureUtils {

    fun createCopyView(view: View, toolbar: Toolbar): View {
        val copy = copyViewImage(view)

        // On preLollipop when we create a copy of card view's content its shadow is copied too
        // and we do not need additional card view.

        return (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CardView(view.context).apply {
                cardElevation = resources.getDimension(R.dimen.card_elevation)
                radius = resources.getDimension(R.dimen.card_corner_radius)
                addView(copy)
            }
        } else {
            copy
        }).apply {
            layoutParams = view.layoutParams
            layoutParams.height = view.height
            layoutParams.width = view.width
            x = view.x
            y = view.y + toolbar.height
        }
    }

    public fun copyViewImage(view: View): ImageView {
        val copy = ImageView(view.context)

        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        copy.setImageBitmap(bitmap)
        return copy
    }


    public fun copyViewImageAsBitmap(view: View):Bitmap{
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

}