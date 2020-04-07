package com.oymj.greenearthhero.adapters.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.facebook.shimmer.ShimmerFrameLayout

abstract class UniversalRecyclerItem(val dataClassName : String, val viewLayoutId : Int){

    lateinit var inflatedView:View
    lateinit var viewHolder: UniversalViewHolder

    open fun inflateView(parent : ViewGroup, context : Context) : View {
        if(viewLayoutId == -1){
            throw Exception("Calling inflateView without a valid layout")
        }
        inflatedView =LayoutInflater.from(context).inflate(viewLayoutId, parent, false)

        return inflatedView
    }

    abstract open fun getViewHolder(parent: ViewGroup, context: Context,adapter: UniversalAdapter):UniversalViewHolder
}