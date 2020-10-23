package com.lixiaoyun.aike.utils.aliyunLogUtils

import com.alibaba.fastjson.JSON
import com.aliyun.sls.android.sdk.model.Log
import com.aliyun.sls.android.sdk.model.LogGroup
import com.lixiaoyun.aike.BuildConfig
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.entity.AliyunLogEntity
import com.lixiaoyun.aike.utils.DateUtils
import com.lixiaoyun.aike.utils.empty
import java.util.*

/**
 * @data on 2019/7/18
 */
object HandleLogEntity {

    val TOPIC_ONCREATE = "onCreate"
    val TOPIC_ONRESUME = "onResume"
    val TOPIC_ONSTOP = "onStop"
    val TOPIC_ONDESTROY = "onDestroy"

    val TOPIC_REQUEST = "request"
    val EVENT_REQUEST_GET = "get_request"
    val EVENT_REQUEST_POST = "post_request"
    val EVENT_REQUEST_PUT = "put_request"
    val EVENT_REQUEST_DELETE = "delete_request"

    val TOPIC_BROADCAST_RECEIVE = "broadcast_receive"
    val EVENT_PUSH_SERVICE = "push_service"
    val EVENT_MESSAGE_PUSH_SERVICE_TYPE_GT = "gt_push"
    val EVENT_MESSAGE_PUSH_SERVICE_TYPE_XM = "xm_push"
    val EVENT_MESSAGE_PUSH_SERVICE_TYPE_HW = "hw_push"
    val EVENT_MESSAGE_PUSH_TAKE_CALL = "take_call"
    val EVENT_MESSAGE_PUSH_TAKE_PUBLIC_CALL = "take_public_call"
    val EVENT_MESSAGE_PUSH_TAKE_CALL_ERROR = "take_call_error"

    //电销相关
    val TOPIC_SALES_DYNAMICS = "sales_dynamics"
    //重传的动态个数
    val EVENT_SALES_DYNAMICS = "sales_dynamics_reload"
    //获取通话时长
    val EVENT_GET_CALL_DURATION = "get_call_duration"
    //获取通话时长[重传]
    val EVENT_GET_RE_CALL_DURATION = "get_re_call_duration"
    //获取通话录音
    val EVENT_GET_CALL_RECORDING = "get_call_recording"
    //获取通话录音[重传]
    val EVENT_GET_RE_CALL_RECORDING = "get_re_call_recording"
    //获取通话文件路径
    val EVENT_GET_RECORDING_PATH = "get_recording_path"
    //生成动态
    val EVENT_CREATE_SALES_DYNAMICS = "create_sales_dynamics"
    //更新动态
    val EVENT_UPDATE_SALES_DYNAMICS = "update_sales_dynamics"
    //上传录音到七牛
    val EVENT_UPDATE_TO_QINIU = "update_to_qiniu"
    //上传录音到七牛[重传]
    val EVENT_RE_UPDATE_TO_QINIU = "re_update_to_qiniu"
    //更新动态[重传]
    val EVENT_RE_UPDATE_SALES_DYNAMICS = "re_update_sales_dynamics"
    //电话状态监听服务
    val EVENT_PHONE_STATUS_SERVICE = "event_phone_status_service"
    //上传请求失败日志
    val EVENT_MESSAGE_RESPONSE_FAILURE = "response_failure"

    //Socket
    val TOPIC_SOCKET_MSG = "socket_message"
    val TOPIC_SOCKET_RECEIVE_MSG = "socket_message"
    val EVENT_SOCKET_STATUS = "socket_status"
    //双卡轮播
    val TOPIC_CAROUSEL = "carousel"
    val EVENT_CAROUSEL_INTENT_MSG = "event_carousel_intent_msg"
    //base
    val TOPIC_BASE = "base"
    //receive_h5_msg
    val TOPIC_RECEIVE_H5_MSG = "receive_h5_msg"
    val EVENT_CALL_PUBLIC_PHONE = "event_call_public_phone"
    val EVENT_CALL_PHONE = "event_call_phone"
    //上传错误日志
    val EVENT_ERROR_MESSAGE = "event_error_message"

    /**
     * 创建LogGroup
     *
     * @param bean AliyunLogEntity
     * @return LogGroup
     */
    fun createLogGroup(bean: AliyunLogEntity?): LogGroup {
        val logGroup = LogGroup(BuildConfig.APPLICATION_ID, "")
        val log = createLog(bean2Map(bean!!))
        logGroup.PutLog(log)
        return logGroup
    }

    /**
     * 创建Log
     *
     * @param dataMap Map<String></String>, Object>
     * @return Log
     */
    fun createLog(dataMap: Map<String, Any>): Log {
        val log = Log()
        for ((key, value) in dataMap) {
            log.PutContent(key, value.toString())
        }
        return log
    }

