package com.lixiaoyun.aike.network

import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.utils.DateUtils
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandlePostLog
import com.lixiaoyun.aike.utils.empty
import com.lixiaoyun.aike.utils.isSame
import com.lixiaoyun.aike.utils.printJsonData
import com.orhanobut.logger.Logger
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer

class BasicParamsInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = dealWithRequest(chain.request())
        val url = request.url.newBuilder()
        if (request.method == "GET") {
            url.addQueryParameter("user_token", AppConfig.getUserToken())
            url.addQueryParameter("device", NetWorkConfig.DEVICE)
            url.addQueryParameter("version_code", NetWorkConfig.VERSION_CODE)
        }

        //头部信息
        val authorization = StringBuffer()
        authorization.append("Token token=\"${AppConfig.getUserToken()}\", ")
        authorization.append("device=\"${NetWorkConfig.DEVICE}\", version_code=\"${NetWorkConfig.VERSION_CODE}\"")
        val requestBuilder = request.newBuilder()
        requestBuilder.addHeader("Content-Type", "application/json;charset=UTF-8")
        requestBuilder.addHeader("Authorization", authorization.toString())

        requestBuilder.url(url.build())
        request = requestBuilder.build()
        val startNs = DateUtils.instance.getNowMills()
        val response = chain.proceed(request)
        val tookMs = DateUtils.instance.getNowMills() - startNs
        getNetWorkInfo(request, response, tookMs)
        return response
    }

    /**
     * 处理多base url的情况
     * @param originalRequest Request
     * @return Request
     */
    private fun dealWithRequest(originalRequest: Request): Request {
        val originalHttpUrl = originalRequest.url
        val requestBuilder = originalRequest.newBuilder()
        val headersValues = originalRequest.headers("url_name")
        return if (headersValues.isNotEmpty()) {
            requestBuilder.removeHeader("url_name")
            val headerValue = headersValues[0]
            val baseURL = if (headerValue.isSame(NetWorkConfig.HEADERS_PUSH_CONFIRM)) {
                NetWorkConfig.getAppPushConfirmUrl().toHttpUrlOrNull()!!
            } else {
                originalHttpUrl
            }
            val newHttpUrl = originalHttpUrl.newBuilder()
                    .scheme(baseURL.scheme)
                    .host(baseURL.host)
                    .port(baseURL.port)
                    .build()
            val newRequest: Request = requestBuilder.url(newHttpUrl).build()
            newRequest
        } else {
            originalRequest
        }
    }

    private fun getNetWorkInfo(request: Request, response: Response, tookMs: Long) {
        val sb = StringBuffer()
        val url = request.url
        val requestType = request.method
        //url
        sb.append("Request Url: \n$url ($tookMs-ms) \n \n")

        //requestQuery
        if (!url.encodedQuery.empty()) {
            val queryDataString = "{\"" +
                    url.encodedQuery!!
                            .replace("=", "\":\"")
                            .replace("&", "\",\"") +
                    "\"}"
            sb.append().append("Query Data: ${queryDataString.printJsonData()} \n \n")
        }

        //requestBody
        val requestBody = request.body
        var requestBodyString: String? = ""
        if (requestBody != null) {
            val requestBuffer = Buffer()
            requestBody.writeTo(requestBuffer)
            val contentType = requestBody.contentType()
            var charset = charset("UTF8")
            if (contentType != null) {
                charset = contentType.charset(charset)!!
            }
            requestBodyString = requestBuffer.readString(charset)
            sb.append().append("Request Body: ${requestBodyString.printJsonData()} \n \n")
        }

        //responseBody
        val responseBody = response.body
        var responseBodyString: String? = ""
        if (responseBody != null) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            val responseBuffer = source.buffer
            var charset = charset("UTF8")
            val contentType = responseBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(charset)!!
            }
            responseBodyString = responseBuffer.clone().readString(charset)
            sb.append().append("Response Body: ${responseBodyString.printJsonData()} \n")
        }
        Logger.d(sb.toString())
        when {
            "$url".empty() -> return
            "$url".contains(AppConfig.ALIYUN_TOKEN_URL) -> return
            "$url".contains(AppConfig.RECORD_PATH_URL) -> return
            else -> HandlePostLog.postLogRequestTopic("$url", requestType, requestBodyString, responseBodyString, "($tookMs-ms)")
        }
    }
}