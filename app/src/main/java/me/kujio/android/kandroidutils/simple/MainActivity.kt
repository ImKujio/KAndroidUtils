package me.kujio.android.kandroidutils.simple

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import me.kujio.android.kandroidutils.*
import me.kujio.android.kandroidutils.simple.databinding.ActivityMainBinding
import me.kujio.android.kandroidutils.simple.databinding.DialogTestBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyImmersive(ThemeType.LIGHT)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        logd("测试logd")
        loge("测试loge")
        applyTitleBar {
            title("标题", Gravity.START)
            txtBtn("按钮") {

            }
        }
        logd(Any().hashCode().toString())
        logd(Any().hashCode().toString())
        binding.dialogCenter.setOnClickListener {
            KDialog(
                this,
                R.layout.dialog_test,
                KDialog.LayoutType.CenterByPadding(DisPlay.height / 4, DisPlay.height / 4, 24.dp, 24.dp)
            ) { binding ->
                if (binding !is DialogTestBinding) return@KDialog
                binding.title.text = "中心弹窗"
                binding.cancel.text = "取消"
                binding.cancel.setOnClickListener {
                    cancel()
                }
            }.show()
        }
        binding.dialogBottom.setOnClickListener {
            KDialog(
                this,
                R.layout.dialog_test,
                KDialog.LayoutType.BottomBySize(DisPlay.width, DisPlay.height / 2)
            ) { binding ->
                if (binding !is DialogTestBinding) return@KDialog
                binding.title.text = "中心弹窗"
                binding.cancel.text = "取消"
                binding.cancel.setOnClickListener {
                    cancel()
                }
            }.show()
        }
        binding.crashCatch.setOnClickListener {
            throw Exception("测试异常捕获")
        }
        logd(DataStore.cacheDir.absolutePath)
        logd(DataStore.filesDir.absolutePath)
    }

    override fun onResume() {
        super.onResume()
        logd("onResume")
    }

    override fun onBackPressed() {
        if (cancelKDialog()) return
        super.onBackPressed()
    }
}