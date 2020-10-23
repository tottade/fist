package com.lixiaoyun.aike.activity.ui.registered.confirm

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kaopiz.kprogresshud.KProgressHUD
import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.activity.LoginActivity
import com.lixiaoyun.aike.entity.TipsViewData
import com.lixiaoyun.aike.utils.StatusBarUtil
import com.lixiaoyun.aike.utils.clickAntiShake
import com.lixiaoyun.aike.widget.AKTitleBar
import com.lixiaoyun.aike.widget.TipsTextView
import com.orhanobut.logger.Logger
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.vAkTitleBar
import kotlinx.android.synthetic.main.fragment_registered_confirm.*

private const val CONFIRM_PHONE = "Confirm_Phone"
private const val CONFIRM_USER_TOKEN = "Confirm_User_Token"

class RegisteredConfirmFragment : Fragment(), ConfirmContract.View, TextWatcher {
    private var mConfirmPhone: String? = null
    private var mConfirmUserToken: String? = null
    private var mDisposableList: ArrayList<Disposable> = ArrayList()
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private var mPresenter: ConfirmContract.Presenter = ConfirmPresenterImpl(this)
    private lateinit var mProgressDialog: KProgressHUD

    private var mTvOrgNameChecked = false
    private var mTvNameChecked = false
    private var mTvPswChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = context!!
        mActivity = activity!!

        arguments?.let {
            mConfirmPhone = it.getString(CONFIRM_PHONE)
            mConfirmUserToken = it.getString(CONFIRM_USER_TOKEN)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_registered_confirm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        StatusBarUtil.setImmersiveStatusBar(mActivity, vAkTitleBar, true, false, mContext.getColor(R.color.colorPrimary))
        mPresenter.configurationData()
        mPresenter.configurationView()

        initView()
        initListener()
    }

    private fun initView() {
        mProgressDialog = KProgressHUD.create(mContext).setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setDetailsLabel("请稍后...")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
    }

    private fun initListener() {
        vAkTitleBar.setLeftClick(object : AKTitleBar.ClickListener {
            override fun onClick() {
                activity?.run {
                    this.onBackPressed()
                }
            }
        })

        vTvOrgName.setInputFocusChange {
            mTvOrgNameChecked = it
            if (mTvOrgNameChecked && mTvNameChecked && mTvPswChecked) {
                setConfirmClickable(true)
            } else {
                setConfirmClickable(false)
            }
        }

        vTvName.setInputFocusChange {
            mTvNameChecked = it
            if (mTvOrgNameChecked && mTvNameChecked && mTvPswChecked) {
                setConfirmClickable(true)
            } else {
                setConfirmClickable(false)
            }
        }

        vTvPsw.setInputFocusChange {
            mTvPswChecked = it
            if (mTvOrgNameChecked && mTvNameChecked && mTvPswChecked) {
                setConfirmClickable(true)
            } else {
                setConfirmClickable(false)
            }
        }
        vTvOrgName.getInputView().addTextChangedListener(this)
        vTvName.getInputView().addTextChangedListener(this)
        vTvPsw.getInputView().addTextChangedListener(this)

        vTvConfirm.clickAntiShake {
            mConfirmUserToken?.run {
                mPresenter.checkUpData(this)
            }
        }
    }

    companion object {
        /**
         * @param confirmPhone
         * @param confirmUserToken
         * @return A new instance of fragment RegisteredConfirmFragment.
         */
        @JvmStatic
        fun newInstance(confirmPhone: String, confirmUserToken: String) =
                RegisteredConfirmFragment().apply {
                    arguments = Bundle().apply {
                        putString(CONFIRM_PHONE, confirmPhone)
                        putString(CONFIRM_USER_TOKEN, confirmUserToken)
                    }
                }
    }

    override fun setDisposable(disposable: Disposable) {
        mDisposableList.add(disposable)
    }

    override fun setPresenter(presenter: ConfirmContract.Presenter) {
        mPresenter = presenter
    }

    override fun loadPage(viewIndex: Int, tipsViewData: TipsViewData) {
        Logger.d("$viewIndex = $tipsViewData")
    }

    override fun showProgressDialog(show: Boolean) {
        if (show) {
            if (!mProgressDialog.isShowing) {
                mProgressDialog.show()
            }
        } else {
            if (mProgressDialog.isShowing) {
                mProgressDialog.dismiss()
            }
        }
    }

    override fun getTvOrgName(): TipsTextView {
        return vTvOrgName
    }

    override fun getTvName(): TipsTextView {
        return vTvName
    }

    override fun getTvPosition(): TipsTextView {
        return vTvPosition
    }

    override fun getTvMail(): TipsTextView {
        return vTvMail
    }

    override fun getTvPsw(): TipsTextView {
        return vTvPsw
    }

    override fun setConfirmClickable(clickable: Boolean) {
        vTvConfirm.isEnabled = clickable
        if (clickable) {
            vTvConfirm.background = mContext.resources.getDrawable(R.drawable.rectangle_main_5dp, mContext.theme)
        } else {
            vTvConfirm.background = mContext.resources.getDrawable(R.drawable.rectangle_main99_5dp, mContext.theme)
        }
    }

    override fun signUpSuccess() {
        LoginActivity.intentToLoginActivity(mContext)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        for (disposable in mDisposableList) {
            disposable.dispose()
        }
    }

    //edit listener ----------start
    override fun afterTextChanged(s: Editable?) {
        if (vTvOrgName.checkInputValue() && vTvName.checkInputValue() && vTvPsw.checkInputValue()) {
            setConfirmClickable(true)
        } else {
            setConfirmClickable(false)
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }
    //edit listener ----------end
}
