package com.oymj.greenearthhero

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.oymj.greenearthhero.Utils.LocationUtils

import kotlinx.android.synthetic.main.activity_recycle.*



class RecycleActivity : AppCompatActivity() {
    private lateinit var myBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
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
                        var cameraUpdate: CameraUpdate = CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                LocationUtils!!.getLastKnownLocation()!!.latitude,
                                LocationUtils!!.getLastKnownLocation()!!.longitude
                            ), 15f
                        )
                        googleMap.animateCamera(cameraUpdate)
                    }
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_recycle)

        linkAllButtonWithOnClickListener()
        syncMaterialInfoWithEditText()

        setupGoogleMap()
        setupBottomSheet()
    }

    private fun setupGoogleMap(){
        mapFragment = supportFragmentManager.findFragmentById(R.id.recycle_map_view) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback {
                googleMap->
            this.googleMap = googleMap
            this.googleMap.isMyLocationEnabled = true
            this.googleMap.uiSettings.isMyLocationButtonEnabled = false

        })
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

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            recycle_expanded_closebtn,
            recycle_menu_icon,
            recycle_request_location_label,
            recycle_location_search_back_btn,
            recycle_mapbox_recenter_btn
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }

    //expand bottom sheet
    private fun showExpandedView() {
        val fadeInAnimation: Animation =
            AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in)
        val fadeOutAnimation: Animation =
            AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out)

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
            AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in)
        val fadeOutAnimation: Animation =
            AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out)

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

    /**
     * Function use to sync material info edittext with the info textview
     */
    private fun syncMaterialInfoWithEditText() {

        recyle_metal_edittext.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                recycle_metal_info_textview.setText("$s KG")
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
            }
        })
        recyle_plastic_edittext.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                recycle_plastic_info_textview.setText("$s KG")
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
            }
        })
        recyle_paper_edittext.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                recycle_paper_info_textview.setText("$s KG")
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
            }
        })
        recyle_glass_edittext.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                recycle_glass_info_textview.setText("$s KG")
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
            }
        })
    }

    private fun showLocationPanel(yesOrNo: Boolean) {
        val slideUpAnimation: Animation =
            AnimationUtils.loadAnimation(applicationContext, R.anim.slide_up)
        val slideDownAnimation: Animation =
            AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down)
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
