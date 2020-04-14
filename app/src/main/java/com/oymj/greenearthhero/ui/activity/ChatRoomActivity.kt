package com.oymj.greenearthhero.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.data.ChatMessage
import com.oymj.greenearthhero.data.ChatRoom
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.ui.dialog.SuccessDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_chat_room.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatRoomActivity: AppCompatActivity() {
    private lateinit var chatRoomDetail: ChatRoom

    private var chatList = ArrayList<Any>()
    private lateinit var recyclerViewAdapter: UniversalAdapter
    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                btnBack->{
                    finish()
                }
                btnSend->{
                    if(tvMessageBox.text.toString() != ""){
                        var message = ChatMessage("-1",Date(),FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@ChatRoomActivity)!!,tvMessageBox.text.toString())
                        tvMessageBox.setText("")

                        sendMessageToChatRoom(message,callback = {
                                success->
                            if(!success){
                                var errorDialog = ErrorDialog(this@ChatRoomActivity,"Oops","Sorry, We have encountered some error when connecting with Firebase.")
                                errorDialog.show()
                            }else{
                                chatList.add(0,message)
                                Log.d("asd","update chat")
                                recyclerViewAdapter.notifyDataSetChanged()
                            }
                        })
                    }

                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        setupUI()
        setupRecyclerView()
        linkAllButtonWithOnClickListener()

        chatRoomDetail = intent.getSerializableExtra("chatRoom") as ChatRoom

        //if room havent created then create in database
        if(chatRoomDetail.id == "-1"){
            var loadingDialog = LoadingDialog(this)
            loadingDialog.show()

            createChatRoomInFirebase {
                success->

                if(success){
                    Log.d("asd","haha success")
                    loadingDialog.hide()
                    loadingDialog.dismiss()
                }else{
                    loadingDialog.dismiss()
                    var errorDialog = ErrorDialog(this,"Oops","Sorry, We have encountered some error when connecting with Firebase.")
                    errorDialog.show()
                }
            }
        }else{
            chatList.addAll(chatRoomDetail.messagesList)
            recyclerViewAdapter.notifyDataSetChanged()
        }





    }

    fun setupUI(){
        btnBack.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.transparent),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            100f,0)

        tvMessageBox.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            Color.parseColor("#515151"),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            20f,0)

        btnSend.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.transparent),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            100f,0)
    }

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            btnSend,
            btnBack
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }

    private fun setupRecyclerView(){
        recyclerViewAdapter = object: UniversalAdapter(chatList,this@ChatRoomActivity,chatRecyclerView){
            override fun getVerticalSpacing(): Int {
                //5px spacing
                return 5
            }
            override fun onItemClickedListener(data: Any, clickType:Int) {
                if(data is ChatMessage){


                }
            }
        }
        var layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        chatRecyclerView.layoutManager = layoutManager
        chatRecyclerView.adapter = recyclerViewAdapter
    }

    private fun sendMessageToChatRoom(message: ChatMessage, callback: (Boolean)->Unit){
        //create a firebase document
        val messageDocument = hashMapOf(
            "dateSent" to SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(message.dateSent),
            "message" to message.message,
            "userSend" to message.userSend
        )

        FirebaseFirestore.getInstance().collection("Chat_Room/${chatRoomDetail.id}/MessagesList").add(messageDocument)
            .addOnSuccessListener {
                message.id = it.id
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    private fun createChatRoomInFirebase(callback:(Boolean)->Unit){
        //create a firebase document
        val chatRoomDoc = hashMapOf(
            "chatUsers" to arrayListOf<String>(chatRoomDetail.chatUser1.userId,chatRoomDetail.chatUser2.userId)
        )

        FirebaseFirestore.getInstance().collection("Chat_Room").add(chatRoomDoc)
            .addOnSuccessListener {
                doc->
                chatRoomDetail.id = doc.id
                callback(true)
            }
            .addOnFailureListener {
                    e ->
                callback(false)
            }
    }

}