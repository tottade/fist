package com.lixiaoyun.aike.utils.socketUtils

import com.lixiaoyun.aike.entity.WebSocketInfo
import io.reactivex.Observable
import okio.ByteString
import java.util.concurrent.TimeUnit


/**
 * 定义WebSocket Api接口
 * @data on 2019-12-24
 */
interface WebSocketWorker {
    /**
     * 获取连接，并返回观察对象
     */
    fun get(url: String): Observable<WebSocketInfo>

    /**
     * 设置一个超时时间，在指定时间内如果没有收到消息，会尝试重连
     *
     * @param timeout  超时时间
     * @param timeUnit 超时时间单位
     */
    fun get(url: String, timeout: Long, timeUnit: TimeUnit): Observable<WebSocketInfo>

    /**
     * 发送，url的WebSocket已打开的情况下使用，否则会抛出异常
     *
     * @param msg 消息，看具体和后端协商的格式，一般为json
     */
    fun send(msg: String): Observable<Boolean>

    /**
     * 发送，同上
     *
     * @param byteString 信息类型为ByteString
     */
    fun send(byteString: ByteString): Observable<Boolean>

    /**
     * 关闭指定Url的连接
     */
    fun close(): Observable<Boolean>

    /**
     * 马上关闭指定Url的连接
     */
    fun closeNow(): Boolean

    /**
     * 心跳任务
     *
     * @param url String
     * @param period Int
     * @param unit TimeUnit
     * @param heartMsg: String,
     */
    fun heartBeat(url: String, period: Long, unit: TimeUnit, heartMsg: String): Observable<Boolean>
}