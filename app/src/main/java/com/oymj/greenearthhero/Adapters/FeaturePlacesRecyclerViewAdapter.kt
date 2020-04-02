package com.oymj.greenearthhero.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.RecyclerViewOnItemClickListener
import com.oymj.greenearthhero.data.FeaturePlaces
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.listitem_feature_places.view.*

class FeaturePlacesRecyclerViewAdapter(val data : ArrayList<FeaturePlaces>, val context: Context, val onItemClickListener: RecyclerViewOnItemClickListener) : RecyclerView.Adapter<ViewHolder>() {

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return data.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.listitem_feature_places, parent, false))
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var currentData = data[position]

        //set data
        holder.tvLocationTitle.text = currentData.title
        holder.tvDistance.text = "10 km"
        holder.tvAddress.text = currentData.fullAddress

        //set ripple background
        holder.mainContainer.background = RippleUtil.getRippleButtonOutlineDrawable(holder.mainContainer.context,
            holder.mainContainer.context.resources.getColor(R.color.transparent),
            holder.mainContainer.context.resources.getColor(R.color.transparent_pressed),
            holder.mainContainer.context.resources.getColor(R.color.transparent),
            0f,0
        )

        holder.mainContainer.setOnClickListener{
            onItemClickListener.onItemClick(data[position])
        }

    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {

    val mainContainer = view.mainContainer
    val tvLocationTitle = view.tvLocationTitle
    val tvDistance = view.tvDistance
    val tvAddress = view.tvAddress
}