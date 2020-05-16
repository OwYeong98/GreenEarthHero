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
import com.oymj.greenearthhero.data.Notification
import com.oymj.greenearthhero.utils.RippleUtil
import java.text.SimpleDateFormat

class RecyclerItemNotification : UniversalRecyclerItem(Notification::class.java.simpleName, R.layout.listitem_notification){

    override fun inflateView(parent: ViewGroup, context: Context): View {
        return super.inflateView(parent, context)
    }

    override fun getViewHolder(parent: ViewGroup, context: Context, adapter: UniversalAdapter) : UniversalViewHolder {
        this.viewHolder = ViewHolder(inflateView(parent,context),adapter)
        return viewHolder
    }

    inner class ViewHolder (var view: View,var adapter: UniversalAdapter) : UniversalViewHolder(view,this) {

        override fun onBindViewHolder(data:Any) {
            if(data is Notification){
                var mainContainer = view.findViewById<LinearLayout>(R.id.mainContainer)
                var tvNotificationTitle = view.findViewById<TextView>(R.id.tvNotificationTitle)
                var tvNotificationDate = view.findViewById<TextView>(R.id.tvNotificationDate)
                var ivNotificationIcon = view.findViewById<ImageView>(R.id.ivNotificationIcon)

                tvNotificationTitle.text = data.title
                tvNotificationDate.text = SimpleDateFormat("dd/MM EEE HH:mm").format(data.date)
                ivNotificationIcon.setImageResource(data.getNotificationIconImageResourceId())

                mainContainer.background = RippleUtil.getRippleButtonOutlineDrawable(
                    view.context,
                    view.context.resources.getColor(R.color.transparent),
                    view.context.resources.getColor(R.color.transparent_pressed),
                    view.context.resources.getColor(R.color.transparent),
                    0f,0)

                mainContainer.setOnClickListener {
                    adapter.onItemClickedListener(data,1)
                }

            }
        }
    }
}