package com.lixiaoyun.aike.widget

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.lixiaoyun.aike.R
import com.orhanobut.logger.Logger
import com.lixiaoyun.aike.utils.clickAntiShake

class AKTitleBar : RelativeLayout {

    private var mContext: Context

    private var mLeftShowAble: Boolean = false
    private var mLeftIconDrawable: Drawable? = null
    private var mLeftIconShowAble: Boolean = false
    private lateinit var mLeftText: String
    var mLeftEnable: Boolean = true
    private var mLeftTextShowAble: Boolean = false

    private lateinit var mCenterText: String
    var mCenterEnable: Boolean = true
    private var mCenterTextShowAble: Boolean = false

    private var mRightShowAble: Boolean = false
    private var mRightIconDrawable: Drawable? = null
    var mRightIconEnable: Boolean = true
    private var mRightIconShowAble: Boolean = false
    private lateinit var mRightText: String
    var mRightTextEnable: Boolean = true
    private var mRightTextShowAble: Boolean = false
    private lateinit var mMsgNum: String
    private var mMsgNumShowAble: Boolean = false

    private lateinit var mLlLeft: LinearLayout
    private lateinit var mImgLeft: ImageView
    private lateinit var mTvLeft: TextView
    private lateinit var mTvCenter: TextView
    private lateinit var mClRight: ConstraintLayout
    private lateinit var mImgRight: ImageView
    private lateinit var mTvRight: TextView
    private lateinit var mTvMsgNum: TextView

