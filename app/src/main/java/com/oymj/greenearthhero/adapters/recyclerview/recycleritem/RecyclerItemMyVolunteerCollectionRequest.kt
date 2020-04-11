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
import com.oymj.greenearthhero.data.RecycleRequest
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.LocationUtils
import com.oymj.greenearthhero.utils.RippleUtil

class RecyclerItemMyVolunteerCollectionRequest : UniversalRecyclerItem(RecycleRequest::class.java.simpleName, R.layout.listitem_collecting_recycle_request_with_status){

    override fun inflateView(parent: ViewGroup, context: Context): View {
        return super.inflateView(parent, context)
    }

    override fun getViewHolder(parent: ViewGroup, context: Context, adapter: UniversalAdapter) : UniversalViewHolder {
        return ViewHolder(inflateView(parent,context),adapter)
    }

    inner class ViewHolder (var view: View,var adapter: UniversalAdapter) : UniversalViewHolder(view,this) {

        override fun onBindViewHolder(data:Any) {
            if(data is RecycleRequest){
                val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
                val tvAddress = view.findViewById<TextView>(R.id.tvAddress)
                val tvMetalAmount = view.findViewById<TextView>(R.id.tvMetalAmount)
                val tvPaperAmount = view.findViewById<TextView>(R.id.tvPaperAmount)
                val tvPlasticAmount = view.findViewById<TextView>(R.id.tvPlasticAmount)
                val tvGlassAmount = view.findViewById<TextView>(R.id.tvGlassAmount)
                val tvDistanceAway = view.findViewById<TextView>(R.id.tvDistanceAway)
                val tvTotal = view.findViewById<TextView>(R.id.tvTotal)
                val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
                val btnCancelVolunteer = view.findViewById<TextView>(R.id.btnCancelVolunteer)
                val btnCancelRequest = view.findViewById<TextView>(R.id.btnCancelRequest)
                val btnChat = view.findViewById<TextView>(R.id.btnChat)

                val tvDistanceAwayContainer = view.findViewById<LinearLayout>(R.id.tvDistanceAwayContainer)
                val totalContainer = view.findViewById<LinearLayout>(R.id.totalContainer)


                //set data
                tvTitle.text = data.requestedUser.getFullName()
                tvAddress.text = data.address
                tvMetalAmount.text = "${data.metalWeight} KG"
                tvPaperAmount.text = "${data.paperWeight} KG"
                tvPlasticAmount.text = "${data.plasticWeight} KG"
                tvGlassAmount.text = "${data.glassWeight} KG"
                tvTotal.text = "${data.getTotalAmount()} KG"
                tvStatus.text ="Status: ha"
                btnChat.text = "Chat with the Request Owner"

                btnCancelRequest.visibility = View.GONE

                if(LocationUtils.getLastKnownLocation() != null){
                    var userCurrentLoc = LocationUtils.getLastKnownLocation()
                    tvDistanceAway.text = String.format("%.2f",data.getDistanceBetween()/1000)
                }else{
                    tvDistanceAway.text = "N/A"
                }


                //set ripple background
                btnCancelVolunteer.background = RippleUtil.getRippleButtonOutlineDrawable(view.context,
                    view.context.resources.getColor(R.color.red),
                    view.context.resources.getColor(R.color.transparent_pressed),
                    view.context.resources.getColor(R.color.transparent),
                    30f,0
                )

                //set ripple background
                btnCancelRequest.background = RippleUtil.getRippleButtonOutlineDrawable(view.context,
                    view.context.resources.getColor(R.color.red),
                    view.context.resources.getColor(R.color.transparent_pressed),
                    view.context.resources.getColor(R.color.transparent),
                    25f,0
                )

                //set ripple background
                btnChat.background = RippleUtil.getRippleButtonOutlineDrawable(view.context,
                    view.context.resources.getColor(R.color.darkgrey),
                    view.context.resources.getColor(R.color.transparent_pressed),
                    view.context.resources.getColor(R.color.transparent),
                    25f,0
                )

                btnCancelRequest.setOnClickListener{
                    adapter.onItemClickedListener(data,1)
                }

                btnChat.setOnClickListener{
                    adapter.onItemClickedListener(data,2)
                }
            }
        }
    }
}
