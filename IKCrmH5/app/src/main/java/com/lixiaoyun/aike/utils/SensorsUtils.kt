package com.lixiaoyun.aike.utils

import android.content.Context
import com.lixiaoyun.aike.BuildConfig
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.network.NetWorkUtil
import com.sensorsdata.analytics.android.sdk.SAConfigOptions
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import org.json.JSONObject

/**
 * @data on 2020-01-14
 *
 * 神策埋点
 */
object SensorsUtils {

    //进入主页（每天一次）
    const val E_ENTER_INDEX = "crmIndex"

    fun init(context: Context) {
        val saConfigOptions: SAConfigOptions = SAConfigOptions("https://shence-sdk.lixiaoskb.com:8443/sa?project=crm")
                .enableLog(false)
        SensorsDataAPI.startWithConfigOptions(context, saConfigOptions)
    }

    //设置公共属性并绑定用户id到神策
    fun pushSAProfile(callBack: (success: Boolean) -> Unit) {
        NetWorkUtil.instance.initRetrofit().getSAInit("Android")
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<ResponseBody> {

                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(t: ResponseBody) {
                        //设置公共属性
                        val jo = JSONObject()
                        jo.put("platform_type", "Android_H5")
                        jo.put("project_name", "独立")
                        if (BuildConfig.IS_LX) {
                            jo.put("brand", "励销")
                        } else {
                            jo.put("brand", "爱客")
                        }
                        jo.put("product", "CRM")
                        SensorsDataAPI.sharedInstance().registerSuperProperties(jo)
                        //绑定用户uid
                        val userId = "${AppConfig.getUId()}"
                        SensorsDataAPI.sharedInstance().login(userId)
                        callBack(true)
                    }

                    override fun onError(e: Throwable) {
                        callBack(false)
                    }

                    override fun onComplete() {

                    }
                })
    }

    /**
     * 埋点
     *
     * @param event String  事件名称
     * @param model String? 事件模块
     * @param scope String? 事件页面
     */
    fun setTrack(event: String, model: String?, scope: String?) {
        val jo = JSONObject()
        model?.let {
            jo.put("model_type", it)
        }
        scope?.let {
            jo.put("scope", it)
        }
        SensorsDataAPI.sharedInstance().track(event, jo)
    }
}