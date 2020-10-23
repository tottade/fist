package com.lixiaoyun.aike.widget

import android.content.Context
import android.text.InputFilter
import android.text.method.DigitsKeyListener
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.entity.TipsViewData
import com.lixiaoyun.aike.utils.isSame
import kotlinx.android.synthetic.main.layout_tip_text.view.*

/**
 * @data on 2020/3/5
 */
class TipsTextView : ConstraintLayout {

    private var mContext: Context

    private lateinit var mTipsViewData: TipsViewData

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleRes: Int) : super(context, attributes, defStyleRes) {
        this.mContext = context
        LayoutInflater.from(mContext).inflate(R.layout.layout_tip_text, this, true)
        initView()
    }

    private fun initView() {

    }

    fun setTipsViewData(data: TipsViewData) {
        mTipsViewData = data
        //设置hint
        setInputHint(mTipsViewData.inputHint)
        //设置最大长度
        setInputLength(mTipsViewData.maxLength)
        //设置密码模式
        if (mTipsViewData.editLimit.isSame("password")) {
            setPassWordStatus()
        }
        //设置提示语
        showTip()
    }

    fun setInputFocusChange(callback: (success: Boolean) -> Unit) {
        vEtInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (checkInputValue()) {
                    callback(true)
                    showTip()
                } else {
                    callback(false)
                    showErrorTip()
                }
            }
        }

    }

    private fun setPassWordStatus() {
        vEtInput.keyListener = DigitsKeyListener.getInstance(mContext.getString(R.string.rule_password))
        vEtInput.transformationMethod = PasswordTransformationMethod.getInstance()
        if (mTipsViewData.showImgTips) {
            vImgTip.visibility = View.VISIBLE
            vImgTip.setImageDrawable(mContext.resources.getDrawable(R.drawable.ic_hide_password, mContext.theme))
            vImgTip.setOnClickListener {
                if (vEtInput.transformationMethod == HideReturnsTransformationMethod.getInstance()) {
                    vEtInput.transformationMethod = PasswordTransformationMethod.getInstance()
                    vImgTip.setImageDrawable(mContext.resources.getDrawable(R.drawable.ic_hide_password, mContext.theme))
                } else {
                    vEtInput.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    vImgTip.setImageDrawable(mContext.resources.getDrawable(R.drawable.ic_show_password, mContext.theme))
                }
                vEtInput.setSelection(vEtInput.length())
            }
        }
    }

    fun setInputHint(msg: String) {
        vEtInput.hint = msg
    }

    fun setInputLength(max: Int) {
        vEtInput.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(max))
    }

    fun showTip() {
        vTvMsg.text = mTipsViewData.tipsMsg
        vTvMsg.setTextColor(mContext.getColor(R.color.black_999999))
    }

    fun showErrorTip() {
        vTvMsg.text = mTipsViewData.errorTips
        vTvMsg.setTextColor(mContext.getColor(R.color.red_f04134))
    }

    fun checkInputValue(): Boolean {
        return if (mTipsViewData.required) {
            !(vEtInput.length() > mTipsViewData.maxLength || vEtInput.length() < mTipsViewData.minLength)
        } else {
            true
        }
    }

    fun getInputText(): String {
        return getInputView().text.toString()
    }

    fun getInputView(): EditText {
        return vEtInput
    }

    fun getMsgView(): TextView {
        return vTvMsg
    }

    fun getImgTip(): ImageView {
        return vImgTip
    }
}