package com.lixiaoyun.aike.widget

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.utils.clickAntiShake
import kotlinx.android.synthetic.main.layout_content_dialog.*

/**
 * @data on 2020/3/10
 */
class ContentDialog : DialogFragment() {

    lateinit var mConfirmCallBack: () -> Unit
    lateinit var mCancelCallBack: () -> Unit
    lateinit var mAloneCallBack: () -> Unit

    var isAloneBtn: Boolean = false

    var mTitle: String = "mTitle"
    var mContent: String = "mContent"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_content_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vTvCancel.clickAntiShake {
            mCancelCallBack()
        }

        vTvConfirm.clickAntiShake {
            mConfirmCallBack()
        }

        vTvAlone.clickAntiShake {
            mAloneCallBack()
        }

        vTvTitle.text = mTitle
        vTvContent.text = mContent

        if (isAloneBtn) {
            setAloneBtn()
        }
    }

    fun setConfirmClick(callback: () -> Unit) {
        mConfirmCallBack = callback
    }

    fun setCancelClick(callback: () -> Unit) {
        mCancelCallBack = callback
    }

    private fun setAloneBtn() {
        vTvCancel.visibility = View.GONE
        vDl2.visibility = View.GONE
        vTvConfirm.visibility = View.GONE
        vTvAlone.visibility = View.VISIBLE
    }

    fun setAloneClick(callback: () -> Unit) {
        isAloneBtn = true
        mAloneCallBack = callback
    }

    override fun onResume() {
        super.onResume()
        val window = dialog?.window
        val wmlp = window?.attributes
        wmlp?.gravity = Gravity.CENTER
        window?.attributes = wmlp
        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(dm)
        dialog?.window?.setLayout((dm.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.setCancelable(false)
    }
}