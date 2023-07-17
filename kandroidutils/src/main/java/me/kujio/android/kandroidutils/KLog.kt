package me.kujio.android.kandroidutils

import android.util.Log

fun loge(simple: Boolean = false, msg: () -> String) {
    if (!KApp.debug) return
    if (simple) logSimple(Log.ERROR,msg())
    else log(Log.ERROR, msg())
}

fun logw(simple: Boolean = false, msg: () -> String) {
    if (!KApp.debug) return
    if (simple) logSimple(Log.ERROR,msg())
    else log(Log.WARN, msg())
}

fun logd(simple: Boolean = false, msg: () -> String) {
    if (!KApp.debug) return
    if (simple) logSimple(Log.ERROR,msg())
    else log(Log.DEBUG, msg())
}

fun logi(simple: Boolean = false, msg: () -> String) {
    if (!KApp.debug) return
    if (simple) logSimple(Log.ERROR,msg())
    else log(Log.INFO, msg())
}

private val packages = HashSet<String>()

fun addLogPackage(pkg: String) {
    packages.add(pkg)
}

private fun logSimple(type: Int, msg: String) {
    val stack = Thread.currentThread().stackTrace
    val tag = "KLog:" + stack[4].className.split(".").last()
    msg.lines().forEach { line ->
        Log.println(type, tag, line)
    }
}

private fun log(type: Int, msg: String) {
    val stack = Thread.currentThread().stackTrace
    val tag = "KLog:" + stack[4].className.split(".").last()
    val size = if (stack.size > 10) 10 else stack.size
    Log.println(
        type, tag, "┌──────────────────────────────────────────────────────────────────────────────────────────────────"
    )
    (size - 1 downTo 4).forEach print@{ i ->
        val e = stack[i]
        if (e.isNativeMethod) return@print
        if (e.className.contains(KApp.packageName)) {
            Log.println(type, tag, "\n│${e.className}.${e.methodName}:(${e.fileName}:${e.lineNumber})")
            return@print
        }
        packages.forEach { pkg ->
            if (e.className.contains(pkg)) {
                Log.println(type, tag, "\n│${e.className}.${e.methodName}:(${e.fileName}:${e.lineNumber})")
                return@print
            }
        }
    }
    Log.println(
        type, tag, "├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄"
    )
    msg.lines().forEach { line ->
        Log.println(type, tag, "│$line")
    }
    Log.println(
        type, tag, "└──────────────────────────────────────────────────────────────────────────────────────────────────"
    )
}