package com.lixiaoyun.aike.utils

import androidx.annotation.IntDef
import androidx.annotation.NonNull
import java.net.URL
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日期时间相关
 *
 * millis2String           : 将时间戳转为时间字符串
 * string2Millis           : 将时间字符串转为时间戳
 * string2Date             : 将时间字符串转为 Date 类型
 * date2String             : 将 Date 类型转为时间字符串
 * date2Millis             : 将 Date 类型转为时间戳
 * millis2Date             : 将时间戳转为 Date 类型
 * getTimeSpan             : 获取两个时间差（单位：unit）
 * getFitTimeSpan          : 获取合适型两个时间差
 * getNowMills             : 获取当前毫秒时间戳
 * getNowString            : 获取当前时间字符串
 * getNowDate              : 获取当前 Date
 * getTimeSpanByNow        : 获取与当前时间的差（单位：unit）
 * getFitTimeSpanByNow     : 获取合适型与当前时间的差
 * getFriendlyTimeSpanByNow: 获取友好型与当前时间的差
 * getMillis               : 获取与给定时间等于时间差的时间戳
 * getString               : 获取与给定时间等于时间差的时间字符串
 * getDate                 : 获取与给定时间等于时间差的 Date
 * getMillisByNow          : 获取与当前时间等于时间差的时间戳
 * getStringByNow          : 获取与当前时间等于时间差的时间字符串
 * getDateByNow            : 获取与当前时间等于时间差的 Date
 * isToday                 : 判断是否今天
 * getChineseWeek          : 获取中式星期
 * getUSWeek               : 获取美式式星期
 * getValueByCalendarField : 根据日历字段获取值
 * getNetTime              : 获取网络时间
 */
class DateUtils private constructor() {
    companion object {
        val instance = SingletonHolder.holder

        //毫秒
        const val MILLISECOND = 1
        //秒
        const val SECOND = 1000
        //分
        const val MINUTE = 60000
        //时
        const val HOUR = 3600000
        //天
        const val DAY = 86400000

        const val FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss"
        const val FORMAT_NO_SECOND = "yyyy-MM-dd HH:mm"
        const val FORMAT_UNDERLINED = "yyyy-MM-dd_HH-mm-ss"
        const val FORMAT_NO_MODIFICATION = "yyyyMMddHHmmss"
        const val FORMAT_CN = "yyyy年MM月dd日 HH:mm:ss"
        const val FORMAT_CN_NO_SECONDS = "yyyy年MM月dd日 HH:mm"
        const val FORMAT_T = "yyyy-MM-dd'T'HH:mm:ss"
        const val FORMAT_DAY = "yyyy年MM月dd日"
    }

    private object SingletonHolder {
        val holder = DateUtils()
    }

    @IntDef(MILLISECOND, SECOND, MINUTE, HOUR, DAY)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Unit

    private val SDF_THREAD_LOCAL = ThreadLocal<SimpleDateFormat>()

    fun getSimpleDateFormat(format: String = FORMAT_DEFAULT): SimpleDateFormat {
        var simpleDateFormat: SimpleDateFormat? = SDF_THREAD_LOCAL.get()
        if (simpleDateFormat == null || !format.isSame(FORMAT_DEFAULT)) {
            SDF_THREAD_LOCAL.remove()
            simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
            SDF_THREAD_LOCAL.set(simpleDateFormat)
        }
        return simpleDateFormat
    }

    /**
     * Milliseconds to the formatted time string.
     *
     * The pattern is `yyyy-MM-dd HH:mm:ss`.
     *
     * @param millis The milliseconds.
     * @return the formatted time string
     */
    fun millis2String(millis: Long): String {
        return millis2String(millis, getSimpleDateFormat())
    }

    /**
     * Milliseconds to the formatted time string.
     *
     * The pattern is `yyyy-MM-dd HH:mm:ss`.
     *
     * @param millis The milliseconds.
     * @param format The format.
     * @return the formatted time string
     */
    fun millis2String(millis: Long, @NonNull format: String): String {
        return millis2String(millis, getSimpleDateFormat(format))
    }

    /**
     * Milliseconds to the formatted time string.
     *
     * @param millis The milliseconds.
     * @param format The format.
     * @return the formatted time string
     */
    fun millis2String(millis: Long, @NonNull format: DateFormat): String {
        return format.format(Date(millis))
    }

