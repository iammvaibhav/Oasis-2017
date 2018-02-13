package v2015.oasis.pilani.bits.com.home

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.reflect.TypeToken
import v2015.oasis.pilani.bits.com.home.navbar_items.NotificationData
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService(){

    override fun onMessageReceived(message: RemoteMessage) {

        val title = message.data["title"] ?: ""
        val body = message.data["body"] ?: ""

        val date = Date(message.sentTime)
        val receive = "Sent on ${SimpleDateFormat("dd MMM").format(date)} at ${SimpleDateFormat("HH:mm").format(date)}"

        val tinyDb = TinyDB(applicationContext)
        if (tinyDb.getBoolean("enableNotifications")) {
            sendNotification(title, body)
        }
        val notificationsDataType: Type = object : TypeToken<ArrayList<NotificationData>>(){}.type
        var notificationsData = ArrayList<NotificationData>()
        try {
            notificationsData = tinyDb.getObject<ArrayList<NotificationData>>("notificationsData", notificationsDataType)
        }catch (e: NullPointerException){
            e.printStackTrace()
        }

        notificationsData.add(NotificationData(title, body, receive, date))
        Collections.sort(notificationsData){ o1, o2 -> o2.date.compareTo(o1.date) }
        tinyDb.putObject("notificationsData", notificationsData)
    }

    private fun sendNotification(title: String, body: String){
        val notificationBuilder = NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.notif)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(body))

        val intent = Intent(this, SplashScreen::class.java)
        intent.putExtra("calledBy", "notification")
        val id = Integer.parseInt(System.currentTimeMillis().toString().reversed().substring(0, 9))
        intent.action = "$id"
        val activity = PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_ONE_SHOT)
        notificationBuilder.setContentIntent(activity)

        val notification = notificationBuilder.build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, notification)
    }
}