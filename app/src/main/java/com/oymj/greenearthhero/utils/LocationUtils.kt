package com.oymj.greenearthhero.utils

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.*


object LocationUtils{

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //variable that store request and callback
    private lateinit var locRequest: LocationRequest
    private lateinit var locCallback: LocationCallback

    private var lastKnownLocation:Location? = null

    private var callbackRequestWhenLocationUpdatedList = mutableMapOf<String,(Location)->Unit>()

    fun getLastKnownLocation(): Location? {
        return lastKnownLocation
    }

    fun startConstantUpdateLocation(context: Context){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        locRequest = LocationRequest()
        locRequest.interval = 50000
        locRequest.fastestInterval = 50000
        locRequest.smallestDisplacement = 170f // 170 m = 0.1 mile
        locRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //set according to your app function

        locCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                Log.d("location", "location updated")
                if (locationResult.locations.isNotEmpty()) {
                    //current latest location
                    val location = locationResult.lastLocation

                    Log.d("location", "new location-> lat: ${location.latitude} | long: ${location.longitude}")
                    //update lastKnownLocation
                    lastKnownLocation = location

                    for(callback in callbackRequestWhenLocationUpdatedList.values){
                        //update all activity that request callback when location get updated
                        callback(location)
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locRequest,
            locCallback,
            null /* Looper */
        )
    }

    // stop location updates
    fun stopConstantUpdateLocation() {
        fusedLocationClient.removeLocationUpdates(locCallback)
    }

    //add a callback so when location get updated the callback will be called
    fun addLocationUpdatesCallback(key: String, callback:(Location)->Unit)
    {
        callbackRequestWhenLocationUpdatedList.put(key,callback)
    }

    fun removeLocationUpdatesCallback(key: String)
    {
        callbackRequestWhenLocationUpdatedList.remove(key)
    }





}