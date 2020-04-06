package com.oymj.greenearthhero.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolDragListener
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.api.ApisImplementation
import com.oymj.greenearthhero.utils.LocationUtils
import com.oymj.greenearthhero.data.FeaturePlaces
import com.oymj.greenearthhero.data.TomTomPlacesResult
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.ui.dialog.SuccessDialog
import com.oymj.greenearthhero.ui.fragment.SearchAddressResultFragment
import com.oymj.greenearthhero.utils.FormUtils
import com.oymj.greenearthhero.utils.MapboxManager
import com.oymj.greenearthhero.utils.MapboxManager.getMapBoxStyle
import com.oymj.greenearthhero.utils.RippleUtil

import kotlinx.android.synthetic.main.activity_recycle.*
import java.text.SimpleDateFormat
import java.util.*


class RecycleActivity : AppCompatActivity() {
    private lateinit var myBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var myMapBoxMap: MapboxMap
    private lateinit var mapBoxMapSymbolManager: SymbolManager
    private lateinit var locationSearchResultFragment: SearchAddressResultFragment
    private var currentPinnedLocation: TomTomPlacesResult? = null
    private lateinit var currentPinnedLocationSymbol: Symbol
    private var currentBottomSheetState = BottomSheetBehavior.STATE_COLLAPSED
    private var isLocationPanelOpened = false



    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null
    private var accurateLocation: Location? = null

