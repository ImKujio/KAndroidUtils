package me.kujio.android.kandroidutils


val Int.dp: Int; get() = (DisPlay.dpUnit * this).toInt()

val Int.sp: Int; get() = (DisPlay.spUnit * this).toInt()

val Float.dp: Float; get() = DisPlay.dpUnit * this

val Float.sp: Float; get() = DisPlay.spUnit * this
