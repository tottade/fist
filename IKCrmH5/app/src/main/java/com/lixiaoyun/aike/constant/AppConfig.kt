package com.lixiaoyun.aike.constant

import android.content.Context
import com.lixiaoyun.aike.BuildConfig
import com.lixiaoyun.aike.activity.LoginActivity
import com.lixiaoyun.aike.entity.ResponseUserInfo
import com.lixiaoyun.aike.utils.CacheDiskStaticUtils
import com.lixiaoyun.aike.utils.SPUtils
import com.lixiaoyun.aike.utils.stopForegroundService

/**
 * 配置与程序相关的常量
 */
class AppConfig {
    companion object {
        //调试模式
        const val DEBUG = BuildConfig.LOG_DEBUG

        //SharedPreferences文件名称
        const val DEF_SP_NAME = "AppInfo"

        //WebView缓存地址
        const val WEB_VIEW_CACHE_PATH = "/${BuildConfig.FLAVOR}/webCache/"
        //数据缓存地址
        const val WEB_DATA_CACHE_PATH = "/${BuildConfig.FLAVOR}/webDataCache/"
        //aike照片地址
        const val AIKE_PHOTO_PATH = "/${BuildConfig.FLAVOR}/photos"
        //aike图片压缩地址
        const val ZIP_PIC_FILE = "/${BuildConfig.FLAVOR}/zip-p"

        //aike照片后缀
        const val AIKE_PHOTO_SUFFIX = "_aike_photo"
        //aike水印照片后缀
        const val AIKE_PHOTO_WATERMARK_SUFFIX = "_mark"

        //aike录音文件保存地址
        const val AIKE_AUDIO_PATH = "/${BuildConfig.FLAVOR}/audios/recorder"
        //aike录音文件后缀
        const val AIKE_AUDIO_SUFFIX = "_aike_audio"
        //下载的录音文件
        const val RECORD_PATH_FILE = "down_record_path.json"
        const val RECORD_PATH_URL = "https://recoding.weiwenjia.com/record_path.json"
        //请求阿里云相关
        const val ALIYUN_TOKEN_URL = "http://log-token.weiwenjia.com/"
        const val ALIYUN_END_POINT = "http://cn-shanghai.log.aliyuncs.com"
        const val ALIYUN_PROJECT_NAME = "wwj-app-log"
        const val ALIYUN_STORE_NAME = "app-logs-prod"

        //推送类型
        const val PUSH_TYPE_GT = "igetui"
        const val PUSH_TYPE_MI = "xiaomi"
        const val PUSH_TYPE_HW = "huawei"

        //前台服务
        const val FOREGROUND_SERVICE_ID = 1757
        const val FOREGROUND_NOTIFICATION_CHANNEL = "Keep_alive_${BuildConfig.APPLICATION_ID}"

        //通知栏通道id
        const val NOTIFICATION_CHANNEL = BuildConfig.APPLICATION_ID
        //通知栏通道id
        const val NOTIFICATION_ID = 9613

        //正在通话中
        var PhoneTalking = false
        //是否正在上传
        var UpDataRecording = false
        var UpDataRecordingPhone = ""
        var UpDataRecordingDuration = 0
        var UpDataRecordingPath = ""
        //是否正在重传
        var ReUpDataRecording = false
        var ReUpDataRecordingPhone = ""
        var ReUpDataRecordingDuration = 0
        var ReUpDataRecordingPath = ""
        //缓存的动态id
        var SalesDynamicsId: Long = 0L

        //---分享相关
        const val APP_ID_WX_LX = "wx5d390663adb647b2"
        const val APP_SECRET_WX_LX = "3d3358d475b5805dc6161453d552a11c"

        //---分享相关
        const val APP_ID_WX_IK = "wxadcad31232c6802e"
        const val APP_SECRET_WX_IK = "d9c33d4e9551508e82b3fc7e606a59b2"

        const val APP_KEY_SINA = "3820261837"
        const val APP_SECRET_SINA = "bd1c2be97243995d9a5929880b032dc1"
        const val APP_URL_SINA = "http://sns.whalecloud.com"

        const val APP_ID_QQ = "1109201785"
        const val APP_KEY_QQ = "LtFrqQWALQugIS1c"
        //---分享相关

        /**
         * 用户登录Url类型
         */
        fun setUrlType(type: Int) {
            SPUtils.instance.saveValue(KeySet.KEY_URL_TYPE, type)
        }

        fun getUrlType(): Int {
            return SPUtils.instance.getIntSp(KeySet.KEY_URL_TYPE) ?: 0
        }

        /**
         * 用户Token
         * @param token String
         */
        fun setUserToken(token: String) {
            SPUtils.instance.saveValue(KeySet.KEY_USER_TOKEN, token)
        }

        fun getUserToken(): String {
            return SPUtils.instance.getStringSp(KeySet.KEY_USER_TOKEN) ?: ""
        }

        /**
         * crm_app_token (搜客宝)
         * @param token String
         */
        fun setCATToken(token: String) {
            SPUtils.instance.saveValue(KeySet.KEY_CAT_TOKEN, token)
        }

        fun getCATToken(): String {
            return SPUtils.instance.getStringSp(KeySet.KEY_CAT_TOKEN) ?: ""
        }

        /**
         * 用户Id
         * @param id Int
         */
        fun setUserId(id: Int) {
            SPUtils.instance.saveValue(KeySet.KEY_USER_ID, id)
        }

        fun getUserId(): Int {
            return SPUtils.instance.getIntSp(KeySet.KEY_USER_ID) ?: 0
        }

        /**
         * 用户uid
         * @param uid Int
         */
        fun setUId(uid: Int) {
            SPUtils.instance.saveValue(KeySet.KEY_USER_UID, uid)
        }

        fun getUId(): Int {
            return SPUtils.instance.getIntSp(KeySet.KEY_USER_UID) ?: 0
        }

        /**
         * 用户Login
         * @param login String
         */
        fun setUserLogin(login: String) {
            SPUtils.instance.saveValue(KeySet.KEY_USER_LOGIN, login)
        }

        fun getUserLogin(): String {
            return SPUtils.instance.getStringSp(KeySet.KEY_USER_LOGIN) ?: ""
        }

        /**
         * 用户Token
         * @param token String
         */
        fun setUserPsw(token: String) {
            SPUtils.instance.saveValue(KeySet.KEY_USER_PSW, token)
        }

        fun getUserPsw(): String {
            return SPUtils.instance.getStringSp(KeySet.KEY_USER_PSW) ?: ""
        }

        /**
         * 用户推送标示
         * @param token String
         */
        fun setPushClientId(token: String) {
            SPUtils.instance.saveValue(KeySet.KEY_PUSH_CLIENT_ID, token)
        }

        fun getPushClientId(): String {
            return SPUtils.instance.getStringSp(KeySet.KEY_PUSH_CLIENT_ID) ?: ""
        }

        /**
         * 用户推送类型
         * @param type String
         */
        fun setPushType(type: String) {
            SPUtils.instance.saveValue(KeySet.KEY_PUSH_TYPE, type)
        }

        fun getPushType(): String {
            return SPUtils.instance.getStringSp(KeySet.KEY_PUSH_TYPE) ?: ""
        }

        /**
         * 保存本地录音文件夹地址
         * @param path String
         */
        fun setFindRecordPath(path: String) {
            SPUtils.instance.saveValue(KeySet.KEY_FIND_RECORD_PATH, path)
        }

        fun getFindRecordPath(): String {
            return SPUtils.instance.getStringSp(KeySet.KEY_FIND_RECORD_PATH) ?: ""
        }

        /**
         * 用户设置WebView字体大小
         * @param id Int
         */
        fun setWebTextSize(size: Int) {
            SPUtils.instance.saveValue(KeySet.KEY_WEB_TEXT_SIZE, size)
        }

        fun getWebTextSize(): Int {
            return SPUtils.instance.getIntSp(KeySet.KEY_WEB_TEXT_SIZE) ?: 0
        }

        /**
         * 用户设置呼叫中心
         * @param turnOn Boolean
         */
        fun setCallCenter(turnOn: Boolean) {
            SPUtils.instance.saveValue(KeySet.KEY_CALL_CENTER, turnOn)
        }

        fun getCallCenter(): Boolean {
            return SPUtils.instance.getBoolSp(KeySet.KEY_CALL_CENTER) ?: false
        }

        /**
         * 用户上次进入主页上报时间
         * @param time String
         */
        fun setLastCrmIndexTime(uid: Int, time: String) {
            SPUtils.instance.saveValue(uid.toString(), time)
        }

        fun getLastCrmIndexTime(uid: Int): String {
            return SPUtils.instance.getStringSp(uid.toString()) ?: ""
        }

        /**
         * 用户信息
         * @param userInfo ResponseUserInfo
         */
        fun setUserInfo(userInfo: ResponseUserInfo?) {
            CacheDiskStaticUtils.put(KeySet.CACHE_KEY_USER_INFO, userInfo)
        }

        fun getUserInfo(): ResponseUserInfo? {
            return CacheDiskStaticUtils.getParcelable<ResponseUserInfo>(KeySet.CACHE_KEY_USER_INFO, ResponseUserInfo.CREATOR)
        }

        /**
         * 清除缓存信息
         * @return Boolean
         */
        fun clearCache(): Boolean {
            return CacheDiskStaticUtils.clear()
        }

        /**
         * 清除缓存信息
         * @param key String
         * @return Boolean
         */
        fun clearCache(key: String): Boolean {
            return CacheDiskStaticUtils.remove(key)
        }

        /**
         * 清除用户信息
         */
        fun clearUserInfo() {
            //清除user_token
            SPUtils.instance.remove(KeySet.KEY_USER_TOKEN)
            //清除user_token
            SPUtils.instance.remove(KeySet.KEY_CAT_TOKEN)
            //清除user_id
            SPUtils.instance.remove(KeySet.KEY_USER_ID)
            //用户login
            SPUtils.instance.remove(KeySet.KEY_USER_LOGIN)
            //用户psw
            SPUtils.instance.remove(KeySet.KEY_USER_PSW)
            //用户呼叫中心权限
            SPUtils.instance.remove(KeySet.KEY_CALL_CENTER)
            //清除缓存
            clearCache()
        }

        fun clearSpValue(key: String) {
            SPUtils.instance.remove(key)
        }

        /**
         * 退出APP
         * @param context context
         */
        fun appLogOut(context: Context) {
            clearUserInfo()
            //关闭服务
            context.stopForegroundService()
            //去登陆页面
            LoginActivity.intentToLoginActivity(context)
        }
    }
}