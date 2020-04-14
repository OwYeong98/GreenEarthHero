package com.oymj.greenearthhero.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList


class ChatRoom(
    var id: String,
    var chatUser1:User,
    var chatUser2:User,
    var messagesList:ArrayList<ChatMessage>
) {

    companion object{

        fun getChatRoomListByUserId(callback:(Boolean,String?,ArrayList<ChatRoom>?)->Unit){
            var chatRoomList = ArrayList<ChatRoom>()
            FirebaseFirestore.getInstance().collection("Chat_Room").get()
                .addOnSuccessListener {
                    chatRoomSnapshot->
                    GlobalScope.launch {
                        for(chatRoom in chatRoomSnapshot!!){
                            var id = chatRoom.id
                            var user1 = User.suspendGetSpecificUserFromFirebase(chatRoom.getString("chatUser1")!!)
                            var user2 = User.suspendGetSpecificUserFromFirebase(chatRoom.getString("chatUser2")!!)

                            var messagesList = ChatMessage.suspendGetChatList(id)
                            var newChatRoom = ChatRoom(id,user1!!,user2!!,messagesList)

                            chatRoomList.add(newChatRoom)
                        }

                        callback(true,null,chatRoomList)
                    }
                }
                .addOnFailureListener {
                    ex->
                    callback(false,ex.toString(),null)
                }
        }
    }
}
