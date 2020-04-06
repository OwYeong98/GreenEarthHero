package com.oymj.greenearthhero.adapters.recyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class UniversalViewHolder(view: View): RecyclerView.ViewHolder(view) {

    open abstract fun onBindViewHolder(data:Any)
}