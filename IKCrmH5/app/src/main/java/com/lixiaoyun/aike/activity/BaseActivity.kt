package com.lixiaoyun.aike.activity

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.KeyEvent
import com.kaopiz.kprogresshud.KProgressHUD
import com.lixiaoyun.aike.utils.DateUtils
import com.lixiaoyun.aike.utils.toast
import com.orhanobut.logger.Logger
import io.reactivex.disposables.Disposable
import kotlin.system.exitProcess

abstract class BaseActivity : AppCompatActivity() {

    private var mDisposableList: ArrayList<Disposable> = ArrayList()

    private var mProgressDialog: KProgressHUD? = null

    private var mExitTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mProgressDialog = KProgressHUD.create(this).setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setDetailsLabel("请稍后...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
        mProgressDialog?.setCancellable {
            Logger.e("ProgressDialog Cancel")
        }
    }

    /**
     * loading是否显示
     */
    fun progressIsShowing(): Boolean {
        return mProgressDialog?.isShowing ?: false
    }

    /**
     * 设置取消监听
     */
    fun setProgressCancelListener(listener: DialogInterface.OnCancelListener) {
        mProgressDialog?.setCancellable(listener)
    }

    /**
     * 请求接口开启loading
     */
    fun showProgress() {
        if (!progressIsShowing()) {
            mProgressDialog?.show()
        }
    }

    /**
     * 请求接口返回结果时候关闭loading
     */
    fun dismissProgress() {
        if (progressIsShowing()) {
            mProgressDialog?.dismiss()
        }
    }

    fun addDisposableList(disposable: Disposable) {
        mDisposableList.add(disposable)
    }

    fun cancelDisposableList() {
        for (disposable in mDisposableList) {
            disposable.dispose()
        }
    }

    override fun onDestroy() {
        cancelDisposableList()
        dismissProgress()
        mProgressDialog = null
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (DateUtils.instance.getNowMills() - mExitTime > 2000) {
                mExitTime = DateUtils.instance.getNowMills()
                "再按一次就退出了".toast()
            } else {
                exitProcess(0)
            }
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }
}