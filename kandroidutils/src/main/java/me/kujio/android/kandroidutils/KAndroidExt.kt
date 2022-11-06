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
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.*
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

object App {
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

fun Application.initAppInfo() {
    DisPlay.init(this)
    App.init(this)
    Device.init(this)
    Net.init(this)
}

val Context.primaryColor: Int
    get() = tryRun {
        TypedValue().apply {
            theme.resolveAttribute(android.R.attr.colorPrimary, this, true)
        }.data
    } ?: Color.BLACK
val Context.secondaryColor: Int
    @SuppressLint("InlinedApi") get() = tryRun {
        TypedValue().apply {
            theme.resolveAttribute(android.R.attr.colorSecondary, this, true)
        }.data
    } ?: Color.GRAY
val Context.accentColor: Int
    get() = tryRun {
        TypedValue().apply {
            theme.resolveAttribute(android.R.attr.colorAccent, this, true)
        }.data
    } ?: Color.RED


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
    height: Int = TitleBar.defBarHeight,
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
    val imm = context.getSystemService(InputMethodManager::class.java) as InputMethodManager
    imm.showSoftInput(this, 0)
}

fun loge(msg: String) {
    log(Log.ERROR, msg)
}

fun logw(msg: String) {
    log(Log.WARN, msg)
}

fun logd(msg: String) {
    if (!App.debug) return
    log(Log.DEBUG, msg)
}

private fun log(type: Int, msg: String) {
    val stack = Thread.currentThread().stackTrace
    val tag = stack[4].className.split(".").last()
    StringBuilder().apply {
        append("┌──────────────────────────────────────────────────────────────────────────────────────────────────")
        val size = if (stack.size < 8) stack.size - 4 else 5
        repeat(size) { i ->
            val e = stack[size + 3 - i]
            append("\n│").append(e.className).append(".").append(e.methodName)
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
        color: Int = ctx.primaryColor,
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
        var defBtnColor = Color.parseColor("#333333")
    }

}

@BindingAdapter("simpleAdapter")
fun setSimpleAdapter(recyclerView: RecyclerView, simpleAdapter: SimpleRecyclerAdapter?) {
    if (simpleAdapter == null) return
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



