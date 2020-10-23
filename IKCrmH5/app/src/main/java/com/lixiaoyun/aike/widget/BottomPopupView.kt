package com.lixiaoyun.aike.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.FrameLayout

import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.utils.HardwareUtils
import com.lixiaoyun.aike.utils.getColor
import kotlinx.android.synthetic.main.layout_popup_bottom.view.*

class BottomPopupView : FrameLayout {
    private var mContext: Context

    private var mContentView: View? = null
    private var mContainerView: View

    private var mIsTouch = true
    private var mIsShowing = false

    private lateinit var mAnimBottomOut: Animation
    private lateinit var mAnimBottomIn: Animation

    private var mWindowShowListener: WindowShowListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mContext = context
        //初始化容器
        mContainerView = inflate(mContext, R.layout.layout_popup_bottom, null)
    }

    /**
     * 添加内容View
     * @param layoutId Int
     */
    fun setContentView(layoutId: Int) {
        setContentView(LayoutInflater.from(mContext).inflate(layoutId, null))
    }

    /**
     * 添加内容View
     * @param contentView View
     */
    fun setContentView(contentView: View) {
        mContentView = contentView
        init()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        //添加容器View
        addView(mContainerView)
        //添加内容View
        vFayContentLayout.addView(mContentView)
        mContentView?.visibility = View.GONE

        //底部出去动画
        mAnimBottomOut = AnimationUtils.loadAnimation(mContext, R.anim.bottom_out)
        //动画不恢复原
        mAnimBottomOut.fillAfter = true
        mAnimBottomOut.setAnimationListener(AnimListener())
        //底部进入动画
        mAnimBottomIn = AnimationUtils.loadAnimation(mContext, R.anim.bottom_in)
        //动画不恢复原
        mAnimBottomIn.fillAfter = true
        mAnimBottomIn.setAnimationListener(AnimListener())

        vAlPhaFrameLayout.setOnTouchListener { _, event ->
            if (!HardwareUtils.instance.comprisePoint(vFayContentLayout, event.rawX.toInt(), event.rawY.toInt()) && mIsTouch) {
                mIsTouch = false
                hide()
            }
            true
        }
    }

    /**
     * 显示弹出框
     */
    fun show() {
        if (mContentView?.visibility == View.GONE && !mIsShowing) {
            mIsShowing = true
            mIsTouch = true
            vFayContentLayout.startAnimation(mAnimBottomIn)
            mContentView?.visibility = View.VISIBLE
        }
    }

    /**
     * 隐藏弹出框
     */
    fun hide() {
        if (!mIsShowing) {
            return
        }
        mIsShowing = false
        vFayContentLayout.startAnimation(mAnimBottomOut)
    }

    /**
     * 是否已经弹出
     * @return Boolean
     */
    fun isShowing(): Boolean {
        return mContentView?.visibility == View.VISIBLE
    }

    /**
     * 设置遮罩颜色
     * @param isShowBg Boolean
     */
    fun setShowBackground(isShowBg: Boolean, color: Int = R.color.transparent.getColor(mContext)) {
        if (!isShowBg) {
            vAlPhaFrameLayout.setBackgroundColor(color)
        }
    }

    interface WindowShowListener {
        /**
         * 显示状态
         */
        fun showState(isShow: Boolean)
    }

    private inner class AnimListener : AnimationListener {
        override fun onAnimationStart(animation: Animation) {
            if (mIsShowing) {
                vFayContentLayout.visibility = View.VISIBLE
                vAlPhaFrameLayout.visibility = View.VISIBLE
            }
        }

        override fun onAnimationEnd(animation: Animation) {
            if (mIsShowing) {
                mWindowShowListener?.showState(true)
            } else {
                mContentView?.visibility = View.GONE
                vFayContentLayout.visibility = View.GONE
                vAlPhaFrameLayout.visibility = View.GONE
                mWindowShowListener?.showState(false)
            }
        }

        override fun onAnimationRepeat(animation: Animation) {

        }
    }
}
