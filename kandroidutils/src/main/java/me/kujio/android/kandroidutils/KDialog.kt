package me.kujio.android.kandroidutils

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


abstract class KDialog(
    protected val activity: AppCompatActivity,
    @LayoutRes protected val layoutResId: Int,
    protected val layoutType: LayoutType,
    @ColorInt protected val backgroundColor: Int = Color.argb(80, 0, 0, 0)
) {
    private var status = 1
    private val backPressedCallback = object :OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            cancelAsync()
        }
    }
    private val rootView: FrameLayout = activity.window.decorView.findViewById(android.R.id.content)
    private val coverView: FrameLayout = FrameLayout(activity).apply {
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        visibility = View.GONE
        isClickable = true
        elevation = Float.MAX_VALUE
        setBackgroundColor(backgroundColor)
    }

    private val dialogView =
        DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(activity), layoutResId, rootView, false).apply {
            onViewBinding(this)
            root.layoutParams = when (layoutType) {
                is LayoutType.CenterBySize -> FrameLayout.LayoutParams(
                    layoutType.width,
                    layoutType.height,
                    Gravity.CENTER
                )

                is LayoutType.BottomBySize -> FrameLayout.LayoutParams(
                    layoutType.width,
                    layoutType.height,
                    Gravity.BOTTOM
                )

                is LayoutType.CenterByPadding -> FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                ).also {
                    coverView.setPadding(
                        layoutType.paddingLeft, layoutType.paddingTop, layoutType.paddingRight, layoutType.paddingBottom
                    )
                }

                is LayoutType.BottomByPadding -> FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                ).also {
                    coverView.setPadding(
                        layoutType.paddingLeft, layoutType.paddingTop, layoutType.paddingRight, layoutType.paddingBottom
                    )
                }
            }
            coverView.addView(root)
            rootView.addView(coverView)
            coverView.visibility = View.GONE
        }.root


    protected abstract fun onViewBinding(binding: ViewDataBinding)

    private suspend fun showBottomDialog() = suspendCoroutine { continuation ->
        dialogView.translationY = dialogView.height.toFloat()
        coverView.alpha = 0f
        coverView.visibility = View.VISIBLE
        animatorDecelerate(duration = DisPlay.animationDuration ) {
            dialogView.translationY = dialogView.height - dialogView.height * it
            coverView.alpha = it

            if (it != 1f) return@animatorDecelerate
            coverView.alpha = 1f
            dialogView.translationY = 0f
            status = 3
            continuation.resume(Unit)
        }
    }

    private suspend fun showCenterDialog() = suspendCoroutine { continuation ->
        dialogView.pivotX = dialogView.width.toFloat() / 2
        dialogView.pivotY = dialogView.height.toFloat() / 2
        dialogView.scaleX = 0.8.toFloat()
        dialogView.scaleY = 0.8.toFloat()
        coverView.alpha = 0f
        coverView.visibility = View.VISIBLE
        animatorDecelerate {
            dialogView.pivotX = dialogView.width.toFloat() / 2
            dialogView.pivotY = dialogView.height.toFloat() / 2
            dialogView.scaleX = (0.2 * it + 0.8).toFloat()
            dialogView.scaleY = (0.2 * it + 0.8).toFloat()
            coverView.alpha = it

            if (it != 1f) return@animatorDecelerate
            coverView.alpha = 1f
            dialogView.scaleX = 1f
            dialogView.scaleY = 1f
            status = 3
            continuation.resume(Unit)
        }
    }

    private suspend fun cancelCenterDialog() = suspendCoroutine { continuation ->
        activity.hideKeyboard()
        animatorDecelerate(1f, 0f, 100) {
            coverView.alpha = it
            if (it != 0f) return@animatorDecelerate
            rootView.removeView(coverView)
            status = 5
            continuation.resume(Unit)
        }
    }

    private suspend fun cancelBottomDialog() = suspendCoroutine { continuation ->
        activity.hideKeyboard()
        animatorDecelerate(1f, 0f, 200) {
            dialogView.translationY = dialogView.height - dialogView.height * it
            coverView.alpha = it

            if (it != 0f) return@animatorDecelerate
            rootView.removeView(coverView)
            status = 5
            continuation.resume(Unit)
        }
    }

    open fun onCancel(){}

    suspend fun show() {
        if (status != 1) return
        activity.hideKeyboard()
        activity.clearFocus()
        status = 2
        activity.onBackPressedDispatcher.addCallback(backPressedCallback)
        when (layoutType) {
            is LayoutType.CenterByPadding, is LayoutType.CenterBySize -> showCenterDialog()
            is LayoutType.BottomByPadding, is LayoutType.BottomBySize -> showBottomDialog()
        }
    }

    suspend fun cancel() {
        if (status != 3) return
        status = 4
        onCancel()
        backPressedCallback.isEnabled = false
        when (layoutType) {
            is LayoutType.CenterBySize, is LayoutType.CenterByPadding -> cancelCenterDialog()
            is LayoutType.BottomBySize, is LayoutType.BottomByPadding -> cancelBottomDialog()
        }
    }

    fun cancelAsync() = activity.lifecycleScope.launch {
        cancel()
    }

    sealed interface LayoutType {
        data class CenterByPadding(
            val paddingTop: Int = 0, val paddingBottom: Int = 0, val paddingLeft: Int = 0, val paddingRight: Int = 0
        ) : LayoutType

        data class CenterBySize(
            val width: Int = 0,
            val height: Int = 0,
        ) : LayoutType

        data class BottomByPadding(
            val paddingTop: Int = 0, val paddingBottom: Int = 0, val paddingLeft: Int = 0, val paddingRight: Int = 0
        ) : LayoutType

        data class BottomBySize(
            val width: Int = 0,
            val height: Int = 0,
        ) : LayoutType
    }
}