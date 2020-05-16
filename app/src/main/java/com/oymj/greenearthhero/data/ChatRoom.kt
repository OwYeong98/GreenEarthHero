package com.oymj.greenearthhero.data

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList


class ChatRoom(
    var id: String,
    var chatUser1:User,
    var chatUser2:User,
    var messagesList:ArrayList<ChatMessage>,
    var lastMessage: String,
    var lastMessageSendBy: String
):Serializable {

    companion object{

        fun getChatRoomListByUserIdWithoutMessages(userId:String,callback:(Boolean,String?,ArrayList<ChatRoom>?)->Unit){
            var chatRoomList = ArrayList<ChatRoom>()
            FirebaseFirestore.getInstance().collection("Chat_Room").whereArrayContains("chatUsers",userId).get()
                .addOnSuccessListener {
                    chatRoomSnapshot->
                    GlobalScope.launch {
                        for(chatRoom in chatRoomSnapshot!!){
                            var id = chatRoom.id
                            var lastMessage = chatRoom.getString("lastMessage")?:""
                            var lastMessageSendBy = chatRoom.getString("lastMessageSendBy")?:""
                            var  userList = ArrayList<String>()
                            userList = chatRoom.get("chatUsers") as ArrayList<String>

                            var user1 = User.suspendGetSpecificUserFromFirebase(userList.get(0))
                            var user2 = User.suspendGetSpecificUserFromFirebase(userList.get(1))

                            var messagesList = ArrayList<ChatMessage>()
                            var newChatRoom = ChatRoom(id,user1!!,user2!!,messagesList,lastMessage!!,lastMessageSendBy!!)

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

        fun getSpecificChatRoomProvidingTwoUser(user1Id:String, user2Id:String,callback:(Boolean,String?,ChatRoom?)->Unit){
            FirebaseFirestore.getInstance().collection("Chat_Room").whereArrayContains("chatUsers",user1Id).get()
                .addOnSuccessListener {
                        chatRoomSnapshot->
                    FirebaseFirestore.getInstance().collection("Chat_Room").whereArrayContains("chatUsers",user2Id).get()
                        .addOnSuccessListener {
                            innerChatRoomSnapshot->
                            GlobalScope.launch {
                                if(chatRoomSnapshot.size() == 0 && innerChatRoomSnapshot.size() == 0){
                                    callback(true,null,null)
                                }else{
                                    var foundChatRoom:ChatRoom? = null
                                    for(chatRoom in chatRoomSnapshot){
                                        for(innerChatRoom in innerChatRoomSnapshot){
                                            if(chatRoom.id == innerChatRoom.id){

                                                var id = chatRoom.id
                                                var lastMessage = chatRoom.getString("lastMessage")?:""
                                                var lastMessageSendBy = chatRoom.getString("lastMessageSendBy")?:""
                                                var  userList = ArrayList<String>()
                                                userList = chatRoom.get("chatUsers") as ArrayList<String>

                                                var user1 = User.suspendGetSpecificUserFromFirebase(userList.get(0))
                                                var user2 = User.suspendGetSpecificUserFromFirebase(userList.get(1))

                                                var messagesList = ArrayList<ChatMessage>()
                                                foundChatRoom = ChatRoom(id,user1!!,user2!!,messagesList,lastMessage!!,lastMessageSendBy!!)
                                                break
                                            }
                                        }
                                        if(foundChatRoom != null)
                                            break
                                    }


                                    callback(true,null,foundChatRoom)
                                }
                            }
                        }
                        .addOnFailureListener {
                            ex->
                            callback(false,ex.toString(),null)
                        }
                }
                .addOnFailureListener {
                        ex->
                    callback(false,ex.toString(),null)
                }
        }

        fun getSpecificChatRoomById(chatRoomId:String,callback:(Boolean,String?,ChatRoom?)->Unit){
            FirebaseFirestore.getInstance().collection("Chat_Room").document(chatRoomId).get()
                .addOnSuccessListener {
                        chatRoomSnapshot->
                    GlobalScope.launch {
                        if(!chatRoomSnapshot.exists()){
                            callback(true,null,null)
                        }else{
                            var chatRoom = chatRoomSnapshot

                            var id = chatRoom.id
                            var lastMessage = chatRoom.getString("lastMessage")?:""
                            var lastMessageSendBy = chatRoom.getString("lastMessageSendBy")?:""
                            var  userList = ArrayList<String>()
                            userList = chatRoom.get("chatUsers") as ArrayList<String>

                            var user1 = User.suspendGetSpecificUserFromFirebase(userList.get(0))
                            var user2 = User.suspendGetSpecificUserFromFirebase(userList.get(1))

                            var messagesList = ArrayList<ChatMessage>()
                            var newChatRoom = ChatRoom(id,user1!!,user2!!,messagesList,lastMessage!!,lastMessageSendBy!!)

                            callback(true,null,newChatRoom)
                        }
                    }
                }
                .addOnFailureListener {
                        ex->
                    callback(false,ex.toString(),null)
                }
        }
    }
}
