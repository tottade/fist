package com.lixiaoyun.aike.listener

/**
 * 请求权限Listener
 */
interface DownLoadListener {
    /**
     * 开始
     */
    fun onStart()

    /**
     * 成功
     */
    fun onSuccess()

    /**
     * 失败
     */
    fun onFail(e: String)
}