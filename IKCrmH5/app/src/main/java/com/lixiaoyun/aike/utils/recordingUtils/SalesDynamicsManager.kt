package com.lixiaoyun.aike.utils.recordingUtils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Environment
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.entity.RecordPathBean
import com.lixiaoyun.aike.entity.model.SalesDynamicsModel
import com.lixiaoyun.aike.greendaoDB.SalesDynamicsModelDao
import com.lixiaoyun.aike.network.NetWorkUtil
import com.lixiaoyun.aike.service.PhoneStatusService
import com.lixiaoyun.aike.utils.*
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandleLogEntity
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandlePostLog
import com.orhanobut.logger.Logger
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

/**
 * @data on 2019/6/12
 */
@SuppressLint("MissingPermission")
class SalesDynamicsManager private constructor() {

    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = SalesDynamicsManager()
    }

    //--------------------------------------处理数据库操作
    private val daoSession = DBUtils.instance.dBOperator
    private val salesDynamicsModelDao = daoSession.salesDynamicsModelDao

    fun insertData(model: SalesDynamicsModel): Long {
        return salesDynamicsModelDao.insertOrReplace(model)
    }

    fun getDataList(): List<SalesDynamicsModel> {
        return salesDynamicsModelDao.loadAll()
    }

    fun getDataListWithTakeOff(takeOff: Boolean): List<SalesDynamicsModel> {
        return salesDynamicsModelDao.queryBuilder().where(SalesDynamicsModelDao.Properties.TakeOff.eq(takeOff)).list()
    }

    fun getDateById(Id: Long): SalesDynamicsModel {
        return salesDynamicsModelDao.queryBuilder().where(SalesDynamicsModelDao.Properties.Id.eq(Id)).unique()
    }

    fun deleteDateByDate(data: SalesDynamicsModel) {
        salesDynamicsModelDao.delete(data)
    }

    fun deleteDateById(Id: Long) {
        salesDynamicsModelDao.deleteByKey(Id)
    }

    fun upDataModel(model: SalesDynamicsModel) {
        salesDynamicsModelDao.update(model)
    }

    fun upDataModels(models: List<SalesDynamicsModel>) {
        salesDynamicsModelDao.updateInTx(models)
    }
    //--------------------------------------处理数据库操作


    //--------------------------------------处理下载文件路径
    /**
     * 保存录音文件夹
     */
    fun saveCallRecordDir() {
        try {
            val pathFile = AppConfig.RECORD_PATH_FILE.getInternalStorageFile()
            val model = getRecordPathBean(pathFile)
            var dir = Environment.getExternalStorageDirectory().absolutePath
            if (dir.empty()) {
                dir = "/storage/emulated/0"
            }
            val paths = when {
                Rom.isMiui() -> getDir(dir, Rom.ROM_MIUI, model.path)
                Rom.isOppo() -> getDir(dir, Rom.ROM_OPPO, model.path)
                Rom.isVivo() -> getDir(dir, Rom.ROM_VIVO, model.path)
                Rom.isEmui() -> getDir(dir, Rom.ROM_EMUI, model.path)
                Rom.isFlyme() -> getDir(dir, Rom.ROM_FLYME, model.path)
                Rom.isSamsung() -> getDir(dir, Rom.ROM_SAMSUNG, model.path)
                else -> getDir(dir, Rom.ROM_OTHER, model.path)
            }
            val pathString = paths.toString().replace("[", "").replace("]", "")
            Logger.d("pathString: $pathString")
            var msg = "录音文件夹："
            if (!pathString.empty()) {
                msg += pathString
                AppConfig.setFindRecordPath(pathString)
            } else {
                msg += "未找到录音文件夹"
            }
            Logger.e(msg)
            HandlePostLog.postLogSalesDynamics(HandleLogEntity.EVENT_GET_RECORDING_PATH, msg)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getRecordPathBean(pathFile: File): RecordPathBean {
        try {
            if (pathFile.exists()) {
                Logger.d("读取下载的地址文件")
                val newStringBuilder = StringBuilder()
                val fis = FileInputStream(pathFile)
                val ipsReader = InputStreamReader(fis, "utf-8")
                val reader = BufferedReader(ipsReader)
                var jsonLine = reader.readLine()
                while (jsonLine != null) {
                    newStringBuilder.append(jsonLine)
                    jsonLine = reader.readLine()
                }
                reader.close()
                ipsReader.close()
                fis.close()
                return GsonUtil.instance.gsonToBean(newStringBuilder.toString(), RecordPathBean::class.java)
            } else {
                Logger.d("读取assets中的地址文件")
                val assetsPath = "record_path.json".readAssets()
                return GsonUtil.instance.gsonToBean(assetsPath, RecordPathBean::class.java)
            }
        } catch (e: Exception) {
            Logger.d("读取assets中的地址文件")
            val assetsPath = "record_path.json".readAssets()
            return GsonUtil.instance.gsonToBean(assetsPath, RecordPathBean::class.java)
        }
    }

    /**
     * 获取文件夹
     *
     * @param baseDir String
     * @param rom String
     * @param pathList List<RecordPathBean.PathBean>
     *
     * @return String?
     */
    private fun getDir(baseDir: String, rom: String, pathList: List<RecordPathBean.PathBean>): ArrayList<String> {
        val paths = ArrayList<String>()
        for (pathBean in pathList) {
            if (pathBean.rom.isSame(rom)) {
                for (displayBean in pathBean.display) {
                    if (File(baseDir + displayBean.path).exists()) {
                        paths.add(baseDir + displayBean.path)
                    }
                }
                //设置默认值
                if (paths.size == 0) {
                    paths.add(pathBean.display[0].path)
                }
            }
        }
        return paths
    }

    /**
     * 获取录音路径
     *
     * @return 录音路径
     */
    fun getRecordPath(): String {
        return AppConfig.getFindRecordPath()
    }
    //--------------------------------------处理下载文件路径

    //--------------------------------------处理其他逻辑
    /**
     * App自动拨号挂断电话时通知PC端
     */
    fun dialHangUpNotify() {
        NetWorkUtil.instance.initRetrofit().dialHangUp()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<ResponseBody> {
                    override fun onComplete() {

                    }

                    override fun onSubscribe(d: Disposable) {
                        Logger.d("自动拨号挂断通知PC端")
                    }

                    override fun onNext(t: ResponseBody) {
                        Logger.d("自动拨号挂断通知PC端 onNext：${t.string()}")
                    }

                    override fun onError(e: Throwable) {
                        Logger.d("自动拨号挂断通知PC端 onError：${e.message}")
                    }
                })
    }

    /**
     * 触发重传任务
     *
     * @param context context
     */
    @SuppressLint("CheckResult")
    fun reloadRecord(context: Context) {
        //尝试重启服务
        instance.startPhoneStatusService(context)
        var msg = "[重传条件判断]，"
        msg += if (!AppConfig.UpDataRecording && !AppConfig.ReUpDataRecording) {
            HandleCallRecordReUpLoad(context).handleReUpLoadCallRecord()
            "[执行重传]"
        } else {
            "[不执行重传]，${getUpDataRecording()}，${getReUpDataRecording()}"
        }
        HandlePostLog.postLogSalesDynamics(HandleLogEntity.EVENT_SALES_DYNAMICS, msg)
    }

    private fun getUpDataRecording(): String {
        var msg = "[正在上传中："
        msg += if (AppConfig.UpDataRecording) {
            "true，号码：${AppConfig.UpDataRecordingPhone}，" +
                    "通话时长：${AppConfig.UpDataRecordingDuration}，" +
                    "录音地址：${AppConfig.UpDataRecordingPath}]，"
        } else {
            "false]，"
        }
        return msg
    }

    private fun getReUpDataRecording(): String {
        var msg = "[正在重传中："
        msg += if (AppConfig.ReUpDataRecording) {
            "true，号码：${AppConfig.ReUpDataRecordingPhone}，" +
                    "通话时长：${AppConfig.ReUpDataRecordingDuration}，" +
                    "录音地址：${AppConfig.ReUpDataRecordingPath} ]"
        } else {
            "false]"
        }
        return msg
    }

    /**
     * 呼叫中心服务
     * @param context Context
     */
    fun startPhoneStatusService(context: Context) {
        //录音服务
        context.startService(Intent(context, PhoneStatusService::class.java))
    }
//--------------------------------------处理其他逻辑

}