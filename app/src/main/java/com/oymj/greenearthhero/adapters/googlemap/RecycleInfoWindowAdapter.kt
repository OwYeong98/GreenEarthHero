package com.oymj.greenearthhero.adapters.googlemap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.ui.customxmllayout.GoogleMapWrapperForDispatchingTouchEvent

class RecycleInfoWindowAdapter(var context: Context, var mapWrapper: GoogleMapWrapperForDispatchingTouchEvent) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(p0: Marker?): View? {
        return null
    }

    override fun getInfoWindow(marker: Marker?): View {
        var layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var customView = layoutInflater.inflate(R.layout.googlemap_recycle_infowindow, null)

        var btnCollect = customView.findViewById<TextView>(R.id.btnCollect)


        mapWrapper.setMarkerWithInfoWindow(marker!!, customView)


        return customView
    }
}