package me.kujio.android.kandroidutils

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.hjq.permissions.XXPermissions

val Context.primaryColor: Int
    get() = tryRun {
        TypedValue().apply {
            theme.resolveAttribute(R.attr.colorPrimary, this, true)
        }.data
    } ?: Color.BLACK
val Context.secondaryColor: Int
    @SuppressLint("InlinedApi") get() = tryRun {
        TypedValue().apply {
            theme.resolveAttribute(R.attr.colorSecondary, this, true)
        }.data
    } ?: Color.GRAY
val Context.accentColor: Int
    get() = tryRun {
        TypedValue().apply {
            theme.resolveAttribute(R.attr.colorAccent, this, true)
        }.data
    } ?: Color.RED


fun Context.checkPermission(vararg permissions: Pair<String, String>): Boolean {
    return XXPermissions.isGranted(this, permissions.map { it.second })
}

fun Context.hideKeyboard() {
    if (this !is Activity) return
    val inputMethodManager = getSystemService(InputMethodManager::class.java) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(window.decorView.windowToken, 0)
}

fun Context.clearFocus() {
    if (this !is Activity) return
    window.decorView.findViewById<ViewGroup>(android.R.id.content).clearFocus()
}