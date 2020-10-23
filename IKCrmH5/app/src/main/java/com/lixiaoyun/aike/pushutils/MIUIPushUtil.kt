package com.lixiaoyun.aike.pushutils

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import com.lixiaoyun.aike.BuildConfig
import com.orhanobut.logger.Logger
import com.xiaomi.mipush.sdk.MiPushClient

/**
 * @data on 2019/6/13
 */
object MIUIPushUtil {

    /**
     * 注册小米推送
     * @param ctx
     */
    fun initMIUIPush(ctx: Context) {
        if (shouldInit(ctx)) {
            Logger.d(BuildConfig.XM_PUSH_ID)
            Logger.d(BuildConfig.XM_PUSH_KEY)
            MiPushClient.registerPush(ctx.applicationContext, BuildConfig.XM_PUSH_ID, BuildConfig.XM_PUSH_KEY)
        }
    }

    /**
     * 初始化条件验证
     * @return
     */
    private fun shouldInit(ctx: Context): Boolean {
        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processInfo = am.runningAppProcesses
        val mainProcessName = ctx.packageName
        val myPid = Process.myPid()
        for (info in processInfo) {
            if (info.pid == myPid && mainProcessName == info.processName) {
                return true
            }
        }
        return false
    }
}