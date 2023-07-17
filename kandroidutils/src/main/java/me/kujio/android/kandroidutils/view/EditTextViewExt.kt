package me.kujio.android.kandroidutils.view

import android.app.Activity
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

fun EditText.showKeyboard() {
    isFocusable = true
    isFocusableInTouchMode = true
    requestFocus()
    if (context !is Activity) return
    val imm = context.getSystemService(InputMethodManager::class.java) as InputMethodManager
    imm.showSoftInput(this, 0)
}