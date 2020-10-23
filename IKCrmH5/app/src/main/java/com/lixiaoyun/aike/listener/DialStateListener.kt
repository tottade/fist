package com.lixiaoyun.aike.listener

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.text.TextUtils
import com.lixiaoyun.aike.BuildConfig
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.constant.KeySet
import com.lixiaoyun.aike.entity.model.SalesDynamicsModel
import com.lixiaoyun.aike.utils.DateUtils
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandleLogEntity
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandlePostLog
import com.lixiaoyun.aike.utils.empty
import com.lixiaoyun.aike.utils.recordingUtils.HandleCallRecord
import com.lixiaoyun.aike.utils.recordingUtils.SalesDynamicsManager
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * @data on 2019/5/6
 */
class DialStateListener constructor(context: Context) : PhoneStateListener() {

    private var mContext: Context = context

    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
        super.onCallStateChanged(state, phoneNumber)
        if (BuildConfig.DEBUG) {
            logCallStateChangedInfo(state, phoneNumber)
        }
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                //响铃状态
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                //通话状态
                AppConfig.PhoneTalking = true
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                //空闲状态
                AppConfig.PhoneTalking = false
                if (AppConfig.SalesDynamicsId != 0L) {
                    //处理通话结束
                    handleCallIdle()
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun handleCallIdle() {
        Observable.timer(1500, TimeUnit.MILLISECONDS).map<SalesDynamicsModel> {
            SalesDynamicsManager.instance.getDateById(AppConfig.SalesDynamicsId)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(
                { model ->
                    if (model != null) {
                        //如果是PC调起电话，调用PC端挂断接口
                        if (TextUtils.equals(model.phoneType, KeySet.KEY_CALL_PHONE_TYPE_PUSH)) {
                            SalesDynamicsManager.instance.dialHangUpNotify()
                        }
                        if (!model.phoneNumber.empty()) {
                            //上传录音
                            model.endTime = System.currentTimeMillis()
                            model.takeOff = true
                            SalesDynamicsManager.instance.upDataModel(model)
                            HandleCallRecord(mContext, model).handleCallRecord()
                        } else {
                            SalesDynamicsManager.instance.deleteDateById(AppConfig.SalesDynamicsId)
                            HandlePostLog.postLogSalesDynamics(HandleLogEntity.EVENT_GET_CALL_RECORDING,
                                    "${AppConfig.getUserLogin()}：查找销售动态数据库条目出错：手机号码为空")
                        }
                    } else {
                        HandlePostLog.postLogSalesDynamics(HandleLogEntity.EVENT_GET_CALL_RECORDING,
                                "${AppConfig.getUserLogin()}：查找销售动态数据库条目出错：未找到符合当前Id的数据")
                    }
                    AppConfig.SalesDynamicsId = 0L
                },
                {
                    HandlePostLog.postLogSalesDynamics(HandleLogEntity.EVENT_GET_CALL_RECORDING,
                            "${AppConfig.getUserLogin()}：查找销售动态数据库条目出错：${it.message}")
                    AppConfig.SalesDynamicsId = 0L
                }
        )
    }

    private fun logCallStateChangedInfo(state: Int, phoneNum: String?) {
        val sb = StringBuilder("时间：")
                .append(DateUtils.instance.getNowString())
                .append("\n")
                .append(System.currentTimeMillis())
                .append("\n当前电话设备状态：")
        when (state) {
            0 -> sb.append("空闲状态")
            1 -> sb.append("响铃状态")
            2 -> sb.append("通话状态")
        }
        sb.append("\n当前电话号码：").append(phoneNum ?: "")
        Logger.e(sb.toString())
    }
}