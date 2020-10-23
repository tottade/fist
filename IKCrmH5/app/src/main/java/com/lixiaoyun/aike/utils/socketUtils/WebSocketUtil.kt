package com.lixiaoyun.aike.utils.socketUtils

import com.lixiaoyun.aike.AKApplication
import com.lixiaoyun.aike.entity.WebSocketMsg
import com.lixiaoyun.aike.pushutils.PushTypeHandle
import com.lixiaoyun.aike.utils.DateUtils
import com.lixiaoyun.aike.utils.GsonUtil
import com.lixiaoyun.aike.utils.empty
import com.orhanobut.logger.Logger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * WebSocket具体调用类
 *
 * @data on 2019-12-25
 */
class WebSocketUtil constructor(private var url: String) {

    /**
     * 总资源管理器
     */
    private val mDisposable = ArrayList<Disposable?>()

    /**
     * 心跳间隔
     */
    private val heartBeatTime = 22L

    /**
     * 连接client
     */
    private var mClient: OkHttpClient = OkHttpClient.Builder()
            //读取超时
            .readTimeout(3, TimeUnit.SECONDS)
            //写入超时
            .writeTimeout(3, TimeUnit.SECONDS)
            //连接超时
            .connectTimeout(3, TimeUnit.SECONDS)
            .build()

    /**
     * 构造webSocket实例
     */
    private var rxWebSocket: RxWebSocket = RxWebSocketBuilder(AKApplication.instance)
            .reconnectInterval(5, TimeUnit.SECONDS)
            .client(mClient)
            .build()

    /**
     * 当前连接资源和心跳资源
     */
    private var mWebSocketConnectDisposable: Disposable? = null
    private var mHeartBeatRequestDisposable: Disposable? = null

    /**
     * 最新的心跳时间
     */
    private var mLastPing: Long = 0L
    private var mLastPong: Long = 0L

    /**
     * 入口方法，调用连接
     */
    fun setWebSocketConnect() {
        if (mWebSocketConnectDisposable == null) {
            mWebSocketConnectDisposable = connectWebSocket()
            mDisposable.add(mWebSocketConnectDisposable)
        }
    }

    /**
     * 具体连接实现
     *
     * @return Disposable
     */
    private fun connectWebSocket(): Disposable {
        return rxWebSocket.get(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { webSocketInfo ->
                            when (webSocketInfo.status) {
                                RxWebSocket.RxWebSocketStatus.CONNECT -> {
                                    mLastPong = DateUtils.instance.getNowMills()
                                    Logger.d("处理连接成功")
                                    setHeartBeatConnect()
                                }
                                RxWebSocket.RxWebSocketStatus.RECONNECT -> {
                                    mLastPong = DateUtils.instance.getNowMills()
                                    Logger.d("处理重连")
                                    setHeartBeatConnect()
                                }
                                RxWebSocket.RxWebSocketStatus.PREPARE_RECONNECT -> {
                                    Logger.d("处理正在准备重连")
                                }
                                RxWebSocket.RxWebSocketStatus.CLOSED -> {
                                    Logger.d("处理连接关闭")
                                }
                                RxWebSocket.RxWebSocketStatus.RECEIVE_MSG -> {
                                    Logger.d("处理接收消息（String）")
                                    mLastPong = DateUtils.instance.getNowMills()
                                    if (!webSocketInfo.msg.empty()) {
                                        handlerReceiveMsg(webSocketInfo.msg)
                                    }
                                }
                                RxWebSocket.RxWebSocketStatus.RECEIVE_BYTE_MSG -> {
                                    Logger.d("处理接收消息（Byte）")
                                }
                            }
                        },
                        {
                            Logger.e("Socket连接ERROR: ${it.message}")
                            //重启连接
                            reConnectWebSocket()
                        }
                )
    }

    /**
     * 重连方法
     */
    private fun reConnectWebSocket() {
        if (mWebSocketConnectDisposable != null) {
            if (mWebSocketConnectDisposable!!.isDisposed) {
                //尝试重连
                mWebSocketConnectDisposable = null
                setWebSocketConnect()
            }
        } else {
            setWebSocketConnect()
        }
    }