    /**
     * AliyunLogEntity默认配置
     *
     * @return AliyunLogEntity
     */
    fun defaultLogEntity(): AliyunLogEntity {
        val defaultLogEntity = AliyunLogEntity()
        defaultLogEntity.timestamp = DateUtils.instance.getNowString()
        defaultLogEntity.device_type = android.os.Build.VERSION.RELEASE
        defaultLogEntity.os_version = android.os.Build.MODEL
        defaultLogEntity.app_version = BuildConfig.VERSION_NAME
        defaultLogEntity.user_id = AppConfig.getUserId()
        defaultLogEntity.user_phonenumber = AppConfig.getUserLogin()

        return defaultLogEntity
    }

    /**
     * 转换bean为map
     *
     * @param bean 要转换的bean
     * @param <T>  bean类型
     * @return 转换结果
    </T> */
    private fun bean2Map(bean: Any?): Map<String, Any> {
        val result = HashMap<String, Any>()
        val sourceClass = bean?.javaClass
        //拿到所有的字段,不包括继承的字段
        val sourceFiled = sourceClass?.declaredFields
        try {
            if (sourceFiled != null) {
                for (field in sourceFiled) {
                    field.isAccessible = true
                    result[field.name] = field.get(bean)
                }
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        return result
    }

    class Builder {

        private var defaultLogEntity: AliyunLogEntity? = null

        /**
         * 获取AliyunLogEntity
         */
        init {
            this.defaultLogEntity = defaultLogEntity()
        }

        /**
         * 获取AliyunLogEntity
         */
        fun setLogEntity(logEntity: AliyunLogEntity): Builder {
            defaultLogEntity = logEntity
            return this
        }

        /**
         * 设置__topic__
         */
        fun setTopic(topic: String): Builder {
            defaultLogEntity!!.__topic__ = topic
            return this
        }

        /**
         * 设置eventType
         */
        fun setEventType(eventType: String): Builder {
            defaultLogEntity!!.event_type = eventType
            return this
        }

        /**
         * 设置生命周期方法
         */
        fun setEventOnCreate(className: String): Builder {
            defaultLogEntity!!.event_type = " onCreate $className"
            return this
        }

        /**
         * 设置生命周期方法
         */
        fun setEventOnResume(className: String): Builder {
            defaultLogEntity!!.event_type = " onResume $className"
            return this
        }

        /**
         * 设置生命周期方法
         */
        fun setEventOnStop(className: String): Builder {
            defaultLogEntity!!.event_type = " onStop $className"
            return this
        }

        /**
         * 设置生命周期方法
         */
        fun setEventOnDestroy(className: String): Builder {
            defaultLogEntity!!.event_type = " onDestroy $className"
            return this
        }

        /**
         * 设置请求url
         */
        fun setRequestApi(requestApi: String): Builder {
            defaultLogEntity!!.request_api = requestApi
            return this
        }

        /**
         * 设置请求参数
         */
        fun setRequestParams(map: Map<String, Any>?): Builder {
            if (map != null) {
                defaultLogEntity!!.request_params = JSON.toJSONString(map)
            }
            return this
        }

        /**
         * 设置请求参数
         */
        fun setRequestParams(params: String?): Builder {
            if (!params.empty()) {
                defaultLogEntity!!.request_params = params
            }
            return this
        }

        /**
         * 设置返回参数
         */
        fun setResponseParams(params: String?): Builder {
            if (!params.empty()) {
                defaultLogEntity!!.response_params = params
            }
            return this
        }

        /**
         * 设置请求用时
         */
        fun setRequestTime(time: String?): Builder {
            if (!time.empty()) {
                defaultLogEntity!!.request_time = time
            }
            return this
        }

        /**
         * 设置eventMessage
         */
        fun setEventMessage(eventMessage: String): Builder {
            if (!eventMessage.empty()) {
                defaultLogEntity!!.event_message = eventMessage
            }
            return this
        }

        /**
         * 设置eventMessage
         */
        fun setEventMessageMap(map: Map<String, Any>?): Builder {
            if (map != null && map.isNotEmpty()) {
                defaultLogEntity!!.event_message = JSON.toJSONString(map)
            }
            return this
        }

        /**
         * 设置eventMessage
         */
        fun setEventMessageBean(bean: Any?): Builder {
            if (bean != null) {
                defaultLogEntity!!.event_message = JSON.toJSONString(bean)
            }
            return this
        }

        /**
         * 返回LogGroup
         */
        fun build(): LogGroup {
            return createLogGroup(defaultLogEntity)
        }
    }
}
