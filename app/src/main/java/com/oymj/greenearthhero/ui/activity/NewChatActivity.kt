package com.oymj.greenearthhero.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.data.ChatMessage
import com.oymj.greenearthhero.data.ChatRoom
import com.oymj.greenearthhero.data.User
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_my_chat.*

class NewChatActivity : AppCompatActivity(){

    private var chatRoomList = ArrayList<Any>()
    private lateinit var recyclerViewAdapter: UniversalAdapter
    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                btnMenu->{
                    var intent = Intent(this@NewChatActivity, MenuActivity::class.java)
                    startActivity(intent)
                }
                btnSearch->{
                    Log.d("asd","pressed")
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

        User.getUserListFromFirebase {
                success, message, userList ->
            if(success){
                recyclerViewAdapter.stopSkeletalLoading()
                swipeLayout.isRefreshing = false

                for(user in userList!!){
                    if(user.userId != FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this) && user.getFullName().toLowerCase().contains(keyword.toLowerCase())){
                        var user1 = FirebaseUtil.currentUserDetail //currentlogged in user
                        var user2  = user

                        var chatRoom = ChatRoom("-1",user1!!,user2!!,ArrayList<ChatMessage>())
                        chatRoomList.add(chatRoom)
                    }
                }

                recyclerViewAdapter.notifyDataSetChanged()

            }else{
                recyclerViewAdapter.stopSkeletalLoading()
                swipeLayout.isRefreshing = false

                var errorDialog = ErrorDialog(this@NewChatActivity,"Oops","Sorry, We have encountered some error when connecting with Firebase.")
                errorDialog.show()

            }
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


                }
            }
        }
        chatRoomRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRoomRecyclerView.adapter = recyclerViewAdapter
    }
}