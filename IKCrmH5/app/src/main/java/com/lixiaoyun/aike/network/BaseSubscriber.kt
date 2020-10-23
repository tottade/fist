package com.lixiaoyun.aike.network

import android.content.Context
import com.lixiaoyun.aike.AKApplication
import com.lixiaoyun.aike.utils.GsonUtil
import com.orhanobut.logger.Logger
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 有固定code和data的返回
 * @param T
 * @property mContext Context
 * @property netStatus NetState
 */
abstract class BaseSubscriber<T> protected constructor() : Observer<BaseResult<T>> {

    private val mContext: Context = AKApplication.instance.applicationContext

    private val netStatus: NetStateMonitor.NetState
        get() = NetStateMonitor.instance.getCurrentNetState()

    override fun onSubscribe(d: Disposable) {
        onStart(d)
    }

    override fun onNext(result: BaseResult<T>) {
        when (result.code) {
            0 -> {
                onSuccess(result.code!!, result.data)
            }

            else -> {
                onError(result.code!!, result.message!!)
                onFinish()
            }
        }
    }

    override fun onError(e: Throwable) {
        if (netStatus == NetStateMonitor.NetState.NETWORK_NOT_FIND) {
            onError(CODE_NO_NETWORK, "无网络连接")
        } else {
            when (e) {
                is SocketTimeoutException -> {
                    onError(CODE_SOCKET_TIMEOUT, "网络请求超时，请稍后重试")
                }

                is ConnectException, is HttpException, is IllegalStateException -> {
                    onError(CODE_CONNECT, "网络连接有误，请稍后重试")
                }

                is UnknownHostException -> {
                    onError(CODE_UN_KNOWN_HOST, "网络连接地址有误，请稍后重试")
                }

                else -> {
                    onError(CODE_OTHER_ERROR, "网络连接失败，请稍后重试")
                }
            }
        }
    }

    override fun onComplete() {
        onFinish()
    }

    protected abstract fun onSuccess(code: Int, response: T?)

    protected abstract fun onError(code: Int, message: String)

    protected abstract fun onStart(d: Disposable)

    protected abstract fun onFinish()

    companion object {
        private const val CODE_NO_NETWORK = 995
        private const val CODE_UN_KNOWN_HOST = 996
        private const val CODE_SOCKET_TIMEOUT = 997
        private const val CODE_CONNECT = 998
        private const val CODE_OTHER_ERROR = 999
    }
}
