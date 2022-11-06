package me.kujio.android.kandroidutils.simple

import android.app.Application
import me.kujio.android.kandroidutils.initAppInfo

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        initAppInfo()
    }
}