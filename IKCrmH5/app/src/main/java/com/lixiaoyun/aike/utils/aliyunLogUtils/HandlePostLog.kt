package com.lixiaoyun.aike.utils.aliyunLogUtils

import android.annotation.SuppressLint
import com.alibaba.fastjson.JSON
import com.aliyun.sls.android.sdk.ClientConfiguration
import com.aliyun.sls.android.sdk.LOGClient
import com.aliyun.sls.android.sdk.LogException
import com.aliyun.sls.android.sdk.SLSLog
import com.aliyun.sls.android.sdk.core.auth.StsTokenCredentialProvider
import com.aliyun.sls.android.sdk.core.callback.CompletedCallback
import com.aliyun.sls.android.sdk.model.LogGroup
import com.aliyun.sls.android.sdk.request.PostLogRequest
import com.aliyun.sls.android.sdk.result.PostLogResult
import com.lixiaoyun.aike.AKApplication
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.constant.KeySet
import com.lixiaoyun.aike.entity.AliyunTokenBean
import com.lixiaoyun.aike.network.NetWorkUtil
import com.lixiaoyun.aike.utils.*
import com.orhanobut.logger.Logger
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody

/**
 * @data on 2019/7/25
 */
object HandlePostLog {

    private var sConfiguration: ClientConfiguration? = null

    @SuppressLint("StaticFieldLeak")
    private var sLogClient: LOGClient? = null

    private var showLogs = false