    /**
     * Formatted time string to the milliseconds.
     *
     * The pattern is `yyyy-MM-dd HH:mm:ss`.
     *
     * @param time The formatted time string.
     * @return the milliseconds
     */
    fun string2Millis(time: String): Long {
        return string2Millis(time, getSimpleDateFormat())
    }

    /**
     * Formatted time string to the milliseconds.
     *
     * @param time   The formatted time string.
     * @param format The format.
     * @return the milliseconds
     */
    fun string2Millis(time: String, @NonNull format: DateFormat): Long {
        try {
            return format.parse(time).time
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return -1
    }

    /**
     * Formatted time string to the date.
     *
     * The pattern is `yyyy-MM-dd HH:mm:ss`.
     *
     * @param time The formatted time string.
     * @return the date
     */
    fun string2Date(time: String): Date? {
        return string2Date(time, getSimpleDateFormat())
    }

    /**
     * Formatted time string to the date.
     *
     * @param time   The formatted time string.
     * @param format The format.
     * @return the date
     */
    fun string2Date(time: String, @NonNull format: DateFormat): Date? {
        try {
            return format.parse(time)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * Formatted time string to the date.
     *
     * @param time   The formatted time string.
     * @param format The format.
     * @return the date
     */
    fun string2Date(time: String, @NonNull format: String): Date? {
        try {
            val dateFormat = getSimpleDateFormat(format)
            return dateFormat.parse(time)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * Date to the formatted time string.
     *
     * The pattern is `yyyy-MM-dd HH:mm:ss`.
     *
     * @param date The date.
     * @return the formatted time string
     */
    fun date2String(date: Date): String {
        return date2String(date, getSimpleDateFormat())
    }

    /**
     * Date to the formatted time string.
     *
     * @param date   The date.
     * @param format The format.
     * @return the formatted time string
     */
    fun date2String(date: Date, @NonNull format: DateFormat): String {
        return format.format(date)
    }

    /**
     * Date to the milliseconds.
     *
     * @param date The date.
     * @return the milliseconds
     */
    fun date2Millis(date: Date): Long {
        return date.time
    }

    /**
     * Milliseconds to the date.
     *
     * @param millis The milliseconds.
     * @return the date
     */
    fun millis2Date(millis: Long): Date {
        return Date(millis)
    }

    /**
     * Return the time span, in unit.
     *
     * The pattern is `yyyy-MM-dd HH:mm:ss`.
     *
     * @param time1 The first formatted time string.
     * @param time2 The second formatted time string.
     * @param unit  The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the time span, in unit
     */
    fun getTimeSpan(time1: String,
                    time2: String,
                    @Unit unit: Int): Long {
        return getTimeSpan(time1, time2, getSimpleDateFormat(), unit)
    }

    /**
     * Return the time span, in unit.
     *
     * @param time1  The first formatted time string.
     * @param time2  The second formatted time string.
     * @param format The format.
     * @param unit   The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the time span, in unit
     */
    fun getTimeSpan(time1: String,
                    time2: String,
                    @NonNull format: DateFormat,
                    @Unit unit: Int): Long {
        return millis2TimeSpan(string2Millis(time1, format) - string2Millis(time2, format), unit)
    }

    /**
     * Return the time span, in unit.
     *
     * @param date1 The first date.
     * @param date2 The second date.
     * @param unit  The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the time span, in unit
     */
    fun getTimeSpan(date1: Date,
                    date2: Date,
                    @Unit unit: Int): Long {
        return millis2TimeSpan(date2Millis(date1) - date2Millis(date2), unit)
    }

    /**
     * Return the time span, in unit.
     *
     * @param millis1 The first milliseconds.
     * @param millis2 The second milliseconds.
     * @param unit    The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the time span, in unit
     */
    fun getTimeSpan(millis1: Long,
                    millis2: Long,
                    @Unit unit: Int): Long {
        return millis2TimeSpan(millis1 - millis2, unit)
    }

    /**
     * Return the fit time span.
     *
     * The pattern is `yyyy-MM-dd HH:mm:ss`.
     *
     * @param time1     The first formatted time string.
     * @param time2     The second formatted time string.
     * @param precision The precision of time span.
     *
     *  * precision = 0, return null
     *  * precision = 1, return 天
     *  * precision = 2, return 天, 小时
     *  * precision = 3, return 天, 小时, 分钟
     *  * precision = 4, return 天, 小时, 分钟, 秒
     *  * precision &gt;= 5，return 天, 小时, 分钟, 秒, 毫秒
     *
     * @return the fit time span
     */
    fun getFitTimeSpan(time1: String,
                       time2: String,
                       precision: Int): String? {
        val delta = string2Millis(time1, getSimpleDateFormat()) - string2Millis(time2, getSimpleDateFormat())
        return millis2FitTimeSpan(delta, precision)
    }

    /**
     * Return the fit time span.
     *
     * @param time1     The first formatted time string.
     * @param time2     The second formatted time string.
     * @param format    The format.
     * @param precision The precision of time span.
     *
     *  * precision = 0, return null
     *  * precision = 1, return 天
     *  * precision = 2, return 天, 小时
     *  * precision = 3, return 天, 小时, 分钟
     *  * precision = 4, return 天, 小时, 分钟, 秒
     *  * precision &gt;= 5，return 天, 小时, 分钟, 秒, 毫秒
     *
     * @return the fit time span
     */
    fun getFitTimeSpan(time1: String,
                       time2: String,
                       @NonNull format: DateFormat,
                       precision: Int): String? {
        val delta = string2Millis(time1, format) - string2Millis(time2, format)
        return millis2FitTimeSpan(delta, precision)
    }

    /**
     * Return the fit time span.
     *
     * @param date1     The first date.
     * @param date2     The second date.
     * @param precision The precision of time span.
     *
     *  * precision = 0, return null
     *  * precision = 1, return 天
     *  * precision = 2, return 天, 小时
     *  * precision = 3, return 天, 小时, 分钟
     *  * precision = 4, return 天, 小时, 分钟, 秒
     *  * precision &gt;= 5，return 天, 小时, 分钟, 秒, 毫秒
     *
     * @return the fit time span
     */
    fun getFitTimeSpan(date1: Date, date2: Date, precision: Int): String? {
        return millis2FitTimeSpan(date2Millis(date1) - date2Millis(date2), precision)
    }

    /**
     * Return the fit time span.
     *
     * @param millis1   The first milliseconds.
     * @param millis2   The second milliseconds.
     * @param precision The precision of time span.
     *
     *  * precision = 0, return null
     *  * precision = 1, return 天
     *  * precision = 2, return 天, 小时
     *  * precision = 3, return 天, 小时, 分钟
     *  * precision = 4, return 天, 小时, 分钟, 秒
     *  * precision &gt;= 5，return 天, 小时, 分钟, 秒, 毫秒
     *
     * @return the fit time span
     */
    fun getFitTimeSpan(millis1: Long, millis2: Long, precision: Int): String? =
            millis2FitTimeSpan(millis1 - millis2, precision)

    /**
     * Return the current time in milliseconds.
     *
     * @return the current time in milliseconds
     */
    fun getNowMills(): Long = System.currentTimeMillis()

    /**
     * Return the current time in seconds.
     *
     * @return the current time in seconds
     */
    fun getNowSeconds(): Long {
        val simpleDateFormat = getSimpleDateFormat()
        return string2Millis(getNowString(simpleDateFormat))
    }

    /**
     * Return the current formatted time string.
     *
     * The pattern is `yyyy-MM-dd HH:mm:ss`.
     *
     * @return the current formatted time string
     */
    fun getNowString(): String = millis2String(System.currentTimeMillis(), getSimpleDateFormat())

    /**
     * Return the current formatted time string.
     *
     * @param format The format.
     * @return the current formatted time string
     */
    fun getNowString(@NonNull format: DateFormat): String = millis2String(System.currentTimeMillis(), format)

    /**
     * Return the current formatted time string.
     *
     * @param format The format.
     * @return the current formatted time string
     */
    fun getNowString(@NonNull format: String): String {
        val simpleDateFormat = getSimpleDateFormat(format)
        return getNowString(simpleDateFormat)
    }

    /**
     * Return the current date.
     *
     * @return the current date
     */
    fun getNowDate(): Date {
        return Date()
    }

    /**
     * Return the time span by now, in unit.
     *
     * The pattern is `yyyy-MM-dd HH:mm:ss`.
     *
     * @param time The formatted time string.
     * @param unit The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the time span by now, in unit
     */
    fun getTimeSpanByNow(time: String, @Unit unit: Int): Long {
        return getTimeSpan(time, getNowString(), getSimpleDateFormat(), unit)
    }

    /**
     * Return the time span by now, in unit.
     *
     * @param time   The formatted time string.
     * @param format The format.
     * @param unit   The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the time span by now, in unit
     */
    fun getTimeSpanByNow(time: String,
                         @NonNull format: DateFormat,
                         @Unit unit: Int): Long {
        return getTimeSpan(time, getNowString(format), format, unit)
    }

    /**
     * Return the time span by now, in unit.
     *
     * @param date The date.
     * @param unit The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the time span by now, in unit
     */
    fun getTimeSpanByNow(date: Date, @Unit unit: Int): Long {
        return getTimeSpan(date, Date(), unit)
    }

    /**
     * Return the time span by now, in unit.
     *
     * @param millis The milliseconds.
     * @param unit   The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the time span by now, in unit
     */
    fun getTimeSpanByNow(millis: Long, @Unit unit: Int): Long {
        return getTimeSpan(millis, System.currentTimeMillis(), unit)
    }

    /**
     * Return the fit time span by now.
     *
     * The pattern is `yyyy-MM-dd HH:mm:ss`.
     *
     * @param time      The formatted time string.
     * @param precision The precision of time span.
     *
     *  * precision = 0，返回 null
     *  * precision = 1，返回天
     *  * precision = 2，返回天和小时
     *  * precision = 3，返回天、小时和分钟
     *  * precision = 4，返回天、小时、分钟和秒
     *  * precision &gt;= 5，返回天、小时、分钟、秒和毫秒
     *
     * @return the fit time span by now
     */
    fun getFitTimeSpanByNow(time: String, precision: Int): String? {
        return getFitTimeSpan(time, getNowString(), getSimpleDateFormat(), precision)
    }

    /**
     * Return the fit time span by now.
     *
     * @param time      The formatted time string.
     * @param format    The format.
     * @param precision The precision of time span.
     *
     *  * precision = 0，返回 null
     *  * precision = 1，返回天
     *  * precision = 2，返回天和小时
     *  * precision = 3，返回天、小时和分钟
     *  * precision = 4，返回天、小时、分钟和秒
     *  * precision &gt;= 5，返回天、小时、分钟、秒和毫秒
     *
     * @return the fit time span by now
     */
    fun getFitTimeSpanByNow(time: String,
                            @NonNull format: DateFormat,
                            precision: Int): String? {
        return getFitTimeSpan(time, getNowString(format), format, precision)
    }

    /**
     * Return the fit time span by now.
     *
     * @param date      The date.
     * @param precision The precision of time span.
     *
     *  * precision = 0，返回 null
     *  * precision = 1，返回天
     *  * precision = 2，返回天和小时
     *  * precision = 3，返回天、小时和分钟
     *  * precision = 4，返回天、小时、分钟和秒
     *  * precision &gt;= 5，返回天、小时、分钟、秒和毫秒
     *
     * @return the fit time span by now
     */
    fun getFitTimeSpanByNow(date: Date, precision: Int): String? {
        return getFitTimeSpan(date, getNowDate(), precision)
    }

    /**
     * Return the fit time span by now.
     *
     * @param millis    The milliseconds.
     * @param precision The precision of time span.
     *
     *  * precision = 0，返回 null
     *  * precision = 1，返回天
     *  * precision = 2，返回天和小时
     *  * precision = 3，返回天、小时和分钟
     *  * precision = 4，返回天、小时、分钟和秒
     *  * precision &gt;= 5，返回天、小时、分钟、秒和毫秒
     *
     * @return the fit time span by now
     */
    fun getFitTimeSpanByNow(millis: Long, precision: Int): String? {
        return getFitTimeSpan(millis, System.currentTimeMillis(), precision)
    }

    /**
     * Return the friendly time span by now.
     *
     * The pattern is `yyyy-MM-dd HH:mm:ss`.
     *
     * @param time The formatted time string.
     * @return the friendly time span by now
     *
     *  * 如果小于 1 秒钟内，显示刚刚
     *  * 如果在 1 分钟内，显示 XXX秒前
     *  * 如果在 1 小时内，显示 XXX分钟前
     *  * 如果在 1 小时外的今天内，显示今天15:32
     *  * 如果是昨天的，显示昨天15:32
     *  * 其余显示，2016-10-15
     *  * 时间不合法的情况全部日期和时间信息，如星期六 十月 27 14:21:20 CST 2007
     *
     */
    fun getFriendlyTimeSpanByNow(time: String): String {
        return getFriendlyTimeSpanByNow(time, getSimpleDateFormat())
    }

    /**
     * Return the friendly time span by now.
     *
     * @param time   The formatted time string.
     * @param format The format.
     * @return the friendly time span by now
     *
     *  * 如果小于 1 秒钟内，显示刚刚
     *  * 如果在 1 分钟内，显示 XXX秒前
     *  * 如果在 1 小时内，显示 XXX分钟前
     *  * 如果在 1 小时外的今天内，显示今天15:32
     *  * 如果是昨天的，显示昨天15:32
     *  * 其余显示，2016-10-15
     *  * 时间不合法的情况全部日期和时间信息，如星期六 十月 27 14:21:20 CST 2007
     *
     */
    fun getFriendlyTimeSpanByNow(time: String,
                                 @NonNull format: DateFormat): String {
        return getFriendlyTimeSpanByNow(string2Millis(time, format))
    }

    /**
     * Return the friendly time span by now.
     *
     * @param date The date.
     * @return the friendly time span by now
     *
     *  * 如果小于 1 秒钟内，显示刚刚
     *  * 如果在 1 分钟内，显示 XXX秒前
     *  * 如果在 1 小时内，显示 XXX分钟前
     *  * 如果在 1 小时外的今天内，显示今天15:32
     *  * 如果是昨天的，显示昨天15:32
     *  * 其余显示，2016-10-15
     *  * 时间不合法的情况全部日期和时间信息，如星期六 十月 27 14:21:20 CST 2007
     *
     */
    fun getFriendlyTimeSpanByNow(date: Date): String {
        return getFriendlyTimeSpanByNow(date.time)
    }

    /**
     * Return the friendly time span by now.
     *
     * @param millis The milliseconds.
     * @return the friendly time span by now
     *
     *  * 如果小于 1 秒钟内，显示刚刚
     *  * 如果在 1 分钟内，显示 XXX秒前
     *  * 如果在 1 小时内，显示 XXX分钟前
     *  * 如果在 1 小时外的今天内，显示今天15:32
     *  * 如果是昨天的，显示昨天15:32
     *  * 其余显示，2016-10-15
     *  * 时间不合法的情况全部日期和时间信息，如星期六 十月 27 14:21:20 CST 2007
     *
     */
    fun getFriendlyTimeSpanByNow(millis: Long): String {
        val now = System.currentTimeMillis()
        val span = now - millis

        if (span < 0) {
            return String.format("%tc", millis)
        }

        return when {
            (span < 1000) -> "刚刚"
            (span < MINUTE) -> String.format(Locale.getDefault(), "%d秒前", span / SECOND)
            (span < HOUR) -> String.format(Locale.getDefault(), "%d分钟前", span / MINUTE)

            // 获取当天 00:00
            else -> {
                val wee = getWeeOfToday()
                when {
                    (millis >= wee) -> String.format("今天%tR", millis)
                    (millis >= (wee - DAY)) -> String.format("昨天%tR", millis)
                    else -> String.format("%tF", millis)
                }
            }
        }
    }

    private fun getWeeOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * Return the milliseconds differ time span.
     *
     * @param millis   The milliseconds.
     * @param timeSpan The time span.
     * @param unit     The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the milliseconds differ time span
     */
    fun getMillis(millis: Long,
                  timeSpan: Long,
                  @Unit unit: Int): Long {
        return millis + timeSpan2Millis(timeSpan, unit)
    }

    /**
     * Return the milliseconds differ time span.
     *
     * The pattern is `yyyy-MM-dd HH:mm:ss`.
     *
     * @param time     The formatted time string.
     * @param timeSpan The time span.
     * @param unit     The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the milliseconds differ time span
     */
    fun getMillis(time: String,
                  timeSpan: Long,
                  @Unit unit: Int): Long {
        return getMillis(time, getSimpleDateFormat(), timeSpan, unit)
    }

    /**
     * Return the milliseconds differ time span.
     *
     * @param time     The formatted time string.
     * @param format   The format.
     * @param timeSpan The time span.
     * @param unit     The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the milliseconds differ time span.
     */
    fun getMillis(time: String,
                  @NonNull format: DateFormat,
                  timeSpan: Long,
                  @Unit unit: Int): Long {
        return string2Millis(time, format) + timeSpan2Millis(timeSpan, unit)
    }

    /**
     * Return the milliseconds differ time span.
     *
     * @param date     The date.
     * @param timeSpan The time span.
     * @param unit     The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the milliseconds differ time span.
     */
    fun getMillis(date: Date,
                  timeSpan: Long,
                  @Unit unit: Int): Long {
        return date2Millis(date) + timeSpan2Millis(timeSpan, unit)
    }

    /**
     * Return the formatted time string differ time span.
     *
     * The pattern is `yyyy-MM-dd HH:mm:ss`.
     *
     * @param millis   The milliseconds.
     * @param timeSpan The time span.
     * @param unit     The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the formatted time string differ time span
     */
    fun getString(millis: Long,
                  timeSpan: Long,
                  @Unit unit: Int): String {
        return getString(millis, getSimpleDateFormat(), timeSpan, unit)
    }

    /**
     * Return the formatted time string differ time span.
     *
     * @param millis   The milliseconds.
     * @param format   The format.
     * @param timeSpan The time span.
     * @param unit     The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the formatted time string differ time span
     */
    fun getString(millis: Long,
                  @NonNull format: DateFormat,
                  timeSpan: Long,
                  @Unit unit: Int): String {
        return millis2String(millis + timeSpan2Millis(timeSpan, unit), format)
    }

    /**
     * Return the formatted time string differ time span.
     *
     * The pattern is `yyyy-MM-dd HH:mm:ss`.
     *
     * @param time     The formatted time string.
     * @param timeSpan The time span.
     * @param unit     The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the formatted time string differ time span
     */
    fun getString(time: String,
                  timeSpan: Long,
                  @Unit unit: Int): String {
        return getString(time, getSimpleDateFormat(), timeSpan, unit)
    }

    /**
     * Return the formatted time string differ time span.
     *
     * @param time     The formatted time string.
     * @param format   The format.
     * @param timeSpan The time span.
     * @param unit     The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the formatted time string differ time span
     */
    fun getString(time: String,
                  @NonNull format: DateFormat,
                  timeSpan: Long,
                  @Unit unit: Int): String {
        return millis2String(string2Millis(time, format) + timeSpan2Millis(timeSpan, unit), format)
    }

    /**
     * Return the formatted time string differ time span.
     *
     * The pattern is `yyyy-MM-dd HH:mm:ss`.
     *
     * @param date     The date.
     * @param timeSpan The time span.
     * @param unit     The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the formatted time string differ time span
     */
    fun getString(date: Date,
                  timeSpan: Long,
                  @Unit unit: Int): String {
        return getString(date, getSimpleDateFormat(), timeSpan, unit)
    }

    /**
     * Return the formatted time string differ time span.
     *
     * @param date     The date.
     * @param format   The format.
     * @param timeSpan The time span.
     * @param unit     The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the formatted time string differ time span
     */
    fun getString(date: Date,
                  @NonNull format: DateFormat,
                  timeSpan: Long,
                  @Unit unit: Int): String {
        return millis2String(date2Millis(date) + timeSpan2Millis(timeSpan, unit), format)
    }

    /**
     * Return the date differ time span.
     *
     * @param millis   The milliseconds.
     * @param timeSpan The time span.
     * @param unit     The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the date differ time span
     */
    fun getDate(millis: Long,
                timeSpan: Long,
                @Unit unit: Int): Date {
        return millis2Date(millis + timeSpan2Millis(timeSpan, unit))
    }

    /**
     * Return the date differ time span.
     *
     * The pattern is `yyyy-MM-dd HH:mm:ss`.
     *
     * @param time     The formatted time string.
     * @param timeSpan The time span.
     * @param unit     The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the date differ time span
     */
    fun getDate(time: String,
                timeSpan: Long,
                @Unit unit: Int): Date {
        return getDate(time, getSimpleDateFormat(), timeSpan, unit)
    }

    /**
     * Return the date differ time span.
     *
     * @param time     The formatted time string.
     * @param format   The format.
     * @param timeSpan The time span.
     * @param unit     The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the date differ time span
     */
    fun getDate(time: String,
                @NonNull format: DateFormat,
                timeSpan: Long,
                @Unit unit: Int): Date {
        return millis2Date(string2Millis(time, format) + timeSpan2Millis(timeSpan, unit))
    }

    /**
     * Return the date differ time span.
     *
     * @param date     The date.
     * @param timeSpan The time span.
     * @param unit     The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the date differ time span
     */
    fun getDate(date: Date,
                timeSpan: Long,
                @Unit unit: Int): Date {
        return millis2Date(date2Millis(date) + timeSpan2Millis(timeSpan, unit))
    }

    /**
     * Return the milliseconds differ time span by now.
     *
     * @param timeSpan The time span.
     * @param unit     The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the milliseconds differ time span by now
     */
    fun getMillisByNow(timeSpan: Long, @Unit unit: Int): Long {
        return getMillis(getNowMills(), timeSpan, unit)
    }

    /**
     * Return the formatted time string differ time span by now.
     *
     * The pattern is `yyyy-MM-dd HH:mm:ss`.
     *
     * @param timeSpan The time span.
     * @param unit     The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the formatted time string differ time span by now
     */
    fun getStringByNow(timeSpan: Long, @Unit unit: Int): String {
        return getStringByNow(timeSpan, getSimpleDateFormat(), unit)
    }

    /**
     * Return the formatted time string differ time span by now.
     *
     * @param timeSpan The time span.
     * @param format   The format.
     * @param unit     The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the formatted time string differ time span by now
     */
    fun getStringByNow(timeSpan: Long,
                       @NonNull format: DateFormat,
                       @Unit unit: Int): String {
        return getString(getNowMills(), format, timeSpan, unit)
    }

    /**
     * Return the date differ time span by now.
     *
     * @param timeSpan The time span.
     * @param unit     The unit of time span.
     *
     *  * [MILLISECOND]
     *  * [SECOND]
     *  * [MINUTE]
     *  * [HOUR]
     *  * [DAY]
     *
     * @return the date differ time span by now
     */
    fun getDateByNow(timeSpan: Long, @Unit unit: Int): Date {
        return getDate(getNowMills(), timeSpan, unit)
    }

    /**
     * Return whether it is today.
     *
     * The pattern is `yyyy-MM-dd HH:mm:ss`.
     *
     * @param time The formatted time string.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isToday(time: String): Boolean {
        return isToday(string2Millis(time, getSimpleDateFormat()))
    }

    /**
     * Return whether it is today.
     *
     * @param time   The formatted time string.
     * @param format The format.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isToday(time: String, @NonNull format: DateFormat): Boolean {
        return isToday(string2Millis(time, format))
    }

    /**
     * Return whether it is today.
     *
     * @param date The date.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isToday(date: Date): Boolean {
        return isToday(date.time)
    }

    /**
     * Return whether it is today.
     *
     * @param millis The milliseconds.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isToday(millis: Long): Boolean {
        val wee = getWeeOfToday()
        return millis >= wee && millis < wee + DAY
    }

    /**
     * Return the day of week in Chinese.
     *
     * The pattern is `yyyy-MM-dd HH:mm:ss`.
     *
     * @param time The formatted time string.
     * @return the day of week in Chinese
     */
    fun getChineseWeek(time: String): String {
        return getChineseWeek(string2Date(time, getSimpleDateFormat()))
    }

    /**
     * Return the day of week in Chinese.
     *
     * @param time   The formatted time string.
     * @param format The format.
     * @return the day of week in Chinese
     */
    fun getChineseWeek(time: String, @NonNull format: DateFormat): String {
        return getChineseWeek(string2Date(time, format))
    }

    /**
     * Return the day of week in Chinese.
     *
     * @param date The date.
     * @return the day of week in Chinese
     */
    fun getChineseWeek(date: Date?): String {
        return SimpleDateFormat("E", Locale.CHINA).format(date)
    }

    /**
     * Return the day of week in Chinese.
     *
     * @param millis The milliseconds.
     * @return the day of week in Chinese
     */
    fun getChineseWeek(millis: Long): String {
        return getChineseWeek(Date(millis))
    }

    /**
     * Return the day of week in US.
     *
     * The pattern is `yyyy-MM-dd HH:mm:ss`.
     *
     * @param time The formatted time string.
     * @return the day of week in US
     */
    fun getUSWeek(time: String): String {
        return getUSWeek(string2Date(time, getSimpleDateFormat()))
    }

    /**
     * Return the day of week in US.
     *
     * @param time   The formatted time string.
     * @param format The format.
     * @return the day of week in US
     */
    fun getUSWeek(time: String, @NonNull format: DateFormat): String {
        return getUSWeek(string2Date(time, format))
    }

    /**
     * Return the day of week in US.
     *
     * @param date The date.
     * @return the day of week in US
     */
    fun getUSWeek(date: Date?): String {
        return SimpleDateFormat("EEEE", Locale.US).format(date)
    }

    /**
     * Return the day of week in US.
     *
     * @param millis The milliseconds.
     * @return the day of week in US
     */
    fun getUSWeek(millis: Long): String {
        return getUSWeek(Date(millis))
    }

    /**
     * Returns the value of the given calendar field.
     *
     * The pattern is `yyyy-MM-dd HH:mm:ss`.
     *
     * @param time  The formatted time string.
     * @param field The given calendar field.
     *
     *  * [Calendar.ERA]
     *  * [Calendar.YEAR]
     *  * [Calendar.MONTH]
     *  * ...
     *  * [Calendar.DST_OFFSET]
     *
     * @return the value of the given calendar field
     */
    fun getValueByCalendarField(time: String, field: Int): Int {
        return getValueByCalendarField(string2Date(time, getSimpleDateFormat()), field)
    }

    /**
     * Returns the value of the given calendar field.
     *
     * @param time   The formatted time string.
     * @param format The format.
     * @param field  The given calendar field.
     *
     *  * [Calendar.ERA]
     *  * [Calendar.YEAR]
     *  * [Calendar.MONTH]
     *  * ...
     *  * [Calendar.DST_OFFSET]
     *
     * @return the value of the given calendar field
     */
    fun getValueByCalendarField(time: String, @NonNull format: DateFormat, field: Int): Int {
        return getValueByCalendarField(string2Date(time, format), field)
    }

    /**
     * Returns the value of the given calendar field.
     *
     * @param date  The date.
     * @param field The given calendar field.
     *
     *  * [Calendar.ERA]
     *  * [Calendar.YEAR]
     *  * [Calendar.MONTH]
     *  * ...
     *  * [Calendar.DST_OFFSET]
     *
     * @return the value of the given calendar field
     */
    fun getValueByCalendarField(date: Date?, field: Int): Int {
        val cal = Calendar.getInstance()
        cal.time = date
        return cal.get(field)
    }

    /**
     * Returns the value of the given calendar field.
     *
     * @param millis The milliseconds.
     * @param field  The given calendar field.
     *
     *  * [Calendar.ERA]
     *  * [Calendar.YEAR]
     *  * [Calendar.MONTH]
     *  * ...
     *  * [Calendar.DST_OFFSET]
     *
     * @return the value of the given calendar field
     */
    fun getValueByCalendarField(millis: Long, field: Int): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        return cal.get(field)
    }

    private fun timeSpan2Millis(timeSpan: Long, @Unit unit: Int): Long {
        return timeSpan * unit
    }

    private fun millis2TimeSpan(millis: Long, @Unit unit: Int): Long {
        return millis / unit
    }

    private fun millis2FitTimeSpan(millis: Long, precision: Int): String? {
        var sMillis = millis
        var sPrecision = precision
        if (sPrecision <= 0) return null
        sPrecision = Math.min(sPrecision, 5)
        val units = arrayOf("天", "小时", "分钟", "秒", "毫秒")
        if (sMillis == 0L) return 0.toString() + units[sPrecision - 1]
        val sb = StringBuilder()
        if (sMillis < 0) {
            sb.append("-")
            sMillis = -sMillis
        }
        val unitLen = intArrayOf(86400000, 3600000, 60000, 1000, 1)
        for (i in 0 until sPrecision) {
            if (sMillis >= unitLen[i]) {
                val mode = sMillis / unitLen[i]
                sMillis -= mode * unitLen[i]
                sb.append(mode).append(units[i])
            }
        }
        return sb.toString()
    }

    /**
     * 获取网络时间
     *
     * @param readTime Int
     * @param connectTime Int
     * @return Long
     */
    fun getNetTime(readTime: Int, connectTime: Int): Long {
        return try {
            val uc = URL("http://api.m.taobao.com/rest/api3.do?api=mtop.common.getTimestamp").openConnection()
            uc.readTimeout = readTime
            uc.connectTimeout = connectTime
            uc.connect()
            uc.date
        } catch (e: Exception) {
            0
        }
    }
}