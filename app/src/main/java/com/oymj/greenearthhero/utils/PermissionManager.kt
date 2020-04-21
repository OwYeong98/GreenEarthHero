package com.oymj.greenearthhero.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionManager{
    const val PERMISSION_REQUEST_LOCATION = 1001
    const val PERMISSION_REQUEST_STORAGE = 1002


    val PERMISSIONS_LOCATION = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    fun haveLocationPermission(activity : Activity):Boolean{
        val allowCourseLocation = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val allowFineLocation = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return allowCourseLocation && allowFineLocation
    }

    fun requestLocationPermission(activity: Activity){
        ActivityCompat.requestPermissions(activity, PERMISSIONS_LOCATION, PERMISSION_REQUEST_LOCATION)
    }

    fun hasStorageReadWritePermission(activity: Activity):Boolean{
        val allowRead = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val allowWrite = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val allowCamera = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

        return allowRead && allowWrite && allowCamera
    }

    fun requestStorageReadWritePermission(activity: Activity){
        ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, PERMISSION_REQUEST_STORAGE)
    }


}