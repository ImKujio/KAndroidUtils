package me.kujio.android.kandroidutils.crash

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.color
import me.kujio.android.kandroidutils.ThemeType
import me.kujio.android.kandroidutils.applyImmersive
import me.kujio.android.kandroidutils.applyTitleBar
import me.kujio.android.kandroidutils.dp

class KCrashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val report = intent.getParcelableExtra<CrashReport>("CrashReport") ?: run {
            Toast.makeText(this, "无奔溃信息", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val reportText = report.toTextReport()
        applyImmersive(ThemeType.LIGHT)
        setContentView(ScrollView(this).apply {
            layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            addView(TextView(this@KCrashActivity).apply {
                setPadding(20.dp, 20.dp, 20.dp, 20.dp)
                setTextColor(Color.parseColor("#7E7E7E"))
                textSize = 14f
                text = SpannableStringBuilder().run {
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
            })
        })
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
    }
}