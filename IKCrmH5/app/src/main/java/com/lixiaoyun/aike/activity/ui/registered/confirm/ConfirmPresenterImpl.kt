package com.lixiaoyun.aike.activity.ui.registered.confirm

import android.util.SparseArray
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.constant.KeySet
import com.lixiaoyun.aike.entity.RequestSignUpFillInfo
import com.lixiaoyun.aike.entity.ResponseBean
import com.lixiaoyun.aike.entity.TipsViewData
import com.lixiaoyun.aike.network.NetWorkUtil
import com.lixiaoyun.aike.utils.GsonUtil
import com.lixiaoyun.aike.utils.empty
import com.lixiaoyun.aike.utils.toast
import com.lixiaoyun.aike.widget.TipsTextView
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * @data on 2020/3/6
 */
class ConfirmPresenterImpl constructor(var mView: ConfirmContract.View) : ConfirmContract.Presenter {

    init {
        mView.setPresenter(this)
    }

    private lateinit var mTipsViewDataList: SparseArray<TipsViewData>
    private lateinit var mTipsViewList: SparseArray<TipsTextView>

    override fun configurationData() {
        mTipsViewDataList = SparseArray()
        mTipsViewDataList.put(0, TipsViewData(
                inputHint = "请输入公司名称", tipsMsg = "", isTipsMsgShow = false,
                errorTips = "请输入大于0字符小于20字符的内容",
                showImgTips = false,
                maxLength = 20, minLength = 1,
                required = true, editLimit = "text")
        )
        mTipsViewDataList.put(1, TipsViewData(
                inputHint = "请输入你的真实姓名", tipsMsg = "", isTipsMsgShow = false,
                errorTips = "请输入大于0字符小于20字符的内容",
                showImgTips = false,
                maxLength = 20, minLength = 1,
                required = true, editLimit = "text")
        )
        mTipsViewDataList.put(2, TipsViewData(
                inputHint = "请输入你的职务(选填)", tipsMsg = "", isTipsMsgShow = false,
                errorTips = "请输入大于0字符小于20字符的内容",
                showImgTips = false,
                maxLength = 20, minLength = 1,
                required = false, editLimit = "text")
        )
        mTipsViewDataList.put(3, TipsViewData(
                inputHint = "请输入你的邮箱(选填)", tipsMsg = "", isTipsMsgShow = false,
                errorTips = "请输入大于0字符小于20字符的内容",
                showImgTips = false,
                maxLength = 20, minLength = 1,
                required = false, editLimit = "text")
        )
        mTipsViewDataList.put(4, TipsViewData(
                inputHint = "请输入登录密码", tipsMsg = "*密码请输入6-16位数字，字母或常用符号", isTipsMsgShow = true,
                errorTips = "密码请输入6-16位数字，字母或常用符号",
                showImgTips = true,
                maxLength = 16, minLength = 6,
                required = true, editLimit = "password")
        )
    }

    override fun configurationView() {
        mTipsViewList = SparseArray()
        mTipsViewList.put(0, mView.getTvOrgName())
        mTipsViewList.put(1, mView.getTvName())
        mTipsViewList.put(2, mView.getTvPosition())
        mTipsViewList.put(3, mView.getTvMail())
        mTipsViewList.put(4, mView.getTvPsw())
        for (i in 0..4) {
            mTipsViewList[i].setTipsViewData(mTipsViewDataList[i])
        }
        mView.setConfirmClickable(false)
    }

    override fun checkInput(): Boolean {
        var check = true
        for (i in 0..4) {
            if (mTipsViewList[i].checkInputValue()) {
                mTipsViewList[i].showTip()
            } else {
                check = false
                mTipsViewList[i].showErrorTip()
            }
        }
        return check
    }

    override fun checkUpData(token: String) {
        if (checkInput()) {
            //上传数据
            mView.showProgressDialog(true)
            val requestSignUpFillInfo = RequestSignUpFillInfo(
                    token, mTipsViewList[0].getInputText(),
                    mTipsViewList[1].getInputText(), mTipsViewList[2].getInputText(),
                    mTipsViewList[3].getInputText(), mTipsViewList[4].getInputText())
            AppConfig.setUserToken(token)
            val signUpFillInfoDisposable = Observable.timer(500, TimeUnit.MILLISECONDS).flatMap {
                return@flatMap NetWorkUtil.instance.initRetrofit().signUpFillInfo(requestSignUpFillInfo)
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        AppConfig.clearSpValue(KeySet.KEY_USER_TOKEN)
                        mView.showProgressDialog(false)
                        val responseStr = it.string()
                        if (!responseStr.empty()) {
                            val responseBean = GsonUtil.instance.gsonToBean(responseStr, ResponseBean::class.java)
                            when (responseBean.code) {
                                0 -> {
                                    "注册成功".toast()
                                    mView.signUpSuccess()
                                }
                                else -> {
                                    responseBean.message?.run {
                                        this.toast()
                                    }
                                }
                            }
                        } else {
                            "验证错误，请稍后再试".toast()
                        }
                    }, {
                        Logger.d("checkUpData error: $it, message: $it.message")
                        AppConfig.clearSpValue(KeySet.KEY_USER_TOKEN)
                        mView.showProgressDialog(false)
                    })
            mView.setDisposable(signUpFillInfoDisposable)
        }
    }
}