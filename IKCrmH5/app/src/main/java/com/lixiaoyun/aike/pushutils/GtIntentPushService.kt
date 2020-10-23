package com.lixiaoyun.aike.pushutils

import android.content.Context
import com.alibaba.fastjson.JSON
import com.google.gson.Gson
import com.igexin.sdk.GTIntentService
import com.igexin.sdk.PushManager
import com.igexin.sdk.message.GTCmdMessage
import com.igexin.sdk.message.GTNotificationMessage
import com.igexin.sdk.message.GTTransmitMessage
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.entity.PushConfirm
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandleLogEntity
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandlePostLog
import com.lixiaoyun.aike.utils.empty
import com.orhanobut.logger.Logger
import org.json.JSONObject

/**
 * 继承 GTIntentService 接收来自个推的消息, 所有消息在线程中回调, 如果注册了该服务, 则务必要在 AndroidManifest中声明, 否则无法接受消息
 * onReceiveMessageData 处理透传消息
 * onReceiveClientId 接收 cid
 * onReceiveOnlineState cid 离线上线通知
 * onReceiveCommandResult 各种事件处理回执
 */
class GtIntentPushService : GTIntentService() {

    override fun onReceiveServicePid(context: Context, pid: Int) {
    }

    override fun onReceiveClientId(context: Context, cid: String?) {
        Logger.d("个推CID： $cid")
        AppConfig.setPushClientId(cid ?: "")
        AppConfig.setPushType(AppConfig.PUSH_TYPE_GT)
    }

    override fun onReceiveMessageData(context: Context, message: GTTransmitMessage) {
        Logger.d("onReceiveMessageData = ${JSON.toJSONString(message)}")
        val taskId = message.taskId
        val msgId = message.messageId
        val payload = message.payload
        val result = PushManager.getInstance().sendFeedbackMessage(context, taskId, msgId, 90001)
        Logger.d("个推 taskId：$taskId, msgId：$msgId, result：$result")
        if (result && payload != null) {
            val data = String(payload)
            val dataObj = JSONObject(data)
            val pushType = dataObj.optInt("push_type")
            val action = dataObj.optInt("action")
            Logger.d("个推 pushType：$pushType \naction：$action \ndata：$data \ndataObj：$dataObj")
            HandlePostLog.postLogPushServiceTopic(HandleLogEntity.EVENT_MESSAGE_PUSH_SERVICE_TYPE_GT, data)
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
    }

    override fun onNotificationMessageArrived(context: Context, message: GTNotificationMessage) {
        Logger.d("个推通知消息到达：${Gson().toJson(message)}")
    }

    override fun onNotificationMessageClicked(context: Context, message: GTNotificationMessage) {
    }

    override fun onReceiveCommandResult(context: Context, message: GTCmdMessage) {
    }

    override fun onReceiveOnlineState(context: Context, isOnline: Boolean) {
        Logger.d("个推在线状态：${(if (isOnline) "在线" else "下线")}")
    }

}