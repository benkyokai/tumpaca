package com.tumpaca.tumpaca.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.tumblr.loglr.LoginResult
import com.tumblr.loglr.Loglr
import com.tumpaca.tumpaca.R

/**
 * Created by amake on 6/1/16.
 */
class AuthActivity: AppCompatActivity() {

    var consumerKey = ""
    var consumerSecret = ""
    var urlCallback = "tumpaca://tumblr/auth/ok"

    var tag = "AuthActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_auth)

        var authButton = findViewById(R.id.button_authorize) as Button
        authButton.setOnClickListener { doAuth() }
    }

    fun doAuth() {
        Loglr.getInstance()
                .setConsumerKey(consumerKey)
                .setConsumerSecretKey(consumerSecret)
                .setLoginListener { onLogin(it) }
                .setExceptionHandler { onException(it) }
                .setUrlCallBack(urlCallback)
                .initiateInActivity(this)
    }

    fun onLogin(r: LoginResult) {
        Log.v(tag, "Got login result: ${r.oAuthToken}, ${r.oAuthTokenSecret}")
    }

    fun onException(r: RuntimeException) {
        Log.e(tag, "Exception occurred on login", r)
    }
}