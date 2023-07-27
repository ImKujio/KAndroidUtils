package me.kujio.android.kandroidutils

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.View.OnClickListener
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import me.kujio.android.kandroidutils.KApp.onStopped
import me.kujio.android.kandroidutils.databinding.DialogConfirmBinding
import me.kujio.android.kandroidutils.databinding.DialogFailedBinding
import me.kujio.android.kandroidutils.databinding.DialogLoadingBinding
import me.kujio.android.kandroidutils.databinding.DialogSuccessBinding
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


fun KDialog.Companion.loading(message: String? = null) {
    KApp.curActivity?.let {
        val dialog = KLoadingDialog.newInstance(message)
        dialog.isCancelable = false
        dialog.show(it, "KLoadingDialog")
        it.onStopped("KLoadingDialog") { dismissLoading() }
    }
}

fun KDialog.Companion.dismissLoading() {
    KApp.curActivity?.let {
        if (it !is AppCompatActivity) return
        it.supportFragmentManager.findFragmentByTag("KLoadingDialog")?.let { fragment ->
            if (fragment is KDialog) {
                fragment.dismiss()
            }
        }
    }
}

fun KDialog.Companion.success(message: String? = null) {
    KApp.curActivity?.let { activity ->
        dismissLoading()
        val dialog = KSuccessDialog.newInstance(message)
        dialog.isCancelable = false
        dialog.show(activity, "KSuccessDialog")
        Handler(activity.mainLooper).postDelayed({ dialog.dismiss() }, 2000)
        activity.onStopped("KSuccessDialog") { dialog.dismiss() }
    }
}

fun KDialog.Companion.failed(message: String? = null) {
    KApp.curActivity?.let { activity ->
        dismissLoading()
        val dialog = KFailedDialog.newInstance(message)
        dialog.isCancelable = false
        dialog.show(activity, "KFailedDialog")
        Handler(activity.mainLooper).postDelayed({ dialog.dismiss() }, 2000)
        activity.onStopped("KFailedDialog") { dialog.dismiss() }
    }
}

suspend fun KDialog.Companion.confirm(title: String, content: String) =
    suspendCoroutine { continuation ->
        KApp.curActivity?.let { activity ->
            val dialog = KConfirmDialog()
            dialog.title = title
            dialog.content = content
            dialog.onCancel = OnClickListener {
                continuation.resumeWithException(IgnoreException())
                dialog.dismiss()
            }
            dialog.onConfirm = OnClickListener {
                continuation.resume(Unit)
                dialog.dismiss()
            }
            dialog.isCancelable = false
            dialog.show(activity, "KDialogConfirm")
            activity.onStopped("KDialogConfirm") {
                continuation.resumeWithException(IgnoreException())
                dialog.dismiss()
            }
        } ?: run {
            continuation.resumeWithException(IgnoreException())
        }
    }

