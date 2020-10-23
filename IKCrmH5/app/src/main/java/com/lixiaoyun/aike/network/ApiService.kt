package com.lixiaoyun.aike.network

import com.lixiaoyun.aike.entity.*
import com.lixiaoyun.aike.network.NetWorkConfig.HEADERS_PUSH_CONFIRM
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/**
 * 指定的网络请求接口
 *
 * 爱客当前的接口需要标注 @FormUrlEncoded
 *
 * 按照进销存的接口:
 * (POST)
 * @FormUrlEncoded
 * @POST("api/{version}/users/login.json")
 * fun getTokenLogin(@Path("version") version: String,
 *                   @FieldMap map: Map<String, String>)
 *              : Observable<BaseResult<UserInfo>>
 * 用 @FieldMap 传递 Map 参数
 * 或
 * @FormUrlEncoded
 * @POST("api/{version}/users/verify_code.json")
 * fun verifyMessageCode(@Path("version") version: String,
 *                       @Field("mobile") mobile: String,
 *                       @Field("code") msgCode: String)
 *                  : Observable<BaseResult<String>>
 * 用 @Field 传递单个参数
 *
 * (GET)
 * @GET("api/{version}/users/get_code.json")
 * fun getMessageCode(@Path("version") version: String,
 *                    @Query("mobile") mobile: String)
 *                : Observable<BaseResult<String>>
 * 用 @Query 拼接参数
 *
 * (特殊请求，比如delete需要传递body)
 * @HTTP(method = "DELETE", path = "api/{version}/users/delete_client_id.json", hasBody = true)
 * fun signOutApp(@Path("version") version: String,
 *                @Body signOutAppBody: SignOutAppBody)
 *          : Observable<BaseResult<String>>
 */
interface ApiService {

    /**
     * 登录
     * 不需要corp_id
     * @param loginBean RequestLogin
     * @return Observable<BaseResult<ResponseLogin>>
     */
    @POST("api/v2/auth/login_app")
    fun login(@Body loginBean: RequestLogin): Observable<ResponseBody>

    /**
     * 登录
     * 需要corp_id
     * @param loginBean RequestLoginWithCorpId
     * @return Observable<BaseResult<ResponseLogin>>
     */
    @POST("api/v2/auth/login_app")
    fun loginWithCorpId(@Body loginBean: RequestLoginWithCorpId): Observable<ResponseBody>

    /**
     * 获取悬浮窗信息
     * @param phoneNum String
     */
    @GET("api/v2/dial_logs/caller_id")
    fun getFloatInfo(@Query("phone") phoneNum: String): Observable<BaseResult<ResponseFloatInfo>>

    /**
     * 获取上传七牛的token
     * @param fileType String
     */
    @GET("api/v2/qiniu/auth/upload_token")
    fun getUploadQiNiuToken(@Query("policy") fileType: String): Observable<ResponseBody>

    @GET("api/v2/qiniu/auth/upload_token")
    fun getUploadQiNiuTokenSync(@Query("policy") fileType: String): Call<ResponseQiNiuToken>

    /**
     * APP自动拨号后挂断时需要调用的接口
     * @return Observable<ResponseBody>
     */
    @POST("api/v2/dial_centers/hang_up_nofify?")
    fun dialHangUp(): Observable<ResponseBody>

    /**
     * 生成通话记录
     * @return Observable<ResponseBody>
     */
    @POST("api/v2/dial_logs")
    fun createDialLogs(@Body request: RequestCreateDialog): Observable<BaseResult<ResponseCreateDialLog>>

    /**
     * 更新通话记录为销售动态
     * @return Observable<ResponseBody>
     */
    @PUT("api/v2/dial_logs/{itemId}")
    fun upDataDialLogs(@Path("itemId") itemId: Long, @Body request: RequestUpDataDialLog): Observable<BaseResult<ResponseUpDataDialLog>>

    @PUT("api/v2/dial_logs/{itemId}")
    fun upDataDialLogsSync(@Path("itemId") itemId: Long, @Body request: RequestUpDataDialLog): Call<BaseResult<ResponseUpDataDialLog>>

    /**
     * 获取用户信息
     * @return Observable<BaseResult<ResponseUserInfo>>
     */
    @GET("api/v2/user/info")
    fun getUserInfo(): Observable<BaseResult<ResponseUserInfo>>

    /**
     * 下载
     * @param url String
     * @return Observable<ResponseBody>
     */
    @Streaming
    @GET
    fun download(@Url url: String): Observable<ResponseBody>

    /**
     * 获取阿里云token
     * @param url String
     * @return Observable<ResponseBody>
     */
    @GET
    fun getAliyunToken(@Url url: String): Observable<ResponseBody>

    /**
     * 向服务器注册神策
     * @param deviceType String -> Android
     * @return Observable<ResponseBody>
     */
    @GET("api/v2/sensors/profile_set")
    fun getSAInit(@Query("device_type") deviceType: String): Observable<ResponseBody>

    /**
     * APP推送确认接收接口
     * @return Observable<ResponseBody>
     */
    @Headers("url_name:${HEADERS_PUSH_CONFIRM}")
    @PUT("/api/v1/received")
    fun confirmPush(@Body request: PushConfirm): Observable<ResponseBody>

    /**
     * 注册获取短信验证码接口
     * @return Observable<ResponseBody>
     */
    @POST("api/v2/signup/send_otp_code")
    fun sendOtpCode(@Body request: RequestSendOptCode): Observable<ResponseBody>

    /**
     * 验证手机号和验证码
     * @param request RequestVerificationOptCode
     * @return Observable<BaseResult<ResponseVerificationOptCode>>
     */
    @POST("api/v2/signup/confirm_otp_code")
    fun verificationOtpCode(@Body request: RequestVerificationOptCode): Observable<BaseResult<ResponseVerificationOptCode>>

    /**
     * 注册提交全部数据
     * @param request RequestSignUpFillInfo
     * @return Observable<ResponseBody>
     */
    @POST("api/v2/signup/fill_info")
    fun signUpFillInfo(@Body request: RequestSignUpFillInfo): Observable<ResponseBody>

    /**
     * 找回密码获取短信验证码接口
     * @return Observable<ResponseBody>
     */
    @POST("api/v2/user/reset_password_otp_code")
    fun sendRetrieveOtpCode(@Body request: RequestSendOptCode): Observable<ResponseBody>

    /**
     * 找回密码验证手机号和验证码
     * @param request RequestVerificationOptCode
     * @return Observable<BaseResult<ResponseVerificationOptCode>>
     */
    @POST("api/v2/user/confirm_reset_password_code")
    fun verificationRetrieveOtpCode(@Body request: RequestVerificationOptCode): Observable<BaseResult<ResponseRetrieveVerificationOptCode>>

    /**
     * 修改密码
     * @param request RequestChangePassword
     * @return Observable<ResponseBody>
     */
    @PUT("api/v2/user/change_password")
    fun changePassword(@Body request: RequestChangePassword): Observable<ResponseBody>
}