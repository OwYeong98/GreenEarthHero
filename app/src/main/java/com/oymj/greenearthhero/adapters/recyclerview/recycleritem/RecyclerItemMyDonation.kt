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
import com.oymj.greenearthhero.data.FoodDonation
import com.oymj.greenearthhero.utils.RippleUtil
import java.text.SimpleDateFormat

class RecyclerItemMyDonation : UniversalRecyclerItem(FoodDonation::class.java.simpleName, R.layout.listitem_my_donation){

    override fun getViewHolder(parent: ViewGroup, context: Context, adapter: UniversalAdapter) : UniversalViewHolder {
        return ViewHolder(inflateView(parent,context),adapter)
    }

    inner class ViewHolder (var view: View, var adapter: UniversalAdapter) : UniversalViewHolder(view,this) {

        override fun onBindViewHolder(data:Any) {
            if(data is FoodDonation){
                val mainContainer = view.findViewById<LinearLayout>(R.id.mainContainer)
                val tvRestaurantName = view.findViewById<TextView>(R.id.tvRestaurantName)
                val tvAddress = view.findViewById<TextView>(R.id.tvAddress)
                val tvDatePosted = view.findViewById<TextView>(R.id.tvDatePosted)
                val tvDateEnd = view.findViewById<TextView>(R.id.tvDateEnd)
                val tvTotalFood = view.findViewById<TextView>(R.id.tvTotalFood)

                var dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                //set data
                tvRestaurantName.text = data.location.name
                tvAddress.text = data.location.address
                tvDatePosted.text = dateFormat.format(data.datePosted)
                tvDateEnd.text = dateFormat.format(data.getDonationEndTime())
                tvTotalFood.text = "Total Food: ${data.totalFoodAmount}"

                //set ripple background
                mainContainer.background = RippleUtil.getRippleButtonOutlineDrawable(mainContainer.context,
                    mainContainer.context.resources.getColor(R.color.lightestgrey),
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