package com.lixiaoyun.aike.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import androidx.appcompat.widget.AppCompatImageView
import android.util.AttributeSet
import com.lixiaoyun.aike.R


/**
 * @data on 2019/4/26
 *
 * 圆角图片
 */
class RoundedImageView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        AppCompatImageView(context, attrs, defStyleAttr) {

    private val mContext = context

    private var mTl = true
    private var mTr = true
    private var mBl = true
    private var mBr = true
    private var mRadius: Float
    private var mTlRadius: Float
    private var mTrRadius: Float
    private var mBlRadius: Float
    private var mBrRadius: Float

    private lateinit var mPath: Path
    private lateinit var mRectF: RectF

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        val ta = mContext.obtainStyledAttributes(attrs, R.styleable.RoundedImageView, defStyleAttr, 0)
        mTl = ta.getBoolean(R.styleable.RoundedImageView_tl, true)
        mTr = ta.getBoolean(R.styleable.RoundedImageView_tr, true)
        mBl = ta.getBoolean(R.styleable.RoundedImageView_bl, true)
        mBr = ta.getBoolean(R.styleable.RoundedImageView_br, true)
        mRadius = ta.getDimension(R.styleable.RoundedImageView_radius, 0f)
        mTlRadius = ta.getDimension(R.styleable.RoundedImageView_tlRadius, 0f)
        mTrRadius = ta.getDimension(R.styleable.RoundedImageView_trRadius, 0f)
        mBlRadius = ta.getDimension(R.styleable.RoundedImageView_blRadius, 0f)
        mBrRadius = ta.getDimension(R.styleable.RoundedImageView_brRadius, 0f)
        ta.recycle()
        init()
    }

    private fun init() {
        mPath = Path()
        mRectF = RectF()
    }

    override fun onDraw(canvas: Canvas?) {
        val w = this.width
        val h = this.height
        val rids = getRids()
        mRectF.set(0f, 0f, w.toFloat(), h.toFloat())
        mPath.addRoundRect(mRectF, rids, Path.Direction.CW)
        canvas!!.clipPath(mPath)
        super.onDraw(canvas)
    }

    private fun getRids(): FloatArray {
        //4组[X,Y]的值依次为左上角，右上角，右下角，左下角
        val radius = floatArrayOf(mRadius, mRadius, mRadius, mRadius, mRadius, mRadius, mRadius, mRadius)
        if (!mTl) {
            radius[0] = 0f
            radius[1] = 0f
        } else {
            if (mTlRadius > 0) {
                radius[0] = mTlRadius
                radius[1] = mTrRadius
            }
        }
        if (!mTr) {
            radius[2] = 0f
            radius[3] = 0f
        } else {
            if (mTrRadius > 0) {
                radius[2] = mTrRadius
                radius[3] = mBlRadius
            }
        }
        if (!mBl) {
            radius[4] = 0f
            radius[5] = 0f
        } else {
            if (mBlRadius > 0) {
                radius[4] = mBlRadius
                radius[5] = mBlRadius
            }
        }
        if (!mBr) {
            radius[6] = 0f
            radius[7] = 0f
        } else {
            if (mBrRadius > 0) {
                radius[6] = mBrRadius
                radius[7] = mBrRadius
            }
        }
        return radius
    }
}