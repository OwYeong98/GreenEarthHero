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
import com.oymj.greenearthhero.data.FoodDonation
import com.oymj.greenearthhero.utils.LocationUtils
import com.oymj.greenearthhero.utils.RippleUtil

class RecyclerItemFoodDonation : UniversalRecyclerItem(FoodDonation::class.java.simpleName, R.layout.listitem_food_donation){

    override fun getViewHolder(parent: ViewGroup, context: Context, adapter: UniversalAdapter) : UniversalViewHolder {
        return ViewHolder(inflateView(parent,context),adapter)
    }

    inner class ViewHolder (var view: View, var adapter: UniversalAdapter) : UniversalViewHolder(view,this) {

        override fun onBindViewHolder(data:Any) {
            if(data is FoodDonation){
                val mainContainer = view.findViewById<ConstraintLayout>(R.id.mainContainer)
                val tvRestaurantName = view.findViewById<TextView>(R.id.tvRestaurantName)
                val tvDonatingUser = view.findViewById<TextView>(R.id.tvDonatingUser)
                val tvAddress = view.findViewById<TextView>(R.id.tvAddress)
                val tvTotalFoodQuantity = view.findViewById<TextView>(R.id.tvTotalFoodQuantity)
                val tvDistanceAway = view.findViewById<TextView>(R.id.tvDistanceAway)

                //set data
                tvRestaurantName.text = data.location.name
                tvDonatingUser.text = data.donatorUser.getFullName()
                tvAddress.text = data.location.address
                tvTotalFoodQuantity.text = "Total Food Quantity: ${data.totalFoodAmount}"

                if(LocationUtils.getLastKnownLocation() != null){
                    var userCurrentLoc = LocationUtils.getLastKnownLocation()
                    tvDistanceAway.text = String.format("%.2f",data.getDistanceBetween()/1000)
                }else{
                    tvDistanceAway.text = "N/A"
                }


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