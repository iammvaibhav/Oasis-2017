package v2015.oasis.pilani.bits.com.home.events.inner

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.SystemClock
import android.support.v4.app.NotificationCompat
import android.view.View
import android.view.ViewGroup
import com.ramotion.garlandview.inner.InnerItem
import com.squareup.picasso.Picasso
import v2015.oasis.pilani.bits.com.home.GlobalData
import v2015.oasis.pilani.bits.com.home.GlobalData.tinyDb
import v2015.oasis.pilani.bits.com.home.GlobalData.updateListsWithFavourites
import v2015.oasis.pilani.bits.com.home.Pair
import v2015.oasis.pilani.bits.com.home.R
import v2015.oasis.pilani.bits.com.home.SplashScreen
import v2015.oasis.pilani.bits.com.home.databinding.InnerItemBinding
import v2015.oasis.pilani.bits.com.home.events.NotificationPublisher
import v2015.oasis.pilani.bits.com.home.events.onItemClickListener
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class InnerItem(val binding: InnerItemBinding, val listener: onItemClickListener) : InnerItem(binding.root){

    val mInnerLayout: View = (itemView as ViewGroup).getChildAt(0)
    var bold: Typeface? = null
    var medium: Typeface? = null

    override fun getInnerLayout() = mInnerLayout

    fun clearContent(){
        Picasso.with(binding.categoryIcon.context).cancelRequest(binding.categoryIcon)
        binding.categoryIcon.setImageDrawable(null)
    }

    fun setContent(innerData: InnerData, color: Int, type: Int){
        binding.innerData = innerData
        binding.color = color
        binding.extraDetail.text = if(type == 1) innerData.category else innerData.date

        if (bold == null) {
            bold = Typeface.createFromAsset(binding.categoryIcon.context.assets, "fonts/bold.otf")
            binding.name.typeface = bold
        }
        else
            binding.name.typeface = bold

        if (medium == null){
            medium = Typeface.createFromAsset(binding.categoryIcon.context.assets, "fonts/medium.otf")
            binding.venue.typeface = medium
            binding.extraDetail.typeface = medium
            binding.time.typeface = medium
        }else{
            binding.venue.typeface = medium
            binding.extraDetail.typeface = medium
            binding.time.typeface = medium
        }

        mInnerLayout.setOnClickListener {
            listener.onItemClick(innerData)
        }

        val key = "${innerData.name}|${innerData.date}|${innerData.venue}"
        binding.favourite.isChecked = GlobalData.favourites.contains(key)

        binding.favourite.setOnCheckStateChangeListener { _, checked ->

            val id = Integer.parseInt(System.currentTimeMillis().toString().reversed().substring(0, 9))
            //TODO("Create Channel id for android O")
            if (checked){
                GlobalData.favourites.put(key, id)
                tinyDb.putObject("favourites", GlobalData.favourites)
                //Schedule A notification
                scheduleNotification(binding.categoryIcon.context, innerData, id)
                updateListsWithFavourites()
            }
            else {
                //Cancel a notification
                cancelNotification(binding.categoryIcon.context, innerData, GlobalData.favourites[key] ?: 0)
                GlobalData.favourites.remove(key)
                tinyDb.putObject("favourites", GlobalData.favourites)
                updateListsWithFavourites()
            }
        }

        Picasso.with(binding.categoryIcon.context)
                .load(innerData.categoryIcon)
                .into(binding.categoryIcon)

        binding.executePendingBindings()
    }

    private fun scheduleNotification(context: Context, innerData: InnerData, id: Int){
        val data = getPendingIntentAndTime(context, innerData, id)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, data.second, data.first)
    }

    private fun cancelNotification(context: Context, innerData: InnerData, id: Int){
        val data = getPendingIntentAndTime(context, innerData, id)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(data.first)
    }

    private fun getPendingIntentAndTime(context: Context, innerData: InnerData, id: Int): Pair<PendingIntent, Long>{
        val contentTitle = "Event Reminder"
        val shortContentText = "${innerData.name} is gonna start in ${GlobalData.notficationDelayTime}min from now."
        val longContentText = "${innerData.name} is gonna start in ${GlobalData.notficationDelayTime}min from now."

        val inFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
        var date = Date()
        try {
            date = inFormat.parse("${innerData.date} ${innerData.time}")
        }catch (e: ParseException){
            e.printStackTrace()
        }

        val notificationTime = date.time - GlobalData.notficationDelayTime * 60 * 1000
        val currTime = System.currentTimeMillis()
        val delay = notificationTime - currTime

        //Add Actions
        /*val dismissIntent = Intent(this, PingService::class.java)
        dismissIntent.setAction(CommonConstants.ACTION_DISMISS)
        val piDismiss = PendingIntent.getService(this, 0, dismissIntent, 0)

        val snoozeIntent = Intent(this, PingService::class.java)
        snoozeIntent.setAction(CommonConstants.ACTION_SNOOZE)
        val piSnooze = PendingIntent.getService(this, 0, snoozeIntent, 0)*/

        val notificationBuilder = NotificationCompat.Builder(context)
                .setContentTitle(contentTitle)
                .setContentText(shortContentText)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.notif)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(longContentText))

        val intent = Intent(context, SplashScreen::class.java)
        intent.action = "$id"
        intent.putExtra("innerData", innerData)
        val activity = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_ONE_SHOT)
        notificationBuilder.setContentIntent(activity)

        val notification = notificationBuilder.build()

        val notificationIntent = Intent(context, NotificationPublisher::class.java)
        notificationIntent.action = "$id"
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, id)
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification)
        val pendingIntent = PendingIntent.getBroadcast(context, id, notificationIntent, PendingIntent.FLAG_ONE_SHOT)

        val futureInMillis = SystemClock.elapsedRealtime() + delay

        return Pair(pendingIntent, futureInMillis)
    }
}