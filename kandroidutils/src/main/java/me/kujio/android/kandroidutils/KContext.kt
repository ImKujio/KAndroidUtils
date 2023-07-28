package me.kujio.android.kandroidutils

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager

val Context.primaryColor: Int
    get() = try {
        TypedValue().apply {
            theme.resolveAttribute(R.attr.colorPrimary, this, true)
        }.data
    } catch (_: Exception) {
        Color.BLACK
    }
val Context.secondaryColor: Int
    @SuppressLint("InlinedApi") get() = try {
        TypedValue().apply {
            theme.resolveAttribute(R.attr.colorSecondary, this, true)
        }.data
    } catch (_:Exception){
        Color.GRAY
    }
val Context.accentColor: Int
    get() = try {
        TypedValue().apply {
            theme.resolveAttribute(R.attr.colorAccent, this, true)
        }.data
    } catch (_:Exception){
        Color.RED
    }

fun Context.hideKeyboard() {
    if (this !is Activity) return
    val inputMethodManager = getSystemService(InputMethodManager::class.java) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(window.decorView.windowToken, 0)
}

fun Context.clearFocus() {
    if (this !is Activity) return
    window.decorView.findViewById<ViewGroup>(R.id.content).clearFocus()
}