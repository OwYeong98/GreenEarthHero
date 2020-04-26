package com.oymj.greenearthhero.ui.activity

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.googlemap.InfoWindowElementTouchListener
import com.oymj.greenearthhero.data.ChatMessage
import com.oymj.greenearthhero.data.ChatRoom
import com.oymj.greenearthhero.data.FoodDonation
import com.oymj.greenearthhero.data.User
import com.oymj.greenearthhero.ui.customxmllayout.GoogleMapWrapperForDispatchingTouchEvent
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.LocationUtils
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_food_donation.*
import kotlinx.android.synthetic.main.activity_food_donation.mapWrapper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class FoodDonationActivity : AppCompatActivity() {

    private lateinit var myGoogleMap: GoogleMap
    private lateinit var myBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private var currentBottomSheetState = BottomSheetBehavior.STATE_COLLAPSED

    private var listener:ListenerRegistration? = null
    private var infoButtonListenerList = ArrayList<InfoWindowElementTouchListener>()
    private var listOfMarkerInMap = ArrayList<Marker>()
    private var isLoadingChat = false

    //Better control of onClickListener
    //all button action will be registered here
    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                btnMenu->{
                    var intent = Intent(this@FoodDonationActivity , MenuActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    startActivity(intent)
                }
                btnDonateNow->{
                    myBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
                btnFoodCollection->{
                    var intent = Intent(this@FoodDonationActivity , FoodCollectionActivity::class.java)
                    startActivity(intent)
                }
                btnRecenter -> {
                    if(LocationUtils?.getLastKnownLocation() != null) {
                        if(::myGoogleMap != null){
                            var userCurrentLocationLatLng = LocationUtils!!.getLastKnownLocation()!!
                            var newCameraPosition = com.google.android.gms.maps.model.CameraPosition.builder()
                                .target(
                                    com.google.android.gms.maps.model.LatLng(
                                        userCurrentLocationLatLng.latitude!!,
                                        userCurrentLocationLatLng.longitude!!
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

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.freeze, R.anim.freeze)
        setContentView(R.layout.activity_food_donation)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            revertMenuPageCircularRevealActivity()
        }

        setupUI()
        linkAllButtonWithOnClickListener()
        setupGoogleMap()
        setupBottomSheet()
        listenToFirebaseCollectionChangesAndUpdateUI()
    }

    override fun onResume() {
        super.onResume()
        listenToFirebaseCollectionChangesAndUpdateUI()
        currentBottomSheetState = BottomSheetBehavior.STATE_COLLAPSED
        myBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun onPause() {
        super.onPause()
        listener?.remove()
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

    private fun getFoodDonationFromFirebase(){
        //clear previous data first
        listOfMarkerInMap.clear()

        FoodDonation.getFoodDonationListWithoutFoodListFromFirebase(callback = {
                success,message,foodDonationList->

            runOnUiThread {

                if (success) {
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

    private fun setupUI(){
        btnDonateNow.background = RippleUtil.getGradientRippleButtonOutlineDrawable(this,
            Color.parseColor("#7CDF75"),
            Color.parseColor("#3AD629"),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            resources.getColor(R.color.transparent),
            60f,0,
            GradientDrawable.Orientation.LEFT_RIGHT
        )
    }

    private fun setupBottomSheet(){
        myBottomSheetBehavior = BottomSheetBehavior.from(food_donation_bottom_sheet)

        myBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (currentBottomSheetState == BottomSheetBehavior.STATE_EXPANDED) {
                    food_donation_bottom_sheet.setBackgroundResource(R.drawable.white_rounded_corner_bg_with_shadow)
                } else {
                    food_donation_bottom_sheet.setBackgroundColor(Color.WHITE)
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (currentBottomSheetState != newState) {
                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            currentBottomSheetState = BottomSheetBehavior.STATE_COLLAPSED
                            food_donation_bottom_sheet.setBackgroundResource(R.drawable.white_rounded_corner_bg_with_shadow)

                        }
                        BottomSheetBehavior.STATE_HIDDEN -> {

                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            currentBottomSheetState = BottomSheetBehavior.STATE_EXPANDED

                            var intent = Intent(this@FoodDonationActivity , AddFoodDonationActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(R.anim.fade_in,R.anim.fade_out)
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

                    var loadingDialog = LoadingDialog(this@FoodDonationActivity)
                    loadingDialog.show()

                    var foodDonation = marker?.tag as FoodDonation
                    var requesterUserId = foodDonation.donatorUser.userId

                    ChatRoom.getSpecificChatRoomProvidingTwoUser(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@FoodDonationActivity)!!,requesterUserId, callback = {
                            success,message,chatRoomRef->

                        GlobalScope.launch {
                            if(chatRoomRef!=null){
                                loadingDialog.dismiss()
                                isLoadingChat= false
                                //if found we return previous activity
                                var intent = Intent(this@FoodDonationActivity,ChatRoomActivity::class.java)
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
                                var intent = Intent(this@FoodDonationActivity,ChatRoomActivity::class.java)
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

                    var intent = Intent(this@FoodDonationActivity,FoodDonationDetailActivity::class.java)
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

                    if(foodDonation.donatorUser.userId == FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@FoodDonationActivity)){
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

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            btnMenu,
            btnDonateNow,
            btnFoodCollection,
            btnRecenter
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun revertMenuPageCircularRevealActivity() {


        //add menu view so that we can revert reveal it
        var layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var inflatedView = layoutInflater.inflate(R.layout.activity_menu,null) as ConstraintLayout
        inflatedView.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT)
        rootLayout.addView(inflatedView)

        inflatedView.post {
            runOnUiThread{
                val cx: Int = inflatedView.left + getDips(16) + getDips(56/2)
                val cy: Int = inflatedView.top + getDips(16) + getDips(56/2)
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
}