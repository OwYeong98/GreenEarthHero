package com.oymj.greenearthhero.adapters.recyclerview.recycleritem

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.adapters.recyclerview.UniversalRecyclerItem
import com.oymj.greenearthhero.adapters.recyclerview.UniversalViewHolder
import com.oymj.greenearthhero.data.ChatRoom
import com.oymj.greenearthhero.data.User
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.RippleUtil

class RecyclerItemChatRoom : UniversalRecyclerItem(ChatRoom::class.java.simpleName, R.layout.listitem_chat_room){


    override fun getViewHolder(parent: ViewGroup, context: Context, adapter: UniversalAdapter) : UniversalViewHolder {
        return ViewHolder(inflateView(parent,context),adapter)
    }

    inner class ViewHolder (var view: View, var adapter: UniversalAdapter) : UniversalViewHolder(view,this) {

        override fun onBindViewHolder(data:Any) {
            if(data is ChatRoom){
                val mainContainer = view.findViewById<LinearLayout>(R.id.mainContainer)
                val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
                val tvMessage = view.findViewById<TextView>(R.id.tvMessage)

                var opponentUser: User? = null
                //set data
                if(data.chatUser1.userId == FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(view.context)){
                    tvUserName.text = data.chatUser2.getFullName()
                    opponentUser = data.chatUser2
                }else{
                    tvUserName.text = data.chatUser1.getFullName()
                    opponentUser = data.chatUser1
                }


                if(data.lastMessageSendBy != ""){

                    if(data.lastMessageSendBy ==  FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(view.context)){
                        tvMessage.text = "You: ${data.lastMessage}"
                    }else{
                        tvMessage.text = "Him: ${data.lastMessage}"
                    }

                }else{
                    tvMessage.text = "Say Hi to ${opponentUser.lastName}!"
                }

                //set ripple background
                mainContainer.background = RippleUtil.getRippleButtonOutlineDrawable(mainContainer.context,
                    mainContainer.context.resources.getColor(R.color.transparent),
                    mainContainer.context.resources.getColor(R.color.transparent_pressed),
                    mainContainer.context.resources.getColor(R.color.transparent),
                    0f,0
                )

                mainContainer.setOnClickListener{
                    adapter.onItemClickedListener(data,1)
                }
            }
        }
    }
}