package com.lixiaoyun.aike.activity.ui.registered.verification

import androidx.fragment.app.FragmentManager
import com.lixiaoyun.aike.entity.RequestSendOptCode
import com.lixiaoyun.aike.entity.RequestVerificationOptCode
import com.lixiaoyun.aike.entity.ResponseBean
import com.lixiaoyun.aike.network.NetWorkUtil
import com.lixiaoyun.aike.utils.GsonUtil
import com.lixiaoyun.aike.utils.empty
import com.lixiaoyun.aike.utils.isSame
import com.lixiaoyun.aike.utils.toast
import com.lixiaoyun.aike.widget.ContentDialog
import com.lixiaoyun.aike.widget.PicVerificationDialog
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * @data on 2020/3/9
 */
class VerificationPresenterImpl constructor(var mView: VerificationContract.View) : VerificationContract.Presenter {

    init {
        mView.setPresenter(this)
    }

    private var mSendType = "phone"
    private var mCanSendVoice = true

    override fun configurationView() {
        mView.setNextClickable(false)
        mView.setGetCodeText("获取验证码")
    }

    override fun getImgCode(phoneNumber: String, fragmentManager: FragmentManager) {
        if (mCanSendVoice) {
            //获取图片验证码
            mView.showProgressDialog(true)
            val picVerificationDialog = PicVerificationDialog()
            picVerificationDialog.show(fragmentManager, "msg")
            picVerificationDialog.checkCode(phoneNumber) { captcha ->
                //校验图片验证码和手机号并获取验证码
                if (mSendType.isSame("voice")) {
                    val getVoiceDialog = ContentDialog().apply {
                        mTitle = "获取语音验证码"
                        mContent = "确认后验证码将以电话形式通知到你，请注意接听"
                    }
                    getVoiceDialog.show(fragmentManager, "voice")
                    getVoiceDialog.setConfirmClick {
                        getVoiceDialog.dismiss()
                        requestOtpCode(phoneNumber, captcha)
                    }
                    getVoiceDialog.setCancelClick {
                        getVoiceDialog.dismiss()
                        mView.showProgressDialog(false)
                    }
                } else {
                    requestOtpCode(phoneNumber, captcha)
                }
            }
            picVerificationDialog.setCancelClick {
                picVerificationDialog.dismiss()
                mView.showProgressDialog(false)
            }
        } else {
            mView.showProgressDialog(false)
            val waitDialog = ContentDialog().apply {
                mTitle = "提示"
                mContent = "让语音验证码飞一会儿，请稍后..."
            }
            waitDialog.show(fragmentManager, "wait")
            waitDialog.setAloneClick {
                waitDialog.dismiss()
            }
        }
    }

    /**
     * 校验图片验证码和手机号并获取验证码
     * @param phoneNumber String
     * @param captcha String
     */
    private fun requestOtpCode(phoneNumber: String, captcha: String) {
        val sendOtpCodeObservable = NetWorkUtil.instance.initRetrofit()
                .sendOtpCode(RequestSendOptCode(phoneNumber, mSendType, captcha))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mView.showProgressDialog(false)
                    val responseStr = it.string()
                    if (!responseStr.empty()) {
                        val responseBean = GsonUtil.instance.gsonToBean(responseStr, ResponseBean::class.java)
                        when (responseBean.code) {
                            0 -> {
                                mView.setGetCodeClickable(false)
                                if (mSendType.isSame("voice")) {
                                    mCanSendVoice = false
                                    "语音验证码已发送，请注意接听".toast()
                                } else {
                                    "短信验证码已发送，请注意查收".toast()
                                    countDownSchedule()
                                }
                            }
                            else -> {
                                responseBean.message?.run {
                                    this.toast()
                                }
                            }
                        }
                    } else {
                        "图片验证错误，请稍后再试".toast()
                    }
                }, {
                    Logger.d("getCode error: $it, message: $it.message")
                    mView.showProgressDialog(false)
                })
        mView.setDisposable(sendOtpCodeObservable)
    }

    /**
     * 倒计时
     */
    private fun countDownSchedule() {
        mSendType = "voice"
        val countDownObservable =
                Observable.intervalRange(0, 60, 0, 1, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            mView.setGetCodeText("没收到？${60 - it}秒后重新获取")
                            if ((60 - it).toInt() == 30) {
                                mCanSendVoice = true
                            }
                        }, {
                            mSendType = "phone"
                            mCanSendVoice = true
                            mView.setGetCodeClickable(true)
                        }, {
                            mSendType = "phone"
                            mCanSendVoice = true
                            mView.setGetCodeClickable(true)
                            mView.setGetCodeText("重新获取验证码")
                        })
        mView.setDisposable(countDownObservable)
    }

    override fun verificationCode(phoneNumber: String, code: String) {
        mView.showProgressDialog(true)
        val verificationOtpCodeObservable = NetWorkUtil.instance.initRetrofit()
                .verificationOtpCode(RequestVerificationOptCode(phoneNumber, code))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mView.showProgressDialog(false)
                    when (it.code) {
                        0 -> {
                            if (it.data != null) {
                                mView.nextPage(it.data!!.user_token, phoneNumber)
                            } else {
                                "验证码验证错误，请稍后再试".toast()
                            }
                        }
                        else -> {
                            it.message?.run {
                                this.toast()
                            }
                        }
                    }
                }, {
                    Logger.d("verificationCode error: $it, message: $it.message")
                    mView.showProgressDialog(false)
                })
        mView.setDisposable(verificationOtpCodeObservable)
    }
}