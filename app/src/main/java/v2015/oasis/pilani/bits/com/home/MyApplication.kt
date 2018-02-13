package v2015.oasis.pilani.bits.com.home

import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import com.androidnetworking.AndroidNetworking


class MyApplication : MultiDexApplication(){
    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        AndroidNetworking.initialize(applicationContext)
        GlobalData.tinyDb = TinyDB(applicationContext)
    }
}

