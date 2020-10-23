package com.lixiaoyun.aike.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.listener.OnSheetItemClickListener
import com.orhanobut.logger.Logger
import com.lixiaoyun.aike.utils.clickAntiShake
import kotlinx.android.synthetic.main.layout_main_popwindow.view.*

/**
 * @data on 2019/5/8
 */
class BottomPopupMore : LinearLayout {

    private var mContext: Context

    var mItemClickListener: OnSheetItemClickListener? = null

    var CANCEL = 0
    var SHARE_WX = 1
    var SHARE_PYQ = 2
    var SHARE_QQ = 3
    var SHARE_URL = 4
    var TOOLS_MSG = 5
    var TOOLS_CONFIG = 6
    var TOOLS_FONT = 7
    var TOOLS_REFRESH = 8

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int)
            : super(context, attributeSet, defStyleAttr) {
        mContext = context
        inflate(mContext, R.layout.layout_main_popwindow, this)
        initView()
    }

    private fun initView() {
        initPopClick()
    }

    private fun initPopClick() {
        tvCancel.clickAntiShake {
            Logger.e("取消")
            mItemClickListener?.onClick(CANCEL)
        }
        llShareWx.clickAntiShake {
            Logger.e("微信好友分享")
            mItemClickListener?.onClick(SHARE_WX)
        }
        llSharePyq.clickAntiShake {
            Logger.e("微信朋友圈分享")
            mItemClickListener?.onClick(SHARE_PYQ)
        }
        llShareQq.clickAntiShake {
            Logger.e("QQ分享")
            mItemClickListener?.onClick(SHARE_QQ)
        }
        llShareUrl.clickAntiShake {
            Logger.e("复制链接")
            mItemClickListener?.onClick(SHARE_URL)
        }
        llToolsMsg.clickAntiShake {
            Logger.e("跳转到消息界面")
            mItemClickListener?.onClick(TOOLS_MSG)
        }
        llToolsConfig.clickAntiShake {
            Logger.e("应用设置")
            mItemClickListener?.onClick(TOOLS_CONFIG)
        }
        llToolsFont.clickAntiShake {
            Logger.e("字体设置")
            mItemClickListener?.onClick(TOOLS_FONT)
        }
        llToolsRefresh.clickAntiShake {
            Logger.e("刷新")
            mItemClickListener?.onClick(TOOLS_REFRESH)
        }
    }

    fun getTvToolsMsgRed(): TextView? {
        return tvToolsMsgRed
    }
}