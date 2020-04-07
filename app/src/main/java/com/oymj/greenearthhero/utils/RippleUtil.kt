package com.oymj.greenearthhero.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.*
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RoundRectShape
import android.graphics.drawable.shapes.Shape
import android.os.Build
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.oymj.greenearthhero.R
import java.util.*

object RippleUtil {
    const val SHAPE_OVAL = GradientDrawable.OVAL
    const val SHAPE_RECTANGLE = GradientDrawable.RECTANGLE
    const val SHAPE_RING = GradientDrawable.RING

    fun getRippleButtonOutlineDrawable(context : Context, @ColorInt normalColor : Int, @ColorInt pressedColor : Int, @ColorInt outlineColor : Int, cornerRadius : Float = 0f, outlineThickness : Int = 3) : Drawable {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            return RippleDrawable(
                ColorStateList.valueOf(pressedColor),
                getStateListDrawableOutline(
                    GradientDrawable.RECTANGLE,
                    normalColor,
                    normalColor,
                    outlineColor,
                    cornerRadius,
                    outlineThickness
                ),
                getRippleMask(ContextCompat.getColor(context, R.color.white), cornerRadius, SHAPE_RECTANGLE)
            )
        }
        else{
            return getStateListDrawableOutline(
                GradientDrawable.RECTANGLE,
                normalColor,
                pressedColor,
                outlineColor,
                cornerRadius,
                outlineThickness
            )
        }

    }

    fun getGradientRippleButtonOutlineDrawable(context : Context, @ColorInt startColor: Int, endColor : Int, @ColorInt pressedColor : Int, @ColorInt outlineStartColor : Int, @ColorInt outlineEndColor : Int, cornerRadius : Float = 0f, outlineThickness : Int = 3, orientation : GradientDrawable.Orientation = GradientDrawable.Orientation.BL_TR) : Drawable {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            val rippleDrawable = RippleDrawable(ColorStateList.valueOf(pressedColor),
                getGradientOutlineDrawable(startColor, endColor, outlineStartColor, outlineEndColor, SHAPE_RECTANGLE, outlineThickness, GradientDrawable.Orientation.BL_TR, cornerRadius),
                getRippleMask(ContextCompat.getColor(context, R.color.white), cornerRadius, SHAPE_RECTANGLE)
            )
            return rippleDrawable
        }
        else{
            return getStateListDrawableOutline(
                GradientDrawable.RECTANGLE,
                startColor,
                pressedColor,
                outlineStartColor,
                cornerRadius,
                outlineThickness
            )
        }
    }

    fun getGradientOutlineDrawable(@ColorInt innerStartColor: Int, innerEndColor : Int, @ColorInt outlineStartColor: Int, @ColorInt outlineEndColor: Int, shape: Int, outlineThickness: Int, angle : GradientDrawable.Orientation, cornerRadius: Float) : Drawable{

        val outlineDrawable = GradientDrawable(angle, intArrayOf(outlineStartColor, outlineEndColor))

        val innerDrawable = GradientDrawable(angle, intArrayOf(innerStartColor, innerEndColor))

        if(shape == SHAPE_OVAL){
            outlineDrawable.shape = GradientDrawable.OVAL
            innerDrawable.shape = GradientDrawable.OVAL
        }
        else{
            outlineDrawable.shape = GradientDrawable.RECTANGLE
            outlineDrawable.cornerRadius = cornerRadius
            innerDrawable.cornerRadius = cornerRadius - outlineThickness
        }

        val layerDrawable = LayerDrawable(arrayOf(outlineDrawable, innerDrawable))


        if(outlineThickness >= 0) {
            layerDrawable.setLayerInset(
                1,
                outlineThickness,
                outlineThickness,
                outlineThickness,
                outlineThickness
            )
        }

        return layerDrawable
    }

    private fun getStateListDrawableOutline(shape : Int, @ColorInt normalColor: Int, @ColorInt pressedColor: Int, @ColorInt outlineColor: Int, cornerRadius : Float = 0f, outlineThickness: Int = 3, dashWidth: Int = 0, dashGap : Int = 0): StateListDrawable {
        val states = StateListDrawable()

        val pressedDrawable = GradientDrawable()
        val normalDrawable = GradientDrawable()

        pressedDrawable.setColor(pressedColor)
        normalDrawable.setColor(normalColor)

        if(shape == SHAPE_OVAL){
            pressedDrawable.shape = GradientDrawable.OVAL
            normalDrawable.shape = GradientDrawable.OVAL
        }
        else{
            pressedDrawable.shape = GradientDrawable.RECTANGLE
            normalDrawable.shape = GradientDrawable.RECTANGLE
            pressedDrawable.cornerRadius = cornerRadius
            normalDrawable.cornerRadius = cornerRadius
        }

        if(dashWidth <= 0 && dashGap <= 0) {
            pressedDrawable.setStroke(outlineThickness, outlineColor)
            normalDrawable.setStroke(outlineThickness, outlineColor)
        }
        else{
            pressedDrawable.setStroke(outlineThickness, outlineColor, dashWidth.toFloat(), dashGap.toFloat())
            normalDrawable.setStroke(outlineThickness, outlineColor, dashWidth.toFloat(), dashGap.toFloat())
        }

        states.addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)
        states.addState(intArrayOf(android.R.attr.state_focused), pressedDrawable)
        states.addState(intArrayOf(android.R.attr.state_activated), pressedDrawable)
        states.addState(intArrayOf(), normalDrawable)
        return states
    }

    private fun getRippleMask(color: Int, cornerRadius : Float = 0f, shapeType : Int = SHAPE_RECTANGLE): Drawable {

        val outerRadii = FloatArray(8)
        Arrays.fill(outerRadii, cornerRadius)

        val shape : Shape
        if(shapeType == SHAPE_RECTANGLE){
            shape = RoundRectShape(outerRadii, null, null)
        }
        else if(shapeType == SHAPE_OVAL){
            shape = OvalShape()
        }
        else{
            shape = RoundRectShape(outerRadii, null, null)
        }

        val shapeDrawable = ShapeDrawable(shape)
        shapeDrawable.paint.color = color
        return shapeDrawable

    }

}