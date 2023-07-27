package me.kujio.android.kandroidutils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.core.view.setPadding
import androidx.databinding.BindingAdapter
import androidx.fragment.app.DialogFragment
import me.kujio.android.kandroidutils.loading.KLoadingView
import java.util.*
import kotlin.math.max

private val LOADING_ID by lazy { View.generateViewId() }
private val FAILED_ID by lazy { View.generateViewId() }

sealed class KLoad(val text: String, val view: View? = null) {
    class Loading(text: String = "加载中···", view: View? = null) : KLoad(text, view)
    class Failed(text: String, view: View? = null) : KLoad(text, view)
}

private fun defKLoadingView(context: Context, size: Int = 32.dp, color: Int? = null): View {
    return KLoadingView(context).apply {
        layoutParams = LinearLayout.LayoutParams(size, size)
        color?.let { this.color = color }
    }
}

private fun defFailedView(context: Context, size: Int = 32.dp, color: Int? = null): View {
    return ImageView(context).apply {
        setImageResource(R.drawable.ic_error)
        scaleType = ImageView.ScaleType.FIT_CENTER
        color?.let { imageTintList = ColorStateList.valueOf(it) }
        layoutParams = LinearLayout.LayoutParams(size, size)
    }
}

private fun defSuccessView(context: Context, size: Int = 32.dp, color: Int? = null): View {
    return ImageView(context).apply {
        setImageResource(R.drawable.ic_success)
        scaleType = ImageView.ScaleType.FIT_CENTER
        color?.let { imageTintList = ColorStateList.valueOf(it) }
        layoutParams = LinearLayout.LayoutParams(size, size)
    }
}


private fun createLoadView(context: Context, text: String, textColor: Int? = null, view: View): View {
    val linearLayout = LinearLayout(context)
    val textView = TextView(context)
    textColor?.let { textView.setTextColor(it) }
    textView.layoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply {
        setMargins(0, 12.dp, 0, 0)
    }
    textView.text = text
    linearLayout.orientation = LinearLayout.VERTICAL
    linearLayout.gravity = Gravity.CENTER
    linearLayout.addView(view)
    linearLayout.addView(textView)
    return linearLayout
}


class KLoadingDialogs(private val text: String, private val view: View) : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return createLoadView(
            requireContext(),
            text,
            Color.WHITE,
            view
        ).apply {
            setBackgroundResource(R.drawable.bk_r16_a60)
            setPadding(24.dp)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        val window = dialog?.window ?: return
        window.setDimAmount(0.2f)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        view.layoutParams = view.layoutParams.apply {
            loge { "width:${view.width} height:${view.height}" }
            width = max(width, height)
            height = max(width, height)
        }
    }

}


private val childsVisibility = HashMap<Int, Int>()

private fun recordVisibility(layout: ViewGroup) {
    val hash = layout.hashCode()
    for (child in layout.children) {
        if (child.id == View.NO_ID) child.id = View.generateViewId()
        childsVisibility[hash + child.id] = child.visibility
        child.visibility = View.GONE
    }
}

private fun findLoadView(layout: ViewGroup): View? {
    var view: View? = null
    for (child in layout.children) {
        if (child.id == LOADING_ID || child.id == FAILED_ID) {
            view = child
            break
        }
    }
    return view
}

private fun resetVisibility(layout: ViewGroup) {
    val view: View = findLoadView(layout) ?: return
    layout.removeView(view)
    val hash = layout.hashCode()
    for (child in layout.children) {
        childsVisibility[hash + child.id]?.let {
            child.visibility = it
        }
        childsVisibility.remove(hash + child.id)
    }
}

private fun getLoadingViewPrepareAdd(layout: ViewGroup, kLoad: KLoad, reload: OnClickListener?): View? {
    var view = findLoadView(layout)

    if (kLoad is KLoad.Loading && view != null && view.id == LOADING_ID) return null
    if (kLoad is KLoad.Failed && view != null && view.id == FAILED_ID) return null
    if (view == null) recordVisibility(layout) else layout.removeView(view)
    if (kLoad is KLoad.Loading) {
        view = createLoadView(
            layout.context,
            kLoad.text,
            null,
            kLoad.view ?: defKLoadingView(layout.context)
        )
        view.id = LOADING_ID
    }
    if (kLoad is KLoad.Failed) {
        view = createLoadView(
            layout.context,
            kLoad.text,
            null,
            kLoad.view ?: defFailedView(layout.context)
        )
        view.id = FAILED_ID
        reload?.let { view.setOnClickListener(it) }
    }
    return view
}

/**
 * 约束布局添加LoadingView
 */
private fun ConstraintLayout.addLoadingView(view: View) {
    val loadViewLayoutParams = ConstraintLayout.LayoutParams(
        ConstraintLayout.LayoutParams.MATCH_PARENT,
        ConstraintLayout.LayoutParams.MATCH_PARENT
    )
    val set = ConstraintSet()
    set.clone(this)
    set.connect(view.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
    set.connect(view.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
    set.connect(view.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
    set.connect(view.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
    addView(view, loadViewLayoutParams)
    set.applyTo(this)
}

@BindingAdapter("android:loading")
fun setConstraintLayoutLoading(layout: ConstraintLayout, kLoad: KLoad?) =
    setConstraintLayoutLoading(layout, kLoad, null)

@BindingAdapter("android:loading", "android:onReload")
fun setConstraintLayoutLoading(layout: ConstraintLayout, kLoad: KLoad?, reload: OnClickListener?) {
    if (kLoad == null) {
        resetVisibility(layout)
        return
    }
    getLoadingViewPrepareAdd(layout, kLoad, reload)?.let { layout.addLoadingView(it) }
}

private fun FrameLayout.addLoadingView(view: View) {
    val layoutParams = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT
    )
    addView(view, layoutParams)
}

@BindingAdapter("android:loading")
fun setFrameLayoutLoading(layout: FrameLayout, kLoad: KLoad?) = setFrameLayoutLoading(layout, kLoad, null)

@BindingAdapter("android:loading", "android:onReload")
fun setFrameLayoutLoading(layout: FrameLayout, kLoad: KLoad?, reload: OnClickListener?) {
    if (kLoad == null) {
        resetVisibility(layout)
        return
    }

    getLoadingViewPrepareAdd(layout, kLoad, reload)?.let { layout.addLoadingView(it) }
}

private fun LinearLayout.addLoadingView(view: View) {
    val layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT
    )
    addView(view, layoutParams)
}

@BindingAdapter("android:loading")
fun setLinearLayoutLoading(layout: LinearLayout, kLoad: KLoad?) = setLinearLayoutLoading(layout, kLoad, null)

@BindingAdapter("android:loading", "android:onReload")
fun setLinearLayoutLoading(layout: LinearLayout, kLoad: KLoad?, reload: OnClickListener?) {
    if (kLoad == null) {
        resetVisibility(layout)
        return
    }

    getLoadingViewPrepareAdd(layout, kLoad, reload)?.let { layout.addLoadingView(it) }
}