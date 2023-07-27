package me.kujio.android.kandroidutils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import java.lang.ref.SoftReference
import java.util.*

object KApp {
    var versionCode = 0; private set
    var versionName = ""; private set
    var packageName = ""; private set
    var debug = false; private set

    private var _application: SoftReference<Application>? = null
    val application: Application? get() = _application?.get()

    private var _activity: SoftReference<Activity>? = null
    val curActivity: Activity? get() = _activity?.get()

    private var _onCreatedHook = WeakHashMap<Activity, HashMap<String, () -> Unit>>()

    private var _onStoppedHook = WeakHashMap<Activity, HashMap<String, () -> Unit>>()

    fun Activity.onCreated(tag: String, action: () -> Unit) {
        if (_onCreatedHook[this] == null) _onCreatedHook[this] = HashMap()
        _onCreatedHook[this]?.put("tag", action)
    }

    fun Activity.onStopped(tag: String, action: () -> Unit) {
        if (_onStoppedHook[this] == null) _onStoppedHook[this] = HashMap()
        _onStoppedHook[this]?.put("tag", action)
    }

    fun Activity.removeHook(tag:String){
        _onCreatedHook[this]?.run {
            remove(tag)
        }
        _onStoppedHook[this]?.run {
            remove(tag)
        }
    }

    fun init(ctx: Context) {
        packageName = ctx.packageName
        versionCode = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionCode
        versionName = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
        debug = try {
            ctx.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        } catch (_: Exception) {
            false
        }
        (ctx as Application).registerActivityLifecycleCallbacks(lifecycleCallbacks)
    }


    private val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            _application = SoftReference(activity.application)
            _activity = SoftReference(activity)
            _onCreatedHook[activity]?.let { it.forEach{ (_, action) -> runCatching { action() }} }
            _onCreatedHook.remove(activity)
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
            _application = SoftReference(activity.application)
            _activity = SoftReference(activity)
        }

        override fun onActivityPaused(activity: Activity) {

        }

        override fun onActivityStopped(activity: Activity) {
            _onStoppedHook[activity]?.let { it.forEach{ (_, action) -> runCatching { action() }} }
            _onStoppedHook.remove(activity)
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

        }

        override fun onActivityDestroyed(activity: Activity) {
            _onCreatedHook.remove(activity)
            if (activity == curActivity) {
                _activity = null
            }
        }

    }

}