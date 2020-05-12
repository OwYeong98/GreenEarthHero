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
import com.oymj.greenearthhero.data.FoodDonationHistory
import com.oymj.greenearthhero.utils.RippleUtil
import java.text.SimpleDateFormat

class RecyclerItemFoodDonationHistory : UniversalRecyclerItem(FoodDonationHistory::class.java.simpleName, R.layout.listitem_food_donation_history){

    override fun getViewHolder(parent: ViewGroup, context: Context, adapter: UniversalAdapter) : UniversalViewHolder {
        return ViewHolder(inflateView(parent,context),adapter)
    }

    inner class ViewHolder (var view: View, var adapter: UniversalAdapter) : UniversalViewHolder(view,this) {

        override fun onBindViewHolder(data:Any) {
            if(data is FoodDonationHistory){
                val mainContainer = view.findViewById<LinearLayout>(R.id.mainContainer)
                val tvDate = view.findViewById<TextView>(R.id.tvDate)
                val tvRestaurantName = view.findViewById<TextView>(R.id.tvRestaurantName)
                val tvAddress = view.findViewById<TextView>(R.id.tvAddress)
                val tvTotalFood = view.findViewById<TextView>(R.id.tvTotalFood)

                tvDate.text = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(data.dateEnded)
                tvRestaurantName.text = data.location.name
                tvAddress.text = data.location.address


                tvTotalFood.background = RippleUtil.getRippleButtonOutlineDrawable(view.context,
                    view.context.resources.getColor(R.color.darkgrey),
                    view.context.resources.getColor(R.color.transparent_pressed),
                    view.context.resources.getColor(R.color.transparent),
                    1000f,0
                )

                //set ripple background
                mainContainer.background = RippleUtil.getRippleButtonOutlineDrawable(mainContainer.context,
                    mainContainer.context.resources.getColor(R.color.lightestgrey),
                    mainContainer.context.resources.getColor(R.color.transparent_pressed),
                    mainContainer.context.resources.getColor(R.color.transparent),
                    25f,0
                )

                mainContainer.setOnClickListener {
                  adapter.onItemClickedListener(data,1)
                }
            }
        }
    }
}