package me.kujio.android.kandroidutils

import java.io.PrintWriter
import java.io.StringWriter
import java.lang.Exception

inline fun <R> tryRun(action: () -> R) = try {
    action()
} catch (_: Exception) {
    null
}

fun Throwable.report(): String {
   return StringWriter().use { sw ->
        PrintWriter(sw).use pUes@{ pw ->
            printStackTrace(pw)
            pw.flush()
            return@pUes sw.toString()
        }
    }
}