package com.lixiaoyun.aike.network

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NetWorkUtil private constructor() {
    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = NetWorkUtil()
    }

    /**
     * @return OkHttpClient
     */
    private fun initOkHttpClient(): OkHttpClient {
        val paramsInterceptor = BasicParamsInterceptor()
        return OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .callTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(paramsInterceptor)
                .build()
    }

    /**
     * @return ApiService
     */
    fun initRetrofit(): ApiService {
        val retrofit = Retrofit.Builder()
                .baseUrl(NetWorkConfig.getBaseUrl())
                .client(initOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        return retrofit.create(ApiService::class.java)
    }

    /**
     * 解析ResponseBody
     * @param t ResponseBody
     * @return String?
     */
    fun resolveResponseBody(t: ResponseBody): String? {
        val resultString: String?
        val source = t.source()
        source.request(Long.MAX_VALUE) // Buffer the entire body.
        val responseBuffer = source.buffer
        var charset = charset("UTF8")
        val contentType = t.contentType()
        if (contentType != null) {
            charset = contentType.charset(charset)!!
        }
        resultString = responseBuffer.clone().readString(charset)
        return resultString
    }
}