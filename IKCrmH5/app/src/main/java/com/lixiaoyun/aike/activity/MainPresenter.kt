package com.lixiaoyun.aike.activity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.SparseArray
import android.view.Gravity
import android.view.WindowManager
import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.constant.KeySet
import com.lixiaoyun.aike.entity.*
import com.lixiaoyun.aike.entity.model.SalesDynamicsModel
import com.lixiaoyun.aike.listener.DownLoadListener
import com.lixiaoyun.aike.listener.OnSheetItemClickListener
import com.lixiaoyun.aike.network.BaseObserver
import com.lixiaoyun.aike.network.BaseSubscriber
import com.lixiaoyun.aike.network.NetStateMonitor
import com.lixiaoyun.aike.network.NetWorkUtil
import com.lixiaoyun.aike.service.RecordService
import com.lixiaoyun.aike.utils.*
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandleLogEntity
import com.lixiaoyun.aike.utils.aliyunLogUtils.HandlePostLog
import com.lixiaoyun.aike.utils.recordingUtils.HandlerCall
import com.lixiaoyun.aike.utils.recordingUtils.SalesDynamicsManager
import com.lixiaoyun.aike.widget.*
import com.networkbench.agent.impl.NBSAppAgent
import com.orhanobut.logger.Logger
import com.yanzhenjie.permission.runtime.Permission
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * @data on 2019/5/5
 */
class MainPresenter constructor(var mView: MainContract.View) : MainContract.Presenter, AKWebView.WVJBHandler {

    //右上角分享/工具按钮
    private var mPopView: BottomPopupMore? = null
    //弹出界面消息个数
    private var mPopupMsgCount: Int = 0
    //播放器
    private var mMediaPlayer: MediaPlayer? = null
    //上传用户信息callback
    private var mGetItemBack: AKWebView.WVJBResponseCallback? = null
    //上传录音callback
    private var mRecordBack: AKWebView.WVJBResponseCallback? = null
    //扫码callback
    private var mScanBack: AKWebView.WVJBResponseCallback? = null
    //是否开启了录音权限
    private var canRecordAudio = false

    /**
     * 注册H5接口
     */
    override fun registerHtmlInterface(wv: AKWebView) {
        //上传用户信息
        wv.registerHandler(mView.getContext().getString(R.string.register_uploadUserInfo), this)
        //获取网络类型
        wv.registerHandler(mView.getContext().getString(R.string.register_networkType), this)
        //获取设备信息
        wv.registerHandler(mView.getContext().getString(R.string.register_phoneInfo), this)
        //设置标题
        wv.registerHandler(mView.getContext().getString(R.string.register_setTitle), this)
        //设置右上角文字
        wv.registerHandler(mView.getContext().getString(R.string.register_setRight), this)
        //监听返回
        wv.registerHandler(mView.getContext().getString(R.string.register_goBack), this)
        //监听关闭
        wv.registerHandler(mView.getContext().getString(R.string.register_close), this)
        //监听菜单
        wv.registerHandler(mView.getContext().getString(R.string.register_setMenu), this)
        //监听菜单和提示
        wv.registerHandler(mView.getContext().getString(R.string.register_setMenuCount), this)
        //监听退出App
        wv.registerHandler(mView.getContext().getString(R.string.register_exitApp), this)
        //获取图片信息
        wv.registerHandler(mView.getContext().getString(R.string.register_fetchImageData), this)
        //打开链接
        wv.registerHandler(mView.getContext().getString(R.string.register_openLink), this)
        //日期选择器
        wv.registerHandler(mView.getContext().getString(R.string.register_datePicker), this)
        //时间选择器
        wv.registerHandler(mView.getContext().getString(R.string.register_timePicker), this)
        //时间选择器
        wv.registerHandler(mView.getContext().getString(R.string.register_dateTimePicker), this)
        //选择上传图库图片
        wv.registerHandler(mView.getContext().getString(R.string.register_uploadImage), this)
        //预览图片
        wv.registerHandler(mView.getContext().getString(R.string.register_previewImage), this)
        //拍照上传
        wv.registerHandler(mView.getContext().getString(R.string.register_uploadImageFromCamera), this)
        //预览文件
        wv.registerHandler(mView.getContext().getString(R.string.register_previewFile), this)
        //分享
        wv.registerHandler(mView.getContext().getString(R.string.register_share), this)
        //扫码
        wv.registerHandler(mView.getContext().getString(R.string.register_scan), this)
        //插入要存储的值
        wv.registerHandler(mView.getContext().getString(R.string.register_domain_setItem), this)
        //删除存入的值
        wv.registerHandler(mView.getContext().getString(R.string.register_domain_removeItem), this)
        //开始录音
        wv.registerHandler(mView.getContext().getString(R.string.register_recordStart), this)
        //结束录音
        wv.registerHandler(mView.getContext().getString(R.string.register_recordStop), this)
        //播放音频
        wv.registerHandler(mView.getContext().getString(R.string.register_audioPlay), this)
        //暂停播放音频
        wv.registerHandler(mView.getContext().getString(R.string.register_audioPause), this)
        //恢复播放音频
        wv.registerHandler(mView.getContext().getString(R.string.register_audioResume), this)
        //停止播放音频
        wv.registerHandler(mView.getContext().getString(R.string.register_audioStop), this)
        //获取定位信息
        wv.registerHandler(mView.getContext().getString(R.string.register_geolocationGet), this)
        //退出app
        wv.registerHandler(mView.getContext().getString(R.string.register_logout), this)
        //调起打电话
        wv.registerHandler(mView.getContext().getString(R.string.register_callPhone), this)
        //调起打电话
        wv.registerHandler(mView.getContext().getString(R.string.register_callPublicTelephone), this)
        //同步日历
        wv.registerHandler(mView.getContext().getString(R.string.register_eventSave), this)
        //查询Sim卡数量
        wv.registerHandler(mView.getContext().getString(R.string.register_getSimAmount), this)
    }

