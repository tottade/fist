package com.lixiaoyun.aike.pushutils

import android.content.Context
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.entity.PushConfirm
import com.lixiaoyun.aike.utils.GsonUtil
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandleLogEntity
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandlePostLog
import com.lixiaoyun.aike.utils.empty
import com.lixiaoyun.aike.utils.isSame
import com.orhanobut.logger.Logger
import com.xiaomi.mipush.sdk.*
import org.json.JSONObject

/**
 * @data on 2019/6/12
 */
class MIUIPushReceiver : PushMessageReceiver() {

    /**
     * 获取注册成功后的regId标识
     *
     * @param context Context?
     * @param message MiPushCommandMessage?
     */
    override fun onCommandResult(context: Context?, message: MiPushCommandMessage?) {
        super.onCommandResult(context, message)
        if (message != null) {
            Logger.d("小米推送获取regId：${GsonUtil.instance.gsonString(message)}")
            val command = message.command
            val arguments = message.commandArguments
            if (MiPushClient.COMMAND_REGISTER.isSame(command) && ErrorCode.SUCCESS.compareTo(message.resultCode) == 0) {
                val regId = arguments[0]
                AppConfig.setPushClientId(regId ?: "")
                AppConfig.setPushType(AppConfig.PUSH_TYPE_MI)
            }
        } else {
            Logger.e("小米推送获取regId失败，message为空")
        }
    }

    override fun onReceivePassThroughMessage(context: Context, message: MiPushMessage) {
        super.onReceivePassThroughMessage(context, message)
        Logger.d("小米推送到达message：${GsonUtil.instance.gsonString(message)}")
        try {
            if (!message.content.empty()) {
                val data = message.content
                val dataObj = JSONObject(data)
                val pushType = dataObj.optInt("push_type")
                val action = dataObj.optInt("action")
                Logger.d("小米推送 message content：$data")
                Logger.d("小米推送 pushType：$pushType \naction：$action")
                HandlePostLog.postLogPushServiceTopic(HandleLogEntity.EVENT_MESSAGE_PUSH_SERVICE_TYPE_XM, data)
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
            }
        } catch (e: Exception) {
            Logger.e("小米推送ERROR：${e.message}")
        }
    }
}