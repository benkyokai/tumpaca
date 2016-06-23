package com.tumpaca.tumpaca.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import com.tumpaca.tumpaca.MainApplication
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.fragment.AuthFragment
import com.tumpaca.tumpaca.fragment.DashboardFragment
import com.tumpaca.tumpaca.util.TumblerService

/**
 * Tumpaca クラスのメインアクティビティ。
 */
class MainActivity: AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val service: TumblerService = getMainApplication().tumblerService!!
            val start = if (service.isLogin) DashboardFragment() else AuthFragment()

            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.replace(R.id.fragment_container, start)
            ft.commit()
        }
    }

    private fun getMainApplication(): MainApplication {
        return application as MainApplication
    }
}
