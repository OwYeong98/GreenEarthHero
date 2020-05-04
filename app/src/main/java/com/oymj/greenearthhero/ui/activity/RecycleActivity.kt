package com.oymj.greenearthhero.ui.activity

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.googlemap.InfoWindowElementTouchListener
import com.oymj.greenearthhero.api.ApisImplementation
import com.oymj.greenearthhero.data.*
import com.oymj.greenearthhero.ui.customxmllayout.GoogleMapWrapperForDispatchingTouchEvent
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.ui.dialog.SuccessDialog
import com.oymj.greenearthhero.ui.dialog.YesOrNoDialog
import com.oymj.greenearthhero.ui.fragment.SearchAddressResultFragment
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.FormUtils
import com.oymj.greenearthhero.utils.LocationUtils
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_menu.menu_bg
import kotlinx.android.synthetic.main.activity_recycle.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class RecycleActivity : AppCompatActivity() {
    //variable to keep track data
    private lateinit var myBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var myGoogleMap: GoogleMap
    private lateinit var currentPinnedLocationMarker: Marker
    private lateinit var locationSearchResultFragment: SearchAddressResultFragment
    private var currentPinnedLocation: TomTomPlacesResult? = null
    private var currentBottomSheetState = BottomSheetBehavior.STATE_COLLAPSED
    private var isLocationPanelOpened = false
    private var isLoadingChat = false

    private var infoButtonListenerList = ArrayList<InfoWindowElementTouchListener>()
    private lateinit var listener:ListenerRegistration

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
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
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
                        if(::myGoogleMap != null){
                            var userCurrentLocationLatLng = LocationUtils!!.getLastKnownLocation()!!
                            var newCameraPosition = com.google.android.gms.maps.model.CameraPosition.builder()
                                .target(
                                    com.google.android.gms.maps.model.LatLng(
                                        userCurrentLocationLatLng.latitude!!,
                                        userCurrentLocationLatLng.longitude!! - 0.01
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
                btnVolunteerCollection -> {
                    startActivity(Intent(this@RecycleActivity, VolunteerCollectionActivity::class.java))
                    overridePendingTransition(R.anim.slide_up_slow, R.anim.freeze)
                }
                btnMyRequest -> {
                    startActivity(Intent(this@RecycleActivity, MyRequestAndRequestHistoryActivity::class.java))
                    overridePendingTransition(R.anim.slide_up_slow, R.anim.freeze)
                }
                btnMyVolunteer -> {
                    startActivity(Intent(this@RecycleActivity, MyVolunteerActivity::class.java))
                    overridePendingTransition(R.anim.slide_up_slow, R.anim.freeze)
                }
                btnRecycleNow -> {
                    sendRecycleRequestToFirebase()
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.freeze,R.anim.freeze)
        setContentView(R.layout.activity_recycle)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            revertMenuPageCircularRevealActivity()
        }

        linkAllButtonWithOnClickListener()
        syncMaterialInfoWithEditText()

        setupUI()

        setupGoogleMap()
        setupBottomSheet()
        setupLocationSearchPanel()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            revertMenuPageCircularRevealActivity()
        }
    }

    override fun onStart() {
        super.onStart()
        listenToFirebaseCollectionChangesAndUpdateUI()
    }

    override fun onStop() {
        super.onStop()
        listener.remove()
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

    private fun setupGoogleMap(){
        var googleMapFragment = supportFragmentManager.findFragmentById(R.id.googleMapFragmentView) as SupportMapFragment
        googleMapFragment.getMapAsync{
                googleMap ->

            //save the instance for later use
            myGoogleMap = googleMap

            myGoogleMap.isMyLocationEnabled = true
            myGoogleMap.uiSettings.isMyLocationButtonEnabled = false
            myGoogleMap.uiSettings.isCompassEnabled = false

            (mapWrapper as GoogleMapWrapperForDispatchingTouchEvent).initializeWrapper(myGoogleMap,getPixelsFromDp(this, 39f))
            setupInfoWindow()

            if (LocationUtils?.getLastKnownLocation() != null) {

                var userCurrentLocationLatLng = LocationUtils!!.getLastKnownLocation()!!

                var newCameraPosition = com.google.android.gms.maps.model.CameraPosition.builder()
                    .target(LatLng(userCurrentLocationLatLng.latitude!!, userCurrentLocationLatLng.longitude!! - 0.01))
                    .zoom(15f)
                    .bearing(90f)
                    .tilt(0f)
                    .build()
                myGoogleMap.animateCamera(com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(newCameraPosition))

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
        locationSearchResultFragment = SearchAddressResultFragment()

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


        //pin selected location in map
        if(!::currentPinnedLocationMarker.isInitialized){
            //first time
            currentPinnedLocationMarker = myGoogleMap.addMarker(MarkerOptions()
                .position(LatLng(data.latLong?.lat!!,data.latLong?.lon!!))
                .draggable(true)
                .title("Drag Me")
                .snippet("Long Click the Marker!"))

            myGoogleMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener{
                override fun onMarkerDragEnd(marker: Marker?) {
                    currentPinnedLocation!!.latLong?.lat = marker?.position?.latitude!!
                    currentPinnedLocation!!.latLong?.lon = marker?.position?.longitude!!

                    updateFeaturePlaceAddressWithLatLong(currentPinnedLocation!!)
                }

                override fun onMarkerDragStart(p0: Marker?) {
                }

                override fun onMarkerDrag(p0: Marker?) {
                }
            })


        }else{
            //change the location of the pin
            currentPinnedLocationMarker.position = LatLng(data.latLong?.lat!!,data.latLong?.lon!!)
        }

        //move to selected location
        var newCameraPosition = com.google.android.gms.maps.model.CameraPosition.builder()
            .target(LatLng(data.latLong?.lat!!,data.latLong?.lon!!))
            .zoom(15f)
            .bearing(90f)
            .tilt(0f)
            .build()
        myGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition))
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
                dateFormat.timeZone = TimeZone.getTimeZone("GMT+8:00")
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
                        recyle_glass_edittext.setText("0")
                        recyle_plastic_edittext.setText("0")
                        recyle_paper_edittext.setText("0")
                        recyle_metal_edittext.setText("0")
                        recycle_metal_info_textview.text = "0 KG"
                        recycle_glass_info_textview.text = "0 KG"
                        recycle_plastic_info_textview.text = "0 KG"
                        recycle_paper_info_textview.text = "0 KG"
                        currentPinnedLocation=null
                        recycle_request_location_label.text = "Select A Pick Up Location"
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

    private fun listenToFirebaseCollectionChangesAndUpdateUI(){
        var db = FirebaseFirestore.getInstance()

        listener = db.collection("Recycle_Request").addSnapshotListener{
                snapshot,e->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null ) {
                //update the UI
                getRecyclerRequestFromFirebase()
            }
        }
    }

    private fun getRecyclerRequestFromFirebase(){
        RecycleRequest.getRecycleRequestFromFirebase{
                success,message,data ->

            if(success){

                //clear existing marker first
                myGoogleMap.clear()
                //loop each recycle request and add marker in google map
                for(recycleRequest in data!!){
                    var markerIcon = 0

                    if(recycleRequest.requestedUser.userId == FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)){
                        markerIcon = R.drawable.ic_recycler_marker_blue
                    }else if(recycleRequest.acceptedCollectUser != null){
                        markerIcon = R.drawable.ic_recycler_marker_red
                    }else{
                        markerIcon = R.drawable.ic_recycler_marker
                    }


                    var marker = myGoogleMap.addMarker(MarkerOptions()
                        .position(LatLng(recycleRequest.location.latitude,recycleRequest.location.longitude))
                        .icon(BitmapDescriptorFactory.fromResource(markerIcon)))

                    //set the request detail into the tag so we can retrive later
                    marker.tag = recycleRequest

                }



            }else{
                var errorDialog = ErrorDialog(this,"Error when getting data from Firebase","Contact the developer. Error Code: $message")
                errorDialog.show()
            }

        }


    }

    private fun setupInfoWindow(){
        var layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var customView = layoutInflater.inflate(R.layout.googlemap_recycle_infowindow, null)

        var btnCollect = customView.findViewById<TextView>(R.id.btnCollect)
        var btnChat = customView.findViewById<ImageButton>(R.id.btnChat)
        var tvRequestingUser = customView.findViewById<TextView>(R.id.tvRequestingUser)
        var tvAddress = customView.findViewById<TextView>(R.id.tvAddress)
        var tvMetalAmount = customView.findViewById<TextView>(R.id.tvMetalAmount)
        var tvPlasticAmount = customView.findViewById<TextView>(R.id.tvPlasticAmount)
        var tvGlassAmount = customView.findViewById<TextView>(R.id.tvGlassAmount)
        var tvPaperAmount = customView.findViewById<TextView>(R.id.tvPaperAmount)
        var tvDistanceAway = customView.findViewById<TextView>(R.id.tvDistanceAway)
        var tvTotal = customView.findViewById<TextView>(R.id.tvTotal)
        var tvCollectingBy = customView.findViewById<TextView>(R.id.tvCollectingBy)

        var chatButtonListener = object: InfoWindowElementTouchListener(btnChat){
            override fun onClickConfirmed(v: View?, marker: Marker?) {
                if(!isLoadingChat){
                    isLoadingChat= true

                    var loadingDialog = LoadingDialog(this@RecycleActivity)
                    loadingDialog.show()

                    var recycleRequest = marker?.tag as RecycleRequest
                    var requesterUserId = recycleRequest.requestedUser.userId
                    Log.d("wtf","user1: ${FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@RecycleActivity)!!} | user2: ${requesterUserId}")
                    ChatRoom.getSpecificChatRoomProvidingTwoUser(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@RecycleActivity)!!,requesterUserId, callback = {
                            success,message,chatRoomRef->

                        GlobalScope.launch {
                            if(chatRoomRef!=null){
                                loadingDialog.dismiss()
                                isLoadingChat= false
                                //if found we return previous activity
                                var intent = Intent(this@RecycleActivity,ChatRoomActivity::class.java)
                                intent.putExtra("chatRoom",chatRoomRef)
                                startActivity(intent)
                            }else{
                                loadingDialog.dismiss()
                                isLoadingChat= false

                                var user1 = User.suspendGetSpecificUserFromFirebase(FirebaseAuth.getInstance().currentUser?.uid!!) //currentlogged in user
                                var user2 = recycleRequest.requestedUser

                                var id = "-1"
                                var lastMessage = ""
                                var lastMessageSendBy = ""
                                var messageList = ArrayList<ChatMessage>()

                                var newChatRoom = ChatRoom(id, user1!!, user2!!, messageList, lastMessage,lastMessageSendBy)
                                var intent = Intent(this@RecycleActivity,ChatRoomActivity::class.java)
                                intent.putExtra("chatRoom",newChatRoom)
                                startActivity(intent)
                            }
                        }
                    })
                }
            }
        }
        btnChat.setOnTouchListener(chatButtonListener)
        infoButtonListenerList.add(chatButtonListener)

        var collectButtonListener = object: InfoWindowElementTouchListener(btnCollect){
            override fun onClickConfirmed(v: View?, marker: Marker?) {

                var confirmationDialog = YesOrNoDialog(this@RecycleActivity,"Are you sure you want to collect material from this request?",callback = {
                        isYes->

                    //if user pressed yes
                    if(isYes){
                        var recycleRequest = marker?.tag as RecycleRequest

                        //update firebase that this user accept the request
                        var db = FirebaseFirestore.getInstance()
                        db.collection("Recycle_Request").document(recycleRequest.id).update(mapOf(
                            "accepted_collect_by" to FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@RecycleActivity)!!
                        )).addOnSuccessListener {
                            var successDialog = SuccessDialog(this@RecycleActivity,"Success","Successfully notify the request owner that you are going to collect her request!")
                            successDialog.show()
                        }.addOnFailureListener {
                                exception ->
                            var failureDialog = ErrorDialog(this@RecycleActivity,"Error","We have encountered some error when connecting to Firebase! Please check ur internet connection.")
                            failureDialog.show()
                        }
                    }
                })
                confirmationDialog.show()
            }
        }
        btnCollect.setOnTouchListener(collectButtonListener)
        infoButtonListenerList.add(collectButtonListener)



        myGoogleMap.setInfoWindowAdapter(object: GoogleMap.InfoWindowAdapter{
            override fun getInfoContents(p0: Marker?): View? {
                return null
            }

            override fun getInfoWindow(marker: Marker?): View? {

                if(marker?.tag != null) {
                    //update currently viewing marker
                    for (listener in infoButtonListenerList) {
                        listener.setMarker(marker!!)
                    }
                    //update currently viewing marker
                    mapWrapper.setMarkerWithInfoWindow(marker!!, customView)

                    //set marker data based on request that stored in tag
                    var recycleRequest = marker.tag as RecycleRequest
                    tvRequestingUser.text = recycleRequest.requestedUser.getFullName()
                    tvAddress.text = recycleRequest.address
                    tvPaperAmount.text = "${recycleRequest.paperWeight} KG"
                    tvMetalAmount.text = "${recycleRequest.metalWeight} KG"
                    tvGlassAmount.text = "${recycleRequest.glassWeight} KG"
                    tvPlasticAmount.text = "${recycleRequest.plasticWeight} KG"
                    tvDistanceAway.text =
                        String.format("%.2f", recycleRequest.getDistanceBetween() / 1000)
                    tvTotal.text = "${recycleRequest.getTotalAmount()} KG"

                    //if this request is request by the current logged in user
                    //hide the Collect button and chat button
                    if (recycleRequest.requestedUser.userId == FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(
                            this@RecycleActivity
                        )
                    ) {
                        btnCollect.visibility = View.INVISIBLE
                        btnChat.visibility = View.INVISIBLE
                    } else {
                        btnChat.visibility = View.VISIBLE

                        //if someone accept the request show collecting by who
                        if (recycleRequest.acceptedCollectUser != null) {
                            tvCollectingBy.text =
                                "Collecting By:\n${recycleRequest.acceptedCollectUser!!.getFullName()}"
                            tvCollectingBy.visibility = View.VISIBLE
                            btnCollect.visibility = View.INVISIBLE
                        } else {
                            tvCollectingBy.visibility = View.GONE
                            btnCollect.visibility = View.VISIBLE
                        }
                    }

                    if (LocationUtils.getLastKnownLocation() != null) {
                        var userCurrentLoc = LocationUtils.getLastKnownLocation()
                        var results: FloatArray = FloatArray(2)
                        Location.distanceBetween(
                            userCurrentLoc?.latitude!!,
                            userCurrentLoc?.longitude!!,
                            recycleRequest.location.latitude,
                            recycleRequest.location.longitude,
                            results
                        )
                        tvDistanceAway.text = String.format("%.2f", results[0] / 1000)
                    } else {
                        tvDistanceAway.text = "N/A"
                    }



                    return customView
                }else{
                    return null
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun revertMenuPageCircularRevealActivity() {


        //add menu view so that we can revert reveal it
        var layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var inflatedView = layoutInflater.inflate(R.layout.activity_menu,null) as ConstraintLayout
        inflatedView.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,ConstraintLayout.LayoutParams.MATCH_PARENT)
        rootLayout.addView(inflatedView)

        inflatedView.post {
            runOnUiThread{
                val cx: Int = menu_bg.left + getDips(16) + getDips(56/2)
                val cy: Int = menu_bg.top + getDips(16) + getDips(56/2)
                val finalRadius: Float = Math.max(inflatedView.width, inflatedView.height).toFloat()
                val circularReveal =
                    ViewAnimationUtils.createCircularReveal(inflatedView, cx, cy, finalRadius, 0f)
                circularReveal.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animator: Animator) {}
                    override fun onAnimationEnd(animator: Animator) {
                        rootLayout.removeView(inflatedView)
                    }

                    override fun onAnimationCancel(animator: Animator) {}
                    override fun onAnimationRepeat(animator: Animator) {}
                })
                circularReveal.duration = 1000
                circularReveal.start()
            }
        }
    }

    private fun getDips(dps: Int): Int {
        val resources: Resources = resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dps.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    private fun getPixelsFromDp(context: Context, dp: Float): Int{
        var scale:Float  = context.resources.displayMetrics.density
        return (dp*scale + 0.5f).toInt()
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
