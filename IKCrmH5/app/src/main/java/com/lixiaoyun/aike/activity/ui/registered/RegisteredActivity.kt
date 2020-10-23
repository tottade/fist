package com.lixiaoyun.aike.activity.ui.registered

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.activity.ui.registered.verification.RegisteredVerificationFragment

class RegisteredActivity : AppCompatActivity() {

    var fragmentManager: FragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registered_activity)
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, RegisteredVerificationFragment.newInstance())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commitNow()
        }
    }
}
