package com.lixiaoyun.aike.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.lixiaoyun.aike.AKApplication
import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.service.KeepAliveService
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandleLogEntity
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandlePostLog
import com.lixiaoyun.aike.widget.AKWebView
import com.orhanobut.logger.Logger
import com.yanzhenjie.album.api.widget.Widget
import com.yanzhenjie.permission.runtime.Permission
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.regex.Pattern

/**
 * 扩展方法
 * 按备注索引
 *
 * 验证手机号
 * 验证密码是否符合
 * 密码校验规则
 * 邮箱规则验证
 * String判空
 * 改变窗口透明度
 * toast()
 * 点击防抖
 * 是否快速触发
 * 根据路径获取外部存储文件
 * 根据路径获取内部存储files里的文件
 * 根据路径获取内部存储cache里的文件
 * 通过设置全屏，设置状态栏透明
 * 打开第三方软件
 * 打开本地文件
 * 打开网络文件
 * 判断文件类型
 * 读取assets文件夹的json字符串
 * 创建图片选择器的Widget
 * 获取颜色
 * H5 CallBack
 * String去除空格短横线下划线
 * 打印json字符串
 * 拨打电话
 * 拨打电话（sim卡）
 * 获取当前sim卡数量
 * 开启前台服务
 */

/**
 * 验证手机号
 * @param msg 提示信息
 * @return boolean
 */
fun String?.isPhoneNumOK(msg: String = "请输入11位手机号码"): Boolean {
    return if (this.isNullOrBlank() || this.trim().length != 11) {
        msg.toast()
        false
    } else {
        true
    }
}

/**
 * 验证密码是否符合
 * @param matchLetter 是否校验密码规则
 * @param msg 提示信息
 * @return boolean
 */
fun String?.isPasswordOK(matchLetter: Boolean, msg: String = "密码不能低于8位"): Boolean {
    return if (matchLetter) {
        if (this.isNullOrBlank() || this.trim().length < 8 || !this.matchesInput()) {
            "密码长度为8-16位，且必须包含数字及大、小写字母".toast()
            false
        } else {
            true
        }
    } else {
        if (this.isNullOrBlank() || this.trim().length < 8) {
            msg.toast()
            false
        } else {
            true
        }
    }
}

/**
 * 密码校验规则：（1）密码必须包含大小写和数字 （2）允许特殊符号 （3）8-16位
 * @return boolean
 */
fun String.matchesInput(): Boolean {
    val reTxtPwd = "^(?=.*[0-9].*)(?=.*[A-Z].*)(?=.*[a-z].*).{8,16}$"
    val p = Pattern.compile(reTxtPwd)
    val m = p.matcher(this)
    return m.matches()
}

/**
 * 验证邮箱
 * @return boolean
 */
fun String.checkEmail(): Boolean {
    val pattern = Pattern.compile(
            "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$")
    val matcher = pattern.matcher(this)
    return matcher.matches()
}

/**
 * String判空
 * @return boolean
 */
fun String?.empty(): Boolean {
    return TextUtils.isEmpty(this)
}

/**
 * String相等判断
 * @param other
 * @return boolean
 */
fun String?.isSame(other: String?): Boolean {
    return TextUtils.equals(this, other)
}

/**
 * 改变窗口透明度
 *
 * @param alpha float
 */
fun Activity.changeWindowAlpha(alpha: Float) {
    val layoutParams = this.window.attributes
    layoutParams.alpha = alpha
    this.window.attributes = layoutParams
}

/**
 * toast(String)
 */
fun String.toast() {
    ToastUtils.instance.showToast(this)
}

/**
 * toast(Int)
 */
fun Int.toast() {
    ToastUtils.instance.showToast(this)
}

/**
 * 点击防抖
 */
var currentHash: Int = 0
var lastClickTime: Long = 0
fun View.clickAntiShake(intervals: Long = 1000, clickAction: () -> Unit) {
    this.setOnClickListener {
        if (this.hashCode() != currentHash) {
            currentHash = this.hashCode()
            lastClickTime = System.currentTimeMillis()
            clickAction()
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > intervals) {
                lastClickTime = System.currentTimeMillis()
                clickAction()
            }
        }
    }
}

/**
 * 是否快速触发
 */
var lastTriggerTime: Long = 0

fun isFastTrigger(duration: Long): Boolean {
    val time = System.currentTimeMillis()
    val timeD = time - lastTriggerTime
    if (timeD in 1 until duration) {
        return true
    }
    lastTriggerTime = time
    return false
}

/**
 * 根据路径获取外部存储文件
 */
