package com.lixiaoyun.aike.listener

import android.content.Context
import com.yanzhenjie.permission.RequestExecutor

/**
 * 请求权限Listener
 */
interface PermissionListener {
    /**
     * 请求的权限被拒绝时
     */
    fun onRationale(context: Context, data: MutableList<String>, executor: RequestExecutor)

    /**
     * 请求的所有权限通过时
     */
    fun onGranted(it: List<String>)

    /**
     * 权限拒绝不再提示
     */
    fun onDeniedAlways(context: Context, permissions: List<String>)

    /**
     * 被拒绝
     */
    fun onDenied(context: Context, permissions: List<String>)

}