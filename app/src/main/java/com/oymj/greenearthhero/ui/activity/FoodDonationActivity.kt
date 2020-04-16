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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.ui.customxmllayout.GoogleMapWrapperForDispatchingTouchEvent
import com.oymj.greenearthhero.utils.LocationUtils
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_food_donation.*
import kotlinx.android.synthetic.main.activity_food_donation.mapWrapper

class FoodDonationActivity : AppCompatActivity() {

    private lateinit var myGoogleMap: GoogleMap
    private lateinit var myBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private var currentBottomSheetState = BottomSheetBehavior.STATE_COLLAPSED

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

    override fun onResume() {
        super.onResume()

        currentBottomSheetState = BottomSheetBehavior.STATE_COLLAPSED
        myBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun setupInfoWindow(){

    }

    private fun getPixelsFromDp(context: Context, dp: Float): Int{
        var scale:Float  = context.resources.displayMetrics.density
        return (dp*scale + 0.5f).toInt()
    }

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            btnMenu,
            btnDonateNow
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