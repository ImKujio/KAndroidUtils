package me.kujio.android.kandroidutils

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.color
import androidx.databinding.DataBindingUtil

class CrashCatcherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val report = intent.getParcelableExtra<CrashReport>("CrashReport") ?: run {
            Toast.makeText(this, "无奔溃信息", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val reportText = report.toTextReport()
        applyImmersive(ThemeType.LIGHT)
        setContentView(R.layout.activity_crash_catcher)
        applyTitleBar {
            title("似乎发生了异常", Gravity.START, color = Color.RED)
            txtBtn("报告给开发者") {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, "应用异常信息: ")
                intent.putExtra(Intent.EXTRA_TEXT, reportText)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(Intent.createChooser(intent, "报告给："))
            }
        }
        findViewById<TextView>(R.id.info).text = SpannableStringBuilder().run {
            val valueColor = Color.parseColor("#C15151")
            reportText.lines().forEach { line ->
                val index = line.indexOf("：")
                val name = if (index == -1) "" else line.substring(0, index + 1)
                val value = if (index == -1) line else line.substring(index + 1)
                append(name)
                color(valueColor) {
                    append(value)
                }
                append("\n")
            }
            SpannableString.valueOf(this)
        }

    }
}