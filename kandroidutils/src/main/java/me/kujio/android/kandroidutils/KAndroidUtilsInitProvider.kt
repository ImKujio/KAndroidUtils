package me.kujio.android.kandroidutils

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import java.lang.NullPointerException

class KAndroidUtilsInitProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        val context = context?.applicationContext ?: getContextByReflect()
        KAndroidExt.init(context)
        return true
    }

    @SuppressLint("PrivateApi")
    private fun getContextByReflect(): Context {
        return tryRun {
            val activityThread = Class.forName("android.app.ActivityThread")
            val thread = activityThread.getMethod("currentActivityThread").invoke(null)
            val app = activityThread.getMethod("getApplication").invoke(thread)
            app?.let { it as Application } ?: run {
                throw NullPointerException("无法获取Application")
            }
        } ?: run {
            throw NullPointerException("无法获取Application")
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }


    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return 0
    }
}