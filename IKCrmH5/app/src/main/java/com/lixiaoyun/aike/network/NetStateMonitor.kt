package com.lixiaoyun.aike.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import com.lixiaoyun.aike.AKApplication
import com.lixiaoyun.aike.utils.isSame
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

class NetStateMonitor private constructor() {

    private object Holder {
        val INSTANCE = NetStateMonitor()
    }

    companion object {
        val instance: NetStateMonitor by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            Holder.INSTANCE
        }
    }

    /**
     * 网络状态
     */
    enum class NetState {
        NETWORK_NOT_FIND, NETWORK_2G, NETWORK_3G, NETWORK_4G, NETWORK_WIFI, NETWORK_UNKNOWN
    }

    private var mObjectSubject: Subject<Any>? = null
    private var mNetStatusReceiver: NetStatusReceiver? = null

    /**
     * 绑定观察
     *
     * @return NetState
     */
    fun observe(): Observable<NetState> {
        if (mObjectSubject == null) {
            //toSerialized保证线程安全
            mObjectSubject = PublishSubject.create<Any>().toSerialized()
        }
        //注册网络监听广播
        registerReceiver(mObjectSubject!!)
        return mObjectSubject!!.cast(NetState::class.java)
    }

    /**
     * 解绑观察
     *
     * @param disposable 绑定持有
     */
    fun dispose(disposable: Disposable?) {
        if (disposable == null) {
            throw IllegalArgumentException("Context can not be null!")
        }
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
        if (!mObjectSubject!!.hasObservers()) {
            unregisterReceiver()
        }
    }

    /**
     * 注册广播
     *
     * @param subject subject
     */
    private fun registerReceiver(subject: Subject<Any>) {
        if (mNetStatusReceiver == null) {
            val context = AKApplication.instance.applicationContext
            mNetStatusReceiver = NetStatusReceiver(subject)
            val intentFilter = IntentFilter()
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            context.registerReceiver(mNetStatusReceiver, intentFilter)
        }
    }

    /**
     * 解绑
     */
    private fun unregisterReceiver() {
        if (mNetStatusReceiver != null) {
            val context = AKApplication.instance.applicationContext
            context.unregisterReceiver(mNetStatusReceiver)
            mNetStatusReceiver = null
        }
    }

    /**
     * 获取当前网络状态
     *
     * @return NetState
     */
    fun getCurrentNetState(): NetState {
        val netState: NetState
        val context = AKApplication.instance.applicationContext
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        netState = if (networkInfo != null && networkInfo.isConnectedOrConnecting) {
            when (networkInfo.type) {
                ConnectivityManager.TYPE_WIFI -> {
                    NetState.NETWORK_WIFI
                }
                ConnectivityManager.TYPE_MOBILE ->
                    when (networkInfo.subtype) {
                        TelephonyManager.NETWORK_TYPE_GPRS,
                        TelephonyManager.NETWORK_TYPE_CDMA,
                        TelephonyManager.NETWORK_TYPE_EDGE,
                        TelephonyManager.NETWORK_TYPE_1xRTT,
                        TelephonyManager.NETWORK_TYPE_IDEN -> {
                            NetState.NETWORK_2G
                        }

                        TelephonyManager.NETWORK_TYPE_EVDO_A,
                        TelephonyManager.NETWORK_TYPE_UMTS,
                        TelephonyManager.NETWORK_TYPE_EVDO_0,
                        TelephonyManager.NETWORK_TYPE_HSDPA,
                        TelephonyManager.NETWORK_TYPE_HSUPA,
                        TelephonyManager.NETWORK_TYPE_HSPA,
                        TelephonyManager.NETWORK_TYPE_EVDO_B,
                        TelephonyManager.NETWORK_TYPE_EHRPD,
                        TelephonyManager.NETWORK_TYPE_HSPAP -> {
                            NetState.NETWORK_3G
                        }

                        TelephonyManager.NETWORK_TYPE_LTE -> {
                            NetState.NETWORK_4G
                        }

                        else -> {
                            NetState.NETWORK_UNKNOWN
                        }
                    }
                else -> {
                    NetState.NETWORK_UNKNOWN
                }
            }
        } else {
            NetState.NETWORK_NOT_FIND
        }
        return netState
    }

    private inner class NetStatusReceiver(private val mObjectSubject: Subject<Any>) : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action.isSame(ConnectivityManager.CONNECTIVITY_ACTION)) {
                //监听网络变化，发送消息
                mObjectSubject.onNext(getCurrentNetState())
            }
        }

    }
}
