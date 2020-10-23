package com.lixiaoyun.aike.widget

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.network.NetWorkConfig
import com.lixiaoyun.aike.utils.DateUtils
import com.lixiaoyun.aike.utils.clickAntiShake
import com.lixiaoyun.aike.utils.toast
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.layout_pic_verification_dialog.*

/**
 * @data on 2020/3/10
 */
class PicVerificationDialog : DialogFragment() {

    lateinit var mConfirmCallBack: (value: String) -> Unit
    lateinit var mCancelCallBack: () -> Unit

    lateinit var mPhoneNumber: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_pic_verification_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getImgCode()

        vImgCode.clickAntiShake {
            getImgCode()
        }

        vTvCancel.setOnClickListener {
            mCancelCallBack()
        }

        vTvConfirm.clickAntiShake {
            if (vEtInput.length() < 4) {
                "请输入右边的验证码".toast()
            } else {
                dialog?.run {
                    dismiss()
                    mConfirmCallBack(vEtInput.text.toString())
                }
            }
        }
    }

    private fun getImgCode() {
        context?.run {
            val url = "${NetWorkConfig.getBaseUrl()}api/v2/captcha/fetch_captcha?device=android&phone=$mPhoneNumber&time=${DateUtils.instance.getNowMills()}"
            Logger.d("img code url: $url")
            Glide.with(this).load(url).error(R.drawable.bg_img_error).into(vImgCode)
        }
    }

    fun checkCode(phoneNumber: String, callback: (value: String) -> Unit) {
        mPhoneNumber = phoneNumber
        mConfirmCallBack = callback
    }

    fun setCancelClick(callback: () -> Unit) {
        mCancelCallBack = callback
    }

    override fun onResume() {
        super.onResume()
        val window = dialog?.window
        val wmlp = window?.attributes
        wmlp?.gravity = Gravity.CENTER
        window?.attributes = wmlp
        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(dm)
        dialog?.window?.setLayout((dm.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.setCancelable(false)
    }
}