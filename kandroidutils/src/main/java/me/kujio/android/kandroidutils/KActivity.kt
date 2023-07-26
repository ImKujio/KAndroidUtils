package me.kujio.android.kandroidutils

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get

enum class ThemeType { LIGHT, DARK }

fun Activity.applyImmersive(type: ThemeType) {
    actionBar?.apply { hide() }
    if (this is AppCompatActivity) supportActionBar?.apply { hide() }
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = Color.TRANSPARENT
    window.decorView.systemUiVisibility = when (type) {
        ThemeType.LIGHT -> View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        ThemeType.DARK -> View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
}

fun Activity.applyTitleBar(
    height: Int = KTitleBar.defBarHeight,
    backgroundColor: Int = KTitleBar.defBackgroundColor,
    background: Drawable? = ColorDrawable(backgroundColor),
    fitBackground: Boolean = true,
    fitStatusBar: Boolean = true,
    elevation: Int = 1.dp,
    exec: KTitleBar.() -> Unit
) {
    val rootView = window.decorView.findViewById(android.R.id.content) as FrameLayout
    val titleBarView = FrameLayout(this)
    val sumHeight = height + if (fitStatusBar) KDisPlay.statusBar else 0
    titleBarView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, sumHeight)
    titleBarView.setPadding(
        8.dp, if (fitStatusBar) KDisPlay.statusBar else 0, 8.dp, 0
    )
    titleBarView.background = background
    titleBarView.elevation = elevation.toFloat()
    rootView.addView(titleBarView, 0)
    val bodyView = rootView[1]
    if (fitBackground) {
        rootView.background = bodyView.background
        bodyView.background = null
    }
    (bodyView.layoutParams as FrameLayout.LayoutParams).topMargin += sumHeight
    KTitleBar(this, titleBarView).exec()
}