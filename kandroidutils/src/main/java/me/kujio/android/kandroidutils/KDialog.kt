package me.kujio.android.kandroidutils

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment

abstract class KDialog(
    @LayoutRes protected val layoutResId: Int,
    private val layoutType: LayoutType,
) : DialogFragment() {

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

            is LayoutType.CenterByPadding -> {
                window.setGravity(Gravity.CENTER)
                layoutParams.width = DisPlay.width - layoutType.paddingLeft - layoutType.paddingRight
                layoutParams.height = DisPlay.height - layoutType.paddingTop - layoutType.paddingBottom
                window.setWindowAnimations(R.style.KDialogStyle_Center)
            }

            is LayoutType.BottomByPadding -> {
                window.setGravity(Gravity.BOTTOM)
                layoutParams.width = DisPlay.width - layoutType.paddingLeft - layoutType.paddingRight
                layoutParams.height = DisPlay.height - layoutType.paddingTop - layoutType.paddingBottom
                window.setWindowAnimations(R.style.KDialogStyle_Bottom)
            }
        }
        window.attributes = layoutParams
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DataBindingUtil.inflate(inflater, layoutResId, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val window = dialog?.window ?: return
        window.setDimAmount(0.2f)
        applyLayoutParams(window)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        onViewBinding(_binding)
    }

    abstract fun onViewBinding(binding: ViewDataBinding)

    open fun show(activity: Activity,tag:String = activity.hashCode().toString()){
        if (activity is AppCompatActivity){
            show(activity.supportFragmentManager,tag)
        }
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