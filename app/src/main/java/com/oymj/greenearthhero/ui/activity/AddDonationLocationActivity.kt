package com.oymj.greenearthhero.ui.activity

import android.app.Activity
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
import com.oymj.greenearthhero.data.Location
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
    private lateinit var currentEditingLocation: Location
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
                    var intent = Intent()
                    intent.putExtra("id",currentEditingLocation.id)
                    setResult(Activity.RESULT_OK,intent)
                    finish()
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
            currentEditingLocation = donateLocation as Location
        }

        setupUI()
        setupGoogleMap()
        linkAllButtonWithOnClickListener()

    }

    private fun displayDonateLocationData(data:Location){

        tvTitle.text = "Edit Donation Location"
        btnAdd.text = "Edit"

        btnDelete.visibility = View.VISIBLE
        inputDonateLocationName.setText(data?.name)
        inputDonateLocationAddress.setText(data?.address)

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
            Location.getLocationListOfUser(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)!!,callback = {
                success,message,locationList->
                var isLocationNameAlreadyExist = false
                for(location in locationList!!){
                    if(location.name.toLowerCase() == donateLocationName.toLowerCase()){
                        if(::currentEditingLocation.isInitialized){
                            if(currentEditingLocation.name.toLowerCase() != location.name.toLowerCase())
                                isLocationNameAlreadyExist = true
                        }else{
                            isLocationNameAlreadyExist = true
                        }
                    }
                }

                if(isLocationNameAlreadyExist){
                    inputDonateLocationName.error = "Location Name Already exist. Please put another name!"
                    inputDonateLocationName.requestFocus()
                }else{
                    val donateLocationData = hashMapOf(
                        "address" to currentPinnedLocation?.address?.fullAddress,
                        "location" to GeoPoint(currentPinnedLocation?.latLong?.lat!!,currentPinnedLocation?.latLong?.lon!!),
                        "name" to inputDonateLocationName.text.toString(),
                        "userId" to FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)
                    )

                    var loadingDialog = LoadingDialog(this)
                    loadingDialog.show()

                    if(::currentEditingLocation.isInitialized){
                        FirebaseFirestore.getInstance().collection("Location").document(currentEditingLocation.id).set(donateLocationData)
                            .addOnSuccessListener {
                                    doc->
                                loadingDialog.dismiss()


                                var intent = Intent()
                                intent.putExtra("id",currentEditingLocation.id)
                                setResult(Activity.RESULT_OK,intent)
                                finish()
                            }
                            .addOnFailureListener {
                                    e -> Log.d("error", "Error writing document", e)
                            }
                    }else{
                        FirebaseFirestore.getInstance().collection("Location").add(donateLocationData)
                            .addOnSuccessListener {
                                    doc->
                                loadingDialog.dismiss()


                                var intent = Intent()
                                intent.putExtra("id",doc.id)
                                setResult(Activity.RESULT_OK,intent)
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
            })
        }
    }

    private fun removeDonationLocationFromFirebase(){
        FirebaseFirestore.getInstance().collection("Location").document(currentEditingLocation.id).delete()
            .addOnSuccessListener {
                doc->

                var intent = Intent()
                setResult(Activity.RESULT_OK,intent)
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

            //Display edit data
//            if(::currentEditingLocation.isInitialized){
//
//            }
//                displayDonateLocationData(currentEditingLocation)
            var userCurrentLocationLatLng:LatLng? = null
            if(::currentEditingLocation.isInitialized){
                userCurrentLocationLatLng = LatLng(currentEditingLocation.location.lat!!,currentEditingLocation.location.lon!!)
                displayDonateLocationData(currentEditingLocation)
            }else if (LocationUtils?.getLastKnownLocation() != null) {
                userCurrentLocationLatLng = LatLng(LocationUtils!!.getLastKnownLocation()!!.latitude,LocationUtils!!.getLastKnownLocation()!!.longitude)
                updateCurrentPinnedLocationWithLatLong(userCurrentLocationLatLng.latitude!!, userCurrentLocationLatLng.longitude!!)
            }else{
                var kualaLumpur=LatLng(3.140853, 101.693207)
                userCurrentLocationLatLng = kualaLumpur
                updateCurrentPinnedLocationWithLatLong(kualaLumpur.latitude,kualaLumpur.longitude)
            }

            var newCameraPosition = com.google.android.gms.maps.model.CameraPosition.builder()
                .target(userCurrentLocationLatLng)
                .zoom(15f)
                .bearing(90f)
                .tilt(0f)
                .build()

            currentPinnedLocationMarker = myGoogleMap.addMarker(
                MarkerOptions()
                    .position(userCurrentLocationLatLng)
                    .draggable(true)
                    .title("Drag Me")
                    .snippet("Long Click the Marker!"))

            myGoogleMap.animateCamera(com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(newCameraPosition))



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