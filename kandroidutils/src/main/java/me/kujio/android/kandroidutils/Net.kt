package me.kujio.android.kandroidutils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object Net {
    private lateinit var cm: ConnectivityManager
    val hasNet: Boolean
        get() = cm.getNetworkCapabilities(cm.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            ?: true
    val hasWifi: Boolean
        get() = cm.getNetworkCapabilities(cm.activeNetwork)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: true
    val hasMobile: Boolean
        get() = cm.getNetworkCapabilities(cm.activeNetwork)?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            ?: true

    fun init(ctx: Context) {
        cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
}