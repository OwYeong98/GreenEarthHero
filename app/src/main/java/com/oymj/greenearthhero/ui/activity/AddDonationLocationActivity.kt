package com.oymj.greenearthhero.ui.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.api.ApisImplementation
import com.oymj.greenearthhero.data.DonateLocation
import com.oymj.greenearthhero.data.TomTomAddress
import com.oymj.greenearthhero.data.TomTomPlacesResult
import com.oymj.greenearthhero.data.TomTomPosition
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.ui.dialog.YesOrNoDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.FormUtils
import com.oymj.greenearthhero.utils.LocationUtils
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_add_donation_location.*

class AddDonationLocationActivity: AppCompatActivity() {

    private lateinit var myGoogleMap: GoogleMap
    private lateinit var currentPinnedLocationMarker: Marker
    private lateinit var currentEditingDonateLocation: DonateLocation
    private var currentPinnedLocation: TomTomPlacesResult = TomTomPlacesResult()

    //Better control of onClickListener
    //all button action will be registered here
    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                btnAdd->{
                    submitAddLocationToFirebase()
                }
                btnCancel->{
                    finish()
                }
                btnRecenter->{
                    if(LocationUtils?.getLastKnownLocation() != null) {
                        if(::myGoogleMap != null){
                            var userCurrentLocationLatLng = LocationUtils!!.getLastKnownLocation()!!
                            var newCameraPosition = com.google.android.gms.maps.model.CameraPosition.builder()
                                .target(
                                    com.google.android.gms.maps.model.LatLng(
                                        currentPinnedLocation.latLong?.lat!!,
                                        currentPinnedLocation.latLong?.lon!!
                                    )
                                )
                                .zoom(15f)
                                .bearing(90f)
                                .tilt(0f)
                                .build()
                            myGoogleMap.animateCamera(com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(newCameraPosition))
                        }
                    }
                }
                btnDelete->{
                    var confirmDialog = YesOrNoDialog(this@AddDonationLocationActivity, "Are you sure you want to remove this Donate Location?",callback = {
                        isYesPressed->

                        if(isYesPressed)
                            removeDonationLocationFromFirebase()
                    })
                    confirmDialog.show()
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_donation_location)

        currentPinnedLocation.address = TomTomAddress()
        currentPinnedLocation.latLong = TomTomPosition()

        var donateLocation = intent.getSerializableExtra("donateLocation")
        if(donateLocation!=null){
            currentEditingDonateLocation = donateLocation as DonateLocation
        }

