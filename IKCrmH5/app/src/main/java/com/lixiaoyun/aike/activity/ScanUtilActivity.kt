package com.lixiaoyun.aike.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.view.KeyEvent
import cn.bingoogolapple.qrcode.core.QRCodeView
import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.constant.KeySet
import com.lixiaoyun.aike.utils.StatusBarUtil
import com.lixiaoyun.aike.utils.clickAntiShake
import com.lixiaoyun.aike.utils.toast
import com.lixiaoyun.aike.widget.AKTitleBar
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_scan_util.*

/**
 * 扫码类
 */
class ScanUtilActivity : BaseActivity() {

    companion object {
        fun intentToScanUtilActivity(activity: Activity, requestCode: Int = KeySet.REQUEST_CODE_SCAN) {
            activity.startActivityForResult(
                    Intent(activity, ScanUtilActivity::class.java), requestCode
            )
        }
    }

    var mFlashLampStatus = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_util)
        StatusBarUtil.setImmersiveStatusBar(this, root, true, false, getColor(R.color.colorPrimary))

        vAkTitleBar.setLeftClick(object : AKTitleBar.ClickListener {
            override fun onClick() {
                finish()
            }
        })

        vImgFlashLamp.clickAntiShake(500) {
            if (mFlashLampStatus) {
                vZBarView.closeFlashlight()
            } else {
                vZBarView.openFlashlight()
            }
            mFlashLampStatus = !mFlashLampStatus
        }

        vZBarView.setDelegate(object : QRCodeView.Delegate {
            override fun onScanQRCodeSuccess(result: String?) {
                //扫描成功
                vibrate()
                Logger.e("扫码结果：$result")
                "扫描成功".toast()
                val intent = Intent()
                intent.putExtra(KeySet.I_RESULT_SCAN, result)
                setResult(RESULT_OK, intent)
                finish()
            }

            override fun onCameraAmbientBrightnessChanged(isDark: Boolean) {
                //监听摄像头环境光
                Logger.e("摄像头环境亮度：${if (isDark) "暗" else "亮"}")

            }

            override fun onScanQRCodeOpenCameraError() {
                //打开摄像头有误
                Logger.e("打开相机出错")
                "打开相机出错，请检查手机".toast()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        vZBarView.startCamera()
        vZBarView.startSpotAndShowRect()
    }

    override fun onPause() {
        if (mFlashLampStatus) {
            vZBarView.closeFlashlight()
        }
        super.onPause()
    }

    override fun onStop() {
        vZBarView.stopCamera()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        if (mFlashLampStatus) {
            vZBarView.openFlashlight()
        }
    }

    override fun onDestroy() {
        if (mFlashLampStatus) {
            vZBarView.closeFlashlight()
        }
        vZBarView.onDestroy()
        super.onDestroy()
    }

    /**
     * 震动
     */
    private fun vibrate() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(300)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish()
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }
}