fun String.getExternalStorageFile(): File {
    val path = Environment.getExternalStorageDirectory().absolutePath + this
    val file = File(path)
    if (!file.exists()) {
        file.mkdirs()
    }
    return file
}

/**
 * 根据路径获取内部存储files里的文件
 */
fun String.getInternalStorageFile(): File {
    var file = AKApplication.instance.applicationContext.getExternalFilesDir(this)

    if (file == null) {
        file = this.getExternalStorageFile()
    }

    return file
}

/**
 * 根据路径获取内部存储cache里的文件
 */
fun String.getInternalStorageCache(): File {
    val baseCacheFile = AKApplication.instance.applicationContext.externalCacheDir

    return if (baseCacheFile == null) {
        this.getExternalStorageFile()
    } else {
        val path = baseCacheFile.absolutePath + this
        val file = File(path)
        if (!file.exists()) {
            file.mkdirs()
        }
        return file
    }

}

/**
 * 通过设置全屏，设置状态栏透明
 */
fun Activity.fullScreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            val decorView = window.decorView
            val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            decorView.systemUiVisibility = option
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        } else {
            val window = this.window
            val attributes = window.attributes
            val flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            attributes.flags = attributes.flags or flagTranslucentStatus
            window.attributes = attributes
        }
    }
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
        val window = this.window
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}

/**
 * 打开第三方软件
 * @receiver Context
 * @param packageName String
 */
fun Context.openThirdSoft(packageName: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setPackage(packageName)
    this.startActivity(intent)
}

/**
 *  e.g
 *  var path = Environment.getExternalStorageDirectory().toString() + "/文档/微问家员工手册.pdf"
 *  var file = File(path)
 *  openThirdFileWithUrl(applicationContext, file)
 *
 *
 * 打开本地文件
 *
 * @param file 文件路径
 */
fun Context.openThirdFile(context: Context, file: Any) {
    try {
        //设置参数
        val intent = Intent()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.action = Intent.ACTION_VIEW
        //判断文件类型
        when (file) {
            is File -> {
                //设置文件type，用第三方打开
                val type = file.getMIMEType(context)
                intent.setDataAndType(Uri.fromFile(file), type)
                //跳转
            }
            is String -> {
                //设置跳转data，用浏览器下载
                intent.data = Uri.parse(file)
                //跳转
            }
            else -> "此附件不支持！".toast()
        }
        this.startActivity(intent)
    } catch (e: Exception) {
        "附件不能打开，请下载相关软件！".toast()
    }
}

/**
 * 打开网络文件
 *
 * @param context 文件url
 */
fun String.openThirdFileWithUrl(context: Context) {
    try {
        //设置参数
        val intent = Intent()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.action = Intent.ACTION_VIEW
        //设置跳转data，用浏览器下载
        intent.data = Uri.parse(this)
        //跳转
        context.startActivity(intent)
    } catch (e: Exception) {
        "附件不能打开，请下载相关软件！".toast()
    }
}

/**
 * 判断文件类型
 *
 * @receiver File
 * @param context Context
 * @return String
 */
fun File.getMIMEType(context: Context): String {
    var type = "*/*"
    val fileName = this.name
    //获取后缀名前的分隔符"."在fName中的位置
    val dotIndex = fileName.lastIndexOf(".")
    if (dotIndex < 0) {
        return type
    }
    //获取文件的后缀名
    val end = fileName.substring(dotIndex, fileName.length).toLowerCase()
    if (end.isSame("")) {
        return type
    }
    //在MIME和文件类型的匹配表中找到对应的MIME类型
    val mimeType = context.resources.getStringArray(R.array.mimeType)
    val value = context.resources.getStringArray(R.array.mimeValue)

    for ((i, _) in mimeType.withIndex()) {
        if (end.isSame(mimeType[i])) {
            type = value[i]
            break
        }
    }
    return type
}

/**
 * 读取assets文件夹的json字符串
 * @receiver String
 * @return String
 */
