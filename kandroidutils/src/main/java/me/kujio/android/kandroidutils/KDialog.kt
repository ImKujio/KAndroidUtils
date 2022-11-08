package me.kujio.android.kandroidutils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import java.lang.ref.SoftReference
import java.util.InputMismatchException


fun Context.showDialog(
    @LayoutRes layoutResId: Int,
    layoutType: KDialog.LayoutType,
    cancelable: Boolean = true,
    @ColorInt backgroundColor: Int = Color.argb(80, 0, 0, 0),
    bindAction: KDialog.(binding: ViewDataBinding) -> Unit
) {
    KDialog(this, layoutResId, layoutType, cancelable, backgroundColor, bindAction).show()
}

class KDialog(
    val context: Context,
    @LayoutRes val layoutResId: Int,
    private val layoutType: LayoutType,
    private val cancelable: Boolean = true,
    @ColorInt val backgroundColor: Int = Color.argb(80, 0, 0, 0),
    val bindAction: KDialog.(binding: ViewDataBinding) -> Unit
) {
    private var status = 0
    private lateinit var rootView: FrameLayout
    private lateinit var contentView: View
    private lateinit var dialogView: FrameLayout
    private var postCancel = false

    val isShowing: Boolean
        get() = status in 2..4
    val isCancelable: Boolean
        get() = cancelable

    private var cancelStartAction: () -> Unit = {}
    private var cancelEndAction: () -> Unit = {}
    private var showStartAction: () -> Unit = {}
    private var showEndAction: () -> Unit = {}

    companion object {
        var lastDialog: SoftReference<KDialog>? = null
    }

    private fun createDialogView() {
        dialogView = FrameLayout(context)
        dialogView.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialogView.setBackgroundColor(backgroundColor)
        if (cancelable) dialogView.setOnClickListener { cancel() }
        contentView =
            DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(context), layoutResId, rootView, false).apply {
                bindAction(this)
            }.root
        contentView.layoutParams = when (layoutType) {
            is LayoutType.CenterBySize -> FrameLayout.LayoutParams(layoutType.width, layoutType.height, Gravity.CENTER)
            is LayoutType.BottomBySize -> FrameLayout.LayoutParams(layoutType.width, layoutType.height, Gravity.BOTTOM)
            is LayoutType.CenterByPadding -> FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            ).also {
                dialogView.setPadding(
                    layoutType.paddingLeft, layoutType.paddingTop, layoutType.paddingRight, layoutType.paddingBottom
                )
            }

            is LayoutType.BottomByPadding -> FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            ).also {
                dialogView.setPadding(
                    layoutType.paddingLeft, layoutType.paddingTop, layoutType.paddingRight, layoutType.paddingBottom
                )
            }
        }
        dialogView.visibility = View.GONE
        dialogView.isClickable = true
        contentView.isClickable = true
        dialogView.elevation = Float.MAX_VALUE
        dialogView.addView(contentView)
        status = 1
    }

    private fun showBottomDialog() {
        val inAnimator: ValueAnimator = getBottomInAnimator()
        inAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator, isReverse: Boolean) {
                super.onAnimationStart(animation, isReverse)
                contentView.translationY = contentView.height.toFloat()
                dialogView.alpha = 0f
                dialogView.visibility = View.VISIBLE
                showStartAction()
            }

            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                super.onAnimationEnd(animation, isReverse)
                dialogView.alpha = 1f
                contentView.translationY = 0f
                status = 3
                showEndAction()
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
                contentView.pivotX = contentView.width.toFloat() / 2
                contentView.pivotY = contentView.height.toFloat() / 2
                contentView.scaleX = 0.8.toFloat()
                contentView.scaleY = 0.8.toFloat()
                dialogView.alpha = 0f
                dialogView.visibility = View.VISIBLE
                showStartAction()
            }

            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                super.onAnimationEnd(animation, isReverse)
                dialogView.setAlpha(1f)
                contentView.scaleX = 1f
                contentView.scaleY = 1f
                status = 3
                showEndAction()
                if (postCancel) cancel()
            }
        })
        inAnimator.start()
    }

    private fun cancelCenterDialog() {
        val outAnimator = getCenterOutAnimator()
        outAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                super.onAnimationEnd(animation, isReverse)
                rootView.removeView(dialogView)
                status = 5
                cancelEndAction()
                context.hideKeyboard()
            }
        })
        outAnimator.start()
    }

    private fun cancelBottomDialog() {
        val outAnimator = getBottomOutAnimator()
        outAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                super.onAnimationEnd(animation, isReverse)
                rootView.removeView(dialogView)
                status = 5
                cancelEndAction()
                context.hideKeyboard()
            }
        })
        outAnimator.start()
    }

    fun show() {
        rootView = (context as Activity).window.decorView.findViewById(android.R.id.content)
        createDialogView()
        if (status != 1) return
        context.hideKeyboard()
        context.clearFocus()
        status = 2
        rootView.addView(dialogView)
        lastDialog = SoftReference(this)
        when (layoutType) {
            is LayoutType.CenterByPadding, is LayoutType.CenterBySize -> showCenterDialog()
            is LayoutType.BottomByPadding, is LayoutType.BottomBySize -> showBottomDialog()
        }
    }

    fun cancel() {
        if (status != 3) {
            postCancel = true
            return
        }
        status = 4
        Handler(context.mainLooper).post {
            cancelStartAction()
            when (layoutType) {
                is LayoutType.CenterBySize, is LayoutType.CenterByPadding -> cancelCenterDialog()
                is LayoutType.BottomBySize, is LayoutType.BottomByPadding -> cancelBottomDialog()
            }
        }
    }

    fun onCancelStart(action: () -> Unit) {
        cancelStartAction = action
    }

    fun onCancelEnd(action: () -> Unit) {
        cancelEndAction = action
    }

    fun onShowEnd(action: () -> Unit) {
        showEndAction = action
    }

    fun onShowStart(action: () -> Unit) {
        showStartAction = action
    }

    private fun getCenterInAnimator(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 300
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation: ValueAnimator ->
            val pos = animation.animatedValue as Float
            contentView.pivotX = contentView.width.toFloat() / 2
            contentView.pivotY = contentView.height.toFloat() / 2
            contentView.scaleX = (0.2 * pos + 0.8).toFloat()
            contentView.scaleY = (0.2 * pos + 0.8).toFloat()
            dialogView.alpha = pos
        }
        return animator
    }

    private fun getCenterOutAnimator(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(1f, 0f)
        animator.duration = 100
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { animation: ValueAnimator ->
            val pos = animation.animatedValue as Float
            dialogView.alpha = pos
        }
        return animator
    }

    private fun getBottomInAnimator(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 300
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation: ValueAnimator ->
            val pos = animation.animatedValue as Float
            contentView.translationY = contentView.height - contentView.height * pos
            dialogView.alpha = pos
        }
        return animator
    }

    private fun getBottomOutAnimator(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(1f, 0f)
        animator.duration = 200
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { animation: ValueAnimator ->
            val pos = animation.animatedValue as Float
            contentView.translationY = contentView.height - contentView.height * pos
            dialogView.alpha = pos
        }
        return animator
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
