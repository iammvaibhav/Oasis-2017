package v2015.oasis.pilani.bits.com.home

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.crashlytics.android.Crashlytics
import com.google.firebase.messaging.FirebaseMessaging
import io.fabric.sdk.android.Fabric
import v2015.oasis.pilani.bits.com.home.GlobalData.favouriteClassType
import v2015.oasis.pilani.bits.com.home.GlobalData.init
import v2015.oasis.pilani.bits.com.home.GlobalData.tinyDb
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Fabric.with(this, Crashlytics())

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        val intentToMain = Intent(this, MainActivity::class.java)
        if (intent.extras != null)
            intentToMain.putExtras(intent.extras)


        val isCacheDataAvailable = tinyDb.getBoolean("isCacheDataAvailable")

        try {
            GlobalData.notificationsData = tinyDb.getObject("notificationsData", GlobalData.notificationsDataType)
        }catch (e: Exception){
            //First Time
        }

        try {
            GlobalData.favourites = tinyDb.getObject("favourites", favouriteClassType)
        }catch (e: Exception){
            //First Time
        }

        try{
            GlobalData.N2OVotingData = tinyDb.getObject("n2oData", GlobalData.N2OVotingDataType)
        }catch (e: Exception){
            //First Time
        }

        if (!isCacheDataAvailable) {
            tinyDb.putInt("timeFormat", 24)
            tinyDb.putBoolean("enableNotifications", true)
            FirebaseMessaging.getInstance().subscribeToTopic("all")
            GlobalData.syncDataAndSaveAndDo(this) { startActivity(intentToMain) }
        }
        else
            Thread{
                if (isOnline())
                    GlobalData.syncDataAndSaveAndDo(this) { startActivity(intentToMain) }
                else{
                    //retrieve datewisedata, categorywisedata and categorycolors
                    GlobalData.dateWiseData = tinyDb.getObject("dateWiseData", GlobalData.eventDataClassType)
                    GlobalData.categoryWiseData = tinyDb.getObject("categoryWiseData", GlobalData.eventDataClassType)
                    GlobalData.categoryColors = tinyDb.getObject("categoryColors", GlobalData.categoryColorsClassType)
                    GlobalData.sponsorsData = tinyDb.getObject("sponsorsData", GlobalData.sponsorsDataType)
                    GlobalData.N2OVotingData = tinyDb.getObject("n2oData", GlobalData.N2OVotingDataType)
                    init()
                    startActivity(intentToMain)
                }
            }.start()
    }

    fun isOnline(): Boolean {
        try {
            val timeoutMs = 1500
            val sock = Socket()
            val sockaddr = InetSocketAddress("8.8.8.8", 53)
            sock.connect(sockaddr, timeoutMs)
            sock.close()

        } catch (e: IOException) {
            return false
        }
        return true
    }
}


