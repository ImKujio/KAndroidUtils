package me.kujio.android.kandroidutils

import java.io.PrintWriter
import java.io.StringWriter

fun Throwable.report(): String {
    return StringWriter().use { sw ->
        PrintWriter(sw).use pUes@{ pw ->
            printStackTrace(pw)
            pw.flush()
            return@pUes sw.toString()
        }
    }
}

class IgnoreException : RuntimeException("此异常为忽略异常，用来取消操作，务必捕获该异常")

