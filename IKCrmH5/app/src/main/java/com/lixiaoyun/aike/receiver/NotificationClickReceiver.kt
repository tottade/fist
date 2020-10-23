package com.lixiaoyun.aike.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lixiaoyun.aike.activity.MainActivity
import com.lixiaoyun.aike.constant.KeySet
import com.lixiaoyun.aike.entity.NotificationClickData
import com.orhanobut.logger.Logger
import org.greenrobot.eventbus.EventBus

/**
 * @data on 2019/7/2
 * 通知栏消息点击广播
 */

class NotificationClickReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val data = intent.getStringExtra(KeySet.I_NOTIFICATION_EXTRA)
        Logger.d("通知栏消息点击广播 intent extra data = $data")
        EventBus.getDefault().post(NotificationClickData(data))
        //进入主页
        val mainIntent = Intent(context, MainActivity::class.java)
        mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(mainIntent)
    }
}