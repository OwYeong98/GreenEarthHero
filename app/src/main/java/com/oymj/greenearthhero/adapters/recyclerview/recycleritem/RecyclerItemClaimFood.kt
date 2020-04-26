package com.oymj.greenearthhero.adapters.recyclerview.recycleritem

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.adapters.recyclerview.UniversalRecyclerItem
import com.oymj.greenearthhero.adapters.recyclerview.UniversalViewHolder
import com.oymj.greenearthhero.data.ClaimFood
import com.oymj.greenearthhero.data.Food

class RecyclerItemClaimFood : UniversalRecyclerItem(ClaimFood::class.java.simpleName, R.layout.listitem_food){

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
                var collectContainer = view.findViewById<LinearLayout>(R.id.collectContainer)

                btnEdit.visibility = View.GONE
                btnDelete.visibility = View.GONE
                collectContainer.visibility = View.VISIBLE

                ivFoodImage.setImageBitmap(data.food.imageBitmap)
                tvFoodName.text = data.food.foodName
                tvFoodDesc.text = data.food.foodDesc
                tvFoodQuantity.text = "Total Quantity: ${data.food.foodQuantity}"

                tvCurrentAmount.text = data.claimAmount.toString()

                btnEdit.setOnClickListener {
                    adapter.onItemClickedListener(data,1)
                }

                btnDelete.setOnClickListener {
                    adapter.onItemClickedListener(data,2)
                }

                btnAddQuantity.setOnClickListener {
                    data.claimAmount++
                    adapter.notifyDataSetChanged()
                }

                btnMinusQuantity.setOnClickListener {
                    if(data.claimAmount > 0)
                        data.claimAmount--

                    adapter.notifyDataSetChanged()
                }
            }
        }
    }
}