package com.lixiaoyun.aike.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.utils.StatusBarUtil
import com.lixiaoyun.aike.widget.AKTitleBar
import kotlinx.android.synthetic.main.activity_disclaimer.*

class DisclaimerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disclaimer)
        StatusBarUtil.setImmersiveStatusBar(this, root, true, false, getColor(R.color.colorPrimary))
        vAkTitleBar.setLeftClick(object : AKTitleBar.ClickListener {
            override fun onClick() {
                finish()
            }
        })
    }
}
