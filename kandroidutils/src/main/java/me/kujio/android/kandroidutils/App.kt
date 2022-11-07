package me.kujio.android.kandroidutils

import android.content.Context
import android.content.pm.ApplicationInfo

object App {
    var versionCode = 0; private set
    var versionName = ""; private set
    var packageName = ""; private set
    var debug = false; private set
    fun init(ctx: Context) {
        packageName = ctx.packageName
        versionCode = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionCode
        versionName = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
        debug = tryRun {
            ctx.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        } ?: false
    }
}