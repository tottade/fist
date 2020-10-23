package com.lixiaoyun.aike.utils.socketUtils

import android.content.Context
import android.os.Looper
import android.os.SystemClock
import com.lixiaoyun.aike.AKApplication
import com.lixiaoyun.aike.entity.WebSocketInfo
import com.lixiaoyun.aike.entity.WebSocketMsg
import com.lixiaoyun.aike.network.NetStateMonitor
import com.lixiaoyun.aike.utils.DateUtils
import com.lixiaoyun.aike.utils.GsonUtil
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandleLogEntity
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandlePostLog
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager


/**
 * 具体实现类
 *
 * @data on 2019-12-24
 */
class WebSocketWorkerImpl(
        private var mContext: Context,
        private var mClient: OkHttpClient,
        mSslSocketFactory: SSLSocketFactory?,
        mTrustManager: X509TrustManager?,
        private var mReconnectInterval: Long,
        private var mReconnectIntervalTimeUnit: TimeUnit) : WebSocketWorker {

    private var mObservableWebSocketInfo: Observable<WebSocketInfo>? = null
    private var mWebSocketInfo: WebSocketInfo = WebSocketInfo()
    private var mWebSocket: WebSocket? = null

    init {
        //添加ssl
        if (mSslSocketFactory != null && mTrustManager != null) {
            mClient = mClient.newBuilder().sslSocketFactory(mSslSocketFactory, mTrustManager).build()
        }
    }

    /**
     * 获取WebSocket，建立重试机制
     * @param url String
     * @return Observable<WebSocketInfo>
     */
    override fun get(url: String): Observable<WebSocketInfo> {
        return getWebSocketInfo(url)
    }

    /**
     * 获取WebSocket，建立重试机制
     * @param url String
     * @param timeout Long 重试间隔
     * @param timeUnit TimeUnit
     * @return Observable<WebSocketInfo>
     */
    override fun get(url: String, timeout: Long, timeUnit: TimeUnit): Observable<WebSocketInfo> {
        return getWebSocketInfo(url, timeout, timeUnit)
    }

    /**
     * 发送消息
     *
     * @param msg String
     * @return Observable<Boolean>
     */
    override fun send(msg: String): Observable<Boolean> {
        return Observable.create {
            if (mWebSocket == null) {
                it.onError(Exception(RxWebSocket.ERROR_WS_NULL))
            } else {
                it.onNext(mWebSocket!!.send(msg))
            }
        }
    }

    /**
     * 发送消息
     *
     * @param byteString ByteString
     * @return Observable<Boolean>
     */
    override fun send(byteString: ByteString): Observable<Boolean> {
        return Observable.create {
            if (mWebSocket == null) {
                it.onError(Exception(RxWebSocket.ERROR_WS_NULL))
            } else {
                it.onNext(mWebSocket!!.send(byteString))
            }
        }
    }

    /**
     * 关闭连接
     *
     * @return Observable<Boolean>
     */
    override fun close(): Observable<Boolean> {
        return Observable.create(ObservableOnSubscribe<WebSocket> {
            if (mWebSocket == null) {
                it.onError(Exception(RxWebSocket.ERROR_WS_NULL))
            } else {
                it.onNext(mWebSocket!!)
            }
        }).map { webSocket ->
            closeWebSocket(webSocket)
        }
    }

    /**
     * 关闭连接
     *
     * @return Boolean
     */
    override fun closeNow(): Boolean {
        return closeWebSocket(mWebSocket)
    }

    /**
     * 构建心跳包观察者
     *
     * @param url String
     * @param period Long
     * @param unit TimeUnit
     * @param heartMsg String
     * @return Observable<Boolean>
     */
    override fun heartBeat(url: String, period: Long, unit: TimeUnit, heartMsg: String): Observable<Boolean> {
        return Observable.interval(period, unit)
                .timestamp()
                .flatMap {
                    val timesTamp = it.time()
                    Logger.d("发送心跳包: ${DateUtils.instance.millis2String(timesTamp)}")
                    sendHeartBeat()
                }
    }

    /**
     * 发送心跳包
     *
     * @return Observable<Boolean>
     */
    private fun sendHeartBeat(): Observable<Boolean> {
        val hasNetWork =
                AKApplication.instance.mNetState != NetStateMonitor.NetState.NETWORK_NOT_FIND
        return Observable.create {
            if (hasNetWork) {
                if (mWebSocket == null) {
                    it.onError(Exception(RxWebSocket.ERROR_WS_NULL))
                } else {
                    it.onNext(mWebSocket!!.send(GsonUtil.instance.gsonString(WebSocketMsg(3, msgId = DateUtils.instance.getNowString()))!!))
                }
            } else {
                it.onNext(false)
            }
        }
    }

    /**
     * @param url String
     * @return Observable<WebSocketInfo>
     */
    fun getWebSocketInfo(url: String): Observable<WebSocketInfo> {
        return getWebSocketInfo(url, mReconnectInterval, mReconnectIntervalTimeUnit)
    }

    /**
     *
     * @param url String
     * @param timeOut Int
     * @param timeUnit TimeUnit
     * @return Observable<WebSocketInfo>
     */
    fun getWebSocketInfo(url: String, timeOut: Long, timeUnit: TimeUnit): Observable<WebSocketInfo> {
        if (mObservableWebSocketInfo == null) {
            mObservableWebSocketInfo = Observable
                    .create(WebSocketOnSubscribe(url, timeOut, timeUnit))
                    .retry()
                    .doOnDispose {
                        closeNow()
                    }
                    //回调都放在主线成进行
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
        return mObservableWebSocketInfo!!
    }

    /**
     * 创建WebSocket，并建立重试机制
     *
     * @property url String
     * @property timeOut Long
     * @property timeUnit TimeUnit
     * @property isReconnecting Boolean
     * @constructor
     */
    inner class WebSocketOnSubscribe(private var url: String, private var timeOut: Long, private var timeUnit: TimeUnit) : ObservableOnSubscribe<WebSocketInfo> {

        private var webSocketSubscribe: WebSocket? = null
        //是否正在重连
        private var isReconnecting = false

        override fun subscribe(emitter: ObservableEmitter<WebSocketInfo>) {
            //如果当前连接为空，别切正在重连
            if (webSocketSubscribe == null && isReconnecting) {
                //如果当前线程不再主线程
                if (Thread.currentThread() != Looper.getMainLooper().thread) {
                    //由于retry没有延时机制，需要手动睡眠线程
                    var millis = timeUnit.toMillis(timeOut)
                    if (millis == 0L) {
                        millis = 1000L
                    }
                    SystemClock.sleep(millis)
                }
            }
            //创建连接
            initWebSocket(emitter)
        }

        /**
         * 创建连接请求
         *
         * @param url String
         * @return Request
         */
        private fun createRequest(url: String): Request {
            return Request.Builder().get().url(url).build()
        }

        /**
         * 创建连接
         *
         * @param emitter ObservableEmitter<WebSocketInfo>
         */
        @Synchronized
        private fun initWebSocket(emitter: ObservableEmitter<WebSocketInfo>) {
            if (webSocketSubscribe == null) {
                webSocketSubscribe = mClient.newWebSocket(createRequest(url), object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        super.onOpen(webSocket, response)
                        //连接成功
                        if (!emitter.isDisposed) {
                            mWebSocket = webSocketSubscribe
                            if (isReconnecting) {
                                //如果正在重连
                                emitter.onNext(createReconnect(url, webSocket))
                                HandlePostLog.postLogSocketMsg(HandleLogEntity.TOPIC_SOCKET_MSG, HandleLogEntity.EVENT_SOCKET_STATUS, url, "重新连接")
                            } else {
                                emitter.onNext(createConnect(url, webSocket))
                                HandlePostLog.postLogSocketMsg(HandleLogEntity.TOPIC_SOCKET_MSG, HandleLogEntity.EVENT_SOCKET_STATUS, url, "连接成功")
                            }
                        }
                        isReconnecting = false
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        super.onMessage(webSocket, text)
                        //收到消息
                        if (!emitter.isDisposed) {
                            emitter.onNext(createReceiveStringMsg(url, webSocket, text))
                            HandlePostLog.postLogSocketMsg(HandleLogEntity.TOPIC_SOCKET_RECEIVE_MSG, HandleLogEntity.EVENT_SOCKET_STATUS, url,
                                    "接收消息:$text")
                        }
                    }

                    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                        super.onMessage(webSocket, bytes)
                        //收到消息
                        if (!emitter.isDisposed) {
                            emitter.onNext(createReceiveByteStringMsg(url, webSocket, bytes))
                        }
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        super.onClosed(webSocket, code, reason)
                        //关闭连接
                        if (!emitter.isDisposed) {
                            emitter.onNext(createClosed())
                            HandlePostLog.postLogSocketMsg(HandleLogEntity.TOPIC_SOCKET_MSG, HandleLogEntity.EVENT_SOCKET_STATUS, url, "关闭连接")
                        }
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        super.onFailure(webSocket, t, response)
                        //连接失败，清空信息
                        isReconnecting = true
                        webSocketSubscribe = null
                        mWebSocket = null
                        if (!emitter.isDisposed) {
                            HandlePostLog.postLogSocketMsg(HandleLogEntity.TOPIC_SOCKET_MSG, HandleLogEntity.EVENT_SOCKET_STATUS, url, "连接失败")
                            emitter.onNext(createPrepareReconnect(url))
                            emitter.onError(Exception(RxWebSocket.ERROR_PREPARE_RECONNECT))
                        }
                    }
                })
            }
        }
    }

    fun createConnect(url: String, webSocket: WebSocket): WebSocketInfo {
        mWebSocketInfo.reset()
        mWebSocketInfo.isConnect = true
        mWebSocketInfo.webSocket = webSocket
        mWebSocketInfo.url = url
        mWebSocketInfo.status = RxWebSocket.RxWebSocketStatus.CONNECT
        return mWebSocketInfo
    }

    fun createReconnect(url: String, webSocket: WebSocket): WebSocketInfo {
        mWebSocketInfo.reset()
        mWebSocketInfo.isReconnect = true
        mWebSocketInfo.webSocket = webSocket
        mWebSocketInfo.url = url
        mWebSocketInfo.status = RxWebSocket.RxWebSocketStatus.RECONNECT
        return mWebSocketInfo
    }

    private fun createReceiveStringMsg(url: String, webSocket: WebSocket, text: String): WebSocketInfo {
        mWebSocketInfo.reset()
        mWebSocketInfo.isConnect = true
        mWebSocketInfo.webSocket = webSocket
        mWebSocketInfo.url = url
        mWebSocketInfo.msg = text
        mWebSocketInfo.status = RxWebSocket.RxWebSocketStatus.RECEIVE_MSG
        return mWebSocketInfo
    }

    private fun createReceiveByteStringMsg(url: String, webSocket: WebSocket, bytes: ByteString): WebSocketInfo {
        mWebSocketInfo.reset()
        mWebSocketInfo.isConnect = true
        mWebSocketInfo.webSocket = webSocket
        mWebSocketInfo.url = url
        mWebSocketInfo.msgByteString = bytes
        mWebSocketInfo.status = RxWebSocket.RxWebSocketStatus.RECEIVE_BYTE_MSG
        return mWebSocketInfo
    }

    private fun createClosed(): WebSocketInfo {
        mWebSocketInfo.reset()
        mWebSocketInfo.status = RxWebSocket.RxWebSocketStatus.CLOSED
        return mWebSocketInfo
    }

    private fun createPrepareReconnect(url: String): WebSocketInfo {
        mWebSocketInfo.reset()
        mWebSocketInfo.isPrepareReconnect = true
        mWebSocketInfo.url = url
        mWebSocketInfo.status = RxWebSocket.RxWebSocketStatus.PREPARE_RECONNECT
        return mWebSocketInfo
    }

    private fun closeWebSocket(webSocket: WebSocket?): Boolean {
        Logger.d("-----------------关闭连接-----------------")
        if (webSocket == null) {
            return false
        }
        val result = webSocket.close(1000, "CLOSE")
        if (result) {
            mObservableWebSocketInfo = null
            mWebSocket = null
        }
        return result
    }
}