    /**
     * 启动心跳连接
     */
    private fun setHeartBeatConnect() {
        if (mHeartBeatRequestDisposable == null) {
            mHeartBeatRequestDisposable = connectHeartBeat()
            mDisposable.add(mHeartBeatRequestDisposable)
        }
    }

    /**
     * 具体心跳实现
     *
     * @return Disposable
     */
    private fun connectHeartBeat(): Disposable {
        return rxWebSocket
                .heartBeat(url, heartBeatTime, TimeUnit.SECONDS, "There will set heart beat message.")
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            Logger.d("心跳包发送: ${if (it) "成功" else "失败"}")
                            mLastPing = DateUtils.instance.getNowMills()
                            if (!checkHeartBeatConnect()) {
                                Logger.d("心跳超时")
                                reConnectWebSocket()
                            }
                        },
                        {
                            Logger.e("心跳包发送ERROR: ${it.message}")
                            if (it.message == RxWebSocket.ERROR_WS_NULL) {
                                mHeartBeatRequestDisposable?.dispose()
                                mHeartBeatRequestDisposable = null
                                reConnectWebSocket()
                            } else {
                                reSetHeartBeatConnect()
                            }
                        }
                )
    }

    /**
     * 心跳是否超时
     *
     * @return Boolean
     */
    private fun checkHeartBeatConnect(): Boolean {
        return if (abs(mLastPing - mLastPong) > 89000) {
            false
        } else {
            mLastPing = DateUtils.instance.getNowMills()
            true
        }
    }

    /**
     * 心跳重连
     */
    private fun reSetHeartBeatConnect() {
        if (mHeartBeatRequestDisposable != null) {
            if (mHeartBeatRequestDisposable!!.isDisposed) {
                //尝试重连
                mHeartBeatRequestDisposable = null
                setWebSocketConnect()
            }
        } else {
            setWebSocketConnect()
        }
    }

    /**
     * 处理发送信息
     *
     * @param msg String?
     */
    private fun handlerReceiveMsg(msg: String?) {
        val context = AKApplication.instance.applicationContext
        val webSocketMsg: WebSocketMsg = GsonUtil.instance.gsonToBean(msg!!, WebSocketMsg::class.java)
        val msgData = webSocketMsg.msg
        when (webSocketMsg.cmd) {
            1 -> {
                //PUSH_DATA
                Logger.d("发送消息")
                //服务器推送消息后，需要返回一个Ack信息给服务端
                handlerSendMsg(GsonUtil.instance.gsonString(WebSocketMsg(2, msgId = webSocketMsg.msgId))!!)
                //解析消息
                if (msgData != null) {
                    val msgString = GsonUtil.instance.gsonString(msgData)
                    Logger.d("Websocket msgData: $msgString")
                    when (msgData.push_type) {
                        11 -> {
                            PushTypeHandle.actionTuneUpCall(context, msgString!!, 11)
                        }
                        12 -> {
                            PushTypeHandle.actionTuneUpCall(context, msgString!!, 12)
                        }
                    }
                } else {
                    Logger.e("msg为空")
                }
            }
            4 -> {
                //Pong
                Logger.d("接收到Pong消息.")
                mLastPong = DateUtils.instance.getNowMills()
            }
        }
    }

    private fun handlerSendMsg(msg: String) {
        val sendMsgDisposable = rxWebSocket
                .send(msg)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            Logger.d("消息发送成功!")
                        },
                        {
                            Logger.e("消息发送失败: ${it.message}")
                        }
                )
        mDisposable.add(sendMsgDisposable)
    }

    /**
     * 释放资源
     */
    private fun disposableConnect() {
        for (disposable in mDisposable) {
            disposable?.dispose()
        }
        mDisposable.clear()
    }

    /**
     * 关闭连接
     */
    fun closeConnect() {
        disposableConnect()
        mWebSocketConnectDisposable = null
        mHeartBeatRequestDisposable = null
    }
}