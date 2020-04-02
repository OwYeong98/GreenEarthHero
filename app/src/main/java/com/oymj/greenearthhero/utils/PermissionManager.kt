package com.oymj.greenearthhero.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionManager{
    const val PERMISSION_REQUEST_LOCATION = 1001


    val PERMISSIONS_LOCATION = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

    fun haveLocationPermission(activity : Activity):Boolean{
        val allowCourseLocation = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val allowFineLocation = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return allowCourseLocation && allowFineLocation
    }

    fun requestLocationPermission(activity: Activity){
        ActivityCompat.requestPermissions(activity, PERMISSIONS_LOCATION, PERMISSION_REQUEST_LOCATION)
    }


}