    private var mLeftClick: ClickListener? = null
    private var mCenterClick: ClickListener? = null
    private var mRightIconClick: ClickListener? = null
    private var mRightTvClick: ClickListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleRes: Int) : super(context, attributes, defStyleRes) {
        this.mContext = context
        initView(attributes, defStyleRes)
    }

    private fun initView(attributes: AttributeSet?, defStyleRes: Int) {
        val ta = mContext.obtainStyledAttributes(attributes, R.styleable.AKTitleBar, defStyleRes, 0)

        mLeftShowAble = ta.getBoolean(R.styleable.AKTitleBar_leftShowAble, false)
        mLeftIconDrawable = ta.getDrawable(R.styleable.AKTitleBar_leftIconDrawable)
                ?: mContext.getDrawable(R.drawable.ic_arrow_left)
        mLeftIconShowAble = ta.getBoolean(R.styleable.AKTitleBar_leftIconShowAble, false)
        mLeftText = ta.getString(R.styleable.AKTitleBar_leftText) ?: ""
        mLeftEnable = ta.getBoolean(R.styleable.AKTitleBar_leftEnable, true)
        mLeftTextShowAble = ta.getBoolean(R.styleable.AKTitleBar_leftTextShowAble, false)

        mCenterText = ta.getString(R.styleable.AKTitleBar_centerText) ?: ""
        mCenterEnable = ta.getBoolean(R.styleable.AKTitleBar_centerEnable, true)
        mCenterTextShowAble = ta.getBoolean(R.styleable.AKTitleBar_centerTextShowAble, false)

        mRightShowAble = ta.getBoolean(R.styleable.AKTitleBar_rightShowAble, false)
        mRightIconDrawable = ta.getDrawable(R.styleable.AKTitleBar_rightIconDrawable)
                ?: mContext.getDrawable(R.drawable.ic_menu)
        mRightIconEnable = ta.getBoolean(R.styleable.AKTitleBar_rightIconEnable, true)
        mRightIconShowAble = ta.getBoolean(R.styleable.AKTitleBar_rightIconShowAble, false)
        mRightText = ta.getString(R.styleable.AKTitleBar_rightText) ?: ""
        mRightTextEnable = ta.getBoolean(R.styleable.AKTitleBar_rightTextEnable, true)
        mRightTextShowAble = ta.getBoolean(R.styleable.AKTitleBar_rightTextShowAble, false)
        mMsgNum = ta.getString(R.styleable.AKTitleBar_msgNum) ?: ""
        mMsgNumShowAble = ta.getBoolean(R.styleable.AKTitleBar_msgNumShowAble, false)

        ta.recycle()

        LayoutInflater.from(mContext).inflate(R.layout.layout_title, this, true)
        mLlLeft = findViewById(R.id.llLeft)
        mImgLeft = findViewById(R.id.imgLeft)
        mTvLeft = findViewById(R.id.tvLeft)
        mTvCenter = findViewById(R.id.tvCenter)
        mClRight = findViewById(R.id.clRight)
        mImgRight = findViewById(R.id.imgRight)
        mTvRight = findViewById(R.id.tvRight)
        mTvMsgNum = findViewById(R.id.tvMsgNum)

        initData()
        initListener()
    }

    private fun initData() {
        mLlLeft.visibility = if (mLeftShowAble) View.VISIBLE else View.GONE
        mImgLeft.setImageDrawable(mLeftIconDrawable)
        mImgLeft.visibility = if (mLeftIconShowAble) View.VISIBLE else View.GONE
        mTvLeft.text = mLeftText
        mTvLeft.visibility = if (mLeftTextShowAble) View.VISIBLE else View.GONE

        mTvCenter.text = mCenterText
        mTvCenter.visibility = if (mCenterTextShowAble) View.VISIBLE else View.GONE

        mClRight.visibility = if (mRightShowAble) View.VISIBLE else View.GONE
        mImgRight.setImageDrawable(mRightIconDrawable)
        mImgRight.visibility = if (mRightIconShowAble) View.VISIBLE else View.GONE
        mTvRight.text = mRightText
        mTvRight.visibility = if (mRightTextShowAble) View.VISIBLE else View.GONE
        mTvMsgNum.text = mMsgNum
        mTvMsgNum.visibility = if (mMsgNumShowAble) View.VISIBLE else View.GONE
    }

    private fun initListener() {
        mLlLeft.clickAntiShake {
            if (mLeftEnable) {
                mLeftClick?.onClick()
            } else {
                Logger.i(mContext.getString(R.string.log_tip_click_disable))
            }
        }
        mTvCenter.clickAntiShake {
            if (mCenterEnable) {
                mCenterClick?.onClick()
            } else {
                Logger.i(mContext.getString(R.string.log_tip_click_disable))
            }
        }
        mImgRight.clickAntiShake {
            if (mRightIconEnable) {
                mRightIconClick?.onClick()
            } else {
                Logger.i(mContext.getString(R.string.log_tip_click_disable))
            }
        }
        mTvRight.clickAntiShake {
            if (mRightTextEnable) {
                mRightTvClick?.onClick()
            } else {
                Logger.i(mContext.getString(R.string.log_tip_click_disable))
            }
        }
    }

    fun setLeftVisibility(visibility: Boolean) {
        mLeftShowAble = visibility
        mLlLeft.visibility = if (mLeftShowAble) View.VISIBLE else View.GONE
    }

    fun setLeftImgDrawable(drawable: Drawable) {
        mLeftIconDrawable = drawable
        mImgLeft.setImageDrawable(mLeftIconDrawable)
    }

    fun setLeftImgVisibility(visibility: Boolean) {
        mLeftIconShowAble = visibility
        mImgLeft.visibility = if (mLeftIconShowAble) View.VISIBLE else View.GONE
    }

    fun setLeftText(text: String) {
        mLeftText = text
        mTvLeft.text = mLeftText
    }

    fun setLeftTvVisibility(visibility: Boolean) {
        mLeftTextShowAble = visibility
        mTvLeft.visibility = if (mLeftTextShowAble) View.VISIBLE else View.GONE
    }

    fun setCenterText(text: String) {
        mCenterText = text
        mTvCenter.text = mCenterText
    }

    fun setCenterVisibility(visibility: Boolean) {
        mCenterTextShowAble = visibility
        mTvCenter.visibility = if (mCenterTextShowAble) View.VISIBLE else View.GONE
    }

    fun setRightVisibility(visibility: Boolean) {
        mRightShowAble = visibility
        mClRight.visibility = if (mRightShowAble) View.VISIBLE else View.GONE
    }

    fun setRightImgDrawable(drawable: Drawable) {
        mRightIconDrawable = drawable
        mImgRight.setImageDrawable(mRightIconDrawable)
    }

    fun setRightImgVisibility(visibility: Boolean) {
        mRightIconShowAble = visibility
        mImgRight.visibility = if (mRightIconShowAble) View.VISIBLE else View.GONE
    }

    fun setRightText(text: String) {
        mRightText = text
        mTvRight.text = mRightText
    }

    fun setRightTvVisibility(visibility: Boolean) {
        mRightTextShowAble = visibility
        mTvRight.visibility = if (mRightTextShowAble) View.VISIBLE else View.GONE
    }

    fun setMsgNumVisibility(visibility: Boolean) {
        mMsgNumShowAble = visibility
        mTvMsgNum.visibility = if (mMsgNumShowAble) View.VISIBLE else View.GONE
    }

    fun setMsgNumText(text: String) {
        mMsgNum = text
        mTvMsgNum.text = mMsgNum
    }

    fun setMsgInfo(visibility: Boolean, text: String) {
        setMsgNumVisibility(visibility)
        setMsgNumText(text)
    }

    fun setLeftClick(leftClick: ClickListener) {
        this.mLeftClick = leftClick
    }

    fun setCenterClick(centerClick: ClickListener) {
        this.mCenterClick = centerClick
    }

    fun setRightIconClick(rightIconClick: ClickListener) {
        this.mRightIconClick = rightIconClick
    }

    fun setRightTvClick(rightTvClick: ClickListener) {
        this.mRightTvClick = rightTvClick
    }

    fun resetTitle() {
        setCenterText("")
        setLeftVisibility(false)
        setRightVisibility(false)
    }

    interface ClickListener {
        fun onClick()
    }
}