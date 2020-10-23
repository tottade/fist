package com.lixiaoyun.aike.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.SparseArray
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.constant.KeySet
import com.lixiaoyun.aike.entity.*
import com.lixiaoyun.aike.network.NetStateMonitor
import com.lixiaoyun.aike.network.NetWorkConfig
import com.lixiaoyun.aike.utils.*
import com.lixiaoyun.aike.utils.recordingUtils.SalesDynamicsManager
import com.lixiaoyun.aike.widget.AKTitleBar
import com.lixiaoyun.aike.widget.AKWebView
import com.orhanobut.logger.Logger
import com.tencent.smtt.export.external.interfaces.SslError
import com.tencent.smtt.export.external.interfaces.SslErrorHandler
import com.tencent.smtt.sdk.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class MainActivity : BaseActivity(), MainContract.View {

    private lateinit var mWv: AKWebView
    private lateinit var mWvSt: WebSettings

    private var mPresenter: MainPresenter = MainPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        setContentView(R.layout.activity_main)
        StatusBarUtil.setImmersiveStatusBar(this, root, true, false, getColor(R.color.colorPrimary))

        initWebView()
        vFl.addView(mWv)

        vAkTitleBar.setLeftClick(object : AKTitleBar.ClickListener {
            override fun onClick() {
                if (mWv.canGoBack()) {
                    mWv.goBack()
                } else {
                    finish()
                }
            }
        })
        mPresenter.downLoadRecordPath()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        mWv = AKWebView(this)
        mWvSt = mWv.setting

        mWvSt.blockNetworkImage = true

        mWv.webChromeClient = object : WebChromeClient() {

            override fun openFileChooser(callBack: ValueCallback<Uri>?, acceptType: String?, capture: String?) {
            }

            override fun onShowFileChooser(webView: WebView?, callBack: ValueCallback<Array<Uri>>?,
                                           fileChooserParams: FileChooserParams?): Boolean {
                return true
            }
        }

        mWv.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                // 接受所有网站的证书
                handler.proceed()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                return if (url.contains("tel:")) {
                    //拦截电话
                    val mobile = url.substring(url.lastIndexOf("/") + 1)
                    Logger.d("电话： $mobile")
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse(mobile)
                    startActivity(intent)
                    true
                } else {
                    super.shouldOverrideUrlLoading(view, url)
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                mWvSt.blockNetworkImage = false

                //设置返回键
                vAkTitleBar.setLeftVisibility(mWv.canGoBack())
            }
        }

        mWv.loadUrl(NetWorkConfig.getHybridUrl())

        createReloadHeartBeat()
    }

    private fun createReloadHeartBeat() {
        val disposable = Observable
                .interval(1, 3, TimeUnit.MINUTES)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    SalesDynamicsManager.instance.reloadRecord(this)
                }
        addDisposable(disposable)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                KeySet.REQUEST_CODE_SCAN -> {
                    //扫码返回
                    val sendScanCode = SendScanCode(data?.getStringExtra(KeySet.I_RESULT_SCAN))
                    val jsonSendScanCode = GsonUtil.instance.gsonString(sendScanCode)
                    mPresenter.upLoadScan(jsonSendScanCode)
                }
            }
        }
    }

    override fun onResume() {
        mWv.onResume()
        super.onResume()
        SalesDynamicsManager.instance.reloadRecord(this)
        //注册被js调用的方法
        mPresenter.registerHtmlInterface(mWv)
        //尝试注册神策埋点
        val uid = AppConfig.getUId()
        if (uid != 0) {
            //每天第一次进入首页，上报神策事件
            val nowTime = DateUtils.instance.getNowString(DateUtils.FORMAT_DAY)
            val lastDay = AppConfig.getLastCrmIndexTime(uid)
            if (!lastDay.isSame(nowTime)) {
                SensorsUtils.pushSAProfile { success ->
                    if (success) {
                        //每天第一次进入首页，上报神策事件
                        SensorsUtils.setTrack(SensorsUtils.E_ENTER_INDEX, null, null)
                        AppConfig.setLastCrmIndexTime(uid, nowTime)
                    }
                }
            }
        }
    }

    override fun onPause() {
        mWv.onPause()
        super.onPause()
    }

    /**
     * 先把webView从页面中移除，再销毁
     */
    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        mPresenter.destroyMediaPlayer()
        vFl.removeView(mWv)
        mWv.destroy()
        super.onDestroy()
    }

    override fun showProgressDialog() {
        showProgress()
    }

    override fun dismissProgressDialog() {
        dismissProgress()
    }

    override fun progressDialogShowing(): Boolean {
        return progressIsShowing()
    }

    /**
     * 返回键
     */
    override fun onBackPressed() {
        if (vPopBottom.isShowing()) {
            vPopBottom.hide()
        } else {
            super.onBackPressed()
        }
    }

    /**
     * 返回键
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (mWv.canGoBack()) {
                mWv.goBack()
                true
            } else {
                super.onKeyDown(keyCode, event)
            }
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    /**
     * 获取上下文实例
     * @return Context
     */
    override fun getContext(): Context {
        return this
    }

    /**
     * 获取Activity对象
     * @return Activity
     */
    override fun getActivity(): Activity {
        return this@MainActivity
    }

    /**
     * 获取标题栏
     * @return AKTitleBar
     */
    override fun getTitleView(): AKTitleBar {
        return vAkTitleBar
    }

    /**
     * 弹出/隐藏底部弹出popup
     * @return Boolean
     */
    override fun showBtmPopup(): Boolean {
        return if (vPopBottom.isShowing()) {
            vPopBottom.hide()
            false
        } else {
            vPopBottom.show()
            true
        }
    }

    /**
     * 添加网络解绑
     *
     * @param disposable Disposable
     */
    override fun addDisposable(disposable: Disposable) {
        addDisposableList(disposable)
    }

    /**
     * 字体选择返回
     * @param size Int
     */
    override fun fontSizeSelectBack(size: Int) {
        mWvSt.textZoom = size
        AppConfig.setWebTextSize(size)
    }

    /**
     * 设置右侧点击事件
     * @param iconClick Boolean tv or icon
     * @param type String 点击类型
     */
    override fun setRightClick(iconClick: Boolean, type: String) {
        if (iconClick) {
            when (type) {
                "popup" -> {
                    mPresenter.initPopupWindow(vPopBottom)
                }
                else -> {
                    mWv.callHandler(getString(R.string.call_right_click), null) {

                    }
                }
            }
        } else {
            mWv.callHandler(getString(R.string.call_right_click), null) {

            }
        }
    }

    /**
     * 设置菜单点击事件
     * @param item Item
     * @param callback AKWebView.WVJBResponseCallback?
     */
    override fun setMenuClick(item: Item, callback: AKWebView.WVJBResponseCallback?) {
        mWv.callHandler(getString(R.string.call_menu_click), item.id) {
            Logger.d("callHandler")
        }
    }

    /**
     * 设置标题栏消息数量
     * @param count Int
     */
    override fun setTitleBarMsgCount(count: Int) {
        when {
            count in 1..98 -> vAkTitleBar.setMsgInfo(true, "".plus(count))
            count > 99 -> vAkTitleBar.setMsgInfo(true, "99+")
            else -> vAkTitleBar.setMsgNumVisibility(false)
        }
    }

    /**
     * 设置弹出界面消息角标
     * @param count Int
     * @param view TextView?
     */
    override fun setPopupMessageCount(count: Int, view: TextView?) {
        if (view != null) {
            when {
                count in 1..98 -> view.text = "".plus(count)
                count > 99 -> view.text = "99+"
                else -> view.visibility = View.GONE
            }
        }
    }

    /**
     * 监听WebView返回键
     */
    override fun webViewGoBack() {
        if (mWv.canGoBack()) {
            mWv.goBack()
        }
    }

    /**
     * 监听App的finish状态
     */
    override fun webViewfinish() {
        finish()
        exitProcess(0)
    }

    /**
     * 监听App退出
     */
    override fun appQuit() {

    }

    /**
     * 打开链接
     * @param url String
     */
    override fun wvOpenUrl(url: String) {
        mWv.loadUrl(url)
    }

    /**
     * 刷新
     */
    override fun wvReload() {
        mWv.reload()
    }

    /**
     * 调用扫码
     */
    override fun scan() {
        ScanUtilActivity.intentToScanUtilActivity(this)
    }

    /**
     * 拍照上传
     * @param callback AKWebView.WVJBResponseCallback?
     * @param bean UploadImageFromCamera
     */
    override fun uploadImageFromCamera(callback: AKWebView.WVJBResponseCallback?, bean: UploadImageFromCamera) {
        HardwareUtils.instance.albumCamera(this) { originalPath ->
            if (originalPath.isEmpty()) {
                callback.onExResult(WebBackError(WebBackBean("上传图片失败，用户取消拍照", 0)))
            } else {
                //压缩原始图片
                ImageUtils.instance.imageCompression(this, File(originalPath))
                { cpSuccess, cpZipImage, cpImageIndex ->
                    //原始图片压缩成功
                    if (cpSuccess) {
                        showProgressDialog()
                        if (bean.stickers == null) {
                            //不需要水印，直接上传七牛
                            val data = SparseArray<File>()
                            data.put(cpImageIndex, cpZipImage)
                            mPresenter.upLoadImage(callback, data)
                        } else {
                            //需要水印，添加图片水印
                            ImageUtils.instance.createWatermarkPhoto(this, bean.stickers!!, cpZipImage!!.absolutePath)
                            { wmSuccess, wmOutFile ->
                                //添加水印成功
                                if (wmSuccess) {
                                    //压缩水印图片
                                    ImageUtils.instance.imageCompression(this, wmOutFile!!)
                                    { cpWmSuccess, cpWmZipImage, cpWmImageIndex ->
                                        //水印图片压缩成功
                                        if (cpWmSuccess) {
                                            val data = SparseArray<File>()
                                            data.put(cpWmImageIndex, cpWmZipImage)
                                            mPresenter.upLoadImage(callback, data)
                                        } else {
                                            callback.onExResult(WebBackError(WebBackBean("上传图片失败，压缩水印图片失败", 0)))
                                        }
                                    }
                                } else {
                                    callback.onExResult(WebBackError(WebBackBean("上传图片失败，添加水印失败", 0)))
                                }
                            }
                        }
                    } else {
                        callback.onExResult(WebBackError(WebBackBean("上传图片失败，压缩原始图片失败", 0)))
                    }
                }
            }
        }
    }

    /**
     * 图库选择返回
     * @param images ArrayList<File>
     */
    override fun gallerySelectBack(callback: AKWebView.WVJBResponseCallback?, images: SparseArray<File>) {
        showProgressDialog()
        mPresenter.upLoadImage(callback, images)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NetWorkStateEvent) {
        Logger.w("NetWorkStateEvent ${event.netState.name}")
        if (event.netState != NetStateMonitor.NetState.NETWORK_NOT_FIND) {

        }
    }

    /**
     * 录音完成
     * @param event RecordEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: RecordEvent) {
        mPresenter.upLoadRecord(event.recordPath)
    }

    /**
     * 来电弹屏跳转
     * @param event RecordEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: ResponseFloatInfo) {
        Logger.w("ResponseFloatInfo ${GsonUtil.instance.gsonString(event)}")
        mWv.callHandler(getString(R.string.call_callerDisplay), GsonUtil.instance.gsonString(event)) {

        }
    }

    /**
     * 通知栏点击事件
     * @param event RecordEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NotificationClickData) {
        Logger.w("NotificationClickData ${GsonUtil.instance.gsonString(event)}")
        mWv.callHandler(getString(R.string.call_notification), GsonUtil.instance.gsonString(event)) {

        }
    }

    /**
     * 查询双卡轮播事件
     * @param event RecordEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: EnableCarouselData) {
        Logger.w("EnableCarouselData ${GsonUtil.instance.gsonString(event)}")
        mWv.callHandler(getString(R.string.call_enableCarousel), GsonUtil.instance.gsonString(event)) {

        }
    }
}
