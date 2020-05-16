package com.oymj.greenearthhero.data

import android.content.Context
import android.content.Intent
import com.google.firebase.firestore.FirebaseFirestore
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.ui.activity.ChatRoomActivity
import com.oymj.greenearthhero.ui.activity.CurrentPostDetailActivity
import com.oymj.greenearthhero.ui.activity.CurrentPurchaseDetailActivity
import com.oymj.greenearthhero.ui.activity.MyRequestAndRequestHistoryActivity
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import java.text.SimpleDateFormat
import java.util.*

class Notification(
    var id:String,
    var date: Date,
    var userId:String,
    var title:String,
    var relatedId:String,
    var type:String,
    var isRead:Boolean
    ) {

    companion object{
        fun getNotificationListOfUser(userId:String, callback: (Boolean,String?,ArrayList<Notification>?)-> Unit){
            FirebaseFirestore.getInstance().collection("Notification").whereEqualTo("userId",userId).get()
                .addOnSuccessListener {
                        notificationSnapshot->

                    var notificationList = ArrayList<Notification>()

                    for(notification in notificationSnapshot){
                        var id = notification.id
                        var date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(notification.getString("date"))!!
                        var title = notification.getString("title")!!
                        var type = notification.getString("type")!!
                        var userId = notification.getString("userId")!!
                        var relatedId = notification.getString("relatedID")!!
                        var isRead = notification.getBoolean("isRead")!!

                        var newNotification = Notification(id,date,userId,title,relatedId,type,isRead)

                        notificationList.add(newNotification)
                    }

                    callback(true,null,notificationList)

                }
                .addOnFailureListener {
                        ex->
                    callback(true,ex.toString(),null)
                }
        }

        fun getNumOfNotificationOfUser(userId:String, callback: (Boolean,String?,Int?)-> Unit){
            FirebaseFirestore.getInstance().collection("Notification").whereEqualTo("userId",userId).whereEqualTo("isRead",false).get()
                .addOnSuccessListener {
                        notificationSnapshot->

                    callback(true,null,notificationSnapshot.size())
                }
                .addOnFailureListener {
                        ex->
                    callback(true,ex.toString(),null)
                }
        }
    }

    fun redirectToPage(context:Context){
        when(type){
            "Recycle_Request"->{
                var intent = Intent(context,MyRequestAndRequestHistoryActivity::class.java)
                context.startActivity(intent)
            }
            "Recycle_Request_History"->{
                var intent = Intent(context,MyRequestAndRequestHistoryActivity::class.java)
                context.startActivity(intent)
            }
            "Chat_Room"->{
                var loadingDialog = LoadingDialog(context)
                loadingDialog.show()

                ChatRoom.getSpecificChatRoomById(relatedId){
                    success,message,chatRoom->

                    loadingDialog.dismiss()
                    if(success){
                        var intent = Intent(context,ChatRoomActivity::class.java)
                        intent.putExtra("chatRoom",chatRoom!!)
                        context.startActivity(intent)
                    }else{
                        var errorDialog=ErrorDialog(context,"Some Error Occured","We are encountering error when getting chat room!")
                        errorDialog.show()
                    }
                }
            }
            "Item_Purchase"->{
                var intent = Intent(context,CurrentPurchaseDetailActivity::class.java)
                intent.putExtra("itemId",relatedId)
                context.startActivity(intent)
            }
            "Item_Sale"->{
                var intent = Intent(context,CurrentPostDetailActivity::class.java)
                intent.putExtra("itemId",relatedId)
                context.startActivity(intent)
            }
        }
    }

    fun getNotificationIconImageResourceId():Int{
        var imageResourceId= R.drawable.ic_notification_chat
        when(type){
            "Recycle_Request"->{
                imageResourceId= R.drawable.ic_notification_recycle
            }
            "Recycle_Request_History"->{
                imageResourceId= R.drawable.ic_notification_recycle
            }
            "Chat_Room"->{
                imageResourceId= R.drawable.ic_notification_chat
            }
            "Item_Purchase"->{
                imageResourceId= R.drawable.ic_notification_second_hand_platform
            }
            "Item_Sale"->{
                imageResourceId= R.drawable.ic_notification_second_hand_platform
            }
        }

        return imageResourceId
    }
}