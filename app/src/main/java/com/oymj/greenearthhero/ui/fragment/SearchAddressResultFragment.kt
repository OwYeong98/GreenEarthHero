package com.oymj.greenearthhero.ui.fragment

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.RecyclerViewOnItemClickListener
import com.oymj.greenearthhero.adapters.FeaturePlacesRecyclerViewAdapter
import com.oymj.greenearthhero.api.ApisImplementation
import com.oymj.greenearthhero.data.FeaturePlaces
import com.oymj.greenearthhero.utils.LocationUtils
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.fragment_search_address_result.*


class SearchAddressResultFragment(var callback:(FeaturePlaces)->Unit) : Fragment(), RecyclerViewOnItemClickListener {

    var placesList = ArrayList<FeaturePlaces>()
    lateinit var recyclerViewAdapter: FeaturePlacesRecyclerViewAdapter


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
            var currentLocation = FeaturePlaces()
            currentLocation.title = "Current Location"
            currentLocation.latLong = listOf(LocationUtils.getLastKnownLocation()?.longitude!!,LocationUtils.getLastKnownLocation()?.latitude!!)

            callback(currentLocation)
        }

        recyclerViewAdapter = FeaturePlacesRecyclerViewAdapter(placesList,context!!,this)
        myRecyclerView.layoutManager = LinearLayoutManager(view.context)
        myRecyclerView.adapter = recyclerViewAdapter

    }

    override fun onItemClick(data:Any) {
        if(data is FeaturePlaces){
            callback(data)
        }
    }

    fun searchAddressFromMapBoxApi(keyword: String){
        ApisImplementation().searchAddress(context!!,keyword,callback = {
                success, response->

            if(success){
                placesList.clear()
                placesList.addAll(response?.resultList!!)
                recyclerViewAdapter.notifyDataSetChanged()

                //if no places found show zero state
                if (placesList.size > 0){
                    zeroStateContainer.visibility = View.GONE
                }else{
                    zeroStateContainer.visibility = View.VISIBLE
                }
            }else{
                placesList.clear()
                recyclerViewAdapter.notifyDataSetChanged()
                zeroStateContainer.visibility = View.VISIBLE
            }
        })
    }

}