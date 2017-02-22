package com.tumpaca.tp.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.tumpaca.tp.R
import com.tumpaca.tp.fragment.AuthFragment
import com.tumpaca.tp.model.TPRuntime
import java.util.Properties

/**
 * Tumpaca クラスのメインアクティビティ。
 */
class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private const val ADMOB_APP_ID_PROP = "admob.app.id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate()")
        setContentView(R.layout.activity_main)

        TPRuntime.mainApplication.analytics.logEvent("launched", null)

        MobileAds.initialize(applicationContext, loadAdMobAppId());

        if (savedInstanceState == null) {
            val start = AuthFragment()
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.replace(R.id.fragment_container, start)
            ft.commit()
        }
    }

    private fun loadAdMobAppId(): String {
        val admobProps = Properties()
        resources.openRawResource(R.raw.admob).use { admobProps.load(it) }
        val id = String(Base64.decode(admobProps[ADMOB_APP_ID_PROP] as String, Base64.DEFAULT))
        Log.d(TAG, "Loaded AdMob app ID: id=$id")
        return id
    }
}
