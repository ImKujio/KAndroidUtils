package me.kujio.android.kandroidutils.simple

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineExceptionHandler
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
                val dialog = CenterDialog()
                dialog.show(this@MainActivity)
            }
        }
        binding.dialogBottom.setOnClickListener {
            lifecycleScope.launch {
                val dialog = BottomDialog()
                dialog.show(this@MainActivity)
            }
        }
        binding.crashCatch.setOnClickListener {
            throw Exception("测试异常捕获")
        }

        binding.constraintLoad.setOnClickListener {
            startActivity(Intent(this, ConstraintLoadActivity::class.java))
        }

        binding.frameLoad.setOnClickListener {
            startActivity(Intent(this, FrameLayoutLoadActivity::class.java))
        }

        binding.linearLoad.setOnClickListener {
            startActivity(Intent(this, LinearLayoutLoadActivity::class.java))
        }

        binding.dialogConfirm.setOnClickListener {
            lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
                if (throwable is CancelExcpetion) return@CoroutineExceptionHandler
                throwable.printStackTrace()
            }) {
                KDialog.confirm("确认弹窗", "是否确认？")
                KDialog.success("确认完成")
            }
        }

        logd { KStore.cacheDir.absolutePath }
        logd { KStore.filesDir.absolutePath }
        binding.adapter = kMoreAdapter
        binding.refresh.setOnClickListener {
            list.clear()
            page = 0
            kMoreAdapter.notifyReset()
        }

        binding.dialogLoading.setOnClickListener {
            KDialog.loading()
            Handler(mainLooper).postDelayed({
                KDialog.dismissLoading()
                KDialog.failed("加载失败")

                Handler(mainLooper).postDelayed({
                    KDialog.loading()

                    Handler(mainLooper).postDelayed({
                        KDialog.dismissLoading()
                        KDialog.success("加载成功")
                    }, 3000)
                }, 2000)
            }, 3000)
        }
    }

    private val list = ArrayList<String>()
    private var page = 0

    private suspend fun load() {
        delay(1000 * 3)
        repeat(10) {
            list.add(Math.random().toString())
        }
        kMoreAdapter.notifyAppend()
    }

    private val kMoreAdapter = KMoreRecyclerAdapter(
        resId = { R.layout.item_main to 12 },
        count = { list.size },
        load = {
            page++
            load()
        },
        more = { list.size < 35 }
    ) { _, binding, pos ->
        if (binding !is ItemMainBinding) return@KMoreRecyclerAdapter
        binding.text = list[pos]
    }


    val kAdapter = KRecyclerAdapter(
        resId = { R.layout.item_main to 12 },
        count = { 50 }
    ) { _, binding, pos ->
        if (binding !is ItemMainBinding) return@KRecyclerAdapter
        binding.text = Math.random().toString()
    }

    override fun onResume() {
        super.onResume()
        logd { "onResume" }
    }
}