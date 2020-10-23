package com.lixiaoyun.aike.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import com.lixiaoyun.aike.AKApplication
import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.activity.ui.registered.RegisteredActivity
import com.lixiaoyun.aike.activity.ui.retrieve.RetrieveActivity
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.entity.RequestLogin
import com.lixiaoyun.aike.entity.RequestLoginWithCorpId
import com.lixiaoyun.aike.entity.ResponseLogin
import com.lixiaoyun.aike.listener.OnSheetItemClickListener
import com.lixiaoyun.aike.network.BaseObserver
import com.lixiaoyun.aike.network.NetStateMonitor
import com.lixiaoyun.aike.network.NetWorkUtil
import com.lixiaoyun.aike.utils.*
import com.lixiaoyun.aike.widget.ActionSheetDialog
import com.orhanobut.logger.Logger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*

/**
 * @data on 2019/4/25
 */
class LoginActivity : BaseActivity() {

    companion object {
        fun intentToLoginActivity(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        StatusBarUtil.setImmersiveStatusBar(this, root, true, false, 0)

        initView()
        initListener()
    }

    private fun initView() {
        //设置输入框长度
        vEtUsername.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(11))
        vEtPassword.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(16))
        vTvDisclaimer.paint.isUnderlineText = true
        setNextClickable(false)
    }

    private fun initListener() {
        //切换环境
        if (AppConfig.DEBUG) {
            vImgLogo.clickAntiShake {
                switchNetApi()
            }
        }

        //设置用户名监听器
        vEtUsername.addTextChangedListener(object : TextWatcher {
            private var sOldLength = 0
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (!s.isNullOrEmpty()) {
                    sOldLength = s.length
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty() && sOldLength != s.length) {
                    vEtPassword.text.clear()
                }
                if (vEtUsername.length() < 11 || vEtPassword.length() < 6) {
                    setNextClickable(false)
                } else {
                    setNextClickable(true)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s!!.isEmpty()) {
                    vImgClearUsername.visibility = View.GONE
                } else {
                    vImgClearUsername.visibility = View.VISIBLE
                }
            }
        })

        vEtPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (vEtUsername.length() < 11 || vEtPassword.length() < 6) {
                    setNextClickable(false)
                } else {
                    setNextClickable(true)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        //清空
        vImgClearUsername.clickAntiShake {
            vEtUsername.setText("")
            vEtPassword.setText("")
        }

        //注册
        vTvRegistered.clickAntiShake {
            startActivity(Intent(this, RegisteredActivity::class.java))
        }

        //忘记密码
        vTvForgetPassword.clickAntiShake {
            startActivity(Intent(this, RetrieveActivity::class.java))
        }

        //协议
        vTvDisclaimer.clickAntiShake {
            startActivity(Intent(this, DisclaimerActivity::class.java))
        }

        //协议checkbox
        vTvText.clickAntiShake {
            vCbDisclaimer.isChecked = !vCbDisclaimer.isChecked
        }

        //登陆
        vTvLogin.clickAntiShake {
            if (vCbDisclaimer.isChecked) {
                val username = vEtUsername.text.toString().trim()
                val password = vEtPassword.text.toString().trim()
                when {
                    username.empty() -> getString(R.string.username_empty).toast()
                    password.empty() -> getString(R.string.pwd_empty).toast()
                    else -> login(username, password)
                }
            } else {
                "请您先阅读并同意服务协议".toast()
            }
        }
    }

    /**
     * 登录
     */
    private fun login(username: String, password: String) {
        AppConfig.setUserLogin(username)
        AppConfig.setUserPsw(password)
        if (AKApplication.instance.mNetState != NetStateMonitor.NetState.NETWORK_NOT_FIND) {
            val cId = AppConfig.getPushClientId()
            val requestLogin = RequestLogin(username, password, cId)
            NetWorkUtil.instance.initRetrofit().login(requestLogin)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : BaseObserver<ResponseLogin>(ResponseLogin::class.java) {

                        override fun onStart(d: Disposable) {
                            addDisposableList(d)
                            showProgress()
                        }

                        override fun onSuccess(responseStr: String, responseBean: ResponseLogin) {
                            when (responseBean.code) {
                                0 -> {
                                    dismissProgress()
                                    val data = responseBean.data
                                    AppConfig.setUserId(data.user_id)
                                    AppConfig.setUserToken(data.user_token)
                                    AppConfig.setCATToken(data.crm_app_token)
                                    startActivity(Intent(
                                            this@LoginActivity, MainActivity::class.java)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                                200075 -> {
                                    Logger.d("该账号属于多个企业")
                                    //多企业需要先验证账密
                                    val dataList = responseBean.org_list
                                    if (!dataList.isNullOrEmpty()) {
                                        val org = dataList[0]
                                        val requestLoginWithCorpId = RequestLoginWithCorpId(username, password, cId, org.corp_id)
                                        loginWithCropId(requestLoginWithCorpId)
                                    } else {
                                        "登陆出错，请检查企业配置信息".toast()
                                    }
                                }
                                else -> {
                                    dismissProgress()
                                    if (!responseBean.message.empty()) {
                                        Logger.d(responseBean.message)
                                        responseBean.message.toast()
                                    } else if (!responseBean.error.empty()) {
                                        Logger.d(responseBean.error)
                                        responseBean.error.toast()
                                    } else {
                                        Logger.d("登陆出错，请稍后重试")
                                        "登陆出错，请稍后重试".toast()
                                    }
                                }
                            }
                        }

                        override fun onError(code: Int, message: String) {
                            dismissProgress()
                            Logger.d("登陆出错，请稍后重试")
                            "登陆出错，请稍后重试".toast()
                        }

                        override fun onFinish() {
                        }
                    })
        } else {
            getString(R.string.network_not_fund).toast()
        }
    }

    /**
     * 多企业校验账密
     * @param loginBean RequestLoginWithCorpId
     */
    private fun loginWithCropId(loginBean: RequestLoginWithCorpId) {
        if (AKApplication.instance.mNetState != NetStateMonitor.NetState.NETWORK_NOT_FIND) {
            NetWorkUtil.instance.initRetrofit().loginWithCorpId(loginBean)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : BaseObserver<ResponseLogin>(ResponseLogin::class.java) {

                        override fun onStart(d: Disposable) {
                            addDisposableList(d)
                        }

                        override fun onSuccess(responseStr: String, responseBean: ResponseLogin) {
                            dismissProgress()
                            when (responseBean.code) {
                                0 -> {
                                    //账号密码正确，进入页面
                                    startActivity(Intent(
                                            this@LoginActivity, MainActivity::class.java)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                                else -> {
                                    if (!responseBean.message.empty()) {
                                        Logger.d(responseBean.message)
                                        responseBean.message.toast()
                                    } else if (!responseBean.error.empty()) {
                                        Logger.d(responseBean.error)
                                        responseBean.error.toast()
                                    } else {
                                        Logger.d("登陆出错，请稍后重试")
                                        "登陆出错，请稍后重试".toast()
                                    }
                                }
                            }
                        }

                        override fun onError(code: Int, message: String) {
                            dismissProgress()
                            Logger.d("登陆出错，请稍后重试")
                            "登陆出错，请稍后重试".toast()
                        }

                        override fun onFinish() {
                            dismissProgress()
                        }
                    })
        } else {
            dismissProgress()
            getString(R.string.network_not_fund).toast()
        }
    }

    private fun switchNetApi() {
        ActionSheetDialog(this)
                .builder()
                .setCancelable(true)
                .setCanceledOnTouchOutside(true)
                .addSheetItem("切换到dev环境", object : OnSheetItemClickListener {
                    override fun onClick(which: Int) {
                        AppConfig.setUrlType(0)
                        "切换dev环境成功，当前已处于dev环境".toast()
                    }
                })
                .addSheetItem("切换到test环境", object : OnSheetItemClickListener {
                    override fun onClick(which: Int) {
                        AppConfig.setUrlType(1)
                        "切换test环境成功，当前已处于test环境".toast()
                    }
                })
                .addSheetItem("切换到test环境-搜客宝", object : OnSheetItemClickListener {
                    override fun onClick(which: Int) {
                        AppConfig.setUrlType(2)
                        "切换test环境成功，当前已处于test-搜客宝环境".toast()
                    }
                })
                .addSheetItem("切换到staging环境", object : OnSheetItemClickListener {
                    override fun onClick(which: Int) {
                        AppConfig.setUrlType(3)
                        "切换staging环境成功，当前已处于staging环境".toast()
                    }
                })
                .addSheetItem("切换到production环境", object : OnSheetItemClickListener {
                    override fun onClick(which: Int) {
                        AppConfig.setUrlType(4)
                        "切换production环境成功，当前已处于production环境".toast()
                    }
                })
                .setCancelTxtColor(R.color.colorAccent.getColor(this))
                .show()
    }

    private fun setNextClickable(clickable: Boolean) {
        vTvLogin.isEnabled = clickable
        if (clickable) {
            vTvLogin.background = resources.getDrawable(R.drawable.rectangle_main_5dp, theme)
        } else {
            vTvLogin.background = resources.getDrawable(R.drawable.rectangle_main99_5dp, theme)
        }
    }
}