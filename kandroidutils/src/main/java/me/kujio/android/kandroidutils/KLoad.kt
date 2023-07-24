package me.kujio.android.kandroidutils

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.databinding.BindingAdapter
import me.kujio.android.kandroidutils.loading.KLoadingView
import java.util.*

private val LOADING_ID by lazy { View.generateViewId() }
private val FAILED_ID by lazy { View.generateViewId() }

sealed class KLoad(val text: String, val view: View? = null) {
    class Loading(text: String = "加载中···", view: View? = null) : KLoad(text, view)
    class Failed(text: String, view: View? = null) : KLoad(text, view)
}

private fun defKLoadingView(context: Context): View {
    return KLoadingView(context).apply {
        layoutParams = LinearLayout.LayoutParams(32.dp, 32.dp)
    }
}

private fun defFailedView(context: Context): View {
    return ImageView(context).apply {
        setImageResource(R.drawable.ic_error)
        scaleType = ImageView.ScaleType.FIT_CENTER
        layoutParams = LinearLayout.LayoutParams(32.dp, 32.dp)
    }
}

private fun createLoadView(context: Context, text: String, view: View): View {
    val linearLayout = LinearLayout(context)
    val textView = TextView(context)
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


private val childsVisibility = HashMap<Int, Int>()

@BindingAdapter("android:loading")
fun setConstraintLayoutLoading(layout: ConstraintLayout, kLoad: KLoad?) {
    fun recordVisibility() {
        val hash = layout.hashCode()
        for (child in layout.children) {
            if (child.id == View.NO_ID) child.id = View.generateViewId()
            childsVisibility[hash + child.id] = child.visibility
            child.visibility = View.GONE
        }
    }

    fun resetVisibility() {
        val hash = layout.hashCode()
        for (child in layout.children) {
            childsVisibility[hash + child.id]?.let {
                child.visibility = it
            }
        }
    }

    if (kLoad == null) {
        var view: View? = null
        for (child in layout.children) {
            if (child.id == LOADING_ID || child.id == FAILED_ID) {
                view = child
                break
            }
        }
        if (view == null) return

        layout.removeView(view)
        resetVisibility()
        return
    }

    val loadViewLayoutParams = ConstraintLayout.LayoutParams(
        ConstraintLayout.LayoutParams.WRAP_CONTENT,
        ConstraintLayout.LayoutParams.WRAP_CONTENT
    )

    fun addCenter(view: View) {
        val set = ConstraintSet()
        set.clone(layout)
        set.connect(view.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        set.connect(view.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        set.connect(view.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        set.connect(view.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        layout.addView(view,loadViewLayoutParams)
        set.applyTo(layout)
    }

    var loadingView: View? = null
    var failedView: View? = null
    for (child in layout.children) {
        if (child.id == LOADING_ID) {
            loadingView = child
            break
        }
        if (child.id == FAILED_ID) {
            failedView = child
            break
        }
    }

    if (kLoad is KLoad.Loading) {
        if (loadingView != null) return
        if (failedView != null)
            layout.removeView(failedView)
        else
            recordVisibility()

        loadingView = createLoadView(
            layout.context,
            kLoad.text,
            kLoad.view ?: defKLoadingView(layout.context)
        )
        loadingView.id = LOADING_ID
        addCenter(loadingView)
        return
    }


    if (kLoad is KLoad.Failed) {
        if (failedView != null) return
        if (loadingView != null)
            layout.removeView(loadingView)
        else
            recordVisibility()
        failedView = createLoadView(
            layout.context,
            kLoad.text,
            kLoad.view ?: defFailedView(layout.context)
        )
        failedView.id = FAILED_ID
        addCenter(failedView)
        return
    }
}

