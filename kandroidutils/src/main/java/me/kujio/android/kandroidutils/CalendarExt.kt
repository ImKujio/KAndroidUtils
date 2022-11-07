package me.kujio.android.kandroidutils

import java.text.SimpleDateFormat
import java.util.*


/**
yyyy：年
MM：月
dd：日
hh：1~12小时制(1-12)
HH：24小时制(0-23)
mm：分
ss：秒
S：毫秒
E：星期几
D：一年中的第几天
F：一月中的第几个星期(会把这个月总共过的天数除以7)
w：一年中的第几个星期
W：一月中的第几星期(会根据实际情况来算)
a：上下午标识
k：和HH差不多，表示一天24小时制(1-24)。
K：和hh差不多，表示一天12小时制(0-11)。
z：表示时区
 */


fun Calendar.format(format: String = "yyyy-MM-dd"): String {
    val sdf = SimpleDateFormat(format, Locale.CHINA)
    return sdf.format(Date(this.timeInMillis))
}

infix fun Calendar.difDays(endCalendar: Calendar): Int {
    return ((endCalendar.timeInMillis - this.timeInMillis) / (24 * 60 * 60 * 1000) + 1).toInt()
}

infix fun Calendar.difMonths(endCalendar: Calendar): Int {
    return (endCalendar[Calendar.YEAR] - this[Calendar.YEAR]) * 12 + (endCalendar[Calendar.MONTH] - this[Calendar.MONTH]) + 1
}

