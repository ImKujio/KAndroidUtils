package me.kujio.android.kandroidutils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import java.io.File

object DataStore {
    lateinit var assets: AssetManager; private set
    lateinit var kvStore: SharedPreferences; private set
    lateinit var filesDir: File; private set
    lateinit var cacheDir: File; private set
    fun init(ctx: Context) {
        assets = ctx.assets
        kvStore = ctx.getSharedPreferences("kvData",Context.MODE_PRIVATE)
        filesDir = ctx.filesDir
        cacheDir = ctx.cacheDir
    }
}