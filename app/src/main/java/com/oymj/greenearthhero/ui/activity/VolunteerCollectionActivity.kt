package com.oymj.greenearthhero.ui.activity

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.utils.LocationUtils
import com.oymj.greenearthhero.utils.MapboxManager
import kotlinx.android.synthetic.main.activity_volunteer_collection.*
import kotlinx.android.synthetic.main.activity_volunteer_collection.recycle_bottom_sheet

class VolunteerCollectionActivity : AppCompatActivity() {
    private lateinit var myMapBoxMap: MapboxMap
    private lateinit var myBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private var currentBottomSheetState = BottomSheetBehavior.STATE_COLLAPSED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volunteer_collection)

        setupBottomSheet()

    }

    private fun setupBottomSheet(){
        myBottomSheetBehavior = BottomSheetBehavior.from(recycle_bottom_sheet)

        myBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (currentBottomSheetState != newState) {
                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> {

                        }
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            showBackToListButton()
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {


                        }
                        BottomSheetBehavior.STATE_DRAGGING -> {

                        }
                        BottomSheetBehavior.STATE_SETTLING -> {

                        }
                    }
                }
            }
        })
    }

    private fun setupMapboxMap() {

        mapBoxView?.getMapAsync { mapboxMap ->
            myMapBoxMap = mapboxMap
            //set map style
            mapboxMap.setStyle(MapboxManager.getMapBoxStyle(this)) { style ->


                //show user current location icon in the map
                var locationComponent = mapboxMap.locationComponent
                locationComponent.activateLocationComponent(this, style, true)
                locationComponent.isLocationComponentEnabled = true
                locationComponent.renderMode = RenderMode.COMPASS

                mapboxMap.uiSettings.isCompassEnabled = false

                if (LocationUtils?.getLastKnownLocation() != null) {
                    var userCurrentLocationLatLng = LocationUtils!!.getLastKnownLocation()!!
                    var cameraPosition = CameraPosition.Builder()
                        .target(
                            LatLng(
                                userCurrentLocationLatLng.latitude,
                                userCurrentLocationLatLng.longitude
                            )
                        ) // Sets the new camera position
                        .zoom(17.0) // Sets the zoom
                        .bearing(180.0) // Rotate the camera
                        .tilt(30.0) // Set the camera tilt
                        .build(); // Creates a CameraPosition

                    mapboxMap.animateCamera(
                        CameraUpdateFactory.newCameraPosition(cameraPosition),
                        5000
                    )
                } else {
                    Toast.makeText(
                        this, "Current Location not found! Please turn on Location Services",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showBackToListButton(){
        val slideUpAnimation: Animation = AnimationUtils.loadAnimation(applicationContext,
            R.anim.slide_up
        )
        btnBackToList.visibility=View.VISIBLE
        btnBackToList.startAnimation(slideUpAnimation)

    }

}