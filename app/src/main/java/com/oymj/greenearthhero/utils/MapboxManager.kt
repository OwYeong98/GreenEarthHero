package com.oymj.greenearthhero.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.DrawableRes
import com.mapbox.mapboxsdk.maps.Style
import com.oymj.greenearthhero.R


object MapboxManager {
    const val ID_LOCATION_ICON = "locationicon"

    fun getMapBoxStyle(context: Context): Style.Builder {
        return Style.Builder()
            .fromUri(Style.MAPBOX_STREETS)
            .withImage(ID_LOCATION_ICON, generateBitmap(context,R.drawable.location_icon)!!,false)
    }

    private fun generateBitmap(context: Context, @DrawableRes drawableRes: Int): Bitmap? {
        val drawable: Drawable = context.getResources().getDrawable(drawableRes)
        return getBitmapFromDrawable(drawable)
    }

    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap? {
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            // width and height are equal for all assets since they are ovals.
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
            drawable.draw(canvas)
            bitmap
        }
    }
}