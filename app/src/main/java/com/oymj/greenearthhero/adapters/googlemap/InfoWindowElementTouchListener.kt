package com.oymj.greenearthhero.adapters.googlemap

import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.google.android.gms.maps.model.Marker

abstract class InfoWindowElementTouchListener(var view:View) : View.OnTouchListener {

    private var marker:Marker? = null

    private var touchHandler: Handler = Handler()
    private var pressed = false

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        onClickConfirmed(view,marker)

        return false
    }

    fun setMarker(marker: Marker){
        this.marker = marker
    }

    protected abstract fun onClickConfirmed(v: View?, marker: Marker?)
}