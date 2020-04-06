package com.oymj.greenearthhero.adapters.recyclerview.recycleritem

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.adapters.recyclerview.UniversalRecyclerItem
import com.oymj.greenearthhero.adapters.recyclerview.UniversalViewHolder
import com.oymj.greenearthhero.data.TomTomPlacesResult
import com.oymj.greenearthhero.utils.RippleUtil

class RecyclerItemFeaturePlaces : UniversalRecyclerItem(TomTomPlacesResult::class.java.simpleName, R.layout.listitem_feature_places){

    override fun inflateView(parent: ViewGroup, context: Context): View {
        return super.inflateView(parent, context)
    }

    override fun getViewHolder(parent: ViewGroup, context: Context, adapter: UniversalAdapter) : UniversalViewHolder {
        return ViewHolder(inflateView(parent,context),adapter)
    }

    inner class ViewHolder (var view: View,var adapter: UniversalAdapter) : UniversalViewHolder(view) {

        override fun onBindViewHolder(data:Any) {
            if(data is TomTomPlacesResult){
                val mainContainer = view.findViewById<LinearLayout>(R.id.mainContainer)
                val tvLocationTitle = view.findViewById<TextView>(R.id.tvLocationTitle)
                val tvDistance = view.findViewById<TextView>(R.id.tvDistance)
                val tvAddress = view.findViewById<TextView>(R.id.tvAddress)

                //set data
                tvLocationTitle.text = data.address?.street?:data.address?.taman
                tvDistance.text = "10 km"
                tvAddress.text = data.address?.fullAddress

                //set ripple background
                mainContainer.background = RippleUtil.getRippleButtonOutlineDrawable(mainContainer.context,
                    mainContainer.context.resources.getColor(R.color.transparent),
                    mainContainer.context.resources.getColor(R.color.transparent_pressed),
                    mainContainer.context.resources.getColor(R.color.transparent),
                    0f,0
                )

                mainContainer.setOnClickListener{
                    adapter.onItemClickedListener(data)
                }
            }
        }
    }
}