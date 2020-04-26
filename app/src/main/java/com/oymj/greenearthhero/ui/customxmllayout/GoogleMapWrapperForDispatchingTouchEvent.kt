package com.oymj.greenearthhero.ui.customxmllayout

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class GoogleMapWrapperForDispatchingTouchEvent : RelativeLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private lateinit var googleMap: GoogleMap
    private var bottomOffsetPixels: Int = 0
    var marker: Marker? = null
    private lateinit var infoWindow: View

    fun initializeWrapper(map:GoogleMap,bottomOffsetPixels: Int){
        googleMap = map
        this.bottomOffsetPixels = bottomOffsetPixels
    }

    fun setMarkerWithInfoWindow(marker:Marker,infoWindow:View){
        this.marker = marker
        this.infoWindow = infoWindow
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        var isDispatchSuccessful = false
        if(marker != null && marker?.isInfoWindowShown()?:false && googleMap != null && infoWindow != null){
            var pointTouched = googleMap.projection.toScreenLocation(marker!!.position)

            var copyEvent = MotionEvent.obtain(ev)
            copyEvent.offsetLocation((-pointTouched.x.toDouble() + (infoWindow.width / 2)).toFloat(),
                (-pointTouched.y.toDouble() + infoWindow.height + bottomOffsetPixels).toFloat())

            isDispatchSuccessful = infoWindow.dispatchTouchEvent(copyEvent)

        }
        return isDispatchSuccessful || super.dispatchTouchEvent(ev)
    }
}