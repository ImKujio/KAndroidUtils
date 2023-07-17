package me.kujio.android.kandroidutils.simple

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import me.kujio.android.kandroidutils.DisPlay
import me.kujio.android.kandroidutils.KDialog
import me.kujio.android.kandroidutils.simple.databinding.DialogTestBinding

class BottomDialog(activity: AppCompatActivity):KDialog(activity,
    R.layout.dialog_test,
    LayoutType.BottomBySize(DisPlay.width, DisPlay.height / 2)) {
    override fun onViewBinding(binding: ViewDataBinding) {
        if (binding !is DialogTestBinding) return
        binding.title.text = "中心弹窗"
        binding.cancel.text = "取消"
        binding.cancel.setOnClickListener {
            cancelAsync()
        }
    }
}