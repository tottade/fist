package com.lixiaoyun.aike.utils.recordingUtils

import android.annotation.SuppressLint
import android.content.Context
import com.lixiaoyun.aike.AKApplication
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.entity.RequestCreateDialog
import com.lixiaoyun.aike.entity.ResponseCreateDialLog
import com.lixiaoyun.aike.entity.model.SalesDynamicsModel
import com.lixiaoyun.aike.network.BaseSubscriber
import com.lixiaoyun.aike.network.NetWorkUtil
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandleLogEntity
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandlePostLog
import com.lixiaoyun.aike.utils.callPhone
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * @data on 2019/11/19
 */
class HandlerCall {

    fun handlerCreateModel(model: SalesDynamicsModel, relNumber: String, enableCarousel: Boolean, sim: Int, callback: (msg: String, success: Boolean) -> Unit) {
        //设置数据库中的条目为通话结束
        setLastModelsTakeOff()
        //开始拨打电话
        var msg = "上传通话记录，${model.phoneNumber}，"
        val requestDialLog = RequestCreateDialog.DialLog(model.callId, model.callerType, model.callerId,
                model.name, model.nameType, relNumber, "outgoing",
                if (model.phoneNumber.length > 10) "手机" else "电话"
        )
        val request = RequestCreateDialog(requestDialLog)
        NetWorkUtil.instance.initRetrofit().createDialLogs(request)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseSubscriber<ResponseCreateDialLog>() {
                    override fun onSuccess(code: Int, response: ResponseCreateDialLog?) {
                        if (response != null) {
                            val bean = response.dial_log
                            model.itemId = bean.id
                            val createTime = System.currentTimeMillis()
                            model.createTime = createTime
                            saveModelAndCall(model, enableCarousel, sim, callback)
                            msg += "销售动态创建成功，动态：$bean"
                        } else {
                            msg += "销售动态创建失败，response为空"
                            callback(msg, false)
                        }
                        Logger.d(msg)
                        HandlePostLog.postLogSalesDynamics(HandleLogEntity.EVENT_CREATE_SALES_DYNAMICS, msg)
                    }

                    override fun onError(code: Int, message: String) {
                        msg += "销售动态创建失败：code = $code, message = $message"
                        callback(msg, false)
                        HandlePostLog.postLogSalesDynamics(HandleLogEntity.EVENT_CREATE_SALES_DYNAMICS, msg)
                    }

                    override fun onStart(d: Disposable) {

                    }

                    override fun onFinish() {

                    }
                })

    }

    private fun setLastModelsTakeOff() {
        val modelList = SalesDynamicsManager.instance.getDataListWithTakeOff(false)
        for (salesDynamicsModel in modelList) {
            salesDynamicsModel.takeOff = true
        }
        SalesDynamicsManager.instance.upDataModels(modelList)
    }

    @SuppressLint("CheckResult")
    private fun saveModelAndCall(model: SalesDynamicsModel, enableCarousel: Boolean, sim: Int, callback: (msg: String, success: Boolean) -> Unit) {
        //生成数据库条目
        Observable.timer(500, TimeUnit.MILLISECONDS).map {
            SalesDynamicsManager.instance.insertData(model)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            try {
                                AppConfig.SalesDynamicsId = it
                                callPhone(AKApplication.instance, model.phoneNumber, enableCarousel, sim)
                                callback("拨打电话成功", true)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                val msg = "拨打电话ERROR，${model.phoneNumber}，Message = ${e.message}"
                                callback(msg, false)
                                HandlePostLog.postLogSalesDynamics(HandleLogEntity.EVENT_CREATE_SALES_DYNAMICS, msg)
                                AppConfig.SalesDynamicsId = 0L
                            }
                        },
                        {
                            val msg = "拨打电话ERROR，${model.phoneNumber}，Message = ${it.message}"
                            callback(msg, false)
                            HandlePostLog.postLogSalesDynamics(HandleLogEntity.EVENT_CREATE_SALES_DYNAMICS, msg)
                        }
                )
    }

    /**
     * 直接拨打电话
     *
     * @param context context
     * @param number  电话号码
     */
    @SuppressLint("MissingPermission")
    private fun callPhone(context: Context, number: String, enableCarousel: Boolean, sim: Int) {
        if (enableCarousel) {
            number.callPhone(context, sim)
        } else {
            number.callPhone(context)
        }
    }
}