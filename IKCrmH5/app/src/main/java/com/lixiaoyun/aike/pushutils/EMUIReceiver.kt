package com.lixiaoyun.aike.pushutils

import android.content.Context
import android.os.Bundle
import com.huawei.hms.support.api.push.PushReceiver
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.entity.PushConfirm
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandleLogEntity
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandlePostLog
import com.lixiaoyun.aike.utils.empty
import com.orhanobut.logger.Logger
import org.json.JSONObject

/**
 * @data on 2019/6/13
 */
class EMUIReceiver : PushReceiver() {

    override fun onToken(context: Context?, token: String?) {
        super.onToken(context, token)
        if (token.empty()) {
            Logger.e("华为推送获取token失败")
        } else {
            Logger.d("华为推送 onToken: $token")
            AppConfig.setPushClientId(token!!)
            AppConfig.setPushType(AppConfig.PUSH_TYPE_HW)
        }
    }

    override fun onPushMsg(context: Context?, msgBytes: ByteArray?, extras: Bundle?): Boolean {
        try {
            if (context != null) {
                val msgContent = String(msgBytes!!)
                Logger.d("华为推送 onPushMsg：$msgContent")
                onReceivePassThroughMessage(context, msgContent)
            }
        } catch (e: Exception) {
            Logger.e("华为推送ERROR：${e.message}")
        }
        return super.onPushMsg(context, msgBytes, extras)
    }

    private fun onReceivePassThroughMessage(context: Context, data: String) {
        if (!data.empty()) {
            try {
                val dataObj = JSONObject(data)
                val pushType = dataObj.optInt("push_type")
                val action = dataObj.optInt("action")
                Logger.d("华为推送 message content：$data")
                Logger.d("华为推送 pushType：$pushType \naction：$action")
                HandlePostLog.postLogPushServiceTopic(HandleLogEntity.EVENT_MESSAGE_PUSH_SERVICE_TYPE_HW, data)
                PushTypeHandle.pushConfirm(PushConfirm(dataObj.optInt("id"), dataObj.optInt("user_id")))
                //主动推送
                when (pushType) {
                    11 -> {
                        //App调起通话
                        if (!data.empty()) {
                            PushTypeHandle.actionTuneUpCall(context, data, 11)
                        } else {
                            Logger.e("PC调起拨号：信息有误")
                        }
                    }
                    12 -> {
                        //App调起通话
                        Logger.d("小号推送:${data}")
                        PushTypeHandle.actionTuneUpCall(context, data, 12)
                    }
                    2, 3 -> {
                        PushTypeHandle.createNotification(context, data)
                    }
                }
            } catch (e: Exception) {
                Logger.e("华为推送ERROR：${e.message}")
            }
        }
    }
}