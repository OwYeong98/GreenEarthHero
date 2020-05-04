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
import com.oymj.greenearthhero.data.SecondHandItem
import com.oymj.greenearthhero.utils.RippleUtil
import java.text.SimpleDateFormat

class RecyclerItemCurrentItemPurchase : UniversalRecyclerItem(SecondHandItem::class.java.simpleName, R.layout.listitem_second_hand_item_with_status){

    override fun getViewHolder(parent: ViewGroup, context: Context, adapter: UniversalAdapter) : UniversalViewHolder {
        return ViewHolder(inflateView(parent,context),adapter)
    }

    inner class ViewHolder (var view: View, var adapter: UniversalAdapter) : UniversalViewHolder(view,this) {

        override fun onBindViewHolder(data:Any) {
            if(data is SecondHandItem){
                val mainContainer = view.findViewById<LinearLayout>(R.id.mainContainer)
                val tvDate = view.findViewById<TextView>(R.id.tvDate)
                val tvItemName = view.findViewById<TextView>(R.id.tvItemName)
                val tvPrice = view.findViewById<TextView>(R.id.tvPrice)
                val tvStatus = view.findViewById<TextView>(R.id.tvStatus)

                tvDate.text = "${SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(data.datePosted)}"
                tvItemName.text = data.itemName
                tvPrice.text = "Price: RM ${data.itemPrice}"

                if(data.trackingNo != ""){
                    tvStatus.text = "On Delivery"
                    tvStatus.background = RippleUtil.getRippleButtonOutlineDrawable(view.context,
                        view.context.resources.getColor(R.color.slightdarkgreen),
                        view.context.resources.getColor(R.color.transparent_pressed),
                        view.context.resources.getColor(R.color.transparent),
                        0f,0
                    )
                }else{
                    tvStatus.text = "Pending for Delivery"
                    tvStatus.background = RippleUtil.getRippleButtonOutlineDrawable(view.context,
                        view.context.resources.getColor(R.color.yellow),
                        view.context.resources.getColor(R.color.transparent_pressed),
                        view.context.resources.getColor(R.color.transparent),
                        0f,0
                    )
                }


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