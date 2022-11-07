package me.kujio.android.kandroidutils

import android.util.Log

fun loge(msg: String) {
    log(Log.ERROR, msg)
}

fun logw(msg: String) {
    log(Log.WARN, msg)
}

fun logd(msg: String) {
    if (!App.debug) return
    log(Log.DEBUG, msg)
}

private fun log(type: Int, msg: String) {
    val stack = Thread.currentThread().stackTrace
    val tag = stack[4].className.split(".").last()
    StringBuilder().apply {
        append("┌──────────────────────────────────────────────────────────────────────────────────────────────────")
        val size = if (stack.size < 8) stack.size - 4 else 5
        repeat(size) { i ->
            val e = stack[size + 3 - i]
            append("\n│").append(e.className).append(".").append(e.methodName)
            append(":").append("(").append(e.fileName)
            append(":").append(e.lineNumber).append(")")
        }
        append("\n")
        append("├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄")
        append("\n")
        append("│").append(msg.replace("\\n".toRegex(), "\n│")).append("\n")
        append("└──────────────────────────────────────────────────────────────────────────────────────────────────")
    }.lines().forEach {
        Log.println(type, tag, it)
    }
}