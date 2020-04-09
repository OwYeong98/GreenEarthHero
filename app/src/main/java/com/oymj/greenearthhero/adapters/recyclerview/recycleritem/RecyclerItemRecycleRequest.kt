package com.oymj.greenearthhero.adapters.recyclerview.recycleritem

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.adapters.recyclerview.UniversalRecyclerItem
import com.oymj.greenearthhero.adapters.recyclerview.UniversalViewHolder
import com.oymj.greenearthhero.data.RecycleRequest
import com.oymj.greenearthhero.utils.RippleUtil

class RecyclerItemRecycleRequest : UniversalRecyclerItem(RecycleRequest::class.java.simpleName, R.layout.listitem_recycle_request){

    override fun inflateView(parent: ViewGroup, context: Context): View {
        return super.inflateView(parent, context)
    }

    override fun getViewHolder(parent: ViewGroup, context: Context, adapter: UniversalAdapter) : UniversalViewHolder {
        return ViewHolder(inflateView(parent,context),adapter)
    }

    inner class ViewHolder (var view: View,var adapter: UniversalAdapter) : UniversalViewHolder(view,this) {

        override fun onBindViewHolder(data:Any) {
            if(data is RecycleRequest){
                val mainContainer = view.findViewById<ConstraintLayout>(R.id.mainContainer)
                val tvRequestingUser = view.findViewById<TextView>(R.id.tvRequestingUser)
                val tvAddress = view.findViewById<TextView>(R.id.tvAddress)
                val tvMetalAmount = view.findViewById<TextView>(R.id.tvMetalAmount)
                val tvPaperAmount = view.findViewById<TextView>(R.id.tvPaperAmount)
                val tvPlasticAmount = view.findViewById<TextView>(R.id.tvPlasticAmount)
                val tvGlassAmount = view.findViewById<TextView>(R.id.tvGlassAmount)
                val tvDistanceAway = view.findViewById<TextView>(R.id.tvDistanceAway)
                val tvTotal = view.findViewById<TextView>(R.id.tvTotal)

                //set data
                tvRequestingUser.text = data.requestedUser.getFullName()
                tvAddress.text = data.address
                tvMetalAmount.text = "${data.metalWeight} KG"
                tvPaperAmount.text = "${data.paperWeight} KG"
                tvPlasticAmount.text = "${data.plasticWeight} KG"
                tvGlassAmount.text = "${data.glassWeight} KG"
                tvDistanceAway.text = "10"
                tvTotal.text = "${data.getTotalAmount()} KG"


                //set ripple background
                mainContainer.background = RippleUtil.getRippleButtonOutlineDrawable(mainContainer.context,
                    mainContainer.context.resources.getColor(R.color.white),
                    mainContainer.context.resources.getColor(R.color.transparent_pressed),
                    mainContainer.context.resources.getColor(R.color.transparent),
                    25f,0
                )

                mainContainer.setOnClickListener{
                    adapter.onItemClickedListener(data,1)
                }
            }
        }
    }
}