    //Better control of onClickListener
    //all button action will be registered here
    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                recycle_expanded_closebtn -> {
                    if(syncMaterialInfoWithEditText())
                       myBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                recycle_menu_icon -> {
                    var intent = Intent(this@RecycleActivity, MenuActivity::class.java)
                    startActivity(intent)
                }
                recycle_request_location_label -> {
                    showLocationPanel(true)
                }
                recycle_location_search_back_btn -> {
                    showLocationPanel(false)
                }
                recycle_mapbox_recenter_btn -> {
                    if(LocationUtils?.getLastKnownLocation() != null) {
                        if(::myMapBoxMap != null){
                            var userCurrentLocationLatLng = LocationUtils!!.getLastKnownLocation()!!
                            var cameraPosition = CameraPosition.Builder()
                                .target(LatLng(userCurrentLocationLatLng.latitude, userCurrentLocationLatLng.longitude)) // Sets the new camera position
                                .zoom(17.0) // Sets the zoom
                                .bearing(180.0) // Rotate the camera
                                .tilt(30.0) // Set the camera tilt
                                .build(); // Creates a CameraPosition

                            myMapBoxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),5000)
                        }
                    }
                }
                btnVolunteerCollection -> {
                    startActivity(Intent(this@RecycleActivity, VolunteerCollectionActivity::class.java))
                }
                btnMyRequest -> {

                }
                btnMyVolunteer -> {

                }
                btnRecycleNow -> {
                    sendRecycleRequestToFirebase()
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_recycle)

        linkAllButtonWithOnClickListener()
        syncMaterialInfoWithEditText()

        setupUI()

        setupMapboxMap()
        setupBottomSheet()
        setupLocationSearchPanel()
    }

    private fun setupUI(){
        btnRecycleNow.background = RippleUtil.getGradientRippleButtonOutlineDrawable(this,
            Color.parseColor("#7CDF75"),
            Color.parseColor("#6BF261"),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            resources.getColor(R.color.transparent),
            50f,0, GradientDrawable.Orientation.LEFT_RIGHT
        )

        btnMyRequest.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.darkgrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            25f, 0)

        btnVolunteerCollection.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.darkgrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            25f, 0)

        btnMyVolunteer.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.darkgrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            25f, 0)
    }

    private fun setupMapboxMap(){

        mapBoxView?.getMapAsync{
            mapboxMap ->
            myMapBoxMap = mapboxMap
            //set map style
            mapboxMap.setStyle(getMapBoxStyle(this)) {
                style ->

                mapBoxMapSymbolManager = SymbolManager(mapBoxView,mapboxMap,style)

                //show user current location icon in the map
                var locationComponent = mapboxMap.locationComponent
                locationComponent.activateLocationComponent(this,style,true)
                locationComponent.isLocationComponentEnabled = true
                locationComponent.renderMode = RenderMode.COMPASS

                mapboxMap.uiSettings.isCompassEnabled = false

                if(LocationUtils?.getLastKnownLocation() != null) {
                    var userCurrentLocationLatLng = LocationUtils!!.getLastKnownLocation()!!
                    var cameraPosition = CameraPosition.Builder()
                        .target(LatLng(userCurrentLocationLatLng.latitude, userCurrentLocationLatLng.longitude)) // Sets the new camera position
                        .zoom(17.0) // Sets the zoom
                        .bearing(180.0) // Rotate the camera
                        .tilt(30.0) // Set the camera tilt
                        .build(); // Creates a CameraPosition

                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),5000)
                }else{
                    Toast.makeText(this,"Current Location not found! Please turn on Location Services",Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun setupBottomSheet(){
        myBottomSheetBehavior = BottomSheetBehavior.from(recycle_bottom_sheet)

        myBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (currentBottomSheetState == BottomSheetBehavior.STATE_EXPANDED) {
                    recycle_bottom_sheet.setBackgroundResource(R.drawable.white_rounded_corner_bg_with_shadow)
                } else {
                    recycle_bottom_sheet.setBackgroundColor(Color.WHITE)
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (currentBottomSheetState != newState) {
                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            currentBottomSheetState = BottomSheetBehavior.STATE_COLLAPSED
                            recycle_bottom_sheet.setBackgroundResource(R.drawable.white_rounded_corner_bg_with_shadow)
                            showCollapseView()
                        }
                        BottomSheetBehavior.STATE_HIDDEN -> {

                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            currentBottomSheetState = BottomSheetBehavior.STATE_EXPANDED
                            recycle_bottom_sheet.setBackgroundColor(Color.WHITE)
                            showExpandedView()

                        }
                        BottomSheetBehavior.STATE_DRAGGING -> {
                            //HACK: Lock the drag down action
                            //Alway perform drag up action when user drag
                            // if user drag, Change the state to STATE_EXPANDED
                            myBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                        BottomSheetBehavior.STATE_SETTLING -> {

                        }
                    }
                }
            }
        })
    }

    private fun setupLocationSearchPanel(){
        locationSearchResultFragment = SearchAddressResultFragment{
            data->
            selectLocationFromSearchPanel(data)
        }

        //replace framelayout with fragment
        var fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.recycle_location_search_result_section,locationSearchResultFragment)
        fragmentTransaction.commit()

        recycle_location_search_edittext.setOnEditorActionListener { v, actionId, event ->
            var handled=false
            if(actionId == EditorInfo.IME_ACTION_SEND){
                handled = true
                locationSearchResultFragment.searchAddressFromMapBoxApi(v.text.toString())
            }
            handled
        }
    }

    //this function is called when user select location
    fun selectLocationFromSearchPanel(data:TomTomPlacesResult){
        //hide location panel
        showLocationPanel(false)

        //store current selected location
        this.currentPinnedLocation = data
        if(data.address?.fullAddress == null){
            updateFeaturePlaceAddressWithLatLong(this.currentPinnedLocation!!)
        }else{
            //update the selected location
            recycle_request_location_label.text = data.address?.fullAddress
        }

        mapBoxMapSymbolManager.iconAllowOverlap = true
        mapBoxMapSymbolManager.iconIgnorePlacement = true

        //pin selected location in map
        if(!::currentPinnedLocationSymbol.isInitialized){
            //first time
            currentPinnedLocationSymbol = mapBoxMapSymbolManager.create(SymbolOptions()
                .withLatLng(LatLng(data.latLong?.lat!!,data.latLong?.lon!!))
                .withIconImage(MapboxManager.ID_LOCATION_ICON)
                .withIconSize(2f)
                .withDraggable(true))

            mapBoxMapSymbolManager.addDragListener(object: OnSymbolDragListener {
                 override fun onAnnotationDragStarted(annotation: Symbol?) {

                  }
                  override fun onAnnotationDrag(annotation: Symbol?) {

                  }
                  override fun onAnnotationDragFinished(annotation: Symbol?) {
                        currentPinnedLocation!!.latLong?.lat = annotation?.latLng?.latitude!!
                        currentPinnedLocation!!.latLong?.lon = annotation?.latLng?.longitude!!

                        updateFeaturePlaceAddressWithLatLong(currentPinnedLocation!!)
                  }
            })

        }else{
            //change the location of the pin
            currentPinnedLocationSymbol.latLng = LatLng(LatLng(data.latLong?.lat!!,data.latLong?.lon!!))
            mapBoxMapSymbolManager.update(currentPinnedLocationSymbol)
        }

        //move to selected location
        var cameraPosition = CameraPosition.Builder()
            .target(LatLng(data.latLong?.lat!!,data.latLong?.lon!!)) // Sets the new camera position
            .zoom(17.0) // Sets the zoom
            .bearing(180.0) // Rotate the camera
            .tilt(30.0) // Set the camera tilt
            .build(); // Creates a CameraPosition

        myMapBoxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),5000)
    }

    fun updateFeaturePlaceAddressWithLatLong(location:TomTomPlacesResult){
        var loadingDialog = LoadingDialog(this)
        loadingDialog.show()

        ApisImplementation().reverseGeocodingFromTomTom(this,location.latLong?.lat!!,location.latLong?.lon!!,callback = {
            success,response->
            loadingDialog.hide()

            if(success){

                var address = response!!.addressResult!![0].address

                if(address?.fullAddress == null){
                    address?.fullAddress = "Unnamed Place"
                }

                location.address = address

                recycle_request_location_label.text = address?.fullAddress

            }else{
                loadingDialog.hide()

                var errorDialog = ErrorDialog(this,"Error", "We have encountered some error when updating address with Latitude and Longitude!")
                errorDialog.show()
            }
        })
    }

    private fun sendRecycleRequestToFirebase(){

        if(currentPinnedLocation != null){
            var metalAmount = recycle_metal_info_textview.text.toString().replace("KG","").trim().toInt()
            var glassAmount = recycle_glass_info_textview.text.toString().replace("KG","").trim().toInt()
            var paperAmount = recycle_paper_info_textview.text.toString().replace("KG","").trim().toInt()
            var plasticAmount = recycle_plastic_info_textview.text.toString().replace("KG","").trim().toInt()
            var address = currentPinnedLocation!!.address!!.fullAddress
            var latLong = currentPinnedLocation!!.latLong

            var totalAmount = metalAmount+glassAmount+paperAmount+plasticAmount
            if(totalAmount > 0){

                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val currentDateTime: String = dateFormat.format(Date()) // Find todays date


                //create a firebase document
                val recycleRequestDocument = hashMapOf(
                "userId" to FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this),
                "date_requested" to currentDateTime,
                "address" to address,
                "location" to GeoPoint(latLong?.lat!!,latLong?.lon!!),
                "glass_weight" to glassAmount,
                "metal_weight" to metalAmount,
                "plastic_weight" to plasticAmount,
                "paper_weight" to paperAmount,
                "accepted_collect_by" to ""
                )

                var loadingDialog = LoadingDialog(this)
                loadingDialog.show()

                FirebaseFirestore.getInstance().collection("Recycle_Request").add(recycleRequestDocument)
                    .addOnSuccessListener {
                        loadingDialog.hide()

                        var successDialog = SuccessDialog(this,"Success","You request is made successfully!")
                        successDialog.show()
                    }
                    .addOnFailureListener {
                    e ->
                        Log.d("error", "Error writing document", e)
                        loadingDialog.hide()

                        var errorDialog = ErrorDialog(this,"Oops","Sorry, We have encountered some error when connecting with Firebase.")
                        errorDialog.show()
                }
            }else{
                var errorDialog = ErrorDialog(this,"Input Error","You must at least recycle some material!")
                errorDialog.show()
            }
        }else{
            var errorDialog = ErrorDialog(this,"Input Error","Please select a location first! You can select location by pressing select location button located at the most top of the screen")
            errorDialog.show()
        }

    }

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            recycle_expanded_closebtn,
            recycle_menu_icon,
            recycle_request_location_label,
            recycle_location_search_back_btn,
            recycle_mapbox_recenter_btn,
            btnVolunteerCollection,
            btnMyVolunteer,
            btnMyRequest,
            btnRecycleNow
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }

    //expand bottom sheet
    private fun showExpandedView() {
        val fadeInAnimation: Animation =
            AnimationUtils.loadAnimation(applicationContext,
                R.anim.fade_in
            )
        val fadeOutAnimation: Animation =
            AnimationUtils.loadAnimation(applicationContext,
                R.anim.fade_out
            )

        recycle_bottom_sheet_collapse_view.startAnimation(fadeOutAnimation)
        fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onAnimationEnd(animation: Animation?) {
                recycle_bottom_sheet_collapse_view.alpha = 0.0f
                recycle_bottom_sheet_collapse_view.visibility = View.GONE


            }

            override fun onAnimationStart(animation: Animation?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
        recycle_bottom_sheet_expanded_view.visibility = View.VISIBLE
        recycle_bottom_sheet_expanded_view.alpha = 1f
        recycle_bottom_sheet_expanded_view.startAnimation(fadeInAnimation)


    }

    //collapse bottom sheet
    private fun showCollapseView() {
        val fadeInAnimation: Animation =
            AnimationUtils.loadAnimation(applicationContext,
                R.anim.fade_in
            )
        val fadeOutAnimation: Animation =
            AnimationUtils.loadAnimation(applicationContext,
                R.anim.fade_out
            )

        recycle_bottom_sheet_expanded_view.startAnimation(fadeOutAnimation)
        fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                recycle_bottom_sheet_expanded_view.alpha = 0.0f
                recycle_bottom_sheet_expanded_view.visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })
        recycle_bottom_sheet_collapse_view.visibility = View.VISIBLE
        recycle_bottom_sheet_collapse_view.alpha = 1f
        recycle_bottom_sheet_collapse_view.startAnimation(fadeInAnimation)
    }

    private fun syncMaterialInfoWithEditText(): Boolean {
        var metalQty: String = recyle_metal_edittext.text.toString()
        var plasticQty: String = recyle_plastic_edittext.text.toString()
        var paperQty: String = recyle_paper_edittext.text.toString()
        var glassQty: String = recyle_glass_edittext.text.toString()

        var metalQtyError = "" + FormUtils.isNull("Metal Quantity", metalQty)
        var plasticQtyError = "" + FormUtils.isNull("Plastic Quantity", plasticQty)
        var paperQtyError = "" + FormUtils.isNull("Paper Quantity", paperQty)
        var glassQtyError = "" + FormUtils.isNull("Glass Quantity", glassQty)

        if (metalQtyError != "") {
            recyle_metal_edittext.setError(metalQtyError)
        }

        if (plasticQtyError != "") {
            recyle_plastic_edittext.setError(plasticQtyError)
        }

        if (paperQtyError != "") {
            recyle_paper_edittext.setError(paperQtyError)
        }
        if (glassQtyError != "") {
            recyle_glass_edittext.setError(glassQtyError)
        }

        //if no error
        if (metalQtyError + plasticQtyError + paperQtyError + glassQtyError == "") {
            //convert to int first then convert to string so that if user key in "0009", we will only show 9
            recycle_metal_info_textview.setText("${metalQty.toInt()} KG")
            recycle_plastic_info_textview.setText("${plasticQty.toInt()} KG")
            recycle_paper_info_textview.setText("${paperQty.toInt()} KG")
            recycle_glass_info_textview.setText("${glassQty.toInt()} KG")
            //return success
            return true
        } else {
            ErrorDialog(this, "Input Error", "Please complete the form").show()
            return false
        }
    }



    private fun showLocationPanel(yesOrNo: Boolean) {
        val slideUpAnimation: Animation =
            AnimationUtils.loadAnimation(applicationContext,
                R.anim.slide_up
            )
        val slideDownAnimation: Animation =
            AnimationUtils.loadAnimation(applicationContext,
                R.anim.slide_down
            )
        isLocationPanelOpened = yesOrNo
        if (yesOrNo) {
            recycle_location_select_panel.visibility = View.VISIBLE
            recycle_bottom_sheet.visibility = View.GONE
            //make the cursor focus on edittext
            if (recycle_location_search_edittext.requestFocus()) {
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(
                    recycle_location_search_edittext,
                    InputMethodManager.SHOW_IMPLICIT
                )
            }

            recycle_location_search_result_section.startAnimation(slideUpAnimation)
        } else {
            recycle_location_search_edittext.clearFocus()
            recycle_bottom_sheet.visibility = View.VISIBLE
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(recycle_location_search_edittext.getWindowToken(), 0)

            slideDownAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onAnimationEnd(animation: Animation?) {
                    recycle_location_select_panel.visibility = View.GONE
                }

                override fun onAnimationStart(animation: Animation?) {
                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            })
            recycle_location_search_result_section.startAnimation(slideDownAnimation)
        }

    }

    override fun onBackPressed() {
        if (currentBottomSheetState == BottomSheetBehavior.STATE_EXPANDED) {
            myBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else if (isLocationPanelOpened) {
            //if location panel opened, back button will close it
            showLocationPanel(false)
        } else {
            //else call normal back function
            super.onBackPressed()
        }
    }

}