        setupUI()
        setupGoogleMap()
        linkAllButtonWithOnClickListener()

    }

    private fun displayDonateLocationData(data:DonateLocation){
        currentEditingDonateLocation=data

        tvTitle.text = "Edit Donation Location"
        btnAdd.text = "Edit"
        btnDelete.visibility = View.VISIBLE
        inputDonateLocationName.setText(data?.name)
        inputDonateLocationAddress.setText(data?.address)
        updateCurrentPinnedLocationWithLatLong(data.location.lat!!,data.location.lon!!)

        var newCameraPosition = com.google.android.gms.maps.model.CameraPosition.builder()
            .target(LatLng(data.location.lat!!, data.location.lon!!))
            .zoom(15f)
            .bearing(90f)
            .tilt(0f)
            .build()

        myGoogleMap.animateCamera(com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(newCameraPosition))
    }

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            btnAdd,
            btnCancel,
            btnRecenter,
            btnDelete
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }


    private fun submitAddLocationToFirebase(){
        var donateLocationName: String = inputDonateLocationName.text.toString()
        var donateLocationNameError = ""
        donateLocationNameError+= "${FormUtils.isNull("Donate Location Name",donateLocationName)?:""}|"
        donateLocationNameError+= "${FormUtils.isLengthBetween("Donate Location Name",donateLocationName,3,20)?:""}|"

        if(donateLocationNameError.replace("|","") != ""){
            inputDonateLocationName.error = donateLocationNameError.replace("|","\n")
        }else{

            val donateLocationData = hashMapOf(
                "address" to currentPinnedLocation?.address?.fullAddress,
                "location" to GeoPoint(currentPinnedLocation?.latLong?.lat!!,currentPinnedLocation?.latLong?.lon!!),
                "name" to inputDonateLocationName.text.toString(),
                "userId" to FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)
            )

            var loadingDialog = LoadingDialog(this)
            loadingDialog.show()

            if(::currentEditingDonateLocation.isInitialized){
                FirebaseFirestore.getInstance().collection("Donate_Location").document(currentEditingDonateLocation.id).set(donateLocationData)
                    .addOnSuccessListener {
                            doc->
                        loadingDialog.dismiss()


                        var intent = Intent()
                        intent.putExtra("id",currentEditingDonateLocation.id)
                        setResult(2,intent)
                        finish()
                    }
                    .addOnFailureListener {
                            e -> Log.d("error", "Error writing document", e)
                    }
            }else{
                FirebaseFirestore.getInstance().collection("Donate_Location").add(donateLocationData)
                    .addOnSuccessListener {
                            doc->
                        loadingDialog.dismiss()


                        var intent = Intent()
                        intent.putExtra("id",doc.id)
                        setResult(2,intent)
                        finish()
                    }
                    .addOnFailureListener {
                            e ->
                        var errorDialog = ErrorDialog(this, "Error","We have encountered some error when contacting with firebase!")
                        errorDialog.show()
                        Log.d("error", "Error writing document", e)
                    }
            }
        }
    }

    private fun removeDonationLocationFromFirebase(){
        FirebaseFirestore.getInstance().collection("Donate_Location").document(currentEditingDonateLocation.id).delete()
            .addOnSuccessListener {
                doc->

                var intent = Intent()
                setResult(2,intent)
                finish()
            }
            .addOnFailureListener {
                e->
                var errorDialog = ErrorDialog(this, "Error","We have encountered some error when contacting with firebase!")
                errorDialog.show()
                Log.d("error", "Error writing document", e)
            }
    }

    private fun setupGoogleMap(){
        var googleMapFragment = supportFragmentManager.findFragmentById(R.id.googleMapFragmentView) as SupportMapFragment
        googleMapFragment.getMapAsync{
                googleMap ->

            //save the instance for later use
            myGoogleMap = googleMap

            myGoogleMap.isMyLocationEnabled = true
            myGoogleMap.uiSettings.isMyLocationButtonEnabled = false
            myGoogleMap.uiSettings.isCompassEnabled = false


            if (LocationUtils?.getLastKnownLocation() != null) {

                var userCurrentLocationLatLng = LocationUtils!!.getLastKnownLocation()!!
                updateCurrentPinnedLocationWithLatLong(userCurrentLocationLatLng.latitude!!, userCurrentLocationLatLng.longitude!!)

                var newCameraPosition = com.google.android.gms.maps.model.CameraPosition.builder()
                    .target(LatLng(userCurrentLocationLatLng.latitude!!, userCurrentLocationLatLng.longitude!!))
                    .zoom(15f)
                    .bearing(90f)
                    .tilt(0f)
                    .build()

                currentPinnedLocationMarker = myGoogleMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(userCurrentLocationLatLng.latitude,userCurrentLocationLatLng.longitude))
                        .draggable(true)
                        .title("Drag Me")
                        .snippet("Long Click the Marker!"))

                myGoogleMap.animateCamera(com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(newCameraPosition))
            }else{
                var kualaLumpur=LatLng(3.140853, 101.693207)
                updateCurrentPinnedLocationWithLatLong(kualaLumpur.latitude,kualaLumpur.longitude)
                currentPinnedLocationMarker = myGoogleMap.addMarker(
                    MarkerOptions()
                        .position(kualaLumpur)
                        .draggable(true)
                        .title("Drag Me")
                        .snippet("Long Click the Marker!"))

                var newCameraPosition = com.google.android.gms.maps.model.CameraPosition.builder()
                    .target(kualaLumpur)
                    .zoom(15f)
                    .bearing(90f)
                    .tilt(0f)
                    .build()

                myGoogleMap.animateCamera(com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(newCameraPosition))
            }

            //Display edit data
            if(::currentEditingDonateLocation.isInitialized)
                displayDonateLocationData(currentEditingDonateLocation)

            myGoogleMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener{
                override fun onMarkerDragEnd(marker: Marker?) {

                    updateCurrentPinnedLocationWithLatLong(marker?.position?.latitude!!,marker?.position?.longitude!!)
                }

                override fun onMarkerDragStart(p0: Marker?) {
                }

                override fun onMarkerDrag(p0: Marker?) {
                }
            })
        }
    }

    fun updateCurrentPinnedLocationWithLatLong(latitude:Double,longitude:Double){

        var loadingDialog = LoadingDialog(this)
        loadingDialog.show()

        ApisImplementation().reverseGeocodingFromTomTom(this,latitude,longitude,callback = {
                success,response->
            loadingDialog.dismiss()

            if(success){
                currentPinnedLocation!!.latLong?.lat = latitude
                currentPinnedLocation!!.latLong?.lon = longitude

                currentPinnedLocationMarker.position = LatLng(latitude,longitude)

                var address = response!!.addressResult!![0].address

                if(address?.fullAddress == null){
                    address?.fullAddress = "Unnamed Place"
                }

                currentPinnedLocation!!.address = address

                inputDonateLocationAddress.setText(address?.fullAddress)

            }else{
                loadingDialog.dismiss()

                //revert position
                currentPinnedLocationMarker.position = LatLng(currentPinnedLocation!!.latLong?.lat!!,currentPinnedLocation!!.latLong?.lon!!)

                var errorDialog = ErrorDialog(this,"Error", "We have encountered some error when updating address with Latitude and Longitude!")
                errorDialog.show()
            }
        })

    }

    fun setupUI(){
        inputDonateLocationName.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.inputContainerGrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            20f,0)

        inputDonateLocationAddress.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.inputContainerGrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            20f,0)

        btnAdd.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            Color.parseColor("#37B734"),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            20f,0)

        btnCancel.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.lightgrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            20f,0)

    }

}