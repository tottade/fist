package com.lixiaoyun.aike.activity.ui.registered.verification

import androidx.fragment.app.FragmentManager
import io.reactivex.disposables.Disposable


/**
 * @data on 2020/3/9
 */
class VerificationContract {
    interface Presenter {

        /**
         * 配置页面
         */
        fun configurationView()

        /**
         * 获取图片验证码
         * @param phoneNumber String
         * @param fragmentManager FragmentManager
         */
        fun getImgCode(phoneNumber: String, fragmentManager: FragmentManager)

        /**
         * 验证手机号码和验证码
         * @param phoneNumber String
         * @param code String
         */
        fun verificationCode(phoneNumber: String, code: String)
    }

    interface View {

        /**
         * 初始化
         * @param presenter Presenter
         */
        fun setPresenter(presenter: Presenter)

        /**
         * loading对话框控制
         * @param show Boolean
         */
        fun showProgressDialog(show: Boolean)

        /**
         * 设置获取验证码控件显示文案
         * @param text String
         */
        fun setGetCodeText(text: String)

        /**
         * 验证按钮控制
         * @param clickable Boolean
         */
        fun setNextClickable(clickable: Boolean)

        /**
         * 添加异步和请求
         * @param disposable Disposable
         */
        fun setDisposable(disposable: Disposable)

        /**
         * 获取验证码按钮控制
         * @param clickable Boolean
         */
        fun setGetCodeClickable(clickable: Boolean)

        /**
         * 跳转
         * @param userToken String
         * @param phoneNumber String
         */
        fun nextPage(userToken: String, phoneNumber: String)
    }
}