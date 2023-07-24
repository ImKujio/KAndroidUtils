package me.kujio.android.kandroidutils.crash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.os.Process
import kotlinx.parcelize.Parcelize
import me.kujio.android.kandroidutils.*
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("StaticFieldLeak")
object CrashCatcher : Thread.UncaughtExceptionHandler {
    private lateinit var context: Context

    override fun uncaughtException(t: Thread, e: Throwable) {
        loge(true){e.report()}
        Intent(context, KCrashActivity::class.java).apply {
            var exp: Throwable = e
            while (exp.cause != null) {
                exp = exp.cause!!
            }
            putExtra("CrashReport", CrashReport.parse(exp))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(this)
        }
        killApp()
    }

    private fun killApp() {
        Process.killProcess(Process.myPid())
    }

    fun init(ctx: Context) {
        context = ctx
        Thread.setDefaultUncaughtExceptionHandler(this)
    }
}

@Parcelize
data class CrashReport(
    val expMsg: String,
    var packageName: String,
    val className: String,
    val funName: String,
    val lineNum: String,
    val expType: String,
    val expTime: String,
    val deviceName: String,
    val brandName: String,
    val androidVer: String,
    val cpuABI: String,
    val versionCode: String,
    val versionName: String,
    val stackTrace: String
) : Parcelable {
    companion object {
        fun parse(e: Throwable): CrashReport {
            val ele = e.stackTrace.firstOrNull { it.className.contains(KApp.packageName) } ?: run {
                e.stackTrace[0]
            }
            val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault()).format(Date())
            return CrashReport(
                expMsg = e.message ?: "未知",
                packageName = KApp.packageName,
                className = ele.className,
                funName = ele.methodName,
                lineNum = ele.lineNumber.toString(),
                expType = e::class.java.name,
                expTime = time,
                deviceName = KDevice.name,
                brandName = KDevice.brand,
                androidVer = KDevice.androidVerName,
                cpuABI = KDevice.cpuAbi,
                versionCode = KApp.versionCode.toString(),
                versionName = KApp.versionName,
                stackTrace = e.report()
            )
        }
    }

    fun toTextReport(): String {
        return StringBuilder().apply {
            append("异常：").append(expMsg).append("\n")
            append("包名：").append(packageName).append("\n")
            append("类名：").append(className).append("\n")
            append("方法：").append(funName).append("\n")
            append("行数：").append(lineNum).append("\n")
            append("类型：").append(expType).append("\n")
            append("时间：").append(expTime).append("\n")
            append("版本号：").append(versionCode).append("\n")
            append("版本名：").append(versionName).append("\n")
            append("设备名称：").append(deviceName).append("\n")
            append("设备品牌：").append(brandName).append("\n")
            append("安卓版本：").append(androidVer).append("\n")
            append("CPU-ABI：").append(cpuABI).append("\n")
            append("异常调用栈：").append("\n").append(stackTrace)
        }.toString()
    }
}