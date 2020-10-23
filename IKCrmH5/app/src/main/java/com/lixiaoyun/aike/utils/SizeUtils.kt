package com.lixiaoyun.aike.utils

import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup


/**
 * 屏幕尺寸相关
 *
 * dp2px, px2dp     : dp 与 px 转换
 * sp2px, px2sp     : sp 与 px 转换
 * applyDimension   : 各种单位转换
 * forceGetViewSize : 在 onCreate 中获取视图的尺寸
 * measureView      : 测量视图尺寸
 * getMeasuredWidth : 获取测量视图宽度
 * getMeasuredHeight: 获取测量视图高度
 * screenHeight     : 获取屏幕高度
 * screenWidth      : 获取屏幕宽度
 */
class SizeUtils private constructor() {
    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = SizeUtils()
    }

    /**
     * Value of dp to value of px.
     *
     * @param dpValue The value of dp.
     * @return value of px
     */
    fun dp2px(dpValue: Float): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * Value of px to value of dp.
     *
     * @param pxValue The value of px.
     * @return value of dp
     */
    fun px2dp(pxValue: Float): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * Value of sp to value of px.
     *
     * @param spValue The value of sp.
     * @return value of px
     */
    fun sp2px(spValue: Float): Int {
        val fontScale = Resources.getSystem().displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    /**
     * Value of px to value of sp.
     *
     * @param pxValue The value of px.
     * @return value of sp
     */
    fun px2sp(pxValue: Float): Int {
        val fontScale = Resources.getSystem().displayMetrics.scaledDensity
        return (pxValue / fontScale + 0.5f).toInt()
    }

    /**
     * Converts an unpacked complex data value holding a dimension to its final floating
     * point value. The two parameters <var>unit</var> and <var>value</var>
     * are as in [TypedValue.TYPE_DIMENSION].
     *
     * @param value The value to apply the unit to.
     * @param unit  The unit to convert from.
     * @return The complex floating point value multiplied by the appropriate
     * metrics depending on its unit.
     */
    fun applyDimension(value: Float, unit: Int): Float {
        val metrics = Resources.getSystem().displayMetrics
        when (unit) {
            TypedValue.COMPLEX_UNIT_PX -> return value
            TypedValue.COMPLEX_UNIT_DIP -> return value * metrics.density
            TypedValue.COMPLEX_UNIT_SP -> return value * metrics.scaledDensity
            TypedValue.COMPLEX_UNIT_PT -> return value * metrics.xdpi * (1.0f / 72)
            TypedValue.COMPLEX_UNIT_IN -> return value * metrics.xdpi
            TypedValue.COMPLEX_UNIT_MM -> return value * metrics.xdpi * (1.0f / 25.4f)
        }
        return 0f
    }

    /**
     * Force get the size of view.
     *
     * e.g.
     * <pre>
     * SizeUtils.instance.forceGetViewSize(view, new SizeUtils.instance.onGetSizeListener() {
     * Override
     * public void onGetSize(final View view) {
     * view.getWidth();
     * }
     * });
     * </pre>
     *
     * @param view     The view.
     * @param listener The get size listener.
     */
    fun forceGetViewSize(view: View, listener: onGetSizeListener?) {
        view.post(Runnable {
            listener?.onGetSize(view)
        })
    }

    /**
     * Return the width of view.
     *
     * @param view The view.
     * @return the width of view
     */
    fun getMeasuredWidth(view: View): Int {
        return measureView(view)[0]
    }

    /**
     * Return the height of view.
     *
     * @param view The view.
     * @return the height of view
     */
    fun getMeasuredHeight(view: View): Int {
        return measureView(view)[1]
    }

    /**
     * Measure the view.
     *
     * @param view The view.
     * @return arr[0]: view's width, arr[1]: view's height
     */
    fun measureView(view: View): IntArray {
        var lp = view.layoutParams
        if (lp == null) {
            lp = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val widthSpec = ViewGroup.getChildMeasureSpec(0, 0, lp.width)
        val lpHeight = lp.height
        val heightSpec: Int
        if (lpHeight > 0) {
            heightSpec = View.MeasureSpec.makeMeasureSpec(lpHeight, View.MeasureSpec.EXACTLY)
        } else {
            heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        }
        view.measure(widthSpec, heightSpec)
        return intArrayOf(view.measuredWidth, view.measuredHeight)
    }

    ///////////////////////////////////////////////////////////////////////////
    // interface
    ///////////////////////////////////////////////////////////////////////////

    interface onGetSizeListener {
        fun onGetSize(view: View)
    }

    /**
     * 获取屏幕高度
     * @return Int
     */
    fun screenHeight(): Int {
        val dm = Resources.getSystem().displayMetrics
        return dm.heightPixels
    }

    /**
     * 获取屏幕宽度
     * @return Int
     */
    fun screenWidth(): Int {
        val dm = Resources.getSystem().displayMetrics
        return dm.widthPixels
    }
}