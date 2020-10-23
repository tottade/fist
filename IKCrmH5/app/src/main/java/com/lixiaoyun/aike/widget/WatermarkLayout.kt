package com.lixiaoyun.aike.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.lixiaoyun.aike.R
import kotlinx.android.synthetic.main.layout_watermark.view.*

/**
 * 水印view
 *
 * 要生成带图标的图像，TextView必须使用left而不是start
 */
class WatermarkLayout : ConstraintLayout {

    private var mContext: Context

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int)
            : super(context, attributeSet, defStyleAttr) {
        mContext = context
        LayoutInflater.from(mContext).inflate(R.layout.layout_watermark, this, true)
    }

    fun getTvName(): TextView {
        return tvName
    }

    fun getTvTime(): TextView {
        return tvTime
    }

    fun getTvCoordinate(): TextView {
        return tvCoordinate
    }

}