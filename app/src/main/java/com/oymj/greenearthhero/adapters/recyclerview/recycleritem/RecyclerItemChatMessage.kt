package com.oymj.greenearthhero.adapters.recyclerview.recycleritem

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.adapters.recyclerview.UniversalRecyclerItem
import com.oymj.greenearthhero.adapters.recyclerview.UniversalViewHolder
import com.oymj.greenearthhero.data.ChatMessage
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.RippleUtil
import java.text.SimpleDateFormat

class RecyclerItemChatMessage : UniversalRecyclerItem(ChatMessage::class.java.simpleName, R.layout.listitem_chat_message){

    override fun inflateView(parent: ViewGroup, context: Context): View {
        return super.inflateView(parent, context)
    }

    override fun getViewHolder(parent: ViewGroup, context: Context, adapter: UniversalAdapter) : UniversalViewHolder {
        this.viewHolder = ViewHolder(inflateView(parent,context),adapter)
        return viewHolder
    }

    inner class ViewHolder (var view: View,var adapter: UniversalAdapter) : UniversalViewHolder(view,this) {

        override fun onBindViewHolder(data:Any) {
            if(data is ChatMessage){
                var chatContainer = view.findViewById<LinearLayout>(R.id.chatContainer)
                var tvMessage = view.findViewById<TextView>(R.id.tvMessage)
                var tvDate = view.findViewById<TextView>(R.id.tvDate)
                var rightSpace = view.findViewById<Space>(R.id.rightSpace)
                var leftSpace = view.findViewById<Space>(R.id.leftSpace)

                if(data.userSend == FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(view.context)){
                    rightSpace.visibility = View.GONE
                    leftSpace.visibility = View.VISIBLE

                    chatContainer.gravity = Gravity.RIGHT

                    tvMessage.setTextColor(view.context.resources.getColor(R.color.white))

                    tvMessage.background = RippleUtil.getRippleButtonOutlineDrawable(
                        view.context,
                        Color.parseColor("#2F4AE1"),
                        view.context.resources.getColor(R.color.transparent_pressed),
                        view.context.resources.getColor(R.color.transparent),
                        25f,0)

                }else{
                    rightSpace.visibility = View.VISIBLE
                    leftSpace.visibility = View.GONE

                    chatContainer.gravity = Gravity.LEFT

                    tvMessage.setTextColor(view.context.resources.getColor(R.color.black))

                    tvMessage.background = RippleUtil.getRippleButtonOutlineDrawable(
                        view.context,
                        view.context.resources.getColor(R.color.lightestgrey),
                        view.context.resources.getColor(R.color.transparent_pressed),
                        view.context.resources.getColor(R.color.transparent),
                        25f,0)

                }



                chatContainer.setOnClickListener{
                    if(tvDate.visibility == View.GONE){
                        tvDate.visibility = View.VISIBLE
                    }else{
                        tvDate.visibility = View.GONE
                    }

                }

                tvMessage.text = data.message
                tvDate.text = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(data.dateSent)
            }
        }
    }
}