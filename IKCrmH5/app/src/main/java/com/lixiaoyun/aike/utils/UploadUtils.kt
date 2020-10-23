package com.lixiaoyun.aike.utils

import android.util.SparseArray
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.entity.QiNiuResponse
import com.lixiaoyun.aike.entity.ResponseQiNiuToken
import com.lixiaoyun.aike.network.BaseObserver
import com.lixiaoyun.aike.network.NetWorkUtil
import com.orhanobut.logger.Logger
import com.qiniu.android.http.ResponseInfo
import com.qiniu.android.storage.Configuration
import com.qiniu.android.storage.UploadManager
import com.qiniu.android.storage.UploadOptions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File

/**
 * @data on 2019/5/9
 */
class UploadUtils private constructor() {
    companion object {
        val instance = SingletonHolder.holder

        //音频
        const val FILE_TYPE_AUDIO = "audio2mp3"
        //附件（文件，图片）
        const val FILE_TYPE_ATTACHMENT = "attachment"

        //音频
        const val UP_FILE_TYPE_AUDIO = "audio"
        //附件（文件，图片）
        const val UP_FILE_TYPE_ATTACHMENT = "attachment"
    }

    private object SingletonHolder {
        val holder = UploadUtils()
    }

    /**
     * 获取七牛上传id
     *
     * @param fileType String FILE_TYPE
     * @param callBack (success: Boolean, token: String?) -> Unit
     */
    fun getUploadToken(fileType: String, callBack: (success: Boolean, token: String?) -> Unit) {
        NetWorkUtil.instance.initRetrofit().getUploadQiNiuToken(fileType)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseObserver<ResponseQiNiuToken>(ResponseQiNiuToken::class.java) {

                    override fun onStart(d: Disposable) {

                    }

                    override fun onSuccess(responseStr: String, responseBean: ResponseQiNiuToken) {
                        if (!responseBean.uptoken.empty()) {
                            //上传七牛
                            Logger.e("获取七牛Token成功!")
                            callBack(true, responseBean.uptoken)
                        } else {
                            Logger.e("获取七牛Token失败!")
                            callBack(true, null)
                        }
                    }

                    override fun onError(code: Int, message: String) {
                        Logger.e("获取七牛Token失败!")
                        callBack(true, null)
                    }

                    override fun onFinish() {

                    }
                })
    }

    /**
     * 单个上传
     * @param file File
     * @param token String
     * @param fileType String UP_FILE_TYPE
     * @param callBack (success: Boolean, bean: QiNiuResponse?) -> Unit
     */
    fun uploadFile2QiNiu(file: File, token: String, fileType: String, uploadIndex: Int = 0, callBack: (success: Boolean, bean: QiNiuResponse?, uploadIndex: Int) -> Unit) {
        val parameters = HashMap<String, String>()
        parameters["x:custom_name"] = file.name
        parameters["x:orgid"] = "${AppConfig.getUserInfo()?.organization_id}"
        parameters["x:userid"] = "${AppConfig.getUserId()}"
        parameters["x:attachment_type"] = fileType
        val uploadOptions = UploadOptions(parameters, null, false, null, null)
        val uploadManager = UploadManager()
        uploadManager.put(file, null, token,
                { _, responseInfo, jsonObject ->
                    if (responseInfo.isOK) {
                        Logger.d("上传七牛成功, Name：${file.name}--Type：$fileType, 返回信息：${GsonUtil.instance.gsonString(responseInfo)}")
                        callBack(true, GsonUtil.instance.gsonToBean(jsonObject.toString(), QiNiuResponse::class.java), uploadIndex)
                    } else {
                        callBack(false, null, uploadIndex)
                        Logger.e("上传七牛失败!")
                    }
                }, uploadOptions)
    }

    /**
     * 单个上传
     * @param file File
     * @param token String
     * @param fileType String UP_FILE_TYPE
     *
     * @return ResponseInfo?
     */
    fun uploadFile2QiNiuSync(file: File, token: String, fileType: String, audioable_type: String, audioable_id: Long): ResponseInfo? {
        var responseInfo: ResponseInfo? = null
        val config = Configuration.Builder().connectTimeout(50).build()
        try {
            val parameters = HashMap<String, String>()
            parameters["x:custom_name"] = file.name
            parameters["x:orgid"] = "${AppConfig.getUserInfo()?.organization_id}"
            parameters["x:userid"] = "${AppConfig.getUserId()}"
            parameters["x:attachment_type"] = fileType
            parameters["x:audioable_type"] = audioable_type
            parameters["x:audioable_id"] = audioable_id.toString()
            val uploadOptions = UploadOptions(parameters, null, false, null, null)
            val uploadManager = UploadManager(config, 4)
            responseInfo = uploadManager.syncPut(file, null, token, uploadOptions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return responseInfo
    }

    /**
     * 多个上传
     * @param fileList ArrayList<File>
     * @param token String
     * @param fileType String UP_FILE_TYPE
     * @param callBack (success: Boolean, bean: ArrayList<QiNiuResponse>?) -> Unit
     */
    fun uploadFile2QiNiu(fileList: SparseArray<File>, token: String, fileType: String,
                         callBack: (bean: SparseArray<QiNiuResponse>) -> Unit) {
        val response = SparseArray<QiNiuResponse>()
        for (i in 0 until fileList.size()) {
            uploadFile2QiNiu(fileList[i], token, fileType, i)
            { success, bean, uploadIndex ->
                if (success && bean != null) {
                    response.put(uploadIndex, bean)
                }
                if (fileList.size() == response.size()) {
                    callBack(response)
                }
            }
        }
    }
}