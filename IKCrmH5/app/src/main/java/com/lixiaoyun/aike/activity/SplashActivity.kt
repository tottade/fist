package com.lixiaoyun.aike.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.constant.KeySet
import com.lixiaoyun.aike.listener.PermissionListener
import com.lixiaoyun.aike.pushutils.PushManager
import com.lixiaoyun.aike.utils.PermissionUtils
import com.lixiaoyun.aike.utils.StatusBarUtil
import com.lixiaoyun.aike.utils.empty
import com.lixiaoyun.aike.utils.toast
import com.networkbench.agent.impl.NBSAppAgent
import com.orhanobut.logger.Logger
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.RequestExecutor
import com.yanzhenjie.permission.runtime.Permission
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_splash.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @data on 2019/4/25
 */
class SplashActivity : BaseActivity() {

    private var mTransition: Disposable? = null

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        StatusBarUtil.setImmersiveStatusBar(this, root, true, false, 0)

        val userInfo = AppConfig.getUserInfo()
        if (userInfo != null) {
            NBSAppAgent.setUserIdentifier("${userInfo.id}")
            NBSAppAgent.setUserCrashMessage("用户名：${userInfo.name}", "账号：${userInfo.phone}")
        }

        //欢迎语
        val splashStr = resources.getStringArray(R.array.splash)
        tvSplash.text = splashStr[Random().nextInt(17)]

        //获取华为推送token
        PushManager.getEMUIToken(this)

        //权限检测
        getPermissions(1500L)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == KeySet.REQUEST_PERMISSIONS_CODE) {
            getPermissions(500L)
        }
    }

    private fun getPermissions(delay: Long) {
        //权限检测
        val permissionsObs = getPermissionsObservable()
        //延迟一秒
        mTransition = Observable
                .timer(delay, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()) //主线程
                .flatMap {
                    permissionsObs
                }
                .subscribe {
                    when (it) {
                        PermissionUtils.PERMISSION_SUCCESS -> {
                            //检测登陆
                            val toke = AppConfig.getUserToken()
                            val uid = AppConfig.getUserId()
                            if (toke.empty() || uid <= 0) {
                                //登录页
                                LoginActivity.intentToLoginActivity(this@SplashActivity)
                            } else {
                                //首页
                                "登录成功".toast()
                                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                            }
                        }
                        PermissionUtils.PERMISSION_DENIED -> {
                            finish()
                        }
                        PermissionUtils.PERMISSION_TO_SETTING -> {
                            AndPermission.with(this).runtime().setting().start(KeySet.REQUEST_PERMISSIONS_CODE)
                        }
                    }
                }
        addDisposableList(mTransition!!)
    }

    /**
     * 获取权限被观察者
     *
     * @return Observable<String>
     */
    private fun getPermissionsObservable(): Observable<String> {
        val permissions = arrayOf(Permission.READ_EXTERNAL_STORAGE,
                Permission.WRITE_EXTERNAL_STORAGE,
                Permission.READ_PHONE_STATE,
                Permission.ACCESS_FINE_LOCATION,
                Permission.ACCESS_COARSE_LOCATION,
                Permission.READ_CALL_LOG,
                Permission.CALL_PHONE)

        return Observable.create { emitter ->
            PermissionUtils.instance.getPermissions(this,
                    object : PermissionListener {
                        override fun onRationale(context: Context, data: MutableList<String>, executor: RequestExecutor) {
                            //被拒绝时操作
                            Logger.d("onRationale")
                            runOnUiThread {
                                val permissionNames = Permission.transformText(context, data)
                                val message = context.getString(R.string.message_permission_rationale,
                                        TextUtils.join("\n", permissionNames))
                                AlertDialog.Builder(context)
                                        .setCancelable(false)
                                        .setTitle("提示")
                                        .setMessage(message)
                                        .setPositiveButton("继续") { _, _ ->
                                            Logger.d("继续")
                                            executor.execute()
                                        }
                                        .setNegativeButton("取消") { _, _ ->
                                            Logger.d("取消")
                                            executor.cancel()
                                        }
                                        .show()
                            }
                        }

                        override fun onGranted(it: List<String>) {
                            //通过权限申请
                            emitter.onNext(PermissionUtils.PERMISSION_SUCCESS)
                            emitter.onComplete()
                        }

                        override fun onDeniedAlways(context: Context, permissions: List<String>) {
                            Logger.d("onDeniedAlways")
                            val permissionNames = Permission.transformText(context, permissions)
                            val message = context.getString(R.string.message_permission_always_failed,
                                    TextUtils.join("\n", permissionNames))
                            AlertDialog.Builder(context)
                                    .setCancelable(false)
                                    .setTitle("提示")
                                    .setMessage("$message\n点击设置跳转到APP设置页面")
                                    .setPositiveButton("设置") { _, _ ->
                                        //前往设置页面
                                        emitter.onNext(PermissionUtils.PERMISSION_TO_SETTING)
                                        emitter.onComplete()
                                    }.setNegativeButton("取消") { _, _ ->
                                        //取消权限申请
                                        emitter.onNext(PermissionUtils.PERMISSION_DENIED)
                                        emitter.onComplete()
                                    }.show()
                        }

                        override fun onDenied(context: Context, permissions: List<String>) {
                            Logger.d("onDenied")
                            val permissionNames = Permission.transformText(context, permissions)
                            val message = context.getString(R.string.message_permission_always_failed,
                                    TextUtils.join("\n", permissionNames))
                            AlertDialog.Builder(context)
                                    .setCancelable(false)
                                    .setTitle("提示")
                                    .setMessage("$message\n点击设置跳转到APP设置页面")
                                    .setPositiveButton("设置") { _, _ ->
                                        //前往设置页面
                                        emitter.onNext(PermissionUtils.PERMISSION_TO_SETTING)
                                        emitter.onComplete()
                                    }.setNegativeButton("取消") { _, _ ->
                                        //取消权限申请
                                        emitter.onNext(PermissionUtils.PERMISSION_DENIED)
                                        emitter.onComplete()
                                    }.show()
                        }
                    },
                    *permissions
            )
        }
    }
}