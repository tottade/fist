package com.lixiaoyun.aike.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.NonNull
import android.text.TextUtils
import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.constant.KeySet
import com.lixiaoyun.aike.listener.PermissionListener
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission

/**
 * AndPermission.with(this)
 *         .runtime()
 *         .permission(
 *                 Permission.ACCESS_COARSE_LOCATION,
 *                 Permission.ACCESS_FINE_LOCATION,
 *                 Permission.RECORD_AUDIO,
 *                 Permission.CAMERA,
 *                 Permission.READ_PHONE_STATE,
 *                 Permission.CALL_PHONE,
 *                 Permission.READ_EXTERNAL_STORAGE,
 *                 Permission.WRITE_EXTERNAL_STORAGE
 *         ).rationale { context, data, executor ->
 *             //被拒绝时操作
 *             val permissionNames = Permission.transformText(context, data)
 *             val message = context.getString(R.string.message_permission_rationale, TextUtils.join("\n", permissionNames))
 *             AlertDialog.Builder(context)
 *                     .setCancelable(false)
 *                     .setTitle("提示")
 *                     .setMessage(message)
 *                     .setPositiveButton("继续") { _, _ -> executor.execute() }
 *                     .setNegativeButton("取消") { _, _ -> executor.cancel() }
 *                     .show()
 *         }.onGranted { it ->
 *             //同意
 *             it.forEach {
 *                 Logger.e(it)
 *             }
 *             Logger.e("onGranted --- Permission Success")
 *         }.onDenied { permissions ->
 *             //拒绝
 *             ToastUtils.instance.showToast("未授权")
 *             if (AndPermission.hasAlwaysDeniedPermission(this, permissions)) {
 *                 //一直拒绝时
 *                 val permissionNames = Permission.transformText(this, permissions)
 *                 val message = this.getString(
 *                         R.string.message_permission_always_failed,
 *                         TextUtils.join("\n", permissionNames)
 *                 )
 *                 AlertDialog.Builder(this)
 *                         .setCancelable(false)
 *                         .setTitle("提示")
 *                         .setMessage(message)
 *                         .setPositiveButton("设置") { _, _ ->
 *                             //前往设置页面
 *                             AndPermission.with(this).runtime().setting().start(KeySet.REQUEST_PERMISSIONS_CODE)
 *                         }.setNegativeButton("取消") { _, _ ->
 *                             finish()
 *                         }.show()
 *             } else {
 *                 //前往设置页面
 *                 AndPermission.with(this).runtime().setting().start(KeySet.REQUEST_PERMISSIONS_CODE)
 *                 finish()
 *             }
 *         }.start()
 *
 * 权限工具
 */

class PermissionUtils private constructor() {
    companion object {
        val instance = SingletonHolder.holder
        const val PERMISSION_SUCCESS = "permission_success"
        const val PERMISSION_DENIED = "permission_denied"
        const val PERMISSION_TO_SETTING = "permission_to_setting"
    }

    private object SingletonHolder {
        val holder = PermissionUtils()
    }

    fun getPermissionGroups(context: Context, permissionListener: PermissionListener, @NonNull permissionsGroup: ArrayList<Array<String>>) {
        val permissions = ArrayList<String>()
        for (group in permissionsGroup) {
            for (withIndex in group.withIndex()) {
                permissions.add(withIndex.value)
            }
        }
        val permissionsList = arrayOfNulls<String>(permissions.size)
        for ((i, v) in permissions.withIndex()) {
            permissionsList[i] = v
        }
        AndPermission.with(context)
                .runtime()
                .permission(permissionsList.joinToString()).rationale { rationaleContext, data, executor ->
                    //被拒绝时操作
                    permissionListener.onRationale(rationaleContext, data, executor)
                }.onGranted {
                    //同意
                    permissionListener.onGranted(it)
                }.onDenied {
                    //拒绝
                    if (AndPermission.hasAlwaysDeniedPermission(context, it)) {
                        //不再提示
                        permissionListener.onDeniedAlways(context, it)
                    } else {
                        permissionListener.onDenied(context, it)
                    }
                }.start()
    }

    /**
     * 获取权限
     * @param context Context
     * @param permissionListener PermissionListener
     */
    fun getPermissions(context: Context, permissionListener: PermissionListener, @NonNull vararg permissions: String) {
        AndPermission.with(context)
                .runtime()
                .permission(permissions)
                .rationale { rationaleContext, data, executor ->
                    //被拒绝时操作
                    permissionListener.onRationale(rationaleContext, data, executor)
                }.onGranted {
                    //同意
                    permissionListener.onGranted(it)
                }.onDenied {
                    //拒绝
                    if (AndPermission.hasAlwaysDeniedPermission(context, it)) {
                        //不再提示
                        permissionListener.onDeniedAlways(context, it)
                    } else {
                        permissionListener.onDenied(context, it)
                    }
                }.start()
    }

    /**
     * 获取权限，如果没有开启则进入设置界面
     * @param context Context
     */
    fun checkPermissions(context: Context, @NonNull vararg permissions: String, callback: (success: Boolean) -> Unit) {
        AndPermission.with(context)
                .runtime()
                .permission(permissions)
                .rationale { rationaleContext, data,
                             executor ->
                    //被拒绝时操作
                    val permissionNames = Permission.transformText(context, data)
                    val message = context.getString(R.string.message_permission_rationale,
                            TextUtils.join("\n", permissionNames))
                    AlertDialog.Builder(rationaleContext)
                            .setCancelable(false)
                            .setTitle("提示")
                            .setMessage(message)
                            .setPositiveButton("继续") { _, _ ->
                                executor.execute()
                            }
                            .setNegativeButton("取消") { _, _ ->
                                executor.cancel()
                            }.show()
                }.onGranted {
                    //同意
                    callback(true)
                }.onDenied {
                    //拒绝
                    callback(false)
                    AndPermission.with(context).runtime().setting().start(KeySet.REQUEST_PERMISSIONS_CODE)
                }.start()
    }

    /**
     * 检测悬浮窗权限
     * @param context Context
     */
    fun checkFloating(context: Context, callback: (success: Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(context)) {
            context.getString(R.string.message_permission_float_window).toast()
            callback(false)
        } else {
            callback(true)
        }
    }

    /**
     * 跳转到设置页面
     * @param context Context
     */
    fun jump2Settings(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.data = Uri.parse("package:" + context.packageName)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}