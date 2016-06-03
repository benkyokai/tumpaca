package com.tumpaca.tumpaca.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.widget.Button
import com.tumblr.loglr.LoginResult
import com.tumblr.loglr.Loglr
import com.tumpaca.tumpaca.R
import java.util.*

/**
 * Created by amake on 6/1/16.
 */
class AuthActivity: AppCompatActivity() {

    var consumerKey: String? = null
    var consumerSecret: String? = null
    var urlCallback = "tumpaca://tumblr/auth/ok"

    var tag = "AuthActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_auth)

        var authButton = findViewById(R.id.button_authorize) as Button
        authButton.setOnClickListener { doAuth() }

        var authProps = Properties()
        resources.openRawResource(R.raw.auth).use { authProps.load(it) }
        consumerKey = String(Base64.decode(authProps.get("tumblr.consumer.key") as String, Base64.DEFAULT))
        consumerSecret = String(Base64.decode(authProps.get("tumblr.consumer.secret") as String, Base64.DEFAULT))
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
        var intent = Intent(this, DashboardActivity::class.java)
        intent.putExtra("consumerKey", consumerKey)
        intent.putExtra("consumerSecret", consumerSecret)
        intent.putExtra("authToken", r.oAuthToken)
        intent.putExtra("authTokenSecret", r.oAuthTokenSecret)
        startActivity(intent)
    }

    fun onException(r: RuntimeException) {
        Log.e(tag, "Exception occurred on login", r)
    }
}