class KFailedDialog : KDialog(
    R.layout.dialog_failed,
    LayoutType.CenterBySize(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
) {
    private var message: String? = null

    companion object {
        @JvmStatic
        fun newInstance(message: String?) = KFailedDialog().apply {
            arguments = Bundle().apply {
                putString("message", message)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            message = getString("message")
        }
    }

    override fun onViewBinding(binding: ViewDataBinding) {
        if (binding !is DialogFailedBinding) return
        message?.let { binding.message.text = it }
    }
}

class KSuccessDialog : KDialog(
    R.layout.dialog_success,
    LayoutType.CenterBySize(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
) {
    private var message: String? = null

    companion object {
        @JvmStatic
        fun newInstance(message: String?) = KSuccessDialog().apply {
            arguments = Bundle().apply {
                putString("message", message)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            message = getString("message")
        }
    }

    override fun onViewBinding(binding: ViewDataBinding) {
        if (binding !is DialogSuccessBinding) return
        message?.let { binding.message.text = it }
    }
}

class KLoadingDialog : KDialog(
    R.layout.dialog_loading,
    LayoutType.CenterBySize(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
) {
    private var message: String? = null

    companion object {
        @JvmStatic
        fun newInstance(message: String?) = KLoadingDialog().apply {
            arguments = Bundle().apply {
                putString("message", message)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            message = getString("message")
        }
    }

    override fun onViewBinding(binding: ViewDataBinding) {
        if (binding !is DialogLoadingBinding) return
        message?.let { binding.message.text = it }
    }
}

class KConfirmDialog : KDialog(
    R.layout.dialog_confirm,
    LayoutType.CenterBySize(KDisPlay.width - 64.dp, ViewGroup.LayoutParams.WRAP_CONTENT)
) {
    var title = ""
    var content = ""
    var onCancel = OnClickListener { dismiss() }
    var onConfirm = OnClickListener { dismiss() }

    override fun onViewBinding(binding: ViewDataBinding) {
        isCancelable = false
        if (binding !is DialogConfirmBinding) return
        binding.title.text = title
        binding.content.text = content
        binding.cancel.setOnClickListener(onCancel)
        binding.confirm.setOnClickListener(onConfirm)
        binding.confirm.setTextColor(requireActivity().primaryColor)
    }

}

abstract class KDialog(
    @LayoutRes protected val layoutResId: Int,
    private val layoutType: LayoutType,
) : DialogFragment() {

    companion object;

    private lateinit var _binding: ViewDataBinding

    private fun applyLayoutParams(window: Window) {
        val layoutParams = window.attributes
        when (layoutType) {
            is LayoutType.CenterBySize -> {
                window.setGravity(Gravity.CENTER)
                layoutParams.width = layoutType.width
                layoutParams.height = layoutType.height
                layoutParams.gravity = Gravity.CENTER
                window.setWindowAnimations(R.style.KDialogStyle_Center)
            }

            is LayoutType.BottomBySize -> {
                window.setGravity(Gravity.BOTTOM)
                layoutParams.width = layoutType.width
                layoutParams.height = layoutType.height
                layoutParams.gravity = Gravity.BOTTOM
                window.setWindowAnimations(R.style.KDialogStyle_Bottom)
            }

            is LayoutType.TopBySize -> {
                window.setGravity(Gravity.TOP)
                layoutParams.width = layoutType.width
                layoutParams.height = layoutType.height
                layoutParams.gravity = Gravity.TOP
                window.setWindowAnimations(R.style.KDialogStyle_Top)
            }

            is LayoutType.CenterByPadding -> {
                window.setGravity(Gravity.CENTER)
                layoutParams.width = KDisPlay.width - layoutType.paddingLeft - layoutType.paddingRight
                layoutParams.height = KDisPlay.height - layoutType.paddingTop - layoutType.paddingBottom
                window.setWindowAnimations(R.style.KDialogStyle_Center)
            }

            is LayoutType.BottomByPadding -> {
                window.setGravity(Gravity.BOTTOM)
                layoutParams.width = KDisPlay.width - layoutType.paddingLeft - layoutType.paddingRight
                layoutParams.height = KDisPlay.height - layoutType.paddingTop - layoutType.paddingBottom
                window.setWindowAnimations(R.style.KDialogStyle_Bottom)
            }

            is LayoutType.TopByPadding -> {
                window.setGravity(Gravity.TOP)
                layoutParams.width = KDisPlay.width - layoutType.paddingLeft - layoutType.paddingRight
                layoutParams.height = KDisPlay.height - layoutType.paddingTop - layoutType.paddingBottom
                window.setWindowAnimations(R.style.KDialogStyle_Top)
            }
        }
        window.attributes = layoutParams
    }

    open fun isFullScreen() = false

    override fun getTheme(): Int {
        return if (isFullScreen()) R.style.KDialogStyle
        else super.getTheme()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DataBindingUtil.inflate(
            inflater,
            layoutResId,
            container,
            false
        )
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val window = dialog?.window ?: return
        window.setDimAmount(0.2f)
        applyLayoutParams(window)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        super.onViewCreated(view, savedInstanceState)
        onViewBinding(_binding)
    }

    abstract fun onViewBinding(binding: ViewDataBinding)

    open fun show(activity: Activity, tag: String = activity.hashCode().toString()) {
        if (activity is AppCompatActivity && activity.lifecycle.currentState == Lifecycle.State.RESUMED) {
            show(activity.supportFragmentManager, tag)
        }
    }


    override fun dismiss() {
        runCatching { super.dismiss() }
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

        data class TopByPadding(
            val paddingTop: Int = 0, val paddingBottom: Int = 0, val paddingLeft: Int = 0, val paddingRight: Int = 0
        ) : LayoutType

        data class TopBySize(
            val width: Int = 0,
            val height: Int = 0,
        ) : LayoutType
    }

}