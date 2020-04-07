package com.oymj.greenearthhero.adapters.recyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout

abstract class UniversalViewHolder(view: View,var recyclerItem:UniversalRecyclerItem): RecyclerView.ViewHolder(view) {

    open abstract fun onBindViewHolder(data:Any)

}