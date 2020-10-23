package com.lixiaoyun.aike.activity.ui.retrieve

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.activity.ui.retrieve.verification.RetrieveVerificationFragment

class RetrieveActivity : AppCompatActivity() {

    var fragmentManager: FragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.retrieve_activity)
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, RetrieveVerificationFragment.newInstance())
                    //.replace(R.id.container, RetrieveConfirmFragment.newInstance("18721391063", 1000002, "d3f74079f8e32e3bd745c4bb3b03f5a7"))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commitNow()
        }
    }
}
