package com.oymj.greenearthhero.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import javax.security.auth.callback.Callback

class ChatMessage(
    var id: String,
    var dateSent: Date,
    var userSend:String,
    var message:String
    ):Serializable {

    companion object{

        fun getChatList(roomId:String, callback: (Boolean,String?,ArrayList<ChatMessage>?)-> Unit){
            var chatList = FirebaseFirestore.getInstance().collection("Chat_Room/$roomId/MessagesList").get()
                .addOnSuccessListener {
                    chatSnapshot->

                    var messageList = ArrayList<ChatMessage>()

                    for(chat in chatSnapshot){
                        var id = chat.id
                        var dateSent = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(chat.getString("dateSent"))
                        var message = chat.getString("message")
                        var userSend = chat.getString("userSend")

                        var newMessage = ChatMessage(id,dateSent,userSend!!,message!!)

                        messageList.add(newMessage)
                    }

                    callback(true,null,messageList)

                }
                .addOnFailureListener {
                    ex->
                    callback(true,ex.toString(),null)
                }


        }

        suspend fun suspendGetChatList(roomId:String): ArrayList<ChatMessage> {
            Log.d("asd","getting messageList")
            var chatList = FirebaseFirestore.getInstance().collection("Chat_Room/$roomId/MessagesList").get().await()
            Log.d("asd","done get messageList")
            var messageList = ArrayList<ChatMessage>()
            for(chat in chatList){
                var id = chat.id
                var dateSent = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(chat.getString("dateSent"))
                var message = chat.getString("message")
                var userSend = chat.getString("userSend")

                var newMessage = ChatMessage(id,dateSent,userSend!!,message!!)

                messageList.add(newMessage)
            }

            return messageList
        }
    }
}