package com.lixiaoyun.aike.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.lixiaoyun.aike.listener.DialStateListener
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandleLogEntity
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandlePostLog

/**
 * 监听用户电话状态
 * @data on 2019/5/6
 */
class PhoneStatusService : Service() {

    private var mStateListener: PhoneStateListener? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        HandlePostLog.postLogSalesDynamics(HandleLogEntity.EVENT_PHONE_STATUS_SERVICE, "onCreate")
        val manager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        mStateListener = DialStateListener(applicationContext)
        manager.listen(mStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //当service因内存不足被kill，当内存又有的时候，service又被重新创建
        return START_STICKY
    }

    override fun onDestroy() {
        // 取消电话的监听
        val manager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (mStateListener != null) {
            manager.listen(mStateListener, PhoneStateListener.LISTEN_NONE)
        }
        mStateListener = null
        super.onDestroy()
    }

}