package com.lixiaoyun.aike.utils

import android.os.Handler
import android.os.Looper
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandleLogEntity
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandlePostLog
import com.orhanobut.logger.Logger
import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @data on 2019/11/19
 */

/**
 * 线程池封装
 */
class ThreadPoolManager private constructor() {

    companion object {
        val instance = ThreadPoolManager.holder
    }

    private object ThreadPoolManager {
        val holder = ThreadPoolManager()
    }

    private var executors: ExecutorService? = null

    /**
     * 在线程中执行
     *
     * @param runnable 要执行的runnable
     */
    fun execute(runnable: Runnable) {
        val executorService = getExecutorService()
        if (executorService != null) {
            // 优先使用线程池，提高效率
            executorService.execute(runnable)
        } else {
            // 线程池获取失败，则直接使用线程
            Thread(runnable).start()
        }
    }

    /**
     * 在主线程中执行
     *
     * @param runnable 要执行的runnable
     */
    fun executeInMainThread(runnable: Runnable) {
        Handler(Looper.getMainLooper()).post(runnable)
    }

    /**
     * 获取缓存线程池
     *
     * @return 缓存线程池服务
     */
    private fun getExecutorService(): ExecutorService? {
        if (executors == null) {
            try {
                executors = ThreadPoolExecutor(
                        0,
                        Integer.MAX_VALUE,
                        180L, TimeUnit.SECONDS,
                        SynchronousQueue<Runnable>())
            } catch (e: Exception) {
                val msg = "create thread service error: " + e.message
                HandlePostLog.postLogBaseTopic(HandleLogEntity.TOPIC_BASE, HandleLogEntity.EVENT_ERROR_MESSAGE, msg)
                Logger.e(msg)
            }
        }
        return executors
    }
}