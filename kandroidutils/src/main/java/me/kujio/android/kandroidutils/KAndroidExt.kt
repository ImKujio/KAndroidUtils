package me.kujio.android.kandroidutils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.R
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hjq.permissions.XXPermissions
import java.lang.Exception

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

val Int.dp: Int; get() = (DisPlay.dpUnit * this).toInt()

val Int.sp: Int; get() = (DisPlay.spUnit * this).toInt()

val Float.dp: Float; get() = DisPlay.dpUnit * this

val Float.sp: Float; get() = DisPlay.spUnit * this


object DisPlay {
    var width = 0; private set
    var height = 0; private set
    var statusBar = 0; private set
    var dpUnit = 0f; private set
    var spUnit = 0f; private set
    fun init(ctx: Context) {
        val dm = ctx.resources.displayMetrics
        width = dm.widthPixels
        height = dm.heightPixels
        dpUnit = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, dm)
        spUnit = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 1f, dm)
        statusBar = tryRun {
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
        } ?: 0
    }
}

object Apk {
    var versionCode = 0; private set
    var versionName = ""; private set
    var packageName = ""; private set
    var debug = false; private set
    fun init(ctx: Context) {
        packageName = ctx.packageName
        versionCode = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionCode
        versionName = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
        debug = tryRun {
            ctx.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        } ?: false
    }
}

object Device {
    var id = ""; private set
    var name = ""; private set
    fun init(ctx: Context) {
        id = Settings.System.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID)
        name = Build.PRODUCT
    }
}

object Net {
    private lateinit var cm: ConnectivityManager
    val hasNet: Boolean
        get() = cm.getNetworkCapabilities(cm.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            ?: true
    val hasWifi: Boolean
        get() = cm.getNetworkCapabilities(cm.activeNetwork)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: true
    val hasMobile: Boolean
        get() = cm.getNetworkCapabilities(cm.activeNetwork)?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            ?: true

    fun init(ctx: Context) {
        cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
}

val res: Resources; get() = Res.resources

object Res {
    var primaryColor = Color.BLACK; private set
    var secondaryColor = Color.GRAY; private set
    var accentColor = Color.RED; private set
    lateinit var resources: Resources

    @SuppressLint("InlinedApi")
    fun init(ctx: Context) {
        resources = ctx.resources
        primaryColor = tryRun {
            TypedValue().apply {
                ctx.theme.resolveAttribute(android.R.attr.colorPrimary, this, true)
            }.data
        } ?: Color.BLACK
        secondaryColor = tryRun {
            TypedValue().apply {
                ctx.theme.resolveAttribute(android.R.attr.colorSecondary, this, true)
            }.data
        } ?: Color.GRAY
        accentColor = tryRun {
            TypedValue().apply {
                ctx.theme.resolveAttribute(android.R.attr.colorAccent, this, true)
            }.data
        } ?: Color.RED
    }
}

fun Application.initAppInfo() {
    DisPlay.init(this)
    Apk.init(this)
    Device.init(this)
    Net.init(this)
}

fun ContextThemeWrapper.initThemeInfo() {
    Res.init(this)
}

val Activity.THEME_LIGHT; get() = ThemeType.LIGHT
val Activity.THEME_DARK; get() = ThemeType.DARK

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
    height: Int = TitleBar.defTitleSize,
    backgroundColor: Int = TitleBar.defBackgroundColor,
    background: Drawable? = ColorDrawable(backgroundColor),
    fitBackground: Boolean = true,
    fitStatusBar: Boolean = true,
    elevation: Int = 1.dp,
    exec: TitleBar.() -> Unit
) {
    val rootView = window.decorView.findViewById(android.R.id.content) as FrameLayout
    val titleBarView = FrameLayout(this)
    val sumHeight = height + if (fitStatusBar) DisPlay.statusBar else 0
    titleBarView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, sumHeight)
    titleBarView.setPadding(
        8.dp, if (fitStatusBar) DisPlay.statusBar else 0, 8.dp, 0
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
    TitleBar(this, titleBarView).exec()
}

fun Context.checkPermission(vararg permissions :Pair<String,String>):Boolean{
    return XXPermissions.isGranted(this,permissions.map { it.second })
}

fun Context.hideKeyboard() {
    if (this !is Activity) return
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(window.decorView.windowToken, 0)
}

fun Context.clearFocus() {
    if (this !is Activity) return
    window.decorView.findViewById<ViewGroup>(android.R.id.content).clearFocus()
}

fun FrameLayout.applyTitleBar(
    height: Int = TitleBar.defBarHeight,
    backgroundColor: Int = TitleBar.defBackgroundColor,
    background: Drawable? = ColorDrawable(backgroundColor),
    fitBackground: Boolean = true,
    fitStatusBar: Boolean = true,
    elevation: Int = 1.dp,
    exec: TitleBar.() -> Unit
) {
    val rootView = parent
    if (rootView !is FrameLayout) return
    val sumHeight = height + if (fitStatusBar) DisPlay.statusBar else 0
    this.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, sumHeight)
    this.setPadding(
        8.dp, if (fitStatusBar) DisPlay.statusBar else 0, 8.dp, 0
    )
    this.background = background
    this.elevation = elevation.toFloat()
    val bodyView = rootView[1]
    if (fitBackground) {
        rootView.background = bodyView.background
        bodyView.background = null
    }
    (bodyView.layoutParams as FrameLayout.LayoutParams).topMargin += sumHeight
    TitleBar(rootView.context, this).exec()
}

