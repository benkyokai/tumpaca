package com.tumpaca.tumpaca.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.tumpaca.tumpaca.MainApplication
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.util.TumblerService

/**
 * Created by amake on 6/1/16.
 */
class AuthActivity: AppCompatActivity() {
    val tag = "AuthActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val authButton = findViewById(R.id.button_authorize) as Button
        authButton.setOnClickListener {
            val service: TumblerService = getMainApplication().tumblerService!!
            if (service.isLogin()) {
                goToDashboard()
            } else {
                service.auth(this, {
                    goToDashboard()
                })
            }
        }
    }

    // Base クラスなどに移動するべき
    private fun getMainApplication(): MainApplication {
        return application as MainApplication
    }

    fun goToDashboard() {
        val service = getMainApplication().tumblerService!!
        assert(!service.isLogin(), { "ログインしていません" })
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
    }
}