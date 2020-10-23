package com.lixiaoyun.aike.network

import com.lixiaoyun.aike.AKApplication
import com.lixiaoyun.aike.BuildConfig
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.utils.HardwareUtils

object NetWorkConfig {

    //headers
    const val HEADERS_PUSH_CONFIRM = "push_confirm"

    //设备
    const val DEVICE = "android"
    //版本
    @JvmField
    var VERSION_CODE = HardwareUtils.instance.getVersionName(AKApplication.instance.applicationContext)
    var VERSION_CODE_WITHOUT_V = HardwareUtils.instance.getVersionNameWithOutV(AKApplication.instance.applicationContext)

    //BaseUrl地址
    @JvmField
    var LX_BASE_URL = arrayOf(
            "http://lxcrm-dev.weiwenjia.com/",         //dev
            "http://lxcrm-test.weiwenjia.com/",        //test
            "http://lxcrm-test.weiwenjia.com/",        //test 搜客宝
            "http://lxcrm-staging.weiwenjia.com/",     //staging
            "https://lxcrm.weiwenjia.com/"            //production
    )

    @JvmField
    var IK_BASE_URL = arrayOf(
            "http://ik-dev.ikcrm.com/",         //dev
            "http://ik-test.ikcrm.com/",        //test
            "http://ik-test.ikcrm.com/",        //test 搜客宝
            "http://ik-staging.ikcrm.com/",     //staging
            "https://api.ikcrm.com/"            //production
    )

    @JvmField
    var LX_H5_HYBRID = arrayOf(
            "https://crmh5-dev.weiwenjia.com/lx_duli_hybrid_assets/index.html",           //dev
            "https://crmh5-test.weiwenjia.com/lx_duli_hybrid_assets/index.html",          //test
            "https://crmh5-test.weiwenjia.com/lx_duli_hybrid_assets2/index.html",         //test 搜客宝
            "https://crmh5-staging.weiwenjia.com/lx_duli_hybrid_assets/index.html",       //staging
            "https://crmh5.weiwenjia.com/lx_duli_hybrid_assets/index.html"                //production
    )

    @JvmField
    var IK_H5_HYBRID = arrayOf(
            "https://crmh5-dev.weiwenjia.com/duli_hybrid_assets/index.html",           //dev
            "https://crmh5-test.weiwenjia.com/duli_hybrid_assets/index.html",          //test
            "https://crmh5-test.weiwenjia.com/duli_hybrid_assets/index.html",          //test 搜客宝
            "https://crmh5-staging.weiwenjia.com/duli_hybrid_assets/index.html",       //staging
            "https://crmh5.weiwenjia.com/duli_hybrid_assets/index.html"                //production
    )

    //SocketURL地址
    @JvmField
    var LX_SOCKET_URL = arrayOf(
            "ws://dx-app-push-dev.weiwenjia.com/ws?",         //dev
            "ws://dx-app-push-test.weiwenjia.com/ws?",        //test
            "ws://dx-app-push-test.weiwenjia.com/ws?",        //test 搜客宝
            "ws://dx-app-push-staging.weiwenjia.com/ws?",     //staging
            "ws://dx-app-push.weiwenjia.com/ws?"            //production
    )

    @JvmField
    var IK_SOCKET_URL = arrayOf(
            "ws://dx-app-push-dev.weiwenjia.com/ws?",         //dev
            "ws://dx-app-push-test.weiwenjia.com/ws?",        //test
            "ws://dx-app-push-test.weiwenjia.com/ws?",        //test 搜客宝
            "ws://dx-app-push-staging.weiwenjia.com/ws?",     //staging
            "ws://dx-app-push.weiwenjia.com/ws?"            //production
    )

    @JvmField
    var APP_PUSH_CONFIRM_URL = arrayOf(
            "https://app-push-dev.ikcrm.com/",         //dev,test,staging
            "https://app-push.weiwenjia.com/"        //production
    )

