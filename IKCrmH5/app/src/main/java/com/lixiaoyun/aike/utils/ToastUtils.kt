package com.lixiaoyun.aike.utils

import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.lixiaoyun.aike.AKApplication

class ToastUtils private constructor() {

    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = ToastUtils()
    }

    private var toast: Toast = Toast.makeText(AKApplication.instance, "", Toast.LENGTH_SHORT)

    fun showToast(msg: Any, duration: Int = Toast.LENGTH_SHORT, offSet: Boolean = false,
                  gravity: Int = Gravity.BOTTOM, xOff: Int = 0, yOff: Int = 0) {

        when (msg) {
            is Int -> {
                toast.setText(AKApplication.instance.resources.getString(msg))
            }
            is String -> {
                toast.setText(msg)
            }
            is View -> {
                toast.view = msg
            }
        }


        if (duration != Toast.LENGTH_SHORT) {
            toast.duration = duration
        }

        if (offSet) {
            toast.setGravity(gravity, xOff, yOff)
        }

        toast.show()
    }
}