package com.oymj.greenearthhero.ui.activity

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.firestore.FirebaseFirestore
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.googlemap.InfoWindowElementTouchListener
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.data.RecycleRequest
import com.oymj.greenearthhero.data.SkeletalEmptyModel
import com.oymj.greenearthhero.data.User
import com.oymj.greenearthhero.ui.customxmllayout.GoogleMapWrapperForDispatchingTouchEvent
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.ui.dialog.SuccessDialog
import com.oymj.greenearthhero.ui.dialog.YesOrNoDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.LocationUtils
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_volunteer_collection.*
import java.text.SimpleDateFormat

class VolunteerCollectionActivity : AppCompatActivity(){
    private lateinit var myGoogleMap: GoogleMap
    private var infoButtonListenerList = ArrayList<InfoWindowElementTouchListener>()
    private lateinit var myBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private var currentBottomSheetState = BottomSheetBehavior.STATE_COLLAPSED

    private var recycleRequestList = ArrayList<Any>()
    private var listOfMarkerInMap = ArrayList<Marker>()
    private lateinit var recyclerViewAdapter: UniversalAdapter

    //Better control of onClickListener
    //all button action will be registered here
    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                btnClose->{
                    finish()
                    overridePendingTransition(R.anim.freeze, R.anim.slide_down_slow)
                }
                btnBackToList->{
                    hideBackToListButton {
                        myBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volunteer_collection)

        linkAllButtonWithOnClickListener()
        setupRecyclerView()
        setupBottomSheet()
        setupGoogleMap()
        getRecyclerRequestFromFirebase()
        listenToFirebaseCollectionChangesAndUpdateUI()


    }

    private fun listenToFirebaseCollectionChangesAndUpdateUI(){
        var db = FirebaseFirestore.getInstance()

        db.collection("Recycle_Request").addSnapshotListener{
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

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            btnClose,
            btnBackToList
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }

    private fun getRecyclerRequestFromFirebase(){
        //clear previous data first
        recycleRequestList.clear()
        listOfMarkerInMap.clear()

        //show loading skeletal first while getting data from firestore
        recyclerViewAdapter.startSkeletalLoading(7, UniversalAdapter.SKELETAL_TYPE_2)

        RecycleRequest.getRecycleRequestFromFirebase{
            success,message,data ->

            if(success){
                recyclerViewAdapter.stopSkeletalLoading()

                //add the data retrived from firebase
                recycleRequestList.addAll(data!!)
                //sort by near to far
                recycleRequestList.sortBy { obj-> (obj as RecycleRequest).getDistanceBetween() }

                //refresh recyclerview
                recyclerViewAdapter.notifyDataSetChanged()

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

                    listOfMarkerInMap.add(marker)
                }



            }else{
                recyclerViewAdapter.stopSkeletalLoading()

                var errorDialog = ErrorDialog(this,"Error when getting data from Firebase","Contact the developer. Error Code: $message")
                errorDialog.show()
            }

        }


    }

