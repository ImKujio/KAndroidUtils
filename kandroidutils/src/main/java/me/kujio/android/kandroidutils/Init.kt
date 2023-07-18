package me.kujio.android.kandroidutils

import android.content.Context
import me.kujio.android.kandroidutils.crash.CrashCatcher


/**
 * manifests add:
 * ```
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
 * ```
 * build.gradle(Module) add:
 * ```
 * plugins {
 *     ...
 *     id 'org.jetbrains.kotlin.kapt'
 * }
 * android {
 *     ...
 *     dataBinding {
 *         enabled = true
 *     }
 * }
 * ```
 */

object Init {
    fun init(ctx: Context) {
        KDevice.init(ctx)
        KApp.init(ctx)
        CrashCatcher.init(ctx)
        DisPlay.init(ctx)
        KStore.init(ctx)
    }
}



