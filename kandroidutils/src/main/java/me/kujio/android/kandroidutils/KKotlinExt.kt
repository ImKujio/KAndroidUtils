package me.kujio.android.kandroidutils

import java.lang.Exception

inline fun <R> tryRun(action: () -> R) = try {
    action()
} catch (_: Exception) {
    null
}

fun <R> ok(r: R) = Rst.Ok(r)

fun <R> err(msg: String, code: Int = Int.MIN_VALUE, exp: Exception? = null) = Rst.Err<R>(msg, code, exp)

sealed class Rst<T> {

    class Err<T>(val msg: String, val code: Int = Int.MIN_VALUE, val exp: Exception? = null) : Rst<T>() {
        fun <R> transfer(): Err<R> {
            return Err(msg, code, exp)
        }
    }

    class Ok<T>(val data: T) : Rst<T>()

    infix fun or(def: T): T {
        return when (this) {
            is Err -> def
            is Ok -> data
        }
    }

    inline infix fun or(action: Err<T>.() -> T): T {
        return when (this) {
            is Err -> action()
            is Ok -> data
        }
    }

}