package com.oymj.greenearthhero

import android.R
import android.content.res.Configuration
import android.widget.Toast
import androidx.multidex.MultiDexApplication
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapboxMap
import java.lang.String


class MyApplication : MultiDexApplication() {
    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    override fun onCreate() {
        super.onCreate()

        //setup Mapbox
        Mapbox.getInstance(applicationContext, getString(com.oymj.greenearthhero.R.string.mapbox_access_token))

    }

    // Called by the system when the device configuration changes while your component is running.
    // Overriding this method is totally optional!
    override fun onConfigurationChanged ( newConfig : Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    override fun onLowMemory() {
        super.onLowMemory()
    }

}