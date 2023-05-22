package com.example.appchat.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViews.RemoteView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.appchat.R
import com.example.appchat.view.ChatActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

const val channelName = "com.example.appchat"
class MyFirebaseMessagingService : FirebaseMessagingService() {



    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("ReceiverFCM", message.notification!!.title!!)
        Log.d("ReceiverFCM", message.notification!!.body!!)
        if (message.notification != null) {
            Log.d("ReceiverFc", "aa")
            getFirebaseMessage(message.notification!!.title!!, message.notification!!.body!!)
        }

        super.onMessageReceived(message)
    }

    private fun getFirebaseMessage(title: String, message: String) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val CHANNEL_ID = "MESSAGE"
        var builder : NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_chat)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setContentTitle(title)
            .setContentText(message)
//        builder = builder.setContent(getRemoteView(title, message))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(notificationChannel)
        builder.setChannelId(CHANNEL_ID)
        notificationManager.notify(0, builder.build())

    }
    @SuppressLint("RemoteViewLayout")
    private fun getRemoteView(title: String, message: String): RemoteViews? {
        val remoteView =  RemoteViews(channelName, R.layout.notification)
        remoteView.setTextViewText(R.id.notificationTilte, title)
        remoteView.setTextViewText(R.id.notificationContent, message)
        remoteView.setImageViewResource(R.id.imgNotification, R.drawable.icon_chat)
        return remoteView
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}