    /**
     * 获取BaseUrl
     */
    fun getBaseUrl(): String {
        var baseUrl: String
        if (BuildConfig.IS_LX) {
            baseUrl = LX_BASE_URL[4]
            if (AppConfig.DEBUG) {
                when (AppConfig.getUrlType()) {
                    0 -> {
                        baseUrl = LX_BASE_URL[0]
                    }
                    1 -> {
                        baseUrl = LX_BASE_URL[1]
                    }
                    2 -> {
                        baseUrl = LX_BASE_URL[2]
                    }
                    3 -> {
                        baseUrl = LX_BASE_URL[3]
                    }
                    4 -> {
                        baseUrl = LX_BASE_URL[4]
                    }
                }
            }
        } else {
            baseUrl = IK_BASE_URL[4]
            if (AppConfig.DEBUG) {
                when (AppConfig.getUrlType()) {
                    0 -> {
                        baseUrl = IK_BASE_URL[0]
                    }
                    1 -> {
                        baseUrl = IK_BASE_URL[1]
                    }
                    2 -> {
                        baseUrl = IK_BASE_URL[2]
                    }
                    3 -> {
                        baseUrl = IK_BASE_URL[3]
                    }
                    4 -> {
                        baseUrl = IK_BASE_URL[4]
                    }
                }
            }
        }
        return baseUrl
    }

    /**
     * 获取H5Url
     */
    fun getHybridUrl(): String {
        var hybridUrl: String
        if (BuildConfig.IS_LX) {
            hybridUrl = LX_H5_HYBRID[4]
            if (AppConfig.DEBUG) {
                when (AppConfig.getUrlType()) {
                    0 -> {
                        hybridUrl = LX_H5_HYBRID[0]
                    }
                    1 -> {
                        hybridUrl = LX_H5_HYBRID[1]
                    }
                    2 -> {
                        hybridUrl = LX_H5_HYBRID[2]
                    }
                    3 -> {
                        hybridUrl = LX_H5_HYBRID[3]
                    }
                    4 -> {
                        hybridUrl = LX_H5_HYBRID[4]
                    }
                }
            }
        } else {
            hybridUrl = IK_H5_HYBRID[4]
            if (AppConfig.DEBUG) {
                when (AppConfig.getUrlType()) {
                    0 -> {
                        hybridUrl = IK_H5_HYBRID[0]
                    }
                    1 -> {
                        hybridUrl = IK_H5_HYBRID[1]
                    }
                    2 -> {
                        hybridUrl = IK_H5_HYBRID[2]
                    }
                    3 -> {
                        hybridUrl = IK_H5_HYBRID[3]
                    }
                    4 -> {
                        hybridUrl = IK_H5_HYBRID[4]
                    }
                }
            }
        }
        return hybridUrl
    }

    fun getSocketUrl(): String {
        var socketUrl: String
        if (BuildConfig.IS_LX) {
            socketUrl = LX_SOCKET_URL[4]
            if (AppConfig.DEBUG) {
                when (AppConfig.getUrlType()) {
                    0 -> {
                        socketUrl = LX_SOCKET_URL[0]
                    }
                    1 -> {
                        socketUrl = LX_SOCKET_URL[1]
                    }
                    2 -> {
                        socketUrl = LX_SOCKET_URL[2]
                    }
                    3 -> {
                        socketUrl = LX_SOCKET_URL[3]
                    }
                    4 -> {
                        socketUrl = LX_SOCKET_URL[4]
                    }
                }
            }
        } else {
            socketUrl = IK_SOCKET_URL[4]
            if (AppConfig.DEBUG) {
                when (AppConfig.getUrlType()) {
                    0 -> {
                        socketUrl = IK_SOCKET_URL[0]
                    }
                    1 -> {
                        socketUrl = IK_SOCKET_URL[1]
                    }
                    2 -> {
                        socketUrl = IK_SOCKET_URL[2]
                    }
                    3 -> {
                        socketUrl = IK_SOCKET_URL[3]
                    }
                    4 -> {
                        socketUrl = IK_SOCKET_URL[4]
                    }
                }
            }
        }
        return "${socketUrl}userId=${AppConfig.getUserId()}&appType=0"
    }

    fun getAppPushConfirmUrl(): String {
        var confirmUrl: String
        confirmUrl = APP_PUSH_CONFIRM_URL[1]
        if (AppConfig.DEBUG) {
            when (AppConfig.getUrlType()) {
                0, 1, 2, 3 -> {
                    confirmUrl = APP_PUSH_CONFIRM_URL[0]
                }
                4 -> {
                    confirmUrl = APP_PUSH_CONFIRM_URL[1]
                }
            }
        }
        return confirmUrl
    }
}