    private fun setupRecyclerView(){
        recyclerViewAdapter = object: UniversalAdapter(recycleRequestList,this@VolunteerCollectionActivity,recycleRequestRecyclerView){
            override fun getVerticalSpacing(): Int {
                //20px spacing
                return 20
            }
            override fun onItemClickedListener(data: Any, clickType:Int) {
                if(data is RecycleRequest){
                    //hide the list view
                    myBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

                    //move camera to chosen request
                    var newCameraPosition = CameraPosition.builder()
                        .target(LatLng(data.location.latitude,data.location.longitude))
                        .zoom(15f)
                        .bearing(90f)
                        .tilt(0f)
                        .build()
                    myGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition))

                    //show the info window
                    for(marker in listOfMarkerInMap){
                        if(marker.tag == data){
                            marker.showInfoWindow()
                            break
                        }
                    }

                }
            }
        }
        recycleRequestRecyclerView.layoutManager = LinearLayoutManager(this)
        recycleRequestRecyclerView.adapter = recyclerViewAdapter
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
                            currentBottomSheetState = BottomSheetBehavior.STATE_COLLAPSED
                        }
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            currentBottomSheetState = BottomSheetBehavior.STATE_HIDDEN
                            showBackToListButton()
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            currentBottomSheetState = BottomSheetBehavior.STATE_EXPANDED
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

    private fun setupGoogleMap(){
        var googleMapFragment = supportFragmentManager.findFragmentById(R.id.googleMapFragmentView) as SupportMapFragment
        googleMapFragment.getMapAsync{
            googleMap ->

            //save the instance for later use
            myGoogleMap = googleMap


            (mapWrapper as GoogleMapWrapperForDispatchingTouchEvent).initializeWrapper(myGoogleMap,getPixelsFromDp(this, 39f))

            myGoogleMap.isMyLocationEnabled = true
            myGoogleMap.uiSettings.isCompassEnabled = false

            setupInfoWindow()
            //hide list view when user move the map
            myGoogleMap.setOnCameraMoveStartedListener {
                reason->
                if(reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE){
                    myBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }

            if (LocationUtils?.getLastKnownLocation() != null) {

                var userCurrentLocationLatLng = LocationUtils!!.getLastKnownLocation()!!

                var newCameraPosition = CameraPosition.builder()
                    .target(LatLng(userCurrentLocationLatLng.latitude!!,userCurrentLocationLatLng.longitude!!-0.01))
                    .zoom(15f)
                    .bearing(90f)
                    .tilt(0f)
                    .build()
                myGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition))

            }
        }
    }

    fun setupInfoWindow(){
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
                Toast.makeText(this@VolunteerCollectionActivity,"chat button pressed title: ${marker!!.title}",Toast.LENGTH_SHORT).show()
            }
        }
        btnChat.setOnTouchListener(chatButtonListener)
        infoButtonListenerList.add(chatButtonListener)

        var collectButtonListener = object: InfoWindowElementTouchListener(btnCollect){
            override fun onClickConfirmed(v: View?, marker: Marker?) {

                var confirmationDialog = YesOrNoDialog(this@VolunteerCollectionActivity,"Are you sure you want to collect material from this request?",callback = {
                    isYes->

                    //if user pressed yes
                    if(isYes){
                        var recycleRequest = marker?.tag as RecycleRequest

                        //update firebase that this user accept the request
                        var db = FirebaseFirestore.getInstance()
                        db.collection("Recycle_Request").document(recycleRequest.id).update(mapOf(
                            "accepted_collect_by" to FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@VolunteerCollectionActivity)!!
                        )).addOnSuccessListener {
                            var successDialog = SuccessDialog(this@VolunteerCollectionActivity,"Success","Successfully notify the request owner that you are going to collect her request!")
                            successDialog.show()
                        }.addOnFailureListener {
                            exception ->
                            var failureDialog = ErrorDialog(this@VolunteerCollectionActivity,"Error","We have encountered some error when connecting to Firebase! Please check ur internet connection.")
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

            override fun getInfoWindow(marker: Marker?): View {
                //hide the listview
                myBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

                //update currently viewing marker
                for(listener in infoButtonListenerList){
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
                tvDistanceAway.text = "10 KG"
                tvTotal.text = "${recycleRequest.getTotalAmount()} KG"

                //if this request is request by the current logged in user
                //hide the Collect button and chat button
                if(recycleRequest.requestedUser.userId == FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@VolunteerCollectionActivity)){
                    btnCollect.visibility = View.INVISIBLE
                    btnChat.visibility = View.INVISIBLE
                }else{
                    btnChat.visibility = View.VISIBLE

                    //if someone accept the request show collecting by who
                    if(recycleRequest.acceptedCollectUser != null){
                        tvCollectingBy.text = "Collecting By:\n${recycleRequest.acceptedCollectUser!!.getFullName()}"
                        tvCollectingBy.visibility = View.VISIBLE
                        btnCollect.visibility = View.INVISIBLE
                    }else{
                        tvCollectingBy.visibility = View.GONE
                        btnCollect.visibility = View.VISIBLE
                    }
                }

                if(LocationUtils.getLastKnownLocation() != null){
                    var userCurrentLoc = LocationUtils.getLastKnownLocation()
                    var results: FloatArray = FloatArray(2)
                    Location.distanceBetween(userCurrentLoc?.latitude!!,userCurrentLoc?.longitude!!,
                        recycleRequest.location.latitude,recycleRequest.location.longitude,results)
                    tvDistanceAway.text = String.format("%.2f",results[0]/1000)
                }else{
                    tvDistanceAway.text = "N/A"
                }



                return customView
            }
        })
    }

    private fun getPixelsFromDp(context: Context, dp: Float): Int{
        var scale:Float  = context.resources.displayMetrics.density
        return (dp*scale + 0.5f).toInt()
    }


    private fun showBackToListButton(){
        val slideUpAnimation: Animation = AnimationUtils.loadAnimation(applicationContext,
            R.anim.slide_up
        )
        btnBackToList.visibility=View.VISIBLE
        btnBackToList.startAnimation(slideUpAnimation)

    }

    private fun hideBackToListButton(callback:()->Unit){
        val slideDownAnimation: Animation = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down)
        slideDownAnimation.setAnimationListener(object: Animation.AnimationListener{
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                btnBackToList.visibility=View.GONE
                callback()
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })

        btnBackToList.startAnimation(slideDownAnimation)

    }

}