    /**
     * 获取阿里云日志服务token
     * @param callBack (action: Int) -> Unit
     */
    private fun getAliYunToken(callBack: (action: Int) -> Unit) {
        val lastTime = SPUtils.instance.getLong(KeySet.KEY_AILIYUN_TOKEN_TIMING, 0) ?: 0
        if (lastTime == 0L || System.currentTimeMillis() - lastTime >= 50 * 60 * 1000) {
            NetWorkUtil.instance.initRetrofit().getAliyunToken(AppConfig.ALIYUN_TOKEN_URL)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<ResponseBody> {
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onNext(t: ResponseBody) {
                            val resultString = NetWorkUtil.instance.resolveResponseBody(t)
                            if (!resultString.empty()) {
                                val bean = GsonUtil.instance.gsonToBean(resultString!!, AliyunTokenBean::class.java)
                                if (!bean.StatusCode.empty() && bean.StatusCode.isSame("200")) {
                                    SPUtils.instance.saveValue(KeySet.KEY_ALIYUN_AK, bean.AccessKeyId!!)
                                    SPUtils.instance.saveValue(KeySet.KEY_ALIYUN_SK, bean.AccessKeySecret!!)
                                    SPUtils.instance.saveValue(KeySet.KEY_ALIYUN_TK, bean.SecurityToken!!)
                                    SPUtils.instance.saveValue(KeySet.KEY_AILIYUN_TOKEN_TIMING, System.currentTimeMillis())
                                    callBack(0)
                                } else {
                                    callBack(2)
                                }
                            } else {
                                callBack(2)
                            }
                        }

                        override fun onError(e: Throwable) {
                            callBack(2)
                        }

                        override fun onComplete() {

                        }

                    })
        } else {
            callBack(1)
        }
    }

    /**
     * 配置阿里云日志服务请求项
     */
    private fun createLogClient(request: Int): LOGClient {
        if (sConfiguration == null) {
            sConfiguration = ClientConfiguration()
            sConfiguration!!.connectionTimeout = 15 * 1000 //连接超时，默认15秒
            sConfiguration!!.socketTimeout = 15 * 1000 //socket超时，默认15秒
            sConfiguration!!.maxConcurrentRequest = 6 //最大并发请求书，默认5个
            sConfiguration!!.maxErrorRetry = 2 //失败后最大重试次数，默认2次
            sConfiguration!!.cachable = false
            sConfiguration!!.connectType = ClientConfiguration.NetworkPolicy.WWAN_OR_WIFI
            SLSLog.enableLog()
        }

        if (sLogClient == null || request == 0) {
            val ak = SPUtils.instance.getStringSp(KeySet.KEY_ALIYUN_AK)
            val sk = SPUtils.instance.getStringSp(KeySet.KEY_ALIYUN_SK)
            val tk = SPUtils.instance.getStringSp(KeySet.KEY_ALIYUN_TK)
            val provider = StsTokenCredentialProvider(ak, sk, tk)
            sLogClient = null
            sLogClient = LOGClient(AKApplication.instance, AppConfig.ALIYUN_END_POINT, provider, sConfiguration)
        }

        return sLogClient as LOGClient
    }

    /**
     * 上传阿里云日志
     */
    private fun postLogRequest(logGroup: LogGroup) {
        val request = PostLogRequest(AppConfig.ALIYUN_PROJECT_NAME, AppConfig.ALIYUN_STORE_NAME, logGroup)
        getAliYunToken {
            if (it == 0 || it == 1) {
                val client = createLogClient(it)
                try {
                    client.asyncPostLog(request, object : CompletedCallback<PostLogRequest, PostLogResult> {
                        override fun onSuccess(postLogRequest: PostLogRequest, postLogResult: PostLogResult) {
                            if (showLogs) {
                                Logger.d("ALiYun 上传成功,PostLogResult:${JSON.toJSONString(postLogResult).printJsonData()}")
                            }
                        }

                        override fun onFailure(postLogRequest: PostLogRequest, e: LogException) {
                            if (showLogs) {
                                Logger.d("ALiYun 上传失败,\nMessage:${e.message}")
                            }
                        }
                    })
                } catch (e: Exception) {
                    if (showLogs) {
                        Logger.d("ALiYun 上传失败:${e.message}")
                    }
                }
            } else {
                if (showLogs) {
                    Logger.d("ALiYun 上传失败:token获取失败")
                }
            }
        }
    }

    /**
     * 上传推送日志
     *
     * @param type 推送类型
     * @param msg  推送信息
     */
    fun postLogPushServiceTopic(type: String, msg: String) {
        val builder = HandleLogEntity.Builder()
        builder.setTopic(HandleLogEntity.TOPIC_BROADCAST_RECEIVE)
                .setEventType(HandleLogEntity.EVENT_PUSH_SERVICE)
                .setEventMessage("$type: $msg")
        postLogRequest(builder.build())
    }

    /**
     * 上传销售动态日志
     *
     * @param eventType 请求的类型
     * @param message   信息
     */
    fun postLogSalesDynamics(eventType: String, message: String) {
        val builder = HandleLogEntity.Builder()
        builder.setTopic(HandleLogEntity.TOPIC_SALES_DYNAMICS)
                .setEventType(eventType)
                .setEventMessage(message)
        postLogRequest(builder.build())
    }

    /**
     * 上传请求日志
     *
     * @param url       url
     * @param eventType 请求的类型
     */
    fun postLogRequestTopic(url: String, eventType: String, requestParams: String?, responseParams: String?, requestTime: String?) {
        val builder = HandleLogEntity.Builder()
                .setTopic(HandleLogEntity.TOPIC_REQUEST)
                .setEventType(eventType)
                .setRequestApi(url)
                .setRequestParams(requestParams)
                .setResponseParams(responseParams)
                .setRequestTime(requestTime)
        postLogRequest(builder.build())
    }

    /**
     * 上传日志
     */
    fun postLogBaseTopic(topic: String, eventType: String, message: String) {
        val builder = HandleLogEntity.Builder()
                .setTopic(topic)
                .setEventType(eventType)
                .setEventMessage(message)
        postLogRequest(builder.build())
    }

    /**
     * 上传Socket日志
     *
     * @param eventType 请求的类型
     * @param message   信息
     */
    fun postLogSocketMsg(topic: String, eventType: String, url: String, message: String) {
        val builder = HandleLogEntity.Builder()
        builder.setTopic(topic)
                .setEventType(eventType)
                .setRequestApi(url)
                .setEventMessage(message)
        postLogRequest(builder.build())
    }
}