fun String.readAssets(): String {
    //将json数据变成字符串
    val stringBuilder = StringBuilder()
    try {
        //获取assets资源管理器
        val assetManager = AKApplication.instance.resources.assets
        //通过管理器打开文件并读取
        val bf = BufferedReader(InputStreamReader(assetManager.open(this)))
        var line = bf.readLine()
        while (line != null) {
            stringBuilder.append(line)
            line = bf.readLine()
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return stringBuilder.toString()
}

/**
 * 创建图片选择器的Widget
 */
fun String.makeWidget(
        context: Context,
        barColorNormal: Int = R.color.colorPrimary,
        barColorLight: Int = R.color.white,
        lightColor: Int = R.color.colorPrimary,
        normalColor: Int = R.color.colorPrimary
): Widget {
    val bn = barColorNormal.getColor(context)
    val bl = barColorLight.getColor(context)
    val lc = lightColor.getColor(context)
    val nc = normalColor.getColor(context)
    return Widget.newDarkBuilder(context)
            .title(this)
            .statusBarColor(bn)
            .toolBarColor(bn)
            .navigationBarColor(bl)
            .mediaItemCheckSelector(nc, lc)
            .bucketItemCheckSelector(nc, lc)
            .build()
}

/**
 * 获取颜色
 * @receiver Int
 * @param context Context
 * @return Int
 */
fun Int.getColor(context: Context): Int {
    return ContextCompat.getColor(context, this)
}

/**
 * H5 CallBack
 * @receiver AKWebView.WVJBResponseCallback?
 * @param `object` Any
 */
fun AKWebView.WVJBResponseCallback?.onExResult(`object`: Any?) {
    if (`object` == null) {
        this?.onResult(null)
    } else {
        val resultStr = GsonUtil.instance.gsonString(`object`)
        Logger.d("上传到H5的信息: ${resultStr?.printJsonData()}")
        this?.onResult(resultStr)
    }
}

/**
 * String去除空格短横线下划线
 * @receiver String
 * @return String
 */
fun String.formatSUH(): String {
    return this
            .replace("\\s*".toRegex(), "")
            .replace("-".toRegex(), "")
            .replace("_".toRegex(), "")
            .replace("\\(".toRegex(), "")
            .replace("\\)".toRegex(), "")
}

/**
 * String去除空格短横线下划线括号
 * @receiver String
 * @return String
 */
fun String.formatSUHB(): String {
    return this
            .replace("\\s*".toRegex(), "")
            .replace("-".toRegex(), "")
            .replace("_".toRegex(), "")
            .replace("\\(".toRegex(), "")
            .replace("\\)".toRegex(), "")
}

/**
 * 打印json字符串
 * @receiver String
 * @return String
 */
fun String.printJsonData(): String {
    val message: String
    message = try {
        when {
            this.startsWith("{") -> {
                val jsonObject = JSONObject(this)
                jsonObject.toString(4)
            }
            this.startsWith("[") -> {
                val jsonArray = JSONArray(this)
                jsonArray.toString(4)
            }
            else -> this
        }
    } catch (e: JSONException) {
        this
    }
    return message
}

/**
 * 拨打电话
 * @receiver String
 */
@SuppressLint("MissingPermission")
fun String.callPhone(context: Context) {
    val uri = Uri.parse("tel:$this")
    val intent = Intent(Intent.ACTION_CALL, uri)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

/**
 * 拨打电话
 * @receiver String
 *
 * @param simCard 0-sim卡1；1-sim卡2；
 */
@SuppressLint("MissingPermission")
fun String.callPhone(context: Context, simCard: Int) {
    val uri = Uri.parse("tel:$this")
    val intent = Intent(Intent.ACTION_CALL, uri)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    val tm = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    val pl = tm.callCapablePhoneAccounts as List<PhoneAccountHandle>
    intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, pl[simCard])
    HandlePostLog.postLogBaseTopic(HandleLogEntity.TOPIC_CAROUSEL, HandleLogEntity.EVENT_CAROUSEL_INTENT_MSG, intent.extras.toString())
    context.startActivity(intent)
}

/**
 * 获取当前sim卡数量
 *
 * @receiver Context
 * @return Int
 */
@SuppressLint("MissingPermission")
fun Context.checkSimNum(permission: Boolean, callback: (simAmount: Int) -> Unit) {
    val sum = this.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
    if (permission) {
        callback(sum.activeSubscriptionInfoCount)
    } else {
        PermissionUtils.instance.checkPermissions(this, Permission.READ_PHONE_STATE) {
            if (it) {
                callback(sum.activeSubscriptionInfoCount)
            } else {
                "请开启获取手机信息权限".toast()
                callback(0)
            }
        }
    }
}

/**
 * 开启前台服务
 *
 * @receiver Context
 */
fun Context.openForegroundService() {
    val intent = Intent(this, KeepAliveService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        this.startForegroundService(intent)
    } else {
        this.startService(intent)
    }
}

/**
 * 关闭前台服务
 *
 * @receiver Context
 */
fun Context.stopForegroundService() {
    val intent = Intent(this, KeepAliveService::class.java)
    this.stopService(intent)
}