package com.lixiaoyun.aike.pushutils

import com.igexin.sdk.PushManager
import com.lixiaoyun.aike.AKApplication

object GtPushUtil {

    private val pushManager: PushManager = PushManager.getInstance()
    private val context = AKApplication.instance

    /**
     * 初始化
     */
    fun initialize() {
        pushManager.initialize(context, GtPushService::class.java)
        pushManager.registerPushIntentService(context, GtIntentPushService::class.java)
    }

    /**
     * @param switch 开关
     *
     * 控制推送开关
     */
    fun pushSwitch(switch: Boolean) {
        if (switch) {
            pushManager.turnOnPush(context)
        } else {
            pushManager.turnOffPush(context)
        }
    }

    /**
     * @return String
     * 获取ClientId
     */
    fun getPushClientId(): String {
        return try {
            pushManager.getClientid(context)
        } catch (e: Exception) {
            "GT Cid acquisition failed"
        }
    }

    /**
     * @return Boolean
     * 获取SDK服务状态
     * true：当前推送已打开
     * false：当前推送已关闭
     */
    fun getPushSDKStatus(): Boolean {
        return pushManager.isPushTurnedOn(context)
    }
}
