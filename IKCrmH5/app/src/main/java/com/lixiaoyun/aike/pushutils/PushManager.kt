package com.lixiaoyun.aike.pushutils

import android.app.Activity
import android.app.Application
import com.huawei.android.hms.agent.HMSAgent
import com.lixiaoyun.aike.utils.Rom
import com.orhanobut.logger.Logger

/**
 * @data on 2019/6/14
 */
object PushManager {

    /**
     * 初始化推送
     * @param app Application
     */
    fun initPush(app: Application) {
        when {
            Rom.isEmui() -> {
                Logger.d("初始化华为推送")
                HMSAgent.init(app)
            }
            Rom.isMiui() -> {
                Logger.d("初始化小米推送")
                MIUIPushUtil.initMIUIPush(app)
            }
            else -> {
                Logger.d("初始化个推推送")
                GtPushUtil.initialize()
            }
        }
    }

    fun initGtPush(){
        Logger.d("初始化个推推送-single")
        GtPushUtil.initialize()
    }

    /**
     * 需要手动获取华为token
     * @param activity Activity
     */
    fun getEMUIToken(activity: Activity) {
        if (Rom.isEmui()) {
            HMSAgent.connect(activity) { connect ->
                Logger.e("华为推送 connect：$connect")
                if (connect == 0) {
                    Logger.e("华为推送获取token")
                    HMSAgent.Push.getToken {
                        Logger.e("华为推送 getToken：$it")
                    }
                }
            }
        }
    }
}