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

class CancelExcpetion(msg:String? = null) : RuntimeException(msg)