package com.lixiaoyun.aike.widget

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import com.lixiaoyun.aike.AKApplication
import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.activity.MainActivity
import com.lixiaoyun.aike.constant.KeySet
import com.lixiaoyun.aike.entity.ResponseFloatInfo
import com.lixiaoyun.aike.entity.model.SalesDynamicsModel
import com.lixiaoyun.aike.network.BaseSubscriber
import com.lixiaoyun.aike.network.NetWorkUtil
import com.lixiaoyun.aike.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus

/**
 * @data on 2019/5/6
 */
class FloatWindowManager {

    private var mContext = AKApplication.instance.applicationContext
    private var mFlatView: View? = null
    private var mFlatWindowManager: WindowManager? = null
    private var mLlRoot: LinearLayout? = null
    private var mTvUserName: TextView? = null
    private var mTvCompanyName: TextView? = null

    fun showFloatBox(phoneNumber: String) {
        requestFriedData(phoneNumber)
    }

    /**
     * 请求悬浮框信息
     */
    private fun requestFriedData(phoneNumber: String) {
        NetWorkUtil.instance.initRetrofit().getFloatInfo(phoneNumber)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseSubscriber<ResponseFloatInfo>() {

                    override fun onStart(d: Disposable) {

                    }

                    override fun onSuccess(code: Int, response: ResponseFloatInfo?) {
                        if (response != null && (!response.name.empty() || !response.company_name.empty())) {
                            PermissionUtils.instance.checkFloating(mContext) {
                                if (!it) {
                                    PermissionUtils.instance.jump2Settings(mContext)
                                } else {
                                    showFloat(response, phoneNumber)
                                }
                            }
                        } else {
                            hideFloatBox()
                        }
                    }

                    override fun onError(code: Int, message: String) {

                    }

                    override fun onFinish() {

                    }
                })
    }

    private fun showFloat(response: ResponseFloatInfo, phoneNumber: String) {
        if (isFastTrigger(2000)) {
            return
        }
        val saveData = SalesDynamicsModel()
        saveData.callerType = response.entity_name
        saveData.callerId = "${response.entity_id}"
        saveData.name = response.name
        saveData.nameType = response.entity_name
        saveData.phoneNumber = phoneNumber
        saveData.phoneType = KeySet.KEY_CALL_PHONE_TYPE_FLATWINDOW
        //SalesDynamicsUtils.instance.createDynamics(false, saveData) { itemId ->
        //    saveData.itemId = itemId
        //    saveData.createTime = System.currentTimeMillis()
        //    SalesDynamicsUtils.instance.addSalesDynamicsInfo(saveData)
        //    AppConfig.sSalesDynamicsItemId = itemId
        //}
        if (mFlatView == null) {
            mFlatWindowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val wlp = WindowManager.LayoutParams()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                wlp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                wlp.type = WindowManager.LayoutParams.TYPE_PHONE
            }
            wlp.flags = (WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            wlp.format = PixelFormat.TRANSLUCENT
            wlp.width = ViewGroup.LayoutParams.MATCH_PARENT
            wlp.height = SizeUtils.instance.dp2px(75.0F)
            wlp.gravity = Gravity.LEFT or Gravity.TOP
            wlp.x = 0
            wlp.y = SizeUtils.instance.screenHeight() / 5
            wlp.format = PixelFormat.RGBA_8888

            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            mFlatView = inflater.inflate(R.layout.layout_float_window, null)
            mLlRoot = mFlatView?.findViewById(R.id.ll_root)
            mTvUserName = mFlatView?.findViewById(R.id.tv_name)
            mTvCompanyName = mFlatView?.findViewById(R.id.tv_companyName)
            mTvUserName?.text = response.name
            mTvCompanyName?.text = response.company_name
            mFlatWindowManager?.addView(mFlatView, wlp)
            mLlRoot?.clickAntiShake {
                hideFloatBox()
                EventBus.getDefault().post(response)
                val intent = Intent(mContext, MainActivity::class.java)
                mContext.startActivity(intent)
            }
        } else {
            mTvUserName?.text = response.name
            mTvCompanyName?.text = response.company_name
        }
    }

    fun hideFloatBox() {
        if (mFlatView != null && mFlatWindowManager != null) {
            mFlatWindowManager?.removeView(mFlatView)
            mFlatView = null
            mFlatWindowManager = null
        }
    }
}