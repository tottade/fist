package com.lixiaoyun.aike.utils.recordingUtils

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import android.text.TextUtils
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.constant.KeySet
import com.lixiaoyun.aike.entity.QiNiuErrorResponse
import com.lixiaoyun.aike.entity.QiNiuResponse
import com.lixiaoyun.aike.entity.RequestUpDataDialLog
import com.lixiaoyun.aike.entity.model.SalesDynamicsModel
import com.lixiaoyun.aike.network.NetWorkUtil
import com.lixiaoyun.aike.utils.*
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandleLogEntity
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandlePostLog
import com.orhanobut.logger.Logger
import java.io.File
import java.util.*

/**
 * @data on 2019/11/19
 */
class HandleCallRecord constructor(private val context: Context, private var model: SalesDynamicsModel) {

    fun handleCallRecord() {
        ThreadPoolManager.instance.execute(RunnableMission())
    }

    inner class RunnableMission : Runnable {
        override fun run() {
            val startTime = "[上传] 开始时间：${DateUtils.instance.getNowString()}"
            Logger.d(startTime)
            HandlePostLog.postLogSalesDynamics(HandleLogEntity.EVENT_UPDATE_SALES_DYNAMICS, startTime)
            AppConfig.UpDataRecording = true
            try {
                // 获取用户通话时长
                model = getCallDuration(context)
                // 获取用户录音
                model = getCallRecord()
                // 更新本地数据库条目
                SalesDynamicsManager.instance.upDataModel(model)
                // 上传通话记录
                AppConfig.UpDataRecordingPhone = model.phoneNumber
                AppConfig.UpDataRecordingDuration = model.duration
                model.recordFilePath?.let {
                    AppConfig.UpDataRecordingPath = it
                }
                upLoadCallRecord()
            } catch (e: Exception) {
                e.printStackTrace()
                HandlePostLog.postLogSalesDynamics(HandleLogEntity.EVENT_UPDATE_SALES_DYNAMICS,
                        "[上传] RunnableMission 销售动态更新失败：${e.message}")
            }

            AppConfig.UpDataRecording = false
            AppConfig.UpDataRecordingPhone = ""
            AppConfig.UpDataRecordingDuration = 0
            AppConfig.UpDataRecordingPath = ""
            val endTime = "[上传] 结束时间：${DateUtils.instance.getNowString()}"
            Logger.d(endTime)
            HandlePostLog.postLogSalesDynamics(HandleLogEntity.EVENT_UPDATE_SALES_DYNAMICS, endTime)
        }

        /**
         * 获取用户通话时长
         * <p>
         * 实时上传，只需要获取第一个拨出的通话记录信息
         *
         * @return SalesDynamicsModel
         */
        private fun getCallDuration(context: Context): SalesDynamicsModel {
            val formatPhoneNum = model.phoneNumber.formatSUHB()
            //log
            var msg = "获取用户通话时长"
            //通话记录时间
            var date = 0L
            //通话时长
            var durationTime = 0
            //用户别名
            var contactAlias: String? = ""
            //通话记录游标
            var cursor = getCurrentDialogCursor(context, formatPhoneNum)
            if (cursor != null && cursor.moveToFirst()) {
                date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE))
                //判断查询到的第一条通话记录是否符合条件，当条通话记录的时间必定会大于创建时间
                if (date > model.createTime) {
                    durationTime = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION))
                    val aliasName = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                    if (aliasName != null) {
                        contactAlias = aliasName
                    }
                } else {
                    //如果不符合条件，等两秒再查询一次
                    msg += "[重新查询通话时长]"
                    cursor.close()
                    Thread.sleep(2000L)
                    cursor = getCurrentDialogCursor(context, formatPhoneNum)
                    if (cursor != null && cursor.moveToFirst()) {
                        date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE))
                        durationTime = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION))
                        val aliasName = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                        if (aliasName != null) {
                            contactAlias = aliasName
                        }
                        cursor.close()
                    }
                }
            }
            model.contactAlias = contactAlias
            model.duration = durationTime
            msg += "：电话号码：${model.phoneNumber}" +
                    "，格式化电话号码：${formatPhoneNum}" +
                    "，联系人别名：${(if (TextUtils.isEmpty(contactAlias)) "未知" else contactAlias)}" +
                    "，通话时长：${durationTime}" +
                    "，通话时长[model]：${model.duration}" +
                    "，通话记录日期：${DateUtils.instance.millis2String(date)}"
            Logger.d(msg)
            HandlePostLog.postLogSalesDynamics(HandleLogEntity.EVENT_GET_CALL_DURATION, msg)
            return model
        }

        /**
         * 获取当前通话记录游标
         *
         * @param formatPhoneNum String
         * @return Cursor?
         */
        @SuppressLint("MissingPermission")
        private fun getCurrentDialogCursor(context: Context, formatPhoneNum: String): Cursor? {
            return context.contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DURATION, CallLog.Calls.DATE, CallLog.Calls.CACHED_NAME),
                    CallLog.Calls.NUMBER + " =? and " + CallLog.Calls.TYPE + " =? ",
                    arrayOf(formatPhoneNum, CallLog.Calls.OUTGOING_TYPE.toString() + ""),
                    CallLog.Calls.DEFAULT_SORT_ORDER + " LIMIT 1"
            )
        }

        private fun getCallRecord(): SalesDynamicsModel {
            if (model.duration <= 0) {
                val msg = "电话：${model.phoneNumber}，查找录音：通话时长为0"
                HandlePostLog.postLogSalesDynamics(HandleLogEntity.EVENT_GET_CALL_RECORDING, msg)
                Logger.d(msg)
                return model
            }
            val recordPathString = SalesDynamicsManager.instance.getRecordPath()
            if (TextUtils.isEmpty(recordPathString)) {
                val msg = "电话：${model.phoneNumber}，查找录音：未找到录音文件夹路径"
                HandlePostLog.postLogSalesDynamics(HandleLogEntity.EVENT_GET_CALL_RECORDING, msg)
                Logger.d(msg)
                return model
            }
            val recordPaths = recordPathString.split(", ")
            for (recordPath in recordPaths) {
                val fileDir = File(recordPath)
                var msg = "录音文件夹：${recordPath}，"
                if (fileDir.exists() && fileDir.isDirectory) {
                    val fileList = fileDir.listFiles()
                    if (fileList != null && fileList.isNotEmpty()) {
                        Logger.d("录音文件个数：${fileList.size}")
                        Arrays.sort(fileList, FileComparator())
                        for (file in fileList) {
                            //匹配时间
                            if (file.lastModified() > model.createTime) {
                                //去除空格-_
                                val formatName = file.name.formatSUH()
                                var formatContactAlias = ""
                                model.contactAlias?.let {
                                    formatContactAlias = model.contactAlias.formatSUH()
                                }
                                val formatPhoneNum = model.phoneNumber.formatSUH()
                                Logger.d("录音文件名称：${file.name}" +
                                        "，最后修改时间：${file.lastModified()}" +
                                        "，格式化名称：${formatName}" +
                                        "，格式化时间：${DateUtils.instance.millis2String(file.lastModified())}" +
                                        "，格式化联系人名字：${formatContactAlias}" +
                                        "，格式化电话号码：${formatPhoneNum}")
                                //匹配电话号码或者联系人名字
                                if (formatName.contains(formatPhoneNum) || formatName.contains(formatContactAlias)) {
                                    model.recordFilePath = file.absolutePath
                                    break
                                }
                            }
                        }
                        msg += if (TextUtils.isEmpty(model.recordFilePath)) {
                            "录音文件未找到，电话：${model.phoneNumber}，联系人名称：${model.contactAlias}"
                        } else {
                            "找到录音文件，电话：${model.phoneNumber}，联系人名称：${model.contactAlias}，录音文件路径：${model.recordFilePath}"
                        }
                    } else {
                        msg += "电话：${model.phoneNumber}，查找录音：录音文件个数为0"
                    }
                } else {
                    msg += "电话：${model.phoneNumber}，查找录音：文件夹不存在或目标不是文件夹"
                }
                Logger.d(msg)
                HandlePostLog.postLogSalesDynamics(HandleLogEntity.EVENT_GET_CALL_RECORDING, msg)
            }
            return model
        }
    }

    /**
     * 上传通话记录
     */
    private fun upLoadCallRecord() {
        if (model.duration == 0 || TextUtils.isEmpty(model.recordFilePath)) {
            //如果通话时长为零或者没有录音，直接上传，录音id为0
            upLoadToIk(0, 0, false)
        } else {
            //如果有录音，上传录音到七牛
            upLoadToQiNiu()
        }
    }

    /**
     * 上传通话记录到爱客服务器
     *
     * @param audioId 录音id
     */
    private fun upLoadToIk(audioId: Int, audioTime: Int, audioExists: Boolean) {
        var msg = "更新通话记录，电话：${model.phoneNumber}，audioExists：$audioExists，audioId：$audioId，通话时长[model.duration]：" +
                "${model.duration}，通话时长[audioTime]：$audioTime，"
        try {
            //通话时长，以通话时长或者录音时长较长的为准
            val talkTime: Int = if (model.duration > audioTime) {
                if (model.duration > 43200) {
                    msg += "通话时长异常[model.duration]：${model.duration}，"
                    audioTime
                } else {
                    model.duration
                }
            } else {
                if (audioTime > 43200) {
                    msg += "通话时长异常[audioTime]：$audioTime，"
                    model.duration
                } else {
                    audioTime
                }
            }
            val talkTimeStr = talkTime.toString()
            msg += "通话时长[talkTime]：$talkTime，通话时长[talkTimeStr]：$talkTimeStr，"
            val isConnected = if (talkTime > 0) "true" else "false"
            val requestDialLog = RequestUpDataDialLog.DialLog(talkTimeStr, isConnected, model.callId)
            val request = RequestUpDataDialLog(requestDialLog, arrayOf("$audioId"))
            val call = NetWorkUtil.instance.initRetrofit().upDataDialLogsSync(model.itemId, request)
            val result = call.execute().body()
            when {
                result == null -> msg += "销售动态更新失败: 接口返回值是空"

                result.code != 0 -> msg += "销售动态更新失败: 接口返回值Code不是0"

                result.data == null -> msg += "销售动态更新失败: 接口返回值结果解析为null"

                else -> {
                    msg += "销售动态更新成功，动态：${result.data.toString()}"
                    SalesDynamicsManager.instance.deleteDateByDate(model)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            msg += "销售动态更新失败：${e.message}"
        } finally {
            Logger.d(msg)
            HandlePostLog.postLogSalesDynamics(HandleLogEntity.EVENT_UPDATE_SALES_DYNAMICS, msg)
        }
    }

    /**
     * 上传录音到七牛
     */
    private fun upLoadToQiNiu() {
        val file = File(model.recordFilePath)
        var qiNiuToken: String? = CacheDiskStaticUtils.getString(KeySet.CACHE_KEY_UPLOAD_TOKEN)
        var audioId = 0
        var audioTime = 0
        var exists = false
        var msg = "上传录音到七牛，${model.phoneNumber}，"
        try {
            if (qiNiuToken.empty()) {
                Logger.d("未获取缓存token")
                val call = NetWorkUtil.instance.initRetrofit().getUploadQiNiuTokenSync(UploadUtils.FILE_TYPE_AUDIO)
                val result = call.execute().body()
                if (result == null) {
                    msg += "获取七牛token失败: 返回值是空"
                } else {
                    qiNiuToken = result.uptoken
                    CacheDiskStaticUtils.put(KeySet.CACHE_KEY_UPLOAD_TOKEN, qiNiuToken, 120)
                }
            } else {
                Logger.d("获取缓存token $qiNiuToken")
            }

            if (!qiNiuToken.empty()) {
                val upToQiNiuRs = UploadUtils.instance.uploadFile2QiNiuSync(
                        file, qiNiuToken!!, UploadUtils.UP_FILE_TYPE_AUDIO, "DialLog", model.itemId
                )
                if (upToQiNiuRs != null) {
                    if (upToQiNiuRs.isOK) {
                        val bean: QiNiuResponse = GsonUtil.instance.gsonToBean(upToQiNiuRs.response.toString(), QiNiuResponse::class.java)
                        audioId = bean.id
                        audioTime = bean.duration.toInt()
                        Logger.e("upToQiNiuRs.response $bean")
                        msg += "语音上传成功，录音id：$bean，录音时长[bean.duration]：${bean.duration}，录音时长[audioTime]：$audioTime"
                    } else {
                        exists = true
                        val errorBean: QiNiuErrorResponse = GsonUtil.instance.gsonToBean(upToQiNiuRs.error.toString(), QiNiuErrorResponse::class.java)
                        msg += if (errorBean.errCode == 614) {
                            "录音已存在，语音上传失败，upToQiNiuRs.error $errorBean"
                        } else {
                            "语音上传失败：upToQiNiuRs.isOk == false，${upToQiNiuRs}"
                        }
                    }
                } else {
                    msg += "语音上传失败：upToQiNiuRs 为空"
                }
            } else {
                msg += "获取七牛token失败: token为null"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            msg = "上传录音文件到七牛失败：${e.message}"
        } finally {
            Logger.d(msg)
            HandlePostLog.postLogSalesDynamics(HandleLogEntity.EVENT_UPDATE_TO_QINIU, msg)
        }
        if (audioId != 0 || exists) {
            upLoadToIk(audioId, audioTime, exists)
        }
    }

    /**
     * 倒叙排列（最新的录音在最前面）
     */
    inner class FileComparator : Comparator<File> {
        override fun compare(o1: File, o2: File): Int {
            return o2.lastModified().compareTo(o1.lastModified())
        }
    }
}