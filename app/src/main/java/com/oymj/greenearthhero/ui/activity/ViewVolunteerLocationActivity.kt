package com.oymj.greenearthhero.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.utils.LocationUtils
import kotlinx.android.synthetic.main.activity_view_volunteer_location.*

class ViewVolunteerLocationActivity : AppCompatActivity() {

    private lateinit var myGoogleMap: GoogleMap

    private lateinit var listener: ListenerRegistration

    private lateinit var volunteerUserId:String
    private var destLat:Double = 0.0
    private var destLong:Double = 0.0

    private lateinit var destinationMarker: Marker
    private lateinit var volunteerMarker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_volunteer_location)

        volunteerUserId = intent.getStringExtra("volunteerUserId")
        destLat = intent.getDoubleExtra("destLat",0.0)
        destLong = intent.getDoubleExtra("destLong",0.0)

        btnBack.setOnClickListener {
            finish()
        }

        btnRecenterRider.setOnClickListener{
            if(::myGoogleMap.isInitialized){
                var newCameraPosition = CameraPosition.builder()
                    .target(LatLng(volunteerMarker.position.latitude,volunteerMarker.position.longitude))
                    .zoom(15f)
                    .bearing(90f)
                    .tilt(0f)
                    .build()
                myGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition))
            }
        }

        btnRecenter.setOnClickListener {
            if (LocationUtils.getLastKnownLocation() != null && ::myGoogleMap.isInitialized){
                var newCameraPosition = CameraPosition.builder()
                    .target(LatLng(LocationUtils.getLastKnownLocation()!!.latitude,LocationUtils.getLastKnownLocation()!!.longitude))
                    .zoom(15f)
                    .bearing(90f)
                    .tilt(0f)
                    .build()
                myGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition))
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if(!::myGoogleMap.isInitialized){
            setupGoogleMap()
        }else{
            listenToVolunteerLocationUpdate()
        }
    }

    override fun onStop() {
        super.onStop()
        listener.remove()
    }

    private fun setupGoogleMap(){
        var googleMapFragment = supportFragmentManager.findFragmentById(R.id.googleMapFragmentView) as SupportMapFragment
        googleMapFragment.getMapAsync{
                googleMap ->

            //save the instance for later use
            myGoogleMap = googleMap

            myGoogleMap.isMyLocationEnabled = true
            myGoogleMap.uiSettings.isCompassEnabled = false

            destinationMarker = myGoogleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(destLat,destLong))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_flag)))

            volunteerMarker = myGoogleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(0.0,0.0))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_rider)))

            listenToVolunteerLocationUpdate()
        }
    }


    private fun listenToVolunteerLocationUpdate(){
        var db = FirebaseFirestore.getInstance()

        listener = db.collection("Users").document(volunteerUserId).addSnapshotListener{
                snapshot,e->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null ) {
                //update the UI

                var volunteerLat = snapshot.getDouble("locationLat")
                var volunteerLong = snapshot.getDouble("locationLong")

                if(volunteerLat!=null && volunteerLong!=null ){
                    volunteerMarker.position = LatLng(volunteerLat,volunteerLong)

                    var newCameraPosition = CameraPosition.builder()
                        .target(LatLng(volunteerLat,volunteerLong))
                        .zoom(15f)
                        .bearing(90f)
                        .tilt(0f)
                        .build()
                    myGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition))
                }
            }
        }
    }
}