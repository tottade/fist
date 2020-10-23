package com.lixiaoyun.aike.widget

import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.entity.SheetItem
import com.lixiaoyun.aike.listener.OnSheetItemClickListener
import com.lixiaoyun.aike.utils.clickAntiShake
import com.lixiaoyun.aike.utils.getColor

/**
 * @data on 2019/4/26
 *
 * 仿ios底部条目弹出框
 */
class ActionSheetDialog(context: Context) {

    private val mContext = context
    private lateinit var mPoint: Point

    private var mShowTitle = false
    private var mSheetItemList: ArrayList<SheetItem> = ArrayList()

    private lateinit var mDialog: Dialog
    private lateinit var mTvTitle: TextView
    private lateinit var mTvCancel: TextView
    private lateinit var mSvContent: ScrollView
    private lateinit var mLlContent: LinearLayout

    init {
        val wm = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val mDisplay = wm.defaultDisplay
        mPoint = Point()
        mDisplay.getSize(mPoint)
    }


    fun builder(): ActionSheetDialog {
        val view = LayoutInflater.from(mContext).inflate(R.layout.layout_actionsheet_dialog, null)

        mTvTitle = view.findViewById(R.id.vTvTitle)
        mSvContent = view.findViewById(R.id.vSvContent)
        mLlContent = view.findViewById(R.id.vLlContent)
        mTvCancel = view.findViewById(R.id.vTvCancel)

        view.minimumWidth = mPoint.x

        mDialog = Dialog(mContext, R.style.ActionSheetDialogStyle)
        mDialog.setContentView(view)

        // 定义Dialog布局和参数
        val dialogWindow = mDialog.window
        dialogWindow!!.setGravity(Gravity.START or Gravity.BOTTOM)
        val lp = dialogWindow.attributes
        lp.x = 0
        lp.y = 0
        dialogWindow.attributes = lp

        initListener()
        return this
    }

    private fun initListener() {
        mTvCancel.clickAntiShake {
            mDialog.dismiss()
        }
    }

    fun setTitle(title: String): ActionSheetDialog {
        mShowTitle = true
        mTvTitle.visibility = View.VISIBLE
        mTvTitle.text = "$title"
        return this
    }

    fun setCancelTxtColor(color: Int): ActionSheetDialog {
        mTvCancel.setTextColor(color)
        return this
    }

    fun setCancelable(flag: Boolean): ActionSheetDialog {
        mDialog.setCancelable(flag)
        return this
    }

    fun setCanceledOnTouchOutside(cancel: Boolean): ActionSheetDialog {
        mDialog.setCanceledOnTouchOutside(cancel)
        return this
    }

    /**
     * 设置条目
     *
     * @param itemName String 名称
     * @param color Int 颜色
     * @param listener OnSheetItemClickListener 监听
     * @return ActionSheetDialog
     */
    fun addSheetItem(itemName: String, listener: OnSheetItemClickListener
                     , color: Int = R.color.colorPrimary.getColor(mContext)
    ): ActionSheetDialog {
        mSheetItemList.add(SheetItem(itemName, listener, color))
        return this
    }

    /**
     * 设置条目布局
     */
    private fun setSheetItems() {
        if (mSheetItemList.isEmpty()) {
            return
        }
        val size = mSheetItemList.size

        if (size >= 7) {
            val params = mSvContent.layoutParams as LinearLayout.LayoutParams
            params.height = mPoint.y / 2
            mSvContent.layoutParams = params
        }

        for (i in 1..size) {
            val sheetItem = mSheetItemList[i - 1]
            val itemName = sheetItem.name
            val color = sheetItem.color
            val listener = sheetItem.onItemClickListener

            val textView = TextView(mContext)
            textView.text = "$itemName"
            textView.textSize = 18f
            textView.gravity = Gravity.CENTER

            // 背景图片
            if (size == 1) {
                if (mShowTitle) {
                    textView.setBackgroundResource(R.drawable.selector_actionsheet_bottom)
                } else {
                    textView.setBackgroundResource(R.drawable.selector_actionsheet_single)
                }
            } else {
                if (mShowTitle) {
                    if (i in 1 until 100) {
                        textView.setBackgroundResource(R.drawable.selector_actionsheet_middle)
                    } else {
                        textView.setBackgroundResource(R.drawable.selector_actionsheet_bottom)
                    }
                } else {
                    when {
                        i == 1 -> textView.setBackgroundResource(R.drawable.selector_actionsheet_top)
                        i < size -> textView.setBackgroundResource(R.drawable.selector_actionsheet_middle)
                        else -> textView.setBackgroundResource(R.drawable.selector_actionsheet_bottom)
                    }
                }
            }

            // 字体颜色
            textView.setTextColor(color)

            // 高度
            val scale = mContext.resources.displayMetrics.density
            val height = (45 * scale + 0.5f).toInt()
            textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)

            // 点击事件
            textView.setOnClickListener {
                listener.onClick(i)
                mDialog.dismiss()
            }

            mLlContent.addView(textView)
        }
    }

    fun show() {
        setSheetItems()
        mDialog.show()
    }
}