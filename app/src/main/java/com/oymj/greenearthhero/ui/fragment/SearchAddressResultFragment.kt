package com.oymj.greenearthhero.ui.fragment

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.api.ApisImplementation
import com.oymj.greenearthhero.data.TomTomPlacesResult
import com.oymj.greenearthhero.data.TomTomPosition
import com.oymj.greenearthhero.utils.LocationUtils
import com.oymj.greenearthhero.utils.RippleUtil
import io.supercharge.shimmerlayout.ShimmerLayout
import kotlinx.android.synthetic.main.fragment_search_address_result.*


class SearchAddressResultFragment(var callback:(TomTomPlacesResult)->Unit) : Fragment() {

    var placesList = ArrayList<Any>()
    lateinit var recyclerViewAdapter: UniversalAdapter


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_address_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var myRecyclerView = view.findViewById<RecyclerView>(R.id.myRecyclerView)

        btnDragSelf.background = RippleUtil.getGradientRippleButtonOutlineDrawable(context!!,
            resources.getColor(R.color.darkgreen),
            resources.getColor(R.color.darkgrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            resources.getColor(R.color.transparent),
            20f,0, GradientDrawable.Orientation.BL_TR
        )

        btnDragSelf.setOnClickListener {
            var currentLocation = TomTomPlacesResult()

            var currentLatLong = TomTomPosition()
            currentLatLong.lat = LocationUtils.getLastKnownLocation()?.latitude!!
            currentLatLong.lon = LocationUtils.getLastKnownLocation()?.longitude!!

            currentLocation.latLong = currentLatLong

            callback(currentLocation)
        }

        recyclerViewAdapter = object: UniversalAdapter(placesList,context!!,myRecyclerView){
            override fun onItemClickedListener(data: Any) {
                if(data is TomTomPlacesResult){
                    callback(data)
                }
            }
        }
        myRecyclerView.layoutManager = LinearLayoutManager(view.context)
        myRecyclerView.adapter = recyclerViewAdapter

    }

    fun searchAddressFromMapBoxApi(keyword: String){
        placesList.clear()
        zeroStateContainer.visibility = View.GONE
        recyclerViewAdapter.startSkeletalLoading(8)

        ApisImplementation().geocodingFromTomTom(context!!,keyword,callback = {
            success,response->
            recyclerViewAdapter.stopSkeletalLoading()
            if(success){
                placesList.addAll(response?.results!!)
                recyclerViewAdapter.notifyDataSetChanged()

                //if no places found show zero state
                if (placesList.size > 0){
                    zeroStateContainer.visibility = View.GONE
                }else{
                    zeroStateContainer.visibility = View.VISIBLE
                }
            }else{
                recyclerViewAdapter.notifyDataSetChanged()
                zeroStateContainer.visibility = View.VISIBLE
            }
        })
    }

}