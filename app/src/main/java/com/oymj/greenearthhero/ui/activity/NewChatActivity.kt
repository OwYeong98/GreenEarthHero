package com.oymj.greenearthhero.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.data.ChatMessage
import com.oymj.greenearthhero.data.ChatRoom
import com.oymj.greenearthhero.data.User
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_my_chat.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NewChatActivity : AppCompatActivity(){

    private var chatRoomList = ArrayList<Any>()
    private lateinit var recyclerViewAdapter: UniversalAdapter
    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                btnMenu->{
                    finish()
                }
                btnSearch->{
                    searchForUserFromFirebase(tvSearch.text.toString())
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_chat)

        setupUI()
        setupRecyclerView()
        linkAllButtonWithOnClickListener()

    }

    fun searchForUserFromFirebase(keyword:String){

        chatRoomList.clear()
        recyclerViewAdapter.startSkeletalLoading(6,UniversalAdapter.SKELETAL_TYPE_4)
        swipeLayout.isRefreshing = true


        User.getUserListFromFirebase { success, message, userList ->
            ChatRoom.getChatRoomListByUserIdWithoutMessages(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)!!,callback = { innersuccess, message, currentUserChatRoomList ->
                GlobalScope.launch {
                    if (success && innersuccess) {
                        recyclerViewAdapter.stopSkeletalLoading()
                        swipeLayout.isRefreshing = false


                        for (user in userList!!) {
                            if (user.userId != FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@NewChatActivity) && user.getFullName().toLowerCase().contains(keyword.toLowerCase())) {
                                var user1 =
                                    User.suspendGetSpecificUserFromFirebase(FirebaseAuth.getInstance().uid!!) //currentlogged in user
                                var user2 = user!!

                                var id = "-1"
                                var lastMessage = ""
                                var lastMessageSendBy = ""
                                var messageList = ArrayList<ChatMessage>()
                                for (room in currentUserChatRoomList!!) {
                                    if (room.chatUser1.userId == user.userId || room.chatUser2.userId == user.userId) {
                                        id = room.id
                                        lastMessage = room.lastMessage
                                        lastMessageSendBy = room.lastMessageSendBy
                                        messageList.addAll(room.messagesList)
                                    }
                                }

                                var chatRoom = ChatRoom(id, user1!!, user2!!, messageList,lastMessage,lastMessageSendBy)
                                chatRoomList.add(chatRoom)
                            }
                        }

                        runOnUiThread {
                            recyclerViewAdapter.notifyDataSetChanged()
                        }

                    } else {
                        runOnUiThread {
                            recyclerViewAdapter.stopSkeletalLoading()
                            swipeLayout.isRefreshing = false

                            var errorDialog = ErrorDialog(
                                this@NewChatActivity,
                                "Oops",
                                "Sorry, We have encountered some error when connecting with Firebase."
                            )
                            errorDialog.show()
                        }
                    }
                }
            })
        }
    }

    fun setupUI(){
        tvTitle.text = "New Chat"
        btnNewChat.visibility =  View.GONE
        btnMenu.setImageResource(R.drawable.back_icon)

        btnSearch.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.transparent),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            100f,0)

        tvSearch.setOnEditorActionListener { v, actionId, event ->
            var handled=false
            if(actionId == EditorInfo.IME_ACTION_SEND){
                handled = true
                btnSearch.performClick()
            }
            handled
        }
    }

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            btnMenu,
            btnSearch
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }

    private fun setupRecyclerView(){
        recyclerViewAdapter = object: UniversalAdapter(chatRoomList,this@NewChatActivity,chatRoomRecyclerView){
            override fun getVerticalSpacing(): Int {
                //5px spacing
                return 5
            }
            override fun onItemClickedListener(data: Any, clickType:Int) {
                if(data is ChatRoom){
                    var intent = Intent(this@NewChatActivity,ChatRoomActivity::class.java)
                    intent.putExtra("chatRoom",data)
                    startActivity(intent)
                }
            }
        }
        chatRoomRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRoomRecyclerView.adapter = recyclerViewAdapter
    }
}