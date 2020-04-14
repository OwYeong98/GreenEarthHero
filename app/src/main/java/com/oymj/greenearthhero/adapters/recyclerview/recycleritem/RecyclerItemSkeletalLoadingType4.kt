package com.oymj.greenearthhero.adapters.recyclerview.recycleritem

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.shimmer.ShimmerFrameLayout
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.adapters.recyclerview.UniversalRecyclerItem
import com.oymj.greenearthhero.adapters.recyclerview.UniversalViewHolder
import com.oymj.greenearthhero.data.SkeletalEmptyModel3
import com.oymj.greenearthhero.data.SkeletalEmptyModel4
import com.oymj.greenearthhero.utils.RippleUtil

class RecyclerItemSkeletalLoadingType4 : UniversalRecyclerItem(SkeletalEmptyModel4::class.java.simpleName, R.layout.listitem_skeletal_loading_4){

    override fun inflateView(parent: ViewGroup, context: Context): View {
        return super.inflateView(parent, context)
    }

    override fun getViewHolder(parent: ViewGroup, context: Context, adapter: UniversalAdapter) : UniversalViewHolder {
        this.viewHolder = ViewHolder(inflateView(parent,context),adapter)
        return viewHolder
    }

    inner class ViewHolder (var view: View, var adapter: UniversalAdapter) : UniversalViewHolder(view,this) {

        override fun onBindViewHolder(data:Any) {
            var shimmerLayout = view.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
            var circle = view.findViewById<TextView>(R.id.circle)

            circle.background = RippleUtil.getRippleButtonOutlineDrawable(view.context!!,
                Color.parseColor("#BFB6B6"),
                Color.parseColor("#BFB6B6"),
                Color.parseColor("#BFB6B6"),
                100f,0)

            shimmerLayout.startShimmerAnimation()
        }
    }
}