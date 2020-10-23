package com.lixiaoyun.aike.utils

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.provider.CalendarContract
import com.lixiaoyun.aike.entity.AddCalendarEventBean
import com.orhanobut.logger.Logger
import java.util.*

/**
 * 给日历中写入事件
 */
class CalendarEventsUtils private constructor() {
    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = CalendarEventsUtils()
    }

    private val CALENDAR_URL = "content://com.android.calendar/calendars"
    private val CALENDAR_EVENT_URL = "content://com.android.calendar/events"
    private val CALENDAR_REMINDER_URL = "content://com.android.calendar/reminders"

    private val CALENDARS_NAME = "ikcrm"
    private val CALENDARS_ACCOUNT_NAME = "www.ikcrm.com"
    private val CALENDARS_ACCOUNT_TYPE = "com.android.ikcrm"
    private val CALENDARS_DISPLAY_NAME = "ikcrm账户"

    /**
     * 查询是否存在日历账号
     */
    private fun checkCalendarAccount(context: Context): Int {
        val cursor = context.contentResolver.query(Uri.parse(CALENDAR_URL), null,
                null, null, null)
        cursor.use {
            return if (it == null) {
                Logger.e("没有日历账号，创建ikcrm日历账号")
                //创建并返回账号Id
                addCalendarAccount(context).toInt()
            } else {
                val userIndex = it.count
                return if (userIndex > 0) {
                    it.moveToFirst()
                    Logger.d("日历账号ID ${it.getColumnIndex(CalendarContract.Calendars._ID)}")
                    //返回账号Id
                    it.getColumnIndex(CalendarContract.Calendars._ID)
                } else {
                    Logger.e("没有日历账号，创建ikcrm日历账号")
                    //创建并返回账号Id
                    addCalendarAccount(context).toInt()
                }
            }
        }
    }

    /**
     * 添加一个日历账号
     */
    private fun addCalendarAccount(context: Context): Long {
        val contentValues = ContentValues()
        contentValues.put(CalendarContract.Calendars.NAME, CALENDARS_NAME)
        contentValues.put(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME)
        contentValues.put(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE)
        contentValues.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDARS_DISPLAY_NAME)
        contentValues.put(CalendarContract.Calendars.VISIBLE, 1)
        contentValues.put(CalendarContract.Calendars.CALENDAR_COLOR, Color.BLUE)
        contentValues.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER)
        contentValues.put(CalendarContract.Calendars.SYNC_EVENTS, 1)
        contentValues.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().id)
        contentValues.put(CalendarContract.Calendars.OWNER_ACCOUNT, CALENDARS_ACCOUNT_NAME)
        contentValues.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0)

        val calendarUri = Uri.parse(CALENDAR_URL).buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE)
                .build()
        val result = context.contentResolver.insert(calendarUri, contentValues)
        return if (result == null) {
            Logger.e("添加添加日历账号失败")
            -1
        } else {
            Logger.d("添加添加日历账号成功")
            ContentUris.parseId(result)
        }
    }

    /**
     * 添加日程事件
     */
    fun addCalendarEvent(context: Context, eventBean: AddCalendarEventBean, callBack: (success: Boolean) -> Unit) {
        val userAccountId = checkCalendarAccount(context)
        if (userAccountId < 0) {
            "用户日历账号获取失败".toast()
            callBack(false)
        } else {
            val beginTime: Long = if (eventBean.beginTime.empty()) {
                Calendar.getInstance().timeInMillis
            } else {
                DateUtils.instance.string2Millis(eventBean.beginTime, DateUtils.instance.getSimpleDateFormat(DateUtils.FORMAT_NO_SECOND))
            }
            val endTime: Long = if (eventBean.endTime.empty()) {
                beginTime + 30 * 60 * 1000
            } else {
                DateUtils.instance.string2Millis(eventBean.endTime, DateUtils.instance.getSimpleDateFormat(DateUtils.FORMAT_NO_SECOND))
            }

            val contentValues = ContentValues()
            contentValues.put(CalendarContract.Events.DTSTART, beginTime)
            contentValues.put(CalendarContract.Events.DTEND, endTime)
            contentValues.put(CalendarContract.Events.TITLE, eventBean.title)
            contentValues.put(CalendarContract.Events.DESCRIPTION, eventBean.description)
            contentValues.put(CalendarContract.Events.CALENDAR_ID, 1)
            contentValues.put(CalendarContract.Events.EVENT_LOCATION, eventBean.location)
            contentValues.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            contentValues.put(CalendarContract.Events.HAS_ALARM, 1)
            val eventUri = context.contentResolver.insert(Uri.parse(CALENDAR_EVENT_URL), contentValues)
            val eventId = ContentUris.parseId(eventUri)
            if (eventUri == null || eventId == 0L) {
                Logger.e("添加事件失败")
                "添加日程失败，请稍后重试".toast()
                callBack(false)
            } else {
                Logger.d("添加事件成功")
            }

            val reminderValues = ContentValues()
            reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId)
            reminderValues.put(CalendarContract.Reminders.MINUTES, eventBean.remindTime / 60)
            reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
            val reminderUri = context.contentResolver.insert(Uri.parse(CALENDAR_REMINDER_URL), reminderValues)
            val reminderId = ContentUris.parseId(reminderUri)
            if (reminderUri == null || reminderId == 0L) {
                Logger.e("添加提醒失败")
                "添加日程提醒失败，请稍后重试".toast()
                callBack(false)
            } else {
                Logger.e("添加提醒成功")
                callBack(true)
            }
        }
    }
}