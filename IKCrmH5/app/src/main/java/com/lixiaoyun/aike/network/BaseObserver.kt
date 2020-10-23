package com.lixiaoyun.aike.network

import android.content.Context
import com.lixiaoyun.aike.AKApplication
import com.lixiaoyun.aike.utils.GsonUtil
import com.orhanobut.logger.Logger
import com.lixiaoyun.aike.utils.empty
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 泛型为T,返回为ResponseBody
 * @param T
 * @property mContext Context
 * @property mCls Class<T>
 * @property netStatus NetState
 * @constructor
 */
abstract class BaseObserver<T> constructor(cls: Class<T>) : Observer<ResponseBody> {

    private val mContext: Context = AKApplication.instance.applicationContext
    private val mCls = cls

    private val netStatus: NetStateMonitor.NetState
        get() = NetStateMonitor.instance.getCurrentNetState()

    override fun onSubscribe(d: Disposable) {
        onStart(d)
    }

    override fun onNext(responseBody: ResponseBody) {
        val responseStr = responseBody.string()
        if (responseStr.empty()) {
            onError(CODE_DATA_ERROR, "网络请求异常，请检查账号或与管理员联系")
            onFinish()
        } else {
            val responseBean = GsonUtil.instance.gsonToBean(responseStr, mCls)
            if (responseBean == null) {
                onError(CODE_DATA_ERROR, "网络请求异常，请检查账号或与管理员联系")
                onFinish()
            } else {
                onSuccess(responseStr, responseBean)
            }
        }
    }

    override fun onError(e: Throwable) {
        if (netStatus == NetStateMonitor.NetState.NETWORK_NOT_FIND) {
            Logger.d("onError \n code: $CODE_NO_NETWORK \n message: 无网络连接")
            onError(CODE_NO_NETWORK, "无网络连接")
        } else {
            when (e) {
                is SocketTimeoutException -> {
                    Logger.d("onError \n code: $CODE_SOCKET_TIMEOUT \n message: 网络请求超时")
                    onError(CODE_SOCKET_TIMEOUT, "网络请求超时，请稍后重试")
                }

                is ConnectException, is HttpException, is IllegalStateException -> {
                    Logger.d("onError \n code: $CODE_CONNECT \n message: 网络连接有误")
                    onError(CODE_CONNECT, "网络连接有误，请稍后重试")
                }

                is UnknownHostException -> {
                    Logger.d("onError \n code: $CODE_UN_KNOWN_HOST \n message: 网络连接地址有误")
                    onError(CODE_UN_KNOWN_HOST, "网络连接地址有误，请稍后重试")
                }

                else -> {
                    Logger.d("onError \n code: $CODE_OTHER_ERROR \n message: ${e.message}")
                    onError(CODE_OTHER_ERROR, "网络连接失败，请稍后重试")
                }
            }
        }
    }

    override fun onComplete() {
        onFinish()
    }

    /**
     * 网络请求成功
     * @param responseStr String
     * @param responseBean T?
     */
    protected abstract fun onSuccess(responseStr: String, responseBean: T)

    /**
     * 网络请求失败
     * @param code Int
     * @param message String
     */
    protected abstract fun onError(code: Int, message: String)

    /**
     * 网络请求开始
     * @param d Disposable
     */
    protected abstract fun onStart(d: Disposable)

    /**
     * 网络请求结束
     */
    protected abstract fun onFinish()

    companion object {
        private const val CODE_DATA_ERROR = 994
        private const val CODE_NO_NETWORK = 995
        private const val CODE_UN_KNOWN_HOST = 996
        private const val CODE_SOCKET_TIMEOUT = 997
        private const val CODE_CONNECT = 998
        private const val CODE_OTHER_ERROR = 999
    }
}
