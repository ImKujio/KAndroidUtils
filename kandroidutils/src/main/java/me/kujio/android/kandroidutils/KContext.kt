package me.kujio.android.kandroidutils

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions

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

fun Context.checkPermission(
    permissions: String,
    callback: ((never: Boolean) -> Unit)? = null
): Boolean {
    if (this !is Activity) throw Exception("context is not activity")
    if (permissions.isEmpty()) return true
    if (XXPermissions.isGranted(this, permissions)) return true
    XXPermissions.with(this)
        .permission(permissions)
        .request(object : OnPermissionCallback {
            override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
            }

            override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                callback?.invoke(never)
            }
        })
    return false
}


fun Context.startPermissionActivity(context: Context, permissions: String? = null) {
    if (permissions == null) {
        XXPermissions.startPermissionActivity(context)
    } else {
        XXPermissions.startPermissionActivity(context, permissions)
    }
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