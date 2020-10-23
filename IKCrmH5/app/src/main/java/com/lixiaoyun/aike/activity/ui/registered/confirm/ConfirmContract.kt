package com.lixiaoyun.aike.activity.ui.registered.confirm

import com.lixiaoyun.aike.entity.TipsViewData
import com.lixiaoyun.aike.widget.TipsTextView
import io.reactivex.disposables.Disposable

/**
 * @data on 2020/3/6
 */
class ConfirmContract {

    interface Presenter {
        /**
         * 配置数据
         */
        fun configurationData()

        /**
         * 配置页面
         */
        fun configurationView()

        /**
         * 校验输入
         * @return Boolean
         */
        fun checkInput(): Boolean

        /**
         * 校验提交
         * @param token String
         */
        fun checkUpData(token: String)
    }

    interface View {

        /**
         * 添加异步和请求
         * @param disposable Disposable
         */
        fun setDisposable(disposable: Disposable)

        /**
         * 初始化
         * @param presenter Presenter
         */
        fun setPresenter(presenter: Presenter)

        /**
         * 加载界面
         * @param viewIndex Int
         * @param tipsViewData TipsViewData
         */
        fun loadPage(viewIndex: Int, tipsViewData: TipsViewData)

        /**
         * loading对话框控制
         * @param show Boolean
         */
        fun showProgressDialog(show: Boolean)

        /**
         * 提交按钮控制
         * @param clickable Boolean
         */
        fun setConfirmClickable(clickable: Boolean)

        fun getTvOrgName(): TipsTextView

        fun getTvName(): TipsTextView

        fun getTvPosition(): TipsTextView

        fun getTvMail(): TipsTextView

        fun getTvPsw(): TipsTextView

        /**
         * 注册成功
         */
        fun signUpSuccess()
    }
}