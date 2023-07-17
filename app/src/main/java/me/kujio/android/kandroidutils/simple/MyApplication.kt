package me.kujio.android.kandroidutils.simple

import android.app.Application
import me.kujio.android.kandroidutils.logd

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        logd{"Application:onCreate"}
    }
}