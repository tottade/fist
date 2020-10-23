package com.lixiaoyun.aike.activity

import android.app.Activity
import android.content.Context
import android.util.SparseArray
import android.widget.TextView
import com.lixiaoyun.aike.entity.Item
import com.lixiaoyun.aike.entity.SetMenuData
import com.lixiaoyun.aike.entity.UploadImageFromCamera
import com.lixiaoyun.aike.widget.AKTitleBar
import com.lixiaoyun.aike.widget.AKWebView
import com.lixiaoyun.aike.widget.BottomPopupView
import io.reactivex.disposables.Disposable
import java.io.File

/**
 * @data on 2019/5/5
 */
interface MainContract {

    interface View {

        /**
         * loading dialog
         */
        fun showProgressDialog()

        fun dismissProgressDialog()

        fun progressDialogShowing(): Boolean

        /**
         * 获取上下文实例
         * @return Context
         */
        fun getContext(): Context

        /**
         * 获取Activity对象
         * @return Activity
         */
        fun getActivity(): Activity

        /**
         * 获取标题栏
         * @return AKTitleBar
         */
        fun getTitleView(): AKTitleBar

        /**
         * 弹出/隐藏底部弹出popup
         * @return Boolean
         */
        fun showBtmPopup(): Boolean

        /**
         * 添加网络解绑
         * @param disposable Disposable
         */
        fun addDisposable(disposable: Disposable)

        /**
         * 字体选择返回
         * @param size Int
         */
        fun fontSizeSelectBack(size: Int)

        /**
         * 设置右侧点击事件
         */
        fun setRightClick(iconClick: Boolean, type: String)

        /**
         * 设置菜单点击事件
         * @param item Item
         * @param callback AKWebView.WVJBResponseCallback?
         */
        fun setMenuClick(item: Item, callback: AKWebView.WVJBResponseCallback?)

        /**
         * 设置标题栏消息数量
         * @param count Int
         */
        fun setTitleBarMsgCount(count: Int)

        /**
         * 设置弹出框消息数量
         * @param count Int
         */
        fun setPopupMessageCount(count: Int, view: TextView?)

        /**
         * 监听WebView返回键
         */
        fun webViewGoBack()

        /**
         * 监听App的finish状态
         */
        fun webViewfinish()

        /**
         * 监听App退出
         */
        fun appQuit()

        /**
         * 打开链接
         * @param url String
         */
        fun wvOpenUrl(url: String)

        /**
         * 刷新
         */
        fun wvReload()

        /**
         * 拍照上传
         * @param callback AKWebView.WVJBResponseCallback?
         * @param bean UploadImageFromCamera
         */
        fun uploadImageFromCamera(callback: AKWebView.WVJBResponseCallback?, bean: UploadImageFromCamera)

        /**
         * 图库选择返回
         * @param images ArrayList<File>
         */
        fun gallerySelectBack(callback: AKWebView.WVJBResponseCallback?, images: SparseArray<File>)

        /**
         * 调用扫码
         */
        fun scan()

    }

    interface Presenter {

        /**
         * 注册H5接口
         * @param wv AKWebView
         */
        fun registerHtmlInterface(wv: AKWebView)

        /**
         * 图库选择图片
         * @param max Int 选择最大数
         */
        fun gallerySelect(callback: AKWebView.WVJBResponseCallback?, max: Int)

        /**
         * 字体选择
         */
        fun fontSizeSelect()

        /**
         * 初始化底部弹出popup
         * @param containerView BottomPopupView
         */
        fun initPopupWindow(containerView: BottomPopupView)

        /**
         * 初始化并显示菜单选项
         * @param bean SetMenuData
         * @param callback AKWebView.WVJBResponseCallback?
         */
        fun initActionSheet(bean: SetMenuData?, callback: AKWebView.WVJBResponseCallback?)

        /**
         * 获取用户信息
         */
        fun requestUserInfo()

        /**
         * 上传图片
         * @param callback AKWebView.WVJBResponseCallback?
         * @param fileList ArrayList<File>
         */
        fun upLoadImage(callback: AKWebView.WVJBResponseCallback?, fileList: SparseArray<File>)

        /**
         * 上传录音
         * @param audioPath String
         */
        fun upLoadRecord(audioPath: String)

        /**
         * 播放录音
         * @param url String
         */
        fun startMediaPlayer(url: String)

        /**
         * 停止播放录音
         */
        fun stopMediaPlayer()

        /**
         * 销毁录音
         */
        fun destroyMediaPlayer()

        /**
         * 扫码上传
         * @param scanData String
         */
        fun upLoadScan(scanData: String?)

        /**
         * 下载录音路径
         */
        fun downLoadRecordPath()
    }

}