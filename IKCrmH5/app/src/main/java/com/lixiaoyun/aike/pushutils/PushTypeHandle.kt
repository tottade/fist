package com.lixiaoyun.aike.pushutils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.lixiaoyun.aike.AKApplication
import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.constant.KeySet
import com.lixiaoyun.aike.entity.EnableCarouselData
import com.lixiaoyun.aike.entity.PushAppDialBean
import com.lixiaoyun.aike.entity.PushConfirm
import com.lixiaoyun.aike.entity.PushDataBean
import com.lixiaoyun.aike.entity.model.SalesDynamicsModel
import com.lixiaoyun.aike.network.NetWorkUtil
import com.lixiaoyun.aike.receiver.NotificationClickReceiver
import com.lixiaoyun.aike.utils.*
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandleLogEntity
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandlePostLog
import com.lixiaoyun.aike.utils.recordingUtils.HandlerCall
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus

/**
 * @data on 2019/5/5
 */
object PushTypeHandle {

    /**
     * 上传推送确认
     * @param data PushConfirm
     */
    @SuppressLint("CheckResult")
    fun pushConfirm(data: PushConfirm) {
        NetWorkUtil.instance.initRetrofit().confirmPush(data)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            val resultString = NetWorkUtil.instance.resolveResponseBody(it)
                            Logger.d(resultString)
                        },
                        {
                            Logger.d(it.message)
                        }
                )
    }

    /**
     * PC调起拨号
     *
     * @param data String
     */
    @SuppressLint("CheckResult")
    fun actionTuneUpCall(context: Context, data: String, type: Int) {
        if (isFastTrigger(3000)) {
            return
        }
        var msg = "PC调起APP拨号，"
        when {
            AKApplication.instance.getAppBackstage() -> {
                msg += "调起失败：APP处于后台，"
                context.getString(R.string.toast_take_call_back_stage).toast()
            }

            AppConfig.PhoneTalking -> {
                msg += "调起失败：正在通话中，"
                context.getString(R.string.toast_take_call_calling).toast()
            }

            else -> {
                val bean = GsonUtil.instance.gsonToBean(data, PushAppDialBean::class.java)
                //检测双卡轮播状态
                if (bean.enable_carousel) {
                    try {
                        //开启双卡轮播，检测sim卡数量
                        context.checkSimNum(true) { simNum ->
                            when (simNum) {
                                0, 1 -> {
                                    //报错，并上报数据
                                    msg += "调起失败：sim卡检测到 $simNum 张，$data"
                                    if (type == 11) {
                                        HandlePostLog.postLogPushServiceTopic(HandleLogEntity.EVENT_MESSAGE_PUSH_TAKE_CALL, msg)
                                    } else if (type == 12) {
                                        HandlePostLog.postLogPushServiceTopic(HandleLogEntity.EVENT_MESSAGE_PUSH_TAKE_PUBLIC_CALL, msg)
                                    }
                                    Logger.e(msg)
                                    EventBus.getDefault().post(EnableCarouselData(true))
                                }
                                2 -> {
                                    //直接播出电话
                                    takePhone(msg, bean, context, type, data, true)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        msg += "调起失败：sim卡检测失败：${e.message}，$data"
                        if (type == 11) {
                            HandlePostLog.postLogPushServiceTopic(HandleLogEntity.EVENT_MESSAGE_PUSH_TAKE_CALL, msg)
                        } else if (type == 12) {
                            HandlePostLog.postLogPushServiceTopic(HandleLogEntity.EVENT_MESSAGE_PUSH_TAKE_PUBLIC_CALL, msg)
                        }
                        Logger.e(msg)
                    }
                } else {
                    //未开启双卡轮播，直接播出电话
                    takePhone(msg, bean, context, type, data, false)
                }
            }
        }
    }

    private fun takePhone(logMsg: String, bean: PushAppDialBean, context: Context, type: Int, data: String, dual_card: Boolean) {
        var msg = logMsg
        val userInfo = AppConfig.getUserInfo()
        when {
            bean.checkEmpty() -> {
                msg += "调起失败：推送信息有误，"
            }

            userInfo?.id != bean.user_id -> {
                msg += "调起失败：与登录用户信息不符，"
                context.getString(R.string.toast_take_call_not_same).toast()
            }
            else -> {
                Observable
                        .create<Long> {
                            var netTime = DateUtils.instance.getNetTime(5000, 5000)
                            if (netTime == 0L) {
                                netTime = DateUtils.instance.getNowMills()
                            }
                            it.onNext(netTime)
                            it.onComplete()
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    //格式化推送时间
                                    val pushTime = DateUtils.instance.string2Date(bean.start_time, DateUtils.FORMAT_T)
                                    Logger.d("PC调起拨号 推送时间：$pushTime")
                                    //判断推送时间
                                    if (pushTime == null) {
                                        msg += "调起失败：解析推送时间有误，"
                                    } else {
                                        if (it - pushTime.time <= 70000) {
                                            //保存信息
                                            val saveData = SalesDynamicsModel()
                                            saveData.callId = bean.call_id
                                            saveData.callerType = bean.caller_type
                                            saveData.callerId = bean.caller_id.toString()
                                            saveData.name = userInfo.name
                                            saveData.nameType = bean.name_type
                                            if (type == 11) {
                                                saveData.phoneNumber = bean.number
                                            } else if (type == 12) {
                                                saveData.phoneNumber = bean.middle_call_number
                                            }
                                            saveData.phoneType = KeySet.KEY_CALL_PHONE_TYPE_PUSH
                                            HandlerCall().handlerCreateModel(saveData, bean.number, bean.enable_carousel, bean.carousel_card_no)
                                            { msg, success ->
                                                Logger.d("PUSH TAKE CALL ${success}：$msg")
                                            }
                                        } else {
                                            msg += "调起失败：推送超时，"
                                        }
                                    }
                                },
                                {
                                    msg += "，调起失败，Error：${it.message}，"
                                    HandlePostLog.postLogPushServiceTopic(HandleLogEntity.EVENT_MESSAGE_PUSH_TAKE_CALL_ERROR, msg)
                                    context.getString(R.string.toast_take_call_time_out).toast()
                                }
                        )
            }
        }
        msg += data
        if (type == 11) {
            HandlePostLog.postLogPushServiceTopic(HandleLogEntity.EVENT_MESSAGE_PUSH_TAKE_CALL, msg)
        } else if (type == 12) {
            HandlePostLog.postLogPushServiceTopic(HandleLogEntity.EVENT_MESSAGE_PUSH_TAKE_PUBLIC_CALL, msg)
        }
    }

    fun createNotification(context: Context, dataString: String) {
        Logger.d("生成通知栏消息")
        val pushDataBean = GsonUtil.instance.gsonToBean(dataString, PushDataBean::class.java)
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //创建通知
        val mBuilder = NotificationCompat.Builder(context, AppConfig.NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(pushDataBean.text)
                .setContentText(pushDataBean.title)
                .setStyle(
                        NotificationCompat
                                .BigTextStyle()
                                .setBigContentTitle(pushDataBean.title)
                                .bigText(pushDataBean.text)
                )
                .setOngoing(false)
                .setAutoCancel(true)

        //创建通知channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(AppConfig.NOTIFICATION_CHANNEL, AppConfig.NOTIFICATION_CHANNEL,
                    //设置优先级
                    NotificationManager.IMPORTANCE_HIGH)
            //设置在渠道信息
            channel.name = AKApplication.instance.applicationContext.getString(R.string.notify_channel_name)
            channel.description = AKApplication.instance.applicationContext.getString(R.string.notify_channel_description)
            //绕过请勿打扰
            channel.setBypassDnd(true)
            //设置在锁屏界面上显示这条通知
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            channel.setShowBadge(true)
            //设置呼吸灯
            channel.enableLights(true)
            channel.lightColor = Color.GREEN
            //设置震动
            if (pushDataBean.is_vibrate) {
                channel.enableVibration(true)
                channel.vibrationPattern = longArrayOf(100, 400, 100)
            }
            //设置铃声
            if (pushDataBean.is_ring) {
                //默认有铃声
                channel.setSound(null, null)
            }
            mNotificationManager.createNotificationChannel(channel)
        } else {
            //设置优先级
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //8.0以下 && 7.0及以上 设置优先级
                mBuilder.priority = NotificationManager.IMPORTANCE_HIGH
            } else {
                mBuilder.priority = NotificationCompat.PRIORITY_HIGH
            }
            //设置多媒体
            if (pushDataBean.is_vibrate && pushDataBean.is_ring) {
                mBuilder.setDefaults(Notification.DEFAULT_ALL)
            } else if (pushDataBean.is_vibrate) {
                //设置震动
                mBuilder.setDefaults(Notification.DEFAULT_VIBRATE)
            } else if (pushDataBean.is_ring) {
                //设置铃声
                mBuilder.setDefaults(Notification.DEFAULT_SOUND)
            }
        }

        //设置pendingIntent
        val intent = Intent(context, NotificationClickReceiver::class.java)
        intent.putExtra(KeySet.I_NOTIFICATION_EXTRA, dataString)
        val pendingIntent = PendingIntent.getBroadcast(context, DateUtils.instance.getNowSeconds().toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.setContentIntent(pendingIntent)

        //发送通知
        mNotificationManager.notify(DateUtils.instance.getNowSeconds().toInt(), mBuilder.build())
    }
}