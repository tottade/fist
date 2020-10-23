package com.lixiaoyun.aike.activity.ui.retrieve

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
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.constant.KeySet
import com.lixiaoyun.aike.entity.RequestChangePassword
import com.lixiaoyun.aike.entity.ResponseBean
import com.lixiaoyun.aike.network.NetWorkUtil
import com.lixiaoyun.aike.utils.*
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_retrieve_confirm.*
import java.util.concurrent.TimeUnit

private const val CONFIRM_PHONE = "Confirm_Phone"
private const val CONFIRM_USER_ID = "Confirm_User_Id"
private const val CONFIRM_USER_TOKEN = "Confirm_User_Token"

class RetrieveConfirmFragment : Fragment(), TextWatcher {

    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var mProgressDialog: KProgressHUD
    private var mConfirmPhone: String? = null
    private var mConfirmUserId: Int = 0
    private var mConfirmUserToken: String? = null
    private var mDisposableList: ArrayList<Disposable> = ArrayList()

    companion object {
        fun newInstance(confirmPhone: String, confirmUserId: Int, confirmUserToken: String) =
                RetrieveConfirmFragment().apply {
                    arguments = Bundle().apply {
                        putString(CONFIRM_PHONE, confirmPhone)
                        putInt(CONFIRM_USER_ID, confirmUserId)
                        putString(CONFIRM_USER_TOKEN, confirmUserToken)
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = context!!
        mActivity = activity!!

        arguments?.let {
            mConfirmPhone = it.getString(CONFIRM_PHONE)
            mConfirmUserId = it.getInt(CONFIRM_USER_ID)
            mConfirmUserToken = it.getString(CONFIRM_USER_TOKEN)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_retrieve_confirm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        StatusBarUtil.setImmersiveStatusBar(activity, title, true, false, 0)
        initView()
        initListener()
    }

    private fun initView() {
        mProgressDialog = KProgressHUD.create(mContext).setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setDetailsLabel("请稍后...")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)

        setBtnClickable(false)
    }

    private fun initListener() {
        vImgBack.setOnClickListener {
            activity?.run {
                this.onBackPressed()
            }
        }

        vEtPsw.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (vEtPsw.length() in 1..5) {
                    "密码为6-16位的数字/大写字母/小写字母".toast()
                }
            }
        }

        vEtPswConfirm.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (vEtPswConfirm.length() > 0 && !vEtPswConfirm.text.toString().isSame(vEtPsw.text.toString())) {
                    "两次输入密码不一致".toast()
                }
            }
        }

        vEtPsw.addTextChangedListener(this)
        vEtPswConfirm.addTextChangedListener(this)

        vTvChangePassword.setOnClickListener {
            showProgressDialog(true)
            val psw = vEtPsw.text.toString()
            val cPsw = vEtPswConfirm.text.toString()
            val request = RequestChangePassword(mConfirmPhone!!, mConfirmUserToken!!, psw, cPsw)
            AppConfig.setUserToken(mConfirmUserToken!!)
            val changePasswordObservable = Observable.timer(500, TimeUnit.MILLISECONDS).flatMap {
                return@flatMap NetWorkUtil.instance.initRetrofit().changePassword(request)
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        AppConfig.clearSpValue(KeySet.KEY_USER_TOKEN)
                        showProgressDialog(false)
                        val responseStr = it.string()
                        if (!responseStr.empty()) {
                            val responseBean = GsonUtil.instance.gsonToBean(responseStr, ResponseBean::class.java)
                            when (responseBean.code) {
                                0 -> {
                                    "新密码设置成功了，赶快去登录试试吧".toast()
                                    LoginActivity.intentToLoginActivity(mContext)
                                }
                                else -> {
                                    responseBean.message?.run {
                                        this.toast()
                                    }
                                }
                            }
                        } else {
                            "修改密码错误，请稍后再试".toast()
                        }
                    }, {
                        Logger.d("ChangePassword error: $it, message: $it.message")
                        AppConfig.clearSpValue(KeySet.KEY_USER_TOKEN)
                        showProgressDialog(false)
                    })
            setDisposable(changePasswordObservable)
        }
    }

    fun showProgressDialog(show: Boolean) {
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

    fun setDisposable(disposable: Disposable) {
        mDisposableList.add(disposable)
    }

    /**
     * 检测密码状态
     * @return Int
     */
    private fun checkPswStandard(): Int {
        return if (vEtPsw.length() < 6) {
            1
        } else if (!vEtPswConfirm.text.toString().isSame(vEtPsw.text.toString())) {
            2
        } else {
            0
        }
    }

    /**
     * 提交按钮控制
     * @param clickable Boolean
     */
    private fun setBtnClickable(clickable: Boolean) {
        vTvChangePassword.isEnabled = clickable
        if (clickable) {
            vTvChangePassword.background = mContext.resources.getDrawable(R.drawable.rectangle_main_5dp, mContext.theme)
        } else {
            vTvChangePassword.background = mContext.resources.getDrawable(R.drawable.rectangle_main99_5dp, mContext.theme)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        for (disposable in mDisposableList) {
            disposable.dispose()
        }
    }

    //edit listener ----------start
    override fun afterTextChanged(s: Editable?) {
        when (checkPswStandard()) {
            0 -> {
                setBtnClickable(true)
            }
            1 -> {
                setBtnClickable(false)
            }
            2 -> {
                setBtnClickable(false)
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }
    //edit listener ----------end
}