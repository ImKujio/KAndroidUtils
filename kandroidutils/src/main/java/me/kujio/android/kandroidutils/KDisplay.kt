package me.kujio.android.kandroidutils

import android.R
import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.util.TypedValue
import android.view.WindowManager


val Int.dp: Int; get() = (KDisPlay.dpUnit * this).toInt()

val Int.sp: Int; get() = (KDisPlay.spUnit * this).toInt()

val Float.dp: Float; get() = KDisPlay.dpUnit * this

val Float.sp: Float; get() = KDisPlay.spUnit * this


object KDisPlay {
    var width = 0; private set
    var height = 0; private set
    var statusBar = 0; private set
    var dpUnit = 0f; private set
    var spUnit = 0f; private set
    var animationDuration = 0L; private set
    fun init(ctx: Context) {
        val dm = ctx.resources.displayMetrics
        val size = Point(dm.widthPixels,dm.heightPixels)
        ctx.getSystemService(Context.WINDOW_SERVICE)?.let {
            if (it is WindowManager){
                it.defaultDisplay?.getRealSize(size)
            }
        }
        width = size.x
        height = size.y
        loge { "width:$width height:$height" }
        dpUnit = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, dm)
        spUnit = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 1f, dm)
        animationDuration = ctx.resources.getInteger(R.integer.config_mediumAnimTime).toLong()
        statusBar = try {
            val id = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android")
            if (id <= 0) throw Exception()
            val s1 = ctx.resources.getDimensionPixelSize(id)
            val s2 = Resources.getSystem().getDimensionPixelSize(id)
            if (s2 >= s1 && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) s2
            else {
                val d1 = ctx.resources.displayMetrics.density
                val d2 = Resources.getSystem().displayMetrics.density
                val f = s1 * d2 / d1
                if (f >= 0) (f + 0.5f).toInt() else (f - 0.5f).toInt()
            }
        } catch (_:Exception){
            0
        }
    }
}