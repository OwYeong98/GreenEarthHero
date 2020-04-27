package com.oymj.greenearthhero.adapters.recyclerview.recycleritem

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.storage.FirebaseStorage
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.adapters.recyclerview.UniversalRecyclerItem
import com.oymj.greenearthhero.adapters.recyclerview.UniversalViewHolder
import com.oymj.greenearthhero.data.ClaimFood

class RecyclerItemFoodEditableAmount : UniversalRecyclerItem(ClaimFood::class.java.simpleName, R.layout.listitem_food){

    override fun getViewHolder(parent: ViewGroup, context: Context, adapter: UniversalAdapter) : UniversalViewHolder {
        this.viewHolder = ViewHolder(inflateView(parent,context),adapter)
        return viewHolder
    }

    inner class ViewHolder (var view: View, var adapter: UniversalAdapter) : UniversalViewHolder(view,this) {

        override fun onBindViewHolder(data:Any) {
            if(data is ClaimFood){
                var ivFoodImage = view.findViewById<ImageView>(R.id.ivFoodImage)
                var tvFoodName = view.findViewById<TextView>(R.id.tvFoodName)
                var tvFoodDesc = view.findViewById<TextView>(R.id.tvFoodDesc)
                var tvFoodQuantity = view.findViewById<TextView>(R.id.tvFoodQuantity)
                var btnEdit = view.findViewById<LinearLayout>(R.id.btnEdit)
                var btnDelete = view.findViewById<LinearLayout>(R.id.btnDelete)
                var btnAddQuantity = view.findViewById<ImageButton>(R.id.btnAddQuantity)
                var btnMinusQuantity = view.findViewById<ImageButton>(R.id.btnMinusQuantity)
                var tvCurrentAmount = view.findViewById<TextView>(R.id.tvCurrentAmount)
                var tvFoodLeft = view.findViewById<TextView>(R.id.tvFoodLeft)
                var collectContainer = view.findViewById<LinearLayout>(R.id.collectContainer)

                btnEdit.visibility = View.GONE
                btnDelete.visibility = View.GONE
                collectContainer.visibility = View.VISIBLE

                var storageRef = FirebaseStorage.getInstance().reference
                var foodImageRef = storageRef.child(data.food.imageUrl)

                val TEN_MEGABYTE: Long = 1024 * 1024 * 10
                foodImageRef.getBytes(TEN_MEGABYTE)
                    .addOnSuccessListener {
                            imageData->

                        if(imageData != null){
                            var foodImage = BitmapFactory.decodeByteArray(imageData!!,0,imageData.size)
                            ivFoodImage.setImageBitmap(foodImage)
                        }

                    }
                ivFoodImage.setImageBitmap(data.food.imageBitmap)
                tvFoodName.text = data.food.foodName
                tvFoodDesc.text = data.food.foodDesc
                tvFoodQuantity.text = "Total Quantity: ${data.food.foodQuantity}"
                tvFoodLeft.text = "Food Left: ${data.food.foodQuantity - data.food.claimedFoodQuantity}"

                tvCurrentAmount.text = data.claimAmount.toString()

                btnEdit.setOnClickListener {
                    adapter.onItemClickedListener(data,1)
                }

                btnDelete.setOnClickListener {
                    adapter.onItemClickedListener(data,2)
                }

                btnAddQuantity.setOnClickListener {
                    data.claimAmount++
                    tvCurrentAmount.text = data.claimAmount.toString()
                }

                btnMinusQuantity.setOnClickListener {
                    if(data.claimAmount * -1 < data.food.foodQuantity - data.food.claimedFoodQuantity)
                        data.claimAmount--

                    tvCurrentAmount.text = data.claimAmount.toString()
                }
            }
        }
    }
}