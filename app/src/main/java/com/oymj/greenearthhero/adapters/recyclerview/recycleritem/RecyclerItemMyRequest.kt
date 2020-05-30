package com.oymj.greenearthhero.adapters.recyclerview.recycleritem

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.adapters.recyclerview.UniversalRecyclerItem
import com.oymj.greenearthhero.adapters.recyclerview.UniversalViewHolder
import com.oymj.greenearthhero.data.RecycleRequest
import com.oymj.greenearthhero.utils.RippleUtil

class RecyclerItemMyRequest : UniversalRecyclerItem(RecycleRequest::class.java.simpleName, R.layout.listitem_collecting_recycle_request_with_status){

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
                val btnMarkAsCollected = view.findViewById<TextView>(R.id.btnMarkAsCollected)
                val btnViewVolunteerLocation = view.findViewById<TextView>(R.id.btnViewVolunteerLocation)
                val btnShareLocation = view.findViewById<TextView>(R.id.btnShareLocation)

                val tvDistanceAwayContainer = view.findViewById<CardView>(R.id.tvDistanceAwayContainer)
                val totalContainer = view.findViewById<LinearLayout>(R.id.totalContainer)


                //set data
                tvTitle.text = data.requestedUser.getFullName()
                tvAddress.text = data.address
                tvMetalAmount.text = "${data.metalWeight} KG"
                tvPaperAmount.text = "${data.paperWeight} KG"
                tvPlasticAmount.text = "${data.plasticWeight} KG"
                tvGlassAmount.text = "${data.glassWeight} KG"
                tvTotal.text = "${data.getTotalAmount()} KG"
                btnChat.text = "Chat with the Collector"

                if(data.acceptedCollectUser == null){
                    tvStatus.text ="Status: Pending for Collection"
                }else{
                    tvStatus.text ="Status: Collecting By ${data.acceptedCollectUser!!.getFullName()}"
                }

                if(data.isLocationShared)
                    btnViewVolunteerLocation.visibility = View.VISIBLE
                else
                    btnViewVolunteerLocation.visibility = View.GONE

                //hide
                tvDistanceAwayContainer.visibility = View.GONE
                totalContainer.visibility = View.GONE
                btnShareLocation.visibility = View.GONE

                //hide chat button if there are no anyone accepted the request
                if(data.acceptedCollectUser == null){
                    btnChat.visibility = View.GONE
                }

                btnCancelVolunteer.visibility = View.GONE
                btnMarkAsCollected.visibility = View.GONE

                //set ripple background
                btnCancelRequest.background = RippleUtil.getRippleButtonOutlineDrawable(view.context,
                    view.context.resources.getColor(R.color.red),
                    view.context.resources.getColor(R.color.transparent_pressed),
                    view.context.resources.getColor(R.color.transparent),
                    25f,0
                )

                btnViewVolunteerLocation.background = RippleUtil.getRippleButtonOutlineDrawable(view.context,
                    view.context.resources.getColor(R.color.yellow),
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

                btnViewVolunteerLocation.setOnClickListener {
                    adapter.onItemClickedListener(data,3)

                }

            }
        }
    }
}