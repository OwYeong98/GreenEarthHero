package com.oymj.greenearthhero.ui.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.googlemap.InfoWindowElementTouchListener
import com.oymj.greenearthhero.ui.customxmllayout.GoogleMapWrapperForDispatchingTouchEvent
import com.oymj.greenearthhero.utils.LocationUtils
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_volunteer_collection.*

class VolunteerCollectionActivity : AppCompatActivity(){
    private lateinit var myGoogleMap: GoogleMap
    private var infoButtonListenerList = ArrayList<InfoWindowElementTouchListener>()
    private lateinit var myBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private var currentBottomSheetState = BottomSheetBehavior.STATE_COLLAPSED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volunteer_collection)

        setupBottomSheet()
        setupGoogleMap()


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

                        }
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            showBackToListButton()
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {


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

            setupInfoWindow()

            if (LocationUtils?.getLastKnownLocation() != null) {

                var userCurrentLocationLatLng = LocationUtils!!.getLastKnownLocation()!!

                var newCameraPosition = CameraPosition.builder()
                    .target(LatLng(userCurrentLocationLatLng.latitude!!,userCurrentLocationLatLng.longitude!!-0.01))
                    .zoom(15f)
                    .bearing(90f)
                    .tilt(0f)
                    .build()
                myGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition))

                var marker = myGoogleMap.addMarker(MarkerOptions()
                    .position(LatLng(userCurrentLocationLatLng.latitude!!,userCurrentLocationLatLng.longitude!!-0.05))
                    .title("hehe"))

                var marker2 = myGoogleMap.addMarker(MarkerOptions()
                    .position(LatLng(userCurrentLocationLatLng.latitude!!,userCurrentLocationLatLng.longitude!!+0.05))
                    .title("lol"))
            }




        }
    }

    fun setupInfoWindow(){
        var layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var customView = layoutInflater.inflate(R.layout.googlemap_recycle_infowindow, null)

        var btnCollect = customView.findViewById<TextView>(R.id.btnCollect)
        var btnChat = customView.findViewById<ImageButton>(R.id.btnChat)



        var chatButtonListener = object: InfoWindowElementTouchListener(btnChat){
            override fun onClickConfirmed(v: View?, marker: Marker?) {
                Toast.makeText(this@VolunteerCollectionActivity,"chat button pressed title: ${marker!!.title}",Toast.LENGTH_SHORT).show()
            }
        }
        btnChat.setOnTouchListener(chatButtonListener)
        infoButtonListenerList.add(chatButtonListener)

        var collectButtonListener = object: InfoWindowElementTouchListener(btnCollect){
            override fun onClickConfirmed(v: View?, marker: Marker?) {
                Toast.makeText(this@VolunteerCollectionActivity,"collect button pressed title: ${marker!!.title}",Toast.LENGTH_SHORT).show()
            }
        }
        btnCollect.setOnTouchListener(collectButtonListener)
        infoButtonListenerList.add(collectButtonListener)



        myGoogleMap.setInfoWindowAdapter(object: GoogleMap.InfoWindowAdapter{
            override fun getInfoContents(p0: Marker?): View? {
                return null
            }

            override fun getInfoWindow(marker: Marker?): View {
                //update currently viewing marker
                for(listener in infoButtonListenerList){
                    listener.setMarker(marker!!)
                }
                //update currently viewing marker
                mapWrapper.setMarkerWithInfoWindow(marker!!, customView)

                return customView
            }
        })
    }

    fun getPixelsFromDp(context: Context, dp: Float): Int{
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

}