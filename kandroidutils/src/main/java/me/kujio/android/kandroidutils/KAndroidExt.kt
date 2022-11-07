package me.kujio.android.kandroidutils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.R
import androidx.core.view.get
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


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

object KAndroidExt {
    fun init(ctx: Context) {
        Device.init(ctx)
        App.init(ctx)
        CrashCatcher.init(ctx)
        DisPlay.init(ctx)
        Net.init(ctx)
    }
}



