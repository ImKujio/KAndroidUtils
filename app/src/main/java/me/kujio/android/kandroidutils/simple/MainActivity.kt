package me.kujio.android.kandroidutils.simple

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.kujio.android.kandroidutils.*
import me.kujio.android.kandroidutils.simple.databinding.ActivityMainBinding
import me.kujio.android.kandroidutils.simple.databinding.ItemMainBinding
import me.kujio.android.kandroidutils.view.SimpleRecyclerAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyImmersive(ThemeType.LIGHT)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lifecycleOwner = this
        logw{"测试logw"}
        loge{"测试loge"}
        applyTitleBar {
            title("标题", Gravity.START)
            txtBtn("按钮") {

            }
        }
        logd{Any().hashCode().toString()}
        logd{Any().hashCode().toString()}
        binding.dialogCenter.setOnClickListener {
            lifecycleScope.launch {
                val dialog = CenterDialog(this@MainActivity)
                dialog.show()
            }
        }
        binding.dialogBottom.setOnClickListener {
            lifecycleScope.launch {
                val dialog = BottomDialog(this@MainActivity)
                dialog.show()
            }
        }
        binding.crashCatch.setOnClickListener {
            throw Exception("测试异常捕获")
        }
        logd{KStore.cacheDir.absolutePath}
        logd{KStore.filesDir.absolutePath}
        binding.adapter = SimpleRecyclerAdapter(
            resId = { R.layout.item_main },
            count = { 50 }
        ) { _, binding, pos ->
            if (binding !is ItemMainBinding) return@SimpleRecyclerAdapter
            binding.text = Math.random().toString()
        }
        binding.refresh.setOnClickListener {
            binding.adapter?.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        logd{"onResume"}
    }
}