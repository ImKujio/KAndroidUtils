package me.kujio.android.kandroidutils

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.R
import androidx.core.view.get





class KTitleBar(
    private val ctx: Context,
    private val titleBarView: FrameLayout,
) {

    fun title(
        title: String, gravity: Int = Gravity.CENTER, color: Int = defTitleColor, size: Int = defTitleSize
    ) {
        val titleView = TextView(ctx)
        titleView.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.MATCH_PARENT, gravity
        )
        titleView.setPadding(8.dp, 0, 8.dp, 0)
        titleView.text = title
        titleView.gravity = Gravity.CENTER
        titleView.setTextColor(color)
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.toFloat())
        titleView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        titleBarView.addView(titleView)
    }

    fun txtBtn(
        txt: String,
        color: Int = if (defBtnColor == -1) ctx.primaryColor else defBackColor,
        size: Int = defTxtBtnSize,
        typeface: Typeface = Typeface.DEFAULT_BOLD,
        gravity: Int = Gravity.END,
        onTap: View.OnClickListener
    ) {
        val txtBtn = TextView(ctx)
        txtBtn.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.MATCH_PARENT, gravity
        )
        txtBtn.text = txt
        txtBtn.gravity = Gravity.CENTER
        txtBtn.setPadding(8.dp, 0, 8.dp, 0)
        txtBtn.typeface = typeface
        txtBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.toFloat())
        txtBtn.setTextColor(color)
        txtBtn.setOnClickListener(onTap)
        clickBackground(txtBtn)
        titleBarView.addView(txtBtn)
    }

    fun imgBtn(
        res: Int, color: Int, gravity: Int, onTap: View.OnClickListener
    ) {
        val imgBtn = ImageView(ctx)
        imgBtn.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.MATCH_PARENT, gravity
        )
        imgBtn.setImageResource(res)
        imgBtn.imageTintList = ColorStateList.valueOf(color)
        imgBtn.setOnClickListener(onTap)
        imgBtn.setPadding(8.dp, 8.dp, 8.dp, 8.dp)
        clickBackground(imgBtn)
        titleBarView.addView(imgBtn)
    }

    fun backBtn(
        res: Int = defBackResId, color: Int = defBackColor, onTap: View.OnClickListener = View.OnClickListener {
            if (ctx is Activity) ctx.onBackPressed()
        }
    ) {
        imgBtn(res, color, Gravity.START, onTap)
    }

    private fun clickBackground(view: View) {
        val attrs = intArrayOf(R.attr.selectableItemBackgroundBorderless)
        val typedArray: TypedArray = ctx.obtainStyledAttributes(attrs)
        val backgroundResource = typedArray.getResourceId(0, 0)
        view.setBackgroundResource(backgroundResource)
        typedArray.recycle()
    }


    companion object {
        var defBarHeight = 45.dp
        var defBackgroundColor = Color.WHITE
        var defTitleSize = 16.sp
        var defTitleColor = Color.parseColor("#333333")
        var defBackResId = R.drawable.abc_ic_ab_back_material
        var defBackColor = Color.parseColor("#333333")
        var defTxtBtnSize = 14.sp
        var defBtnColor = -1

        fun apply(
            frameLayout: FrameLayout,
            height: Int = defBarHeight,
            backgroundColor: Int = defBackgroundColor,
            background: Drawable? = ColorDrawable(backgroundColor),
            fitBackground: Boolean = true,
            fitStatusBar: Boolean = true,
            elevation: Int = 1.dp,
            exec: KTitleBar.() -> Unit
        ) {
            val rootView = frameLayout.parent
            if (rootView !is FrameLayout) return
            val sumHeight = height + if (fitStatusBar) KDisPlay.statusBar else 0
            frameLayout.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, sumHeight)
            frameLayout.setPadding(
                8.dp, if (fitStatusBar) KDisPlay.statusBar else 0, 8.dp, 0
            )
            frameLayout.background = background
            frameLayout.elevation = elevation.toFloat()
            val bodyView = rootView[1]
            if (fitBackground) {
                rootView.background = bodyView.background
                bodyView.background = null
            }
            (bodyView.layoutParams as FrameLayout.LayoutParams).topMargin += sumHeight
            KTitleBar(rootView.context, frameLayout).exec()
        }

    }

}