    /**
     * 监听
     */
    override fun handler(data: Any?, handlerName: String?, callback: AKWebView.WVJBResponseCallback?) {
        Logger.d("监听H5接口：\n \n" +
                "handlerName: $handlerName \n \n" +
                "data: ${data.toString().printJsonData()}")
        when (handlerName) {
            //上传用户信息
            mView.getContext().getString(R.string.register_uploadUserInfo) -> {
                if (AppConfig.getUserToken().empty()) {
                    val user = HybridAppConfigUserBean()
                    user.client_id = AppConfig.getPushClientId()
                    user.login = AppConfig.getUserLogin()
                    user.password = AppConfig.getUserPsw()
                    user.token = ""
                    user.device_model = android.os.Build.MODEL
                    user.system_version = android.os.Build.VERSION.RELEASE
                    user.device_id = HardwareUtils.instance.getDeviceId()
                    user.platform = AppConfig.getPushType()
                    val bean = HybridAppConfigBean(user = user, org = HybridAppConfigOrgBean(),
                            api = HybridAppConfigApiBean(), features = UserFeatures(), appToken = AppConfig.getCATToken())
                    callback?.onExResult(bean)
                } else {
                    mGetItemBack = callback
                    requestUserInfo()
                }
            }
            //插入要存储的值
            mView.getContext().getString(R.string.register_domain_setItem) -> {
                val bean = GsonUtil.instance.gsonToBean(data.toString(), HybridAppConfigBean::class.java)
                if (!bean.user?.id.empty()) {
                    AppConfig.setUserId(Integer.parseInt(bean.user?.id!!))
                }
                if (!bean.user?.token.empty()) {
                    AppConfig.setUserToken(bean.user?.token!!)
                }
                //呼叫中心权限
                if (bean.features?.callCenter != null) {
                    AppConfig.setCallCenter(bean.features?.callCenter!!)
                }
                //录音服务
                SalesDynamicsManager.instance.startPhoneStatusService(mView.getContext())
                callback?.onExResult(HybridAppConfigBean())
            }
            //获取网络类型
            mView.getContext().getString(R.string.register_networkType) -> {
                val netType = NetStateMonitor.instance.getCurrentNetState()
                callback?.onResult(netType.name)
                Logger.d(netType.name)
            }
            //获取设备信息
            mView.getContext().getString(R.string.register_phoneInfo) -> {
                mView.getContext().checkSimNum(false) {
                    val phoneInfo = PhoneInfoData()
                    phoneInfo.clientId = AppConfig.getPushClientId()
                    phoneInfo.simAmount = it
                    callback?.onExResult(phoneInfo)
                    Logger.d("$phoneInfo")
                }
            }
            //设置标题
            mView.getContext().getString(R.string.register_setTitle) -> {
                val bean = GsonUtil.instance.gsonToBean(data.toString(), SetTitleData::class.java)
                mView.getTitleView().setCenterText(bean.title)
            }
            //设置标题右按钮
            mView.getContext().getString(R.string.register_setRight) -> {
                callback?.onResult(data)
                val bean = GsonUtil.instance.gsonToBean(data.toString(), SetLeftRight::class.java)
                mView.getTitleView().setRightVisibility(bean.show)

                if (bean.text.empty()) {
                    mView.getTitleView().setRightImgVisibility(true)
                    mView.getTitleView().setRightTvVisibility(false)
                    //设置未读消息数量
                    mPopupMsgCount = bean.num
                    mView.setTitleBarMsgCount(mPopupMsgCount)
                    //设置底部弹出框
                    mView.getTitleView().setRightIconClick(object : AKTitleBar.ClickListener {
                        override fun onClick() {
                            mView.setRightClick(true, "popup")
                        }
                    })
                } else {
                    mView.getTitleView().setRightImgVisibility(false)
                    mView.getTitleView().setMsgNumVisibility(false)
                    mView.getTitleView().setRightTvVisibility(true)
                    mView.getTitleView().setRightText(bean.text)
                    mView.getTitleView().setRightTvClick(object : AKTitleBar.ClickListener {
                        override fun onClick() {
                            mView.setRightClick(false, "else")
                        }
                    })
                }
            }
            //监听返回
            mView.getContext().getString(R.string.register_goBack) -> {
                mView.webViewGoBack()
            }
            //监听关闭
            mView.getContext().getString(R.string.register_close) -> {
                mView.webViewfinish()
            }
            //监听菜单和提示
            mView.getContext().getString(R.string.register_setMenu) -> {
                val bean = GsonUtil.instance.gsonToBean(data.toString(), SetMenuData::class.java)
                mView.getTitleView().setRightImgVisibility(false)
                mView.getTitleView().setMsgNumVisibility(false)
                mView.getTitleView().setRightTvVisibility(true)
                mView.getTitleView().setRightText("更多")
                mView.getTitleView().setRightTvClick(object : AKTitleBar.ClickListener {
                    override fun onClick() {
                        //初始化并显示菜单选项
                        initActionSheet(bean, callback)
                    }
                })
            }
            //监听菜单和提示
            mView.getContext().getString(R.string.register_setMenuCount) -> {
                val bean = GsonUtil.instance.gsonToBean(data.toString(), SetMenuPicData::class.java)
                mView.getTitleView().setRightTvVisibility(false)
                mView.getTitleView().setRightImgVisibility(bean.show)
                mView.getTitleView().setMsgNumVisibility(bean.show)
                //设置未读消息数量
                mPopupMsgCount = bean.count
                mView.setTitleBarMsgCount(mPopupMsgCount)
            }
            //监听退出App
            mView.getContext().getString(R.string.register_exitApp) -> {
                mView.appQuit()
            }
            //获取图片信息
            mView.getContext().getString(R.string.register_fetchImageData) -> {
                var bean = GsonUtil.instance.gsonToBean(data.toString(), SetMenuData::class.java)
            }
            //打开链接
            mView.getContext().getString(R.string.register_openLink) -> {
                val bean = GsonUtil.instance.gsonToBean(data.toString(), OpenLinkData::class.java)
                if (bean.url.startsWith("http") || bean.url.startsWith("https")) {
                    mView.wvOpenUrl(bean.url)
                }
            }
            //日期选择器
            mView.getContext().getString(R.string.register_datePicker) -> {
                val bean = GsonUtil.instance.gsonToBean(data.toString(), TimePickerData::class.java)
                val date = Calendar.getInstance()
                val sdf = SimpleDateFormat(bean.format, Locale.getDefault())
                DatePickerDialog(mView.getContext(), { _, year, month, dayOfMonth ->
                    date.set(Calendar.YEAR, year)
                    date.set(Calendar.MONTH, month)
                    date.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    callback?.onResult(sdf.format(date.time))
                    Logger.d("datePicker==" + sdf.format(date.time))
                }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show()
            }
            //时间选择器
            mView.getContext().getString(R.string.register_timePicker) -> {
                val bean = GsonUtil.instance.gsonToBean(data.toString(), TimePickerData::class.java)
                val date = Calendar.getInstance()
                val sdf = SimpleDateFormat(bean.format, Locale.getDefault())
                TimePickerDialog(mView.getContext(), { _, hourOfDay, minute ->
                    date.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    date.set(Calendar.MINUTE, minute)
                    callback?.onResult(sdf.format(date.time))
                    Logger.d("timePicker==" + sdf.format(date.time))
                }, date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), true).show()
            }
            //时间选择器
            mView.getContext().getString(R.string.register_dateTimePicker) -> {
                val bean = GsonUtil.instance.gsonToBean(data.toString(), TimePickerData::class.java)
                val date = Calendar.getInstance()
                val sdf = SimpleDateFormat(bean.format, Locale.getDefault())
                DatePickerDialog(mView.getContext(), { _, year, month, dayOfMonth ->
                    date.set(Calendar.YEAR, year)
                    date.set(Calendar.MONTH, month)
                    date.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    TimePickerDialog(mView.getContext(), { _, hourOfDay, minute ->
                        date.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        date.set(Calendar.MINUTE, minute)
                        callback?.onResult(sdf.format(date.time))
                        Logger.d("dateTimePicker==" + sdf.format(date.time))
                    }, date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), true).show()
                }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show()
            }
            //图库图片选择上传
            mView.getContext().getString(R.string.register_uploadImage) -> {
                val bean = GsonUtil.instance.gsonToBean(data.toString(), AlbumData::class.java)
                gallerySelect(callback, bean.max)
            }
            //预览图片
            mView.getContext().getString(R.string.register_previewImage) -> {
                val bean = GsonUtil.instance.gsonToBean(data.toString(), PreviewPhotosBean::class.java)
                //检查urls不为空，当前显示页包含于urls中
                if (!bean.urls.isNullOrEmpty() && bean.urls!!.contains(bean.current)) {
                    PreviewPhotosActivity.intentToPreviewPhotosActivity(mView.getContext(), bean)
                } else {
                    R.string.toast_data_err.toast()
                }
            }
            //拍照上传
            mView.getContext().getString(R.string.register_uploadImageFromCamera) -> {
                val bean = GsonUtil.instance.gsonToBean(data.toString(), UploadImageFromCamera::class.java)
                PermissionUtils.instance.checkPermissions(mView.getContext(), Permission.CAMERA)
                { permission ->
                    if (permission) {
                        mView.uploadImageFromCamera(callback, bean)
                    } else {
                        callback.onExResult(WebBackError(WebBackBean("请求相机权限失败", 0)))
                    }
                }
            }
            //预览文件
            mView.getContext().getString(R.string.register_previewFile) -> {
                val bean = GsonUtil.instance.gsonToBean(data.toString(), PreviewFileData::class.java)
                //打开文件
                bean.url?.openThirdFileWithUrl(mView.getContext())
            }
            //分享
            mView.getContext().getString(R.string.register_share) -> {
                val bean = GsonUtil.instance.gsonToBean(data.toString(), ShareData::class.java)
                val shareBoard = AKShareBoard(mView.getActivity(), bean)
                shareBoard.showAtLocation(mView.getActivity().window.decorView, Gravity.BOTTOM, 0, 0)
                mView.getActivity().changeWindowAlpha(0.7f)
                shareBoard.setOnDismissListener {
                    mView.getActivity().changeWindowAlpha(1f)
                }
            }
            //扫码
            mView.getContext().getString(R.string.register_scan) -> {
                PermissionUtils.instance.checkPermissions(mView.getContext(), Permission.CAMERA)
                { permission ->
                    if (permission) {
                        mScanBack = callback
                        mView.scan()
                    } else {
                        mScanBack.onExResult(WebBackError(WebBackBean("请求相机权限失败", 0)))
                    }
                }
            }
            //删除存入的值
            mView.getContext().getString(R.string.register_domain_removeItem) -> {
                val bean = GsonUtil.instance.gsonToBean(data.toString(), SPData::class.java)
                SPUtils.instance.remove(bean.key)
            }
            //定位
            mView.getContext().getString(R.string.register_geolocationGet) -> {
                //判断权限
                PermissionUtils.instance.checkPermissions(mView.getContext(),
                        Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION) {
                    if (it) {
                        HardwareUtils.instance.getLocation(mView.getContext()) { success, location ->
                            if (success) {
                                val callBackData = LocationCallBack(location.longitude, location.latitude)
                                callback?.onExResult(callBackData)
                            } else {
                                callback.onExResult(WebBackError(WebBackBean("请求定位失败", 0)))
                            }
                        }
                    } else {
                        callback.onExResult(WebBackError(WebBackBean("请求定位权限失败", 0)))
                    }
                }
            }
            //调用第三方地图
            mView.getContext().getString(R.string.register_mapView) -> {

            }
            //开始录音
            mView.getContext().getString(R.string.register_recordStart) -> {
                PermissionUtils.instance.checkPermissions(mView.getContext(),
                        Permission.RECORD_AUDIO, Permission.WRITE_EXTERNAL_STORAGE) {
                    if (it) {
                        canRecordAudio = true
                        val intent = Intent(mView.getContext(), RecordService::class.java)
                        mView.getContext().startService(intent)
                        mView.getActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        canRecordAudio = false
                        callback.onExResult(WebBackError(WebBackBean("请求录音权限失败", 0)))
                    }
                }
            }
            //停止录音
            mView.getContext().getString(R.string.register_recordStop) -> {
                if (canRecordAudio) {
                    mRecordBack = callback
                    mView.showProgressDialog()
                    val intent = Intent(mView.getContext(), RecordService::class.java)
                    mView.getContext().stopService(intent)
                    mView.getActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    callback.onExResult(WebBackError(WebBackBean("请求录音权限失败", 0)))
                }
            }
            //录音结束
            mView.getContext().getString(R.string.register_recordEnd) -> {

            }
            //开始播放
            mView.getContext().getString(R.string.register_audioPlay) -> {
                PermissionUtils.instance.checkPermissions(mView.getContext(),
                        Permission.RECORD_AUDIO, Permission.READ_EXTERNAL_STORAGE) {
                    if (it) {
                        canRecordAudio = true
                        val bean = GsonUtil.instance.gsonToBean(data.toString(), PlayAudioBean::class.java)
                        startMediaPlayer(bean.url)
                    } else {
                        canRecordAudio = false
                        callback.onExResult(WebBackError(WebBackBean("请求录音权限失败", 0)))
                    }
                }
            }
            //停止播放
            mView.getContext().getString(R.string.register_audioStop) -> {
                if (canRecordAudio) {
                    stopMediaPlayer()
                }
            }
            //播放结束
            mView.getContext().getString(R.string.register_audioPlayEnd) -> {
                destroyMediaPlayer()
            }
            //退出登录
            mView.getContext().getString(R.string.register_logout) -> {
                AppConfig.appLogOut(mView.getContext())
            }
            //同步日历
            mView.getContext().getString(R.string.register_eventSave) -> {
                val bean = GsonUtil.instance.gsonToBean(data.toString(), AddCalendarEventBean::class.java)
                PermissionUtils.instance.checkPermissions(mView.getContext(),
                        Permission.READ_CALENDAR, Permission.WRITE_CALENDAR) {
                    if (it) {
                        CalendarEventsUtils.instance.addCalendarEvent(mView.getContext(), bean) { success ->
                            if (success) {
                                callback.onExResult(WebBackSuccess(WebBackBean("同步日历成功", 1)))
                            } else {
                                callback.onExResult(WebBackError(WebBackBean("同步日历失败", 0)))
                            }
                        }
                    } else {
                        callback.onExResult(WebBackError(WebBackBean("获取日历权限失败", 0)))
                    }
                }
            }
            //拨打电话
            mView.getContext().getString(R.string.register_callPhone) -> {
                if (isFastTrigger(3000)) {
                    "电话正在处理，请勿重复拨打".toast()
                    return
                }
                HandlePostLog.postLogBaseTopic(HandleLogEntity.TOPIC_RECEIVE_H5_MSG, HandleLogEntity.EVENT_CALL_PHONE, data.toString())
                val bean = GsonUtil.instance.gsonToBean(data.toString(), CallPhoneBean::class.java)
                if (AppConfig.getCallCenter()) {
                    val carouselCardNo = if (bean.carousel_card_no.empty()) {
                        0
                    } else {
                        bean.carousel_card_no.toInt()
                    }
                    HandlerCall().handlerCreateModel(buildSalesDynamicsModel(bean, 11), bean.number, bean.enable_carousel, carouselCardNo)
                    { msg, success ->
                        Observable.timer(2, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                                .subscribe {
                                    callback.onExResult(WebBackSuccess(WebBackBean(msg, (if (success) 1 else 0))))
                                }
                    }
                } else {
                    //拨打电话
                    bean.number.callPhone(mView.getContext())
                    Observable.timer(2, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                callback.onExResult(WebBackSuccess(WebBackBean("拨打电话成功", 1)))
                            }
                }
            }
            //拨打公费电话
            mView.getContext().getString(R.string.register_callPublicTelephone) -> {
                if (isFastTrigger(3000)) {
                    "电话正在处理，请勿重复拨打".toast()
                    return
                }
                HandlePostLog.postLogBaseTopic(HandleLogEntity.TOPIC_RECEIVE_H5_MSG, HandleLogEntity.EVENT_CALL_PUBLIC_PHONE, data.toString())
                //公费电话需要手机上传录音
                val bean = GsonUtil.instance.gsonToBean(data.toString(), CallPhoneBean::class.java)
                val carouselCardNo = if (bean.carousel_card_no.empty()) {
                    0
                } else {
                    bean.carousel_card_no.toInt()
                }
                if (!bean.middle_call_number.empty()) {
                    HandlerCall().handlerCreateModel(buildSalesDynamicsModel(bean, 12), bean.number, bean.enable_carousel, carouselCardNo)
                    { msg, success ->
                        Observable.timer(2, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                                .subscribe {
                                    callback.onExResult(WebBackSuccess(WebBackBean("小号，$msg", (if (success) 1 else 0))))
                                }
                    }
                } else {
                    callback.onExResult(WebBackError(WebBackBean("拨打小号电话失败", 0)))
                }
            }
            //获取Sim卡数量
            mView.getContext().getString(R.string.register_getSimAmount) -> {
                mView.getContext().checkSimNum(false) {
                    val phoneInfo = PhoneInfoData()
                    phoneInfo.simAmount = it
                    callback?.onExResult(phoneInfo)
                }
            }
        }
    }

    /**
     * 创建通话记录数据
     *
     * @param bean CallPhoneBean
     * @param type Int 公费12 or 自费11
     * @return SalesDynamicsModel
     */
    private fun buildSalesDynamicsModel(bean: CallPhoneBean, type: Int): SalesDynamicsModel {
        val saveData = SalesDynamicsModel()
        saveData.callId = bean.call_id
        saveData.callerType = bean.caller_type
        saveData.callerId = bean.caller_id
        saveData.name = bean.name
        saveData.nameType = bean.name_type
        if (type == 11) {
            saveData.phoneNumber = bean.number
        } else {
            saveData.phoneNumber = bean.middle_call_number
        }
        saveData.phoneType = KeySet.KEY_CALL_PHONE_TYPE_CLICK
        return saveData
    }

    /**
     * 图库选择图片
     * @param max Int 选择最大数
     */
    override fun gallerySelect(callback: AKWebView.WVJBResponseCallback?, max: Int) {
        val imageList = SparseArray<File>()
        HardwareUtils.instance.albumGallerySelect(mView.getContext(), max = max)
        { photoList ->
            if (photoList.isNullOrEmpty()) {
                callback.onExResult(WebBackError(WebBackBean("上传图片失败，用户取消选择图片", 0)))
            } else {
                //压缩图片
                for ((i, v) in photoList.withIndex()) {
                    ImageUtils.instance.imageCompression(mView.getContext(), File(v.path), i)
                    { success, zipImage, imageIndex ->
                        if (success) {
                            imageList.put(imageIndex, zipImage)
                            if (photoList.size == imageList.size()) {
                                mView.gallerySelectBack(callback, imageList)
                            }
                        } else {
                            callback.onExResult(WebBackError(WebBackBean("上传图片失败，图片压缩失败", 0)))
                        }
                    }
                }
            }
        }
    }

    /**
     * 字体选择
     */
    override fun fontSizeSelect() {
        ActionSheetDialog(mView.getContext())
                .builder()
                .setCancelable(true)
                .setCanceledOnTouchOutside(true)
                .addSheetItem("小", object : OnSheetItemClickListener {
                    override fun onClick(which: Int) {
                        mView.fontSizeSelectBack(90)
                    }
                })
                .addSheetItem("默认", object : OnSheetItemClickListener {
                    override fun onClick(which: Int) {
                        mView.fontSizeSelectBack(100)
                    }
                })
                .addSheetItem("大", object : OnSheetItemClickListener {
                    override fun onClick(which: Int) {
                        mView.fontSizeSelectBack(110)
                    }
                })
                .addSheetItem("更大", object : OnSheetItemClickListener {
                    override fun onClick(which: Int) {
                        mView.fontSizeSelectBack(120)
                    }
                })
                .addSheetItem("最大", object : OnSheetItemClickListener {
                    override fun onClick(which: Int) {
                        mView.fontSizeSelectBack(130)
                    }
                })
                .setCancelTxtColor(R.color.colorAccent.getColor(mView.getContext()))
                .show()
    }

    /**
     * 初始化底部弹出popup
     * @param containerView BottomPopupView
     */
    override fun initPopupWindow(containerView: BottomPopupView) {
        if (mPopView == null) {
            mPopView = BottomPopupMore(mView.getContext())
            containerView.setContentView(mPopView!!)
            //设置点击监听
            mPopView!!.mItemClickListener = object : OnSheetItemClickListener {
                override fun onClick(which: Int) {
                    mView.showBtmPopup()
                    when (which) {
                        mPopView!!.CANCEL -> {
                        }
                        mPopView!!.SHARE_WX -> {
                        }
                        mPopView!!.SHARE_PYQ -> {
                        }
                        mPopView!!.SHARE_QQ -> {
                        }
                        mPopView!!.SHARE_URL -> {
                        }
                        mPopView!!.TOOLS_MSG -> {
                            //mView.wvOpenUrl()
                        }
                        mPopView!!.TOOLS_CONFIG -> {
                            //startActivity(Intent(this@MainActivity, SettingActivity::class.java))
                        }
                        mPopView!!.TOOLS_FONT -> {
                            fontSizeSelect()
                        }
                        mPopView!!.TOOLS_REFRESH -> {
                            mView.wvReload()
                        }
                    }
                }
            }
        }
        //设置消息数量
        mView.setPopupMessageCount(mPopupMsgCount, mPopView?.getTvToolsMsgRed())
        mView.showBtmPopup()
    }

    /**
     * 初始化并显示菜单选项
     *
     * @param bean SetMenuData
     * @param callback AKWebView.WVJBResponseCallback?
     */
    override fun initActionSheet(bean: SetMenuData?, callback: AKWebView.WVJBResponseCallback?) {
        val actionSheetDialog = ActionSheetDialog(mView.getContext())
                .builder()
                .setCancelable(true)
                .setCanceledOnTouchOutside(true)
        if (bean != null) {
            for (item in bean.items) {
                actionSheetDialog.addSheetItem(item.text, object : OnSheetItemClickListener {
                    override fun onClick(which: Int) {
                        Logger.d(item.toString())
                        mView.setMenuClick(item, callback)
                    }
                })
            }
        }
        actionSheetDialog.show()
    }

    /**
     * 获取用户信息
     */
    override fun requestUserInfo() {
        NetWorkUtil.instance.initRetrofit().getUserInfo()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseSubscriber<ResponseUserInfo>() {
                    var disposable: Disposable? = null
                    override fun onStart(d: Disposable) {
                        disposable = d
                    }

                    override fun onSuccess(code: Int, response: ResponseUserInfo?) {
                        if (response != null) {
                            AppConfig.setUserInfo(response)
                            AppConfig.setUId(response.uid)
                            NBSAppAgent.setUserIdentifier("${response.id}")
                            NBSAppAgent.setUserCrashMessage("用户名：${response.name}", "账号：${response.phone}")
                            if (mGetItemBack != null) {
                                val user = HybridAppConfigUserBean()
                                user.id = "${AppConfig.getUserId()}"
                                user.token = AppConfig.getUserToken()
                                user.client_id = AppConfig.getPushClientId()
                                user.name = response.name
                                user.phone = response.phone
                                user.login = AppConfig.getUserLogin()
                                user.password = AppConfig.getUserPsw()
                                user.avatar = response.avatar_url
                                user.isSuperUser = response.is_super_user
                                user.device_model = android.os.Build.MODEL
                                user.system_version = android.os.Build.VERSION.RELEASE
                                user.device_id = HardwareUtils.instance.getDeviceId()
                                user.platform = AppConfig.getPushType()
                                user.uid = "${response.uid}"
                                val bean = HybridAppConfigBean(user = user, org = HybridAppConfigOrgBean(),
                                        api = HybridAppConfigApiBean(), features = UserFeatures(), appToken = AppConfig.getCATToken())
                                mGetItemBack.onExResult(bean)
                                mView.getContext().openForegroundService()
                            }
                        } else {
                            Logger.e("获取用户信息失败")
                            "获取用户信息失败".toast()
                        }
                    }

                    override fun onError(code: Int, message: String) {
                        Logger.e("error: $message")
                        message.toast()
                        if (code == 100401 && mGetItemBack != null) {
                            val user = HybridAppConfigUserBean()
                            user.token = AppConfig.getUserToken()
                            val bean = HybridAppConfigBean(user = user, org = HybridAppConfigOrgBean(),
                                    api = HybridAppConfigApiBean(), features = UserFeatures(), appToken = AppConfig.getCATToken())
                            mGetItemBack.onExResult(bean)
                        }
                    }

                    override fun onFinish() {
                        disposable?.dispose()
                        mGetItemBack = null
                    }
                })
    }

    /**
     * 上传图片
     * @param callback AKWebView.WVJBResponseCallback?
     * @param fileList ArrayList<File>
     */
    override fun upLoadImage(callback: AKWebView.WVJBResponseCallback?, fileList: SparseArray<File>) {
        //获取上传token
        UploadUtils.instance.getUploadToken(UploadUtils.FILE_TYPE_ATTACHMENT)
        { success, token ->
            if (success) {
                //上传录音到七牛
                UploadUtils.instance.uploadFile2QiNiu(fileList, token!!, UploadUtils.UP_FILE_TYPE_ATTACHMENT)
                { bean ->
                    val resultList = ArrayList<UploadImageBean?>()
                    for (i in 0 until bean.size()) {
                        if (bean.get(i) == null) {
                            resultList.add(UploadImageBean(-1, ""))
                        } else {
                            resultList.add(UploadImageBean(bean.get(i).id, bean.get(i).file_url))
                        }
                    }
                    callback?.onExResult(resultList)
                    mView.dismissProgressDialog()
                }
            } else {
                callback.onExResult(WebBackError(WebBackBean("上传图片失败", 0)))
                mView.dismissProgressDialog()
            }
        }
    }

    /**
     * 上传手动录音
     * @param audioPath String
     */
    @SuppressLint("CheckResult")
    override fun upLoadRecord(audioPath: String) {
        if (audioPath.empty()) {
            mRecordBack?.onExResult(WebBackError(WebBackBean("手动上传录音失败，录音路径为空", 0)))
            mView.dismissProgressDialog()
        } else {
            //上传录音
            Observable.timer(1500, TimeUnit.MILLISECONDS)
                    .flatMap(Function<Long, ObservableSource<ResponseBody>> {
                        return@Function NetWorkUtil.instance.initRetrofit().getUploadQiNiuToken(UploadUtils.FILE_TYPE_AUDIO)
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : BaseObserver<ResponseQiNiuToken>(ResponseQiNiuToken::class.java) {
                        override fun onSuccess(responseStr: String, responseBean: ResponseQiNiuToken) {
                            if (!responseBean.uptoken.empty()) {
                                Logger.d("获取七牛Token成功!")
                                val file = File(audioPath)
                                if (file.exists()) {
                                    UploadUtils.instance.uploadFile2QiNiu(file, responseBean.uptoken, UploadUtils.UP_FILE_TYPE_AUDIO)
                                    { uploadSuccess, bean, _ ->
                                        if (uploadSuccess && bean != null) {
                                            val resultList = UploadRecordBean(bean.id, bean.file_url, "${bean.duration}")
                                            mRecordBack?.onExResult(resultList)
                                        } else {
                                            mRecordBack?.onExResult(WebBackError(WebBackBean("手动上传录音失败", 0)))
                                        }
                                        mView.dismissProgressDialog()
                                    }
                                } else {
                                    Logger.e("录音文件未找到!")
                                    mRecordBack?.onExResult(WebBackError(WebBackBean("手动上传录音失败，录音文件未找到", 0)))
                                    mView.dismissProgressDialog()
                                }
                            } else {
                                Logger.e("获取七牛Token失败!")
                                mView.dismissProgressDialog()
                            }
                        }

                        override fun onError(code: Int, message: String) {
                            Logger.e("获取七牛Token失败!")
                            mView.dismissProgressDialog()
                        }

                        override fun onStart(d: Disposable) {

                        }

                        override fun onFinish() {

                        }
                    })
        }
    }

    /**
     * 播放录音
     * @param url String
     */
    override fun startMediaPlayer(url: String) {
        stopMediaPlayer()
        mMediaPlayer = MediaPlayer()
        mMediaPlayer?.setDataSource(url)
        mMediaPlayer?.setAudioAttributes(AudioAttributes.Builder().setLegacyStreamType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
        mMediaPlayer?.prepareAsync()
        mMediaPlayer?.setOnPreparedListener {
            Logger.d("开始播放")
            mMediaPlayer?.start()
        }
        mMediaPlayer?.setOnCompletionListener {
            Logger.d("完成播放")
            destroyMediaPlayer()
        }
        mMediaPlayer?.setOnErrorListener { _, _, _ ->
            destroyMediaPlayer()
            "录音播放失败".toast()
            return@setOnErrorListener false
        }
    }

    /**
     * 停止播放录音
     */
    override fun stopMediaPlayer() {
        if (mMediaPlayer?.isPlaying == true) {
            mMediaPlayer?.stop()
            mMediaPlayer?.release()
            mMediaPlayer = null
        }
    }

    /**
     * 销毁录音
     */
    override fun destroyMediaPlayer() {
        mMediaPlayer?.stop()
        mMediaPlayer?.release()
        mMediaPlayer = null
    }

    /**
     * 扫码上传
     * @param scanData String
     */
    override fun upLoadScan(scanData: String?) {
        mScanBack.onExResult(scanData)
    }

    /**
     * 下载录音路径
     */
    @SuppressLint("CheckResult")
    override fun downLoadRecordPath() {
        val file = AppConfig.RECORD_PATH_FILE.getInternalStorageFile()
        PermissionUtils.instance.checkPermissions(mView.getContext(),
                Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_EXTERNAL_STORAGE) {
            if (it) {
                HardwareUtils.instance.downLoadFile(AppConfig.RECORD_PATH_URL, file, object : DownLoadListener {
                    override fun onStart() {
                    }

                    override fun onSuccess() {
                        Logger.d("录音文件下载成功")
                    }

                    override fun onFail(e: String) {
                        Logger.e(e)
                    }
                })
            } else {
                Logger.e("录音文件下载失败，请确认权限开启")
            }
        }
    }
}