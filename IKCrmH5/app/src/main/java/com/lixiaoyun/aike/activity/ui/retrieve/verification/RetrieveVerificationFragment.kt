package com.lixiaoyun.aike.activity.ui.retrieve.verification

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.kaopiz.kprogresshud.KProgressHUD
import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.activity.ui.retrieve.RetrieveConfirmFragment
import com.lixiaoyun.aike.utils.StatusBarUtil
import com.lixiaoyun.aike.utils.clickAntiShake
import com.lixiaoyun.aike.utils.isPhoneNumOK
import com.lixiaoyun.aike.utils.toast
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_retrieve_verification.*

class RetrieveVerificationFragment : Fragment(), VerificationContract.View, TextWatcher {

    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private var mDisposableList: ArrayList<Disposable> = ArrayList()
    private var mPresenter: VerificationContract.Presenter = VerificationPresenterImpl(this)
    private lateinit var mProgressDialog: KProgressHUD

    companion object {
        fun newInstance() = RetrieveVerificationFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = context!!
        mActivity = activity!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_retrieve_verification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        StatusBarUtil.setImmersiveStatusBar(activity, title, true, false, 0)
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
        vTvNext.setOnClickListener {
            //校验手机号和验证码
            if (vEtUsername.length() == 11 && vEtCode.length() == 6) {
                mPresenter.verificationCode(vEtUsername.text.toString(), vEtCode.text.toString())
            }
        }

        vImgBack.setOnClickListener {
            activity?.run {
                this.onBackPressed()
            }
        }

        vEtUsername.run {
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    when {
                        length() < 11 -> {
                            setNextClickable(false)
                            if (length() > 0) {
                                "请输入11位有效手机号码".toast()
                            }
                        }
                        vEtCode.length() < 6 -> {
                            setNextClickable(false)
                        }
                        else -> {
                            setNextClickable(true)
                        }
                    }
                }
            }
            addTextChangedListener(this@RetrieveVerificationFragment)
        }

        vEtCode.run {
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    when {
                        length() < 6 -> {
                            setNextClickable(false)
                            if (length() > 0) {
                                "请输入6位有效验证码".toast()
                            }
                        }
                        vEtUsername.length() < 11 -> {
                            setNextClickable(false)
                        }
                        else -> {
                            setNextClickable(true)
                        }
                    }
                }
            }
            addTextChangedListener(this@RetrieveVerificationFragment)
        }

        vTvGetCode.clickAntiShake {
            if (vEtUsername.text.toString().isPhoneNumOK("请输入11位有效手机号码")) {
                //获取验证码
                mPresenter.getImgCode(vEtUsername.text.toString(), childFragmentManager)
            }
        }
    }

    override fun nextPage(userToken: String, userId: Int, phoneNumber: String) {
        activity?.run {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, RetrieveConfirmFragment.newInstance(phoneNumber, userId, userToken))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commitNow()
        }
    }

    override fun setPresenter(presenter: VerificationContract.Presenter) {
        mPresenter = presenter
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

    override fun setGetCodeText(text: String) {
        vTvGetCode.text = text
    }

    override fun setNextClickable(clickable: Boolean) {
        vTvNext.isEnabled = clickable
        if (clickable) {
            vTvNext.background = mContext.resources.getDrawable(R.drawable.rectangle_main_5dp, mContext.theme)
        } else {
            vTvNext.background = mContext.resources.getDrawable(R.drawable.rectangle_main99_5dp, mContext.theme)
        }
    }

    override fun setGetCodeClickable(clickable: Boolean) {
        //vTvGetCode.isEnabled = clickable
        if (clickable) {
            vTvGetCode.setTextColor(mContext.resources.getColor(R.color.colorPrimary, mContext.theme))
        } else {
            vTvGetCode.setTextColor(mContext.resources.getColor(R.color.colorPrimary99, mContext.theme))
        }
    }

    override fun setDisposable(disposable: Disposable) {
        mDisposableList.add(disposable)
    }

    //edit listener ----------start
    override fun afterTextChanged(s: Editable?) {

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (vEtUsername.length() < 11 || vEtCode.length() < 6) {
            setNextClickable(false)
        } else {
            setNextClickable(true)
        }
    }
    //edit listener ----------end

    override fun onDestroyView() {
        super.onDestroyView()
        vEtUsername.run {
            onFocusChangeListener = null
        }
        vEtCode.run {
            onFocusChangeListener = null
        }
        for (disposable in mDisposableList) {
            disposable.dispose()
        }
    }
}