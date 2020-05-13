package com.oymj.greenearthhero.adapters.recyclerview.recycleritem

import android.content.Context
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.storage.FirebaseStorage
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.adapters.recyclerview.UniversalRecyclerItem
import com.oymj.greenearthhero.adapters.recyclerview.UniversalViewHolder
import com.oymj.greenearthhero.data.SecondHandItemHistory
import com.oymj.greenearthhero.utils.RippleUtil
import java.text.SimpleDateFormat

class RecyclerItemSecondHandItemHistory : UniversalRecyclerItem(SecondHandItemHistory::class.java.simpleName, R.layout.listitem_second_hand_item_history){

    override fun getViewHolder(parent: ViewGroup, context: Context, adapter: UniversalAdapter) : UniversalViewHolder {
        return ViewHolder(inflateView(parent,context),adapter)
    }

    inner class ViewHolder (var view: View, var adapter: UniversalAdapter) : UniversalViewHolder(view,this) {

        override fun onBindViewHolder(data:Any) {
            if(data is SecondHandItemHistory){
                val tvDate = view.findViewById<TextView>(R.id.tvDate)
                val tvItemName = view.findViewById<TextView>(R.id.tvItemName)
                val tvBuyerName = view.findViewById<TextView>(R.id.tvBuyerName)
                val tvSellerName = view.findViewById<TextView>(R.id.tvSellerName)
                val tvPrice = view.findViewById<TextView>(R.id.tvPrice)
                val ivItemImage = view.findViewById<ImageView>(R.id.ivItemImage)
                val mainContainer = view.findViewById<LinearLayout>(R.id.mainContainer)
                val shimmerLayout = view.findViewById<ShimmerFrameLayout>(R.id.shimmerLayout)



                tvPrice.background = RippleUtil.getRippleButtonOutlineDrawable(view.context,
                    view.context.resources.getColor(R.color.darkgrey),
                    view.context.resources.getColor(R.color.transparent_pressed),
                    view.context.resources.getColor(R.color.transparent),
                    1000f,0
                )

                //set data
                tvDate.text = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(data.date)
                tvItemName.text = data.itemName
                tvBuyerName.text = data.userBought.getFullName()
                tvSellerName.text = data.userPosted.getFullName()
                tvPrice.text = "RM ${String.format("%.2f",data.itemPrice)}"

                ivItemImage.setImageResource(R.drawable.skeleton_rounded_square)
                shimmerLayout.startShimmerAnimation()


                var storageRef = FirebaseStorage.getInstance().reference
                var foodImageRef = storageRef.child(data.imageUrl)
                val TEN_MEGABYTE: Long = 1024 * 1024 * 10
                foodImageRef.getBytes(TEN_MEGABYTE)
                    .addOnSuccessListener {
                            imageData->

                        if(imageData != null){
                            shimmerLayout.stopShimmerAnimation()
                            var foodImage = BitmapFactory.decodeByteArray(imageData!!,0,imageData.size)
                            ivItemImage.setImageBitmap(foodImage)
                        }

                    }

            }
        }
    }
}