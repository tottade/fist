package com.lixiaoyun.aike.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.network.NetWorkConfig
import com.lixiaoyun.aike.utils.socketUtils.WebSocketUtil


/**
 * @data on 2019-12-24
 */

class KeepAliveService : Service() {

    /**
     * 连接websocket的地址
     */
    private val webSocketUtil: WebSocketUtil = WebSocketUtil(NetWorkConfig.getSocketUrl())

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(AppConfig.FOREGROUND_SERVICE_ID, getNotification())
        }
        //启动连接
        webSocketUtil.setWebSocketConnect()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        //关闭所有连接
        webSocketUtil.closeConnect()
    }

    private fun getNotification(): Notification? {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(AppConfig.FOREGROUND_NOTIFICATION_CHANNEL,
                    AppConfig.FOREGROUND_NOTIFICATION_CHANNEL, NotificationManager.IMPORTANCE_HIGH)
            channel.setBypassDnd(true)
            channel.name = applicationContext.getString(R.string.tip_keep_alive_service)
            channel.description = "若该通知消失请及时重启APP"
            channel.lockscreenVisibility = Notification.VISIBILITY_SECRET
            channel.setShowBadge(true)
            notificationManager.createNotificationChannel(channel)
        }
        val builder = NotificationCompat
                .Builder(this, AppConfig.FOREGROUND_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(applicationContext.getString(R.string.tip_keep_alive_service))
                .setContentText("若该通知消失请及时重启APP")
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setAutoCancel(false)
        val notification = builder.build()
        notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT
        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR
        notification.flags = notification.flags or Notification.FLAG_FOREGROUND_SERVICE
        return notification
    }
}
