package com.oymj.greenearthhero.adapters.recyclerview.recycleritem

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.adapters.recyclerview.UniversalRecyclerItem
import com.oymj.greenearthhero.adapters.recyclerview.UniversalViewHolder
import com.oymj.greenearthhero.data.Food

class RecyclerItemFoodEditable : UniversalRecyclerItem(Food::class.java.simpleName, R.layout.listitem_food){

    override fun getViewHolder(parent: ViewGroup, context: Context, adapter: UniversalAdapter) : UniversalViewHolder {
        this.viewHolder = ViewHolder(inflateView(parent,context),adapter)
        return viewHolder
    }

    inner class ViewHolder (var view: View,var adapter: UniversalAdapter) : UniversalViewHolder(view,this) {

        override fun onBindViewHolder(data:Any) {
            if(data is Food){
                var ivFoodImage = view.findViewById<ImageView>(R.id.ivFoodImage)
                var tvFoodName = view.findViewById<TextView>(R.id.tvFoodName)
                var tvFoodDesc = view.findViewById<TextView>(R.id.tvFoodDesc)
                var tvFoodQuantity = view.findViewById<TextView>(R.id.tvFoodQuantity)
                var btnEdit = view.findViewById<LinearLayout>(R.id.btnEdit)
                var btnDelete = view.findViewById<LinearLayout>(R.id.btnDelete)
                var collectContainer = view.findViewById<LinearLayout>(R.id.collectContainer)

                btnEdit.visibility = View.VISIBLE
                btnDelete.visibility = View.VISIBLE
                collectContainer.visibility = View.GONE

                ivFoodImage.setImageBitmap(data.imageBitmap)
                tvFoodName.text = data.foodName
                tvFoodDesc.text = data.foodDesc
                tvFoodQuantity.text = "Total Quantity: ${data.foodQuantity}"

                btnEdit.setOnClickListener {
                    adapter.onItemClickedListener(data,1)
                }

                btnDelete.setOnClickListener {
                    adapter.onItemClickedListener(data,2)
                }
            }
        }
    }
}