package com.lixiaoyun.aike.pushutils

import android.content.Intent
import android.os.IBinder
import com.igexin.sdk.PushService
import com.orhanobut.logger.Logger

class GtPushService : PushService() {

    override fun onBind(p: Intent?): IBinder? {
        return super.onBind(p)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(p0: Intent?, p1: Int, p2: Int): Int {
        return super.onStartCommand(p0, p1, p2)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }
}