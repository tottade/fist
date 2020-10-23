package com.lixiaoyun.aike.utils.socketUtils

import android.content.Context
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager


/**
 * 构建WebSocket，配置参数
 *
 * @data on 2019-12-24
 */
class RxWebSocketBuilder(context: Context) {
    var mContext: Context = context.applicationContext
    /**
     * 支持外部传入OkHttpClient
     */
    var mClient: OkHttpClient? = null
    /**
     * 支持SSL
     */
    var mSslSocketFactory: SSLSocketFactory? = null
    var mTrustManager: X509TrustManager? = null
    /**
     * 重连间隔时间
     */
    var mReconnectInterval: Long = 0
    /**
     * 重连间隔时间的单位
     */
    var mReconnectIntervalTimeUnit: TimeUnit? = null

    fun client(client: OkHttpClient?): RxWebSocketBuilder {
        mClient = client
        return this
    }

    fun sslSocketFactory(sslSocketFactory: SSLSocketFactory?, trustManager: X509TrustManager?): RxWebSocketBuilder {
        mSslSocketFactory = sslSocketFactory
        mTrustManager = trustManager
        return this
    }

    fun reconnectInterval(reconnectInterval: Long, reconnectIntervalTimeUnit: TimeUnit?): RxWebSocketBuilder {
        mReconnectInterval = reconnectInterval
        mReconnectIntervalTimeUnit = reconnectIntervalTimeUnit
        return this
    }

    fun build(): RxWebSocket {
        return RxWebSocket(this)
    }
}