package com.oymj.greenearthhero.adapters.recyclerview.recycleritem

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.storage.FirebaseStorage
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.adapters.recyclerview.UniversalRecyclerItem
import com.oymj.greenearthhero.adapters.recyclerview.UniversalViewHolder
import com.oymj.greenearthhero.data.SecondHandItem
import com.oymj.greenearthhero.utils.RippleUtil
import java.text.SimpleDateFormat

class RecyclerItemSecondHandItem : UniversalRecyclerItem(SecondHandItem::class.java.simpleName, R.layout.listitem_second_hand_item){

    override fun getViewHolder(parent: ViewGroup, context: Context, adapter: UniversalAdapter) : UniversalViewHolder {
        return ViewHolder(inflateView(parent,context),adapter)
    }

    inner class ViewHolder (var view: View, var adapter: UniversalAdapter) : UniversalViewHolder(view,this) {

        override fun onBindViewHolder(data:Any) {
            if(data is SecondHandItem){
                val ivImage = view.findViewById<ImageView>(R.id.ivItemImage)
                val tvName = view.findViewById<TextView>(R.id.tvItemName)
                val tvItemPrice = view.findViewById<TextView>(R.id.tvItemPrice)
                val mainContainer = view.findViewById<LinearLayout>(R.id.mainContainer)
                val shimmerLayout = view.findViewById<ShimmerFrameLayout>(R.id.shimmerLayout)

                mainContainer.background = RippleUtil.getRippleButtonOutlineDrawable(view.context,
                    view.context.resources.getColor(R.color.transparent),
                    view.context.resources.getColor(R.color.transparent_pressed),
                    view.context.resources.getColor(R.color.transparent),
                    25f,0
                )

                tvName.text = data.itemName
                tvItemPrice.text = "RM ${String.format("%.2f",data.itemPrice)}"

                ivImage.setImageResource(R.drawable.skeleton_rounded_square)
                shimmerLayout.startShimmerAnimation()

                if(data.imageUrl != ""){
                    var storageRef = FirebaseStorage.getInstance().reference
                    var foodImageRef = storageRef.child(data.imageUrl)
                    val TEN_MEGABYTE: Long = 1024 * 1024 * 10
                    foodImageRef.getBytes(TEN_MEGABYTE)
                        .addOnSuccessListener {
                                imageData->

                            if(imageData != null){
                                shimmerLayout.stopShimmerAnimation()
                                var foodImage = BitmapFactory.decodeByteArray(imageData!!,0,imageData.size)
                                ivImage.setImageBitmap(foodImage)
                            }

                        }
                }


                mainContainer.setOnClickListener {
                    adapter.onItemClickedListener(data,1)
                }

            }
        }
    }
}