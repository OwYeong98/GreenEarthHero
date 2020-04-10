package com.oymj.greenearthhero.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.ui.activity.MainActivity
import com.oymj.greenearthhero.utils.FirebaseUtil

class FirebaseCloudMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)


        //update token to firebase database
        var currentLoggedInUserId = FirebaseAuth.getInstance().currentUser?.uid
        if(currentLoggedInUserId != null){
            var db = FirebaseFirestore.getInstance()
            db.collection("Users").document(currentLoggedInUserId).update(mapOf(
                "cloudMessagingId" to token
            ))
        }

    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if (message.notification != null) {

            if (message.data.size > 0) {
                val data: Map<String, String> = message.data

                //message with custom data
                handleNow(data)

            }else{
                //normal message
                sendNotification(message.notification!!.title!!,message.notification!!.body!!)
            }
        }
    }


    fun handleNow(data: Map<String, String>) {
        if (data.containsKey("title") && data.containsKey("message")) {
            sendNotification(data.get("title")!!, data.get("message")!!)
        }
    }

    fun sendNotification(title: String, messsageBody: String) {
        var intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        var pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        var channelId = "1"
        var defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        var notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_notification)
            .setContentTitle(title)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(messsageBody))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        var notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel = NotificationChannel(
                channelId,
                "Cloud Messaging Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notification.build())
    }
}