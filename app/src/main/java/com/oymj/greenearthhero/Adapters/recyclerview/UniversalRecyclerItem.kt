package com.oymj.greenearthhero.adapters.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class UniversalRecyclerItem(val dataClassName : String, val viewLayoutId : Int){

    open fun inflateView(parent : ViewGroup, context : Context) : View {
        if(viewLayoutId == -1){
            throw Exception("Calling inflateView without a valid layout")
        }
        return LayoutInflater.from(context).inflate(viewLayoutId, parent, false)
    }

    abstract open fun getViewHolder(parent: ViewGroup, context: Context,adapter: UniversalAdapter):UniversalViewHolder
}