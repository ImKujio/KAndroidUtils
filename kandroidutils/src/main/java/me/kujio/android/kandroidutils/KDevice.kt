package me.kujio.android.kandroidutils

import android.content.Context
import android.os.Build
import android.provider.Settings

object KDevice {
    var androidId = ""; private set
    var androidVer = 0; private set
    var androidVerName = ""; private set
    var name = ""; private set
    var model = ""; private set
    var brand = ""; private set
    var cpuAbi = ""; private set
    fun init(ctx: Context) {
        androidId = Settings.System.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID)
        name = Build.PRODUCT
        androidVer = Build.VERSION.SDK_INT
        androidVerName = Build.VERSION.RELEASE
        model = Build.MODEL
        brand = Build.BOARD
        cpuAbi = Build.SUPPORTED_ABIS.joinToString()
    }
}