fun EditText.showKeyboard() {
    isFocusable = true
    isFocusableInTouchMode = true
    requestFocus()
    if (context !is Activity) return
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, 0)
}

fun loge(msg: String) {
    log(Log.ERROR, msg)
}

fun logw(msg: String) {
    log(Log.WARN, msg)
}

fun logd(msg: String) {
    if (!Apk.debug) return
    log(Log.DEBUG, msg)
}

private fun log(type: Int, msg: String) {
    var tag = ""
    StringBuilder().apply {
        append("┌──────────────────────────────────────────────────────────────────────────────────────────────────")
        repeat(5) { i ->
            val e = Thread.currentThread().stackTrace[9 - i]
            val c = e.className.split("\\.").last()
            if (i == 4) tag = c
            append("\n│").append(c).append(".").append(e.methodName)
            append(":").append("(").append(e.fileName)
            append(":").append(e.lineNumber).append(")")
        }
        append("\n")
        append("├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄")
        append("\n")
        append("│").append(msg.replace("\\n".toRegex(), "\n│")).append("\n")
        append("└──────────────────────────────────────────────────────────────────────────────────────────────────")
    }.lines().forEach {
        Log.println(type, tag, it)
    }
}

class TitleBar(
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
        color: Int = defBtnColor,
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
            get() {
                if (field == -1) field = Res.primaryColor
                return field
            }
    }

}

@BindingAdapter("simpleAdapter")
fun setSimpleAdapter(recyclerView: RecyclerView,simpleAdapter: SimpleRecyclerAdapter?){
    if(simpleAdapter == null) return
    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context).apply {
        orientation = LinearLayoutManager.VERTICAL
    }
    recyclerView.adapter = simpleAdapter
}

class SimpleRecyclerAdapter(
    private val resId: (pos: Int) -> Int,
    private val count: () -> Int,
    private val bindData: (adapter: SimpleRecyclerAdapter, binding: ViewDataBinding, pos: Int) -> Unit
) : RecyclerView.Adapter<SimpleRecyclerAdapter.SimpleViewHolder>() {
    lateinit var ctx: Context
    private val resIds = ArrayList<Int>()

    class SimpleViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        ctx = recyclerView.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        return SimpleViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), resIds[viewType], parent, false
            )
        )
    }

    override fun getItemViewType(position: Int): Int {
        val id = resId(position)
        if (resIds.indexOf(id) == -1) resIds.add(id)
        return resIds.indexOf(id)
    }

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
        bindData(this, holder.binding, position)
    }

    override fun getItemCount(): Int {
        return count()
    }
}

abstract class KDialog(
    protected val ctx: Context,
    protected val type: Type = Type.CENTER,
    protected val cancelable: Boolean = true,
    private val backgroundColor: Int = Color.argb(80, 0, 0, 0),
    private val layoutType: LayoutType
) {
    private var status = 0
    private lateinit var rootView: FrameLayout
    private lateinit var dialogView: View
    private lateinit var backgroundView: View
    private var postCancel = false

    private fun createBackgroundView(): View {
        val groundView = View(ctx)
        val backgroundParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        groundView.layoutParams = backgroundParams
        groundView.setBackgroundColor(backgroundColor)
        groundView.isClickable = true
        if (cancelable) groundView.setOnClickListener { cancel() }
        return groundView
    }

    fun show() {
        rootView = (ctx as Activity).window.decorView.findViewById(android.R.id.content)
        backgroundView = createBackgroundView()
        dialogView = createDialog()
        if (status != 1) return
        ctx.hideKeyboard()
        ctx.clearFocus()
        status = 2
        when (type) {
            Type.CENTER -> showCenterDialog()
            Type.BOTTOM -> showBottomDialog()
        }
    }

    private fun showBottomDialog() {
        val inAnimator: ValueAnimator = getBottomInAnimator()
        inAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator, isReverse: Boolean) {
                super.onAnimationStart(animation, isReverse)
                dialogView.translationY = dialogView.height.toFloat()
                rootView.addView(backgroundView)
                backgroundView.elevation = Float.MAX_VALUE - 1
                rootView.addView(dialogView)
                dialogView.elevation = Float.MAX_VALUE
                onShowStart()
            }

            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                super.onAnimationEnd(animation, isReverse)
                backgroundView.alpha = 1f
                dialogView.translationY = 0f
                status = 3
                onShowEnd()
                if (postCancel) cancel()
            }
        })
        inAnimator.start()
    }

    private fun showCenterDialog() {
        val inAnimator: ValueAnimator = getCenterInAnimator()
        inAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator, isReverse: Boolean) {
                super.onAnimationStart(animation, isReverse)
                dialogView.pivotX = dialogView.width.toFloat() / 2
                dialogView.pivotY = dialogView.height.toFloat() / 2
                dialogView.scaleX = 0.8.toFloat()
                dialogView.scaleY = 0.8.toFloat()
                backgroundView.setAlpha(0f)
                dialogView.alpha = 0f
                rootView.addView(backgroundView)
                backgroundView.elevation = Float.MAX_VALUE - 1
                rootView.addView(dialogView)
                dialogView.elevation = Float.MAX_VALUE
                onShowStart()
            }

            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                super.onAnimationEnd(animation, isReverse)
                backgroundView.setAlpha(1f)
                dialogView.scaleX = 1f
                dialogView.scaleY = 1f
                dialogView.alpha = 1f
                status = 3
                onShowEnd()
                if (postCancel) cancel()
            }
        })
        inAnimator.start()
    }


    protected open fun getCenterInAnimator(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 300
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation: ValueAnimator ->
            val pos = animation.animatedValue as Float
            dialogView.pivotX = dialogView.width.toFloat() / 2
            dialogView.pivotY = dialogView.height.toFloat() / 2
            dialogView.scaleX = (0.2 * pos + 0.8).toFloat()
            dialogView.scaleY = (0.2 * pos + 0.8).toFloat()
            backgroundView.alpha = pos
            dialogView.alpha = pos
        }
        return animator
    }

    protected open fun getCenterOutAnimator(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(1f, 0f)
        animator.duration = 100
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { animation: ValueAnimator ->
            val pos = animation.animatedValue as Float
            backgroundView.alpha = pos
            dialogView.alpha = pos
        }
        return animator
    }

    protected open fun getBottomInAnimator(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 300
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation: ValueAnimator ->
            val pos = animation.animatedValue as Float
            dialogView.translationY = dialogView.height - dialogView.height * pos
            backgroundView.alpha = pos
            dialogView.alpha = pos
        }
        return animator
    }

    protected open fun getBottomOutAnimator(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(1f, 0f)
        animator.duration = 200
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { animation: ValueAnimator ->
            val pos = animation.animatedValue as Float
            dialogView.translationY = dialogView.height - dialogView.height * pos
            backgroundView.alpha = pos
            dialogView.alpha = pos
        }
        return animator
    }

    fun cancel() {
        if (status != 3) {
            postCancel = true
            return
        }
        status = 4
        Handler(ctx.getMainLooper()).post(Runnable {
            onCancelStart()
            when (type) {
                Type.CENTER -> cancelCenterDialog()
                Type.BOTTOM -> cancelBottomDialog()
            }
        })
    }

    open fun onCancelStart() {
    }

    open fun onCancelEnd() {
        ctx.hideKeyboard()
    }

    open fun onShowEnd() {
    }

    open fun onShowStart() {
    }

    private fun cancelCenterDialog() {
        val outAnimator = getCenterOutAnimator()
        outAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator, isReverse: Boolean) {
                super.onAnimationStart(animation, isReverse)
            }

            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                super.onAnimationEnd(animation, isReverse)
                rootView.removeView(dialogView)
                rootView.removeView(backgroundView)
                status = 5
                onCancelEnd()
            }
        })
        outAnimator.start()
    }

    private fun cancelBottomDialog() {
        val outAnimator = getBottomOutAnimator()
        outAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator, isReverse: Boolean) {
                super.onAnimationStart(animation, isReverse)
            }

            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                super.onAnimationEnd(animation, isReverse)
                rootView.removeView(dialogView)
                rootView.removeView(backgroundView)
                status = 5
                onCancelEnd()
            }
        })
        outAnimator.start()
    }


    private fun createDialog(): View {
        val diaView = createView()
        diaView.layoutParams = if (layoutType.marginT > 0 || layoutType.marginB > 0) {
            FrameLayout.LayoutParams(layoutType.width, layoutType.height, Gravity.CENTER_HORIZONTAL).apply {
                if (layoutType.marginT > 0) topMargin = layoutType.marginT
                else bottomMargin = layoutType.marginB
            }
        } else if (layoutType.marginL > 0 || layoutType.marginR > 0) {
            FrameLayout.LayoutParams(layoutType.width, layoutType.height, Gravity.CENTER_VERTICAL).apply {
                if (layoutType.marginL > 0) leftMargin = layoutType.marginL
                else rightMargin = layoutType.marginR
            }
        } else {
            FrameLayout.LayoutParams(layoutType.width, layoutType.height, Gravity.CENTER)
        }
        diaView.isClickable = true
        status = 1
        return diaView
    }

    abstract fun createView(): View

    data class LayoutType(
        var height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        var width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        var marginT: Int = 0,
        var marginB: Int = 0,
        var marginL: Int = 0,
        var marginR: Int = 0,
    )

    enum class Type {
        CENTER, BOTTOM
    }
}

