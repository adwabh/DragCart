package com.adwait.widget.dragcart.utils

import android.graphics.*
import java.io.ByteArrayInputStream
import java.io.InputStream
import android.graphics.Bitmap.CompressFormat
import java.io.ByteArrayOutputStream


/**
 * Created by Adwait Abhyankar on 1/22/2019.
 */
object ImageUtils {
    fun Bitmap.convertToStream():InputStream{
        val bos = ByteArrayOutputStream()
        this.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
        val bitmapData = bos.toByteArray()
        return ByteArrayInputStream(bitmapData)
    }

    fun getBitmap(cX:Float,cY:Float,radius:Float):Bitmap{
        val bitmap = Bitmap.createBitmap(radius.toInt(), radius.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawCircle(cX,cY,radius, Paint())
        return bitmap
    }

    fun GetBitmapClippedCircle(bitmap: Bitmap, radius: Int): Bitmap {

        val width = bitmap.width
        val height = bitmap.height
        val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val path = Path()
        path.addCircle(
                (width / 2).toFloat(), (height / 2).toFloat(), Math.min(radius,Math.min(width, height / 2)).toFloat(), Path.Direction.CCW)

        val canvas = Canvas(outputBitmap)
        canvas.clipPath(path)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        return outputBitmap
    }

}