package com.lixiaoyun.aike

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.entity.NetWorkStateEvent
import com.lixiaoyun.aike.network.NetStateMonitor
import com.lixiaoyun.aike.pushutils.PushManager
import com.lixiaoyun.aike.utils.SensorsUtils
import com.lixiaoyun.aike.widget.MediaLoader
import com.networkbench.agent.impl.NBSAppAgent
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.tencent.smtt.sdk.QbSdk
import com.umeng.commonsdk.UMConfigure
import com.umeng.socialize.PlatformConfig
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.AlbumConfig
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.properties.Delegates

/**
 * @data on ${Data}
 */
class AKApplication : Application() {

    //单例
    companion object {
        var instance: AKApplication by Delegates.notNull()
    }

    private lateinit var mNetStateMonitorDis: Disposable
    var mNetState: NetStateMonitor.NetState = NetStateMonitor.NetState.NETWORK_NOT_FIND

    private var mActivityCount = 0

    override fun onCreate() {
        super.onCreate()
        //听云
        NBSAppAgent.setLicenseKey(BuildConfig.TY_APP_KEY).withLocationServiceEnabled(true).start(this.applicationContext)
        instance = this
        //初始化神策sdk
        SensorsUtils.init(this)
        //屏蔽9.0弹窗
        disableAPIDialog()
        //X5内核
        QbSdk.initX5Environment(this, null)
        //初始化友盟分享
        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, "")
        PlatformConfig.setWeixin(
                if (BuildConfig.IS_LX) AppConfig.APP_ID_WX_LX else AppConfig.APP_ID_WX_IK,
                if (BuildConfig.IS_LX) AppConfig.APP_SECRET_WX_LX else AppConfig.APP_SECRET_WX_IK
        )
        PlatformConfig.setSinaWeibo(AppConfig.APP_KEY_SINA, AppConfig.APP_SECRET_SINA, AppConfig.APP_URL_SINA)
        PlatformConfig.setQQZone(AppConfig.APP_ID_QQ, AppConfig.APP_KEY_QQ)
        //初始化log工具
        if (AppConfig.DEBUG) {
            Logger.addLogAdapter(AndroidLogAdapter(
                    PrettyFormatStrategy.newBuilder()
                            .showThreadInfo(false)
                            .methodCount(2)
                            .methodCount(1)
                            .tag("LXY")
                            .build()
            ))
        }

        //初始化图片选择器
        Album.initialize(AlbumConfig.newBuilder(this)
                .setAlbumLoader(MediaLoader()).setLocale(Locale.getDefault()).build())

        //设置网络状态监听
        setNetStateListener()
        //初始化推送
        PushManager.initPush(this)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStarted(activity: Activity) {
                mActivityCount++
            }

            override fun onActivityDestroyed(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityStopped(activity: Activity) {
                mActivityCount--
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

            }

            override fun onActivityResumed(activity: Activity) {

            }
        })
    }

    fun getAppBackstage(): Boolean {
        return mActivityCount == 0
    }

    /**
     * 设置网络状态监听
     */
    private fun setNetStateListener() {
        mNetStateMonitorDis = NetStateMonitor.instance.observe()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    EventBus.getDefault().post(NetWorkStateEvent(it))
                    mNetState = it
                    Logger.w("NetState is ${it.name}")
                }
    }

    override fun onTerminate() {
        mNetStateMonitorDis.isDisposed
        super.onTerminate()
    }

    /**
     * android 9.0 调用私有api弹框的解决方案
     */
    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    private fun disableAPIDialog() {
        try {
            val clazz = Class.forName("android.app.ActivityThread")
            val currentActivityThread = clazz.getDeclaredMethod("currentActivityThread")
            currentActivityThread.isAccessible = true
            val activityThread = currentActivityThread.invoke(null)
            val mHiddenApiWarningShown = clazz.getDeclaredField("mHiddenApiWarningShown")
            mHiddenApiWarningShown.isAccessible = true
            mHiddenApiWarningShown.setBoolean(activityThread, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}