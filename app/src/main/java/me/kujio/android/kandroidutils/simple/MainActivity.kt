package me.kujio.android.kandroidutils.simple

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.kujio.android.kandroidutils.*
import me.kujio.android.kandroidutils.simple.databinding.ActivityMainBinding
import me.kujio.android.kandroidutils.simple.databinding.ItemMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyImmersive(ThemeType.LIGHT)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lifecycleOwner = this
        logw { "测试logw" }
        loge { "测试loge" }
        applyTitleBar {
            title("标题", Gravity.START)
            txtBtn("按钮") {

            }
        }
        logd { Any().hashCode().toString() }
        logd { Any().hashCode().toString() }
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

        binding.constraintLoad.setOnClickListener {
            startActivity(Intent(this,ConstraintLoadActivity::class.java))
        }

        logd { KStore.cacheDir.absolutePath }
        logd { KStore.filesDir.absolutePath }
        binding.adapter = loadMoreRecyclerAdapter
        binding.refresh.setOnClickListener {
            binding.adapter?.notifyDataSetChanged()
        }
    }

    private val list = ArrayList<String>()
    private var page = 0

    private suspend fun load() {
        delay(1000 * 3)
        repeat(10) {
            list.add(Math.random().toString())
        }
        loadMoreRecyclerAdapter.notifyDataSetChanged()
    }

    val loadMoreRecyclerAdapter = LoadMoreRecyclerAdapter(
        resId = { R.layout.item_main },
        count = { list.size },
        load = suspend { page++;load() },
        more = { list.size < 35 }
    ) { _, binding, pos ->
        if (binding !is ItemMainBinding) return@LoadMoreRecyclerAdapter
        binding.text = list[pos]
    }

    val simpleRecyclerAdapter = SimpleRecyclerAdapter(
        resId = { R.layout.item_main },
        count = { 50 }
    ) { _, binding, pos ->
        if (binding !is ItemMainBinding) return@SimpleRecyclerAdapter
        binding.text = Math.random().toString()
    }

    override fun onResume() {
        super.onResume()
        logd { "onResume" }
    }
}