package com.oymj.greenearthhero.ui.activity

import android.content.Context
import android.content.Intent
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.googlemap.InfoWindowElementTouchListener
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.data.*
import com.oymj.greenearthhero.ui.customxmllayout.GoogleMapWrapperForDispatchingTouchEvent
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.ui.dialog.SuccessDialog
import com.oymj.greenearthhero.ui.dialog.YesOrNoDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.LocationUtils
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_food_collection.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timer

class FoodCollectionActivity : AppCompatActivity(){
    private lateinit var myGoogleMap: GoogleMap
    private var infoButtonListenerList = ArrayList<InfoWindowElementTouchListener>()
    private lateinit var myBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private var currentBottomSheetState = BottomSheetBehavior.STATE_COLLAPSED
    private lateinit var listener:ListenerRegistration

    private var foodDonationList = ArrayList<Any>()
    private var listOfMarkerInMap = ArrayList<Marker>()
    private lateinit var recyclerViewAdapter: UniversalAdapter
    private var isLoadingChat = false

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
        setContentView(R.layout.activity_food_collection)

        linkAllButtonWithOnClickListener()
        setupRecyclerView()
        setupBottomSheet()
        setupGoogleMap()

    }

    override fun onStart() {
        super.onStart()
        listenToFirebaseCollectionChangesAndUpdateUI()
    }

    override fun onStop() {
        super.onStop()
        listener.remove()
    }

    private fun listenToFirebaseCollectionChangesAndUpdateUI(){
        var db = FirebaseFirestore.getInstance()

        listener = db.collection("Food_Donation").addSnapshotListener{
                snapshot,e->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null ) {
                //update the UI
                getFoodDonationFromFirebase()
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

    private fun getFoodDonationFromFirebase(){
        //clear previous data first
        foodDonationList.clear()
        listOfMarkerInMap.clear()

        //show loading skeletal first while getting data from firestore
        recyclerViewAdapter.startSkeletalLoading(7, UniversalAdapter.SKELETAL_TYPE_2)

        FoodDonation.getFoodDonationListWithoutFoodListFromFirebase(callback = {
            success,message,foodDonationList->

            runOnUiThread {
                recyclerViewAdapter.stopSkeletalLoading()

                if (success) {
                    recyclerViewAdapter.stopSkeletalLoading()

                    //add the data retrived from firebase
                    this.foodDonationList.addAll(foodDonationList!!)
                    //sort by near to far
                    this.foodDonationList.sortBy { obj -> (obj as FoodDonation).getDistanceBetween() }

                    //refresh recyclerview
                    recyclerViewAdapter.notifyDataSetChanged()


                    //clear existing marker first
                    myGoogleMap.clear()
                    //loop each recycle request and add marker in google map
                    for (foodDonation in foodDonationList!!) {
                        var markerIcon = 0

                        if (foodDonation.donatorUser.userId == FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)) {
                            markerIcon = R.drawable.ic_food_donation_marker_blue
                        } else {
                            markerIcon = R.drawable.ic_food_donation_marker_green
                        }


                        var marker = myGoogleMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(foodDonation.donateLocation.location.lat!!,foodDonation.donateLocation.location.lon!!))
                                .icon(BitmapDescriptorFactory.fromResource(markerIcon))
                        )

                        //set the request detail into the tag so we can retrive later
                        marker.tag = foodDonation

                        listOfMarkerInMap.add(marker)
                    }
                } else {
                    var errorDialog = ErrorDialog(
                        this,
                        "Error when getting data from Firebase",
                        "Contact the developer. Error Code: $message"
                    )
                    errorDialog.show()
                }
            }
        })
    }

    private fun setupRecyclerView(){
        recyclerViewAdapter = object: UniversalAdapter(foodDonationList,this@FoodCollectionActivity,foodDonationRecyclerView){
            override fun getVerticalSpacing(): Int {
                //20px spacing
                return 20
            }
            override fun onItemClickedListener(data: Any, clickType:Int) {
                if(data is FoodDonation){
                    //hide the list view
                    myBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

                    //move camera to chosen request
                    var newCameraPosition = CameraPosition.builder()
                        .target(LatLng(data.donateLocation.location.lat!!,data.donateLocation.location.lon!!))
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
        foodDonationRecyclerView.layoutManager = LinearLayoutManager(this)
        foodDonationRecyclerView.adapter = recyclerViewAdapter
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

    private fun setupInfoWindow(){
        var layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var customView = layoutInflater.inflate(R.layout.googlemap_food_donation_infowindow, null)

        var btnCheckDonation = customView.findViewById<TextView>(R.id.btnCheckDonation)
        var btnChat = customView.findViewById<ImageButton>(R.id.btnChat)
        var tvRestaurantName = customView.findViewById<TextView>(R.id.tvRestaurantName)
        var tvAddress = customView.findViewById<TextView>(R.id.tvAddress)
        var tvDonatorUser = customView.findViewById<TextView>(R.id.tvDonatorUser)
        var tvTotal = customView.findViewById<TextView>(R.id.tvTotal)
        var tvDistanceAway = customView.findViewById<TextView>(R.id.tvDistanceAway)
        var tvTimeLeft = customView.findViewById<TextView>(R.id.tvTimeLeft)


        var chatButtonListener = object: InfoWindowElementTouchListener(btnChat){
            override fun onClickConfirmed(v: View?, marker: Marker?) {
                if(!isLoadingChat){
                    isLoadingChat= true

                    var loadingDialog = LoadingDialog(this@FoodCollectionActivity)
                    loadingDialog.show()

                    var foodDonation = marker?.tag as FoodDonation
                    var requesterUserId = foodDonation.donatorUser.userId

                    ChatRoom.getSpecificChatRoomProvidingTwoUser(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@FoodCollectionActivity)!!,requesterUserId, callback = {
                            success,message,chatRoomRef->

                        GlobalScope.launch {
                            if(chatRoomRef!=null){
                                loadingDialog.dismiss()
                                isLoadingChat= false
                                //if found we return previous activity
                                var intent = Intent(this@FoodCollectionActivity,ChatRoomActivity::class.java)
                                intent.putExtra("chatRoom",chatRoomRef)
                                startActivity(intent)
                            }else{
                                loadingDialog.dismiss()
                                isLoadingChat= false

                                var user1 = User.suspendGetSpecificUserFromFirebase(FirebaseAuth.getInstance().currentUser?.uid!!) //currentlogged in user
                                var user2 = foodDonation.donatorUser

                                var id = "-1"
                                var lastMessage = ""
                                var lastMessageSendBy = ""
                                var messageList = ArrayList<ChatMessage>()

                                var newChatRoom = ChatRoom(id, user1!!, user2!!, messageList, lastMessage,lastMessageSendBy)
                                var intent = Intent(this@FoodCollectionActivity,ChatRoomActivity::class.java)
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

        var checkDonationButtonListener = object: InfoWindowElementTouchListener(btnCheckDonation){
            override fun onClickConfirmed(v: View?, marker: Marker?) {
                if(marker?.tag != null){
                    var foodDonation = marker?.tag as FoodDonation

                    var intent = Intent(this@FoodCollectionActivity,FoodDonationDetailActivity::class.java)
                    intent.putExtra("foodDonationId",foodDonation.id)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_up_slow,R.anim.freeze)
                }
            }
        }
        btnCheckDonation.setOnTouchListener(checkDonationButtonListener)
        infoButtonListenerList.add(checkDonationButtonListener)



        myGoogleMap.setInfoWindowAdapter(object: GoogleMap.InfoWindowAdapter{
            override fun getInfoContents(p0: Marker?): View? {
                return null
            }

            override fun getInfoWindow(marker: Marker?): View? {
                if(marker?.tag != null){
                    //hide the listview
                    myBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

                    //update currently viewing marker
                    for(listener in infoButtonListenerList){
                        listener.setMarker(marker!!)
                    }
                    //update currently viewing marker
                    mapWrapper.setMarkerWithInfoWindow(marker!!, customView)

                    //set marker data based on request that stored in tag
                    var foodDonation = marker.tag as FoodDonation
                    tvDonatorUser.text = foodDonation.donatorUser.getFullName()
                    tvAddress.text = foodDonation.donateLocation.address
                    tvRestaurantName.text = foodDonation.donateLocation.name
                    tvTotal.text = "Total Food Quantity: ${foodDonation.totalFoodAmount}"

                    if(LocationUtils.getLastKnownLocation() != null){
                        tvDistanceAway.text = String.format("%.2f",foodDonation.getDistanceBetween()/1000)
                    }else{
                        tvDistanceAway.text = "N/A"
                    }
                    var dateformatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                    tvTimeLeft.text = "Donation End Date:\n ${dateformatter.format(foodDonation.getDonationEndTime())}"

                    if(foodDonation.donatorUser.userId == FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@FoodCollectionActivity)){
                        btnChat.visibility = View.GONE
                    }else{
                        btnChat.visibility = View.VISIBLE
                    }

                    return customView
                }else{
                    return null
                }

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

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.freeze, R.anim.slide_down_slow)
    }
}