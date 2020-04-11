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
import java.text.SimpleDateFormat

class RecyclerItemRecycleHistory : UniversalRecyclerItem(RecycleRequest::class.java.simpleName, R.layout.listitem_collecting_recycle_request_with_status){

    override fun getViewHolder(parent: ViewGroup, context: Context, adapter: UniversalAdapter) : UniversalViewHolder {
        return ViewHolder(inflateView(parent,context),adapter)
    }

    inner class ViewHolder (var view: View, var adapter: UniversalAdapter) : UniversalViewHolder(view,this) {

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

                val tvDistanceAwayContainer = view.findViewById<CardView>(R.id.tvDistanceAwayContainer)
                val totalContainer = view.findViewById<LinearLayout>(R.id.totalContainer)


                //set data
                tvTitle.text = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(data.dateRequested)
                tvAddress.text = data.address
                tvMetalAmount.text = "${data.metalWeight} KG"
                tvPaperAmount.text = "${data.paperWeight} KG"
                tvPlasticAmount.text = "${data.plasticWeight} KG"
                tvGlassAmount.text = "${data.glassWeight} KG"

                tvStatus.visibility = View.GONE
                btnChat.visibility = View.GONE
                btnCancelVolunteer.visibility = View.GONE
                btnCancelRequest.visibility = View.GONE
                tvDistanceAwayContainer.visibility = View.GONE
                totalContainer.visibility = View.GONE
                btnMarkAsCollected.visibility = View.GONE

            }
        }
    }
}