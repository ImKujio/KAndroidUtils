package me.kujio.android.kandroidutils.simple

import androidx.databinding.ViewDataBinding
import me.kujio.android.kandroidutils.KDialog
import me.kujio.android.kandroidutils.simple.databinding.DialogTestBinding

class BottomDialog: KDialog(
    R.layout.dialog_test,
    LayoutType.BottomByPadding()) {
    override fun onViewBinding(binding: ViewDataBinding) {
        if (binding !is DialogTestBinding) return
        binding.title.text = "中心弹窗"
        binding.cancel.text = "取消"
        binding.cancel.setOnClickListener {
            dismiss()
        }
    }
}