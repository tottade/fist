package com.lixiaoyun.aike.utils.socketUtils

import android.content.Context
import com.lixiaoyun.aike.entity.WebSocketInfo
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okio.ByteString
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * 创建代理对象，具体实现由WebSocketWorkerImpl完成
 * 方便添加逻辑
 *
 * @data on 2019-12-25
 */
class RxWebSocket(builder: RxWebSocketBuilder) : WebSocketWorker {

    companion object {
        const val ERROR_WS_NULL = "WebSocket 为空"
        const val ERROR_PREPARE_RECONNECT = "WebSocket 准备重连"
    }

    private var mContext: Context = builder.mContext
    /**
     * 支持外部传入OkHttpClient
     */
    private var mClient: OkHttpClient =
            if (builder.mClient == null) OkHttpClient() else builder.mClient!!
    /**
     * 支持SSL
     */
    private var mSslSocketFactory: SSLSocketFactory? = null
    private var mTrustManager: X509TrustManager? = null
    /**
     * 重连间隔时间
     */
    private var mReconnectInterval: Long =
            if (builder.mReconnectInterval == 0L) 6L else builder.mReconnectInterval
    /**
     * 重连间隔时间的单位
     */
    private var mReconnectIntervalTimeUnit: TimeUnit =
            if (builder.mReconnectIntervalTimeUnit == null) TimeUnit.SECONDS else builder.mReconnectIntervalTimeUnit!!
    /**
     * 具体干活的实现类
     */
    private var mWorkerImpl: WebSocketWorker? = null

    init {
        mSslSocketFactory = builder.mSslSocketFactory
        mTrustManager = builder.mTrustManager
        setup()
    }

    /**
     * 开始配置
     */
    private fun setup() {
        mWorkerImpl = WebSocketWorkerImpl(
                mContext,
                mClient,
                mSslSocketFactory,
                mTrustManager,
                mReconnectInterval,
                mReconnectIntervalTimeUnit)
    }

    override operator fun get(url: String): Observable<WebSocketInfo> {
        return mWorkerImpl!!.get(url)
    }

    override operator fun get(url: String, timeout: Long, timeUnit: TimeUnit): Observable<WebSocketInfo> {
        return mWorkerImpl!!.get(url, timeout, timeUnit)
    }

    override fun send(msg: String): Observable<Boolean> {
        return mWorkerImpl!!.send(msg)
    }

    override fun send(byteString: ByteString): Observable<Boolean> {
        return mWorkerImpl!!.send(byteString)
    }

    override fun close(): Observable<Boolean> {
        return mWorkerImpl!!.close()
    }

    override fun closeNow(): Boolean {
        return mWorkerImpl!!.closeNow()
    }

    override fun heartBeat(url: String, period: Long, unit: TimeUnit, heartMsg: String): Observable<Boolean> {
        return mWorkerImpl!!.heartBeat(url, period, unit, heartMsg)
    }

    /**
     * 状态
     */
    enum class RxWebSocketStatus {
        CONNECT, RECONNECT, RECEIVE_MSG, RECEIVE_BYTE_MSG, CLOSED, PREPARE_RECONNECT
    }
}