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
import com.tumpaca.tumpaca.util.Credentials
import java.util.*

/**
 * Created by amake on 6/1/16.
 */
class AuthActivity: AppCompatActivity() {

    val credentialsFile = "credentials.properties"
    val urlCallback = "tumpaca://tumblr/auth/ok"
    val consumerKeyProp = "tumblr.consumer.key"
    val consumerSecretProp = "tumblr.consumer.secret"
    val authTokenProp = "auth.token"
    val authTokenSecretProp = "auth.token.secret"

    val tag = "AuthActivity"

    val credentials = Credentials()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadCredentials()

        setContentView(R.layout.activity_auth)

        val authButton = findViewById(R.id.button_authorize) as Button
        authButton.setOnClickListener {
            if (credentials.isComplete()) {
                goToDashboard()
            } else {
                doAuth()
            }
        }
    }

    fun loadCredentials() {
        val authProps = Properties()
        resources.openRawResource(R.raw.auth).use { authProps.load(it) }
        credentials.consumerKey = String(Base64.decode(authProps.get(consumerKeyProp) as String, Base64.DEFAULT))
        credentials.consumerSecret = String(Base64.decode(authProps.get(consumerSecretProp) as String, Base64.DEFAULT))

        val prefs = getPreferences(MODE_PRIVATE)
        prefs.getString(authTokenProp, null)?.let { credentials.authToken = String(Base64.decode(it, Base64.DEFAULT)) }
        prefs.getString(authTokenSecretProp, null)?.let { credentials.authTokenSecret = String(Base64.decode(it, Base64.DEFAULT)) }
    }

    fun doAuth() {
        Loglr.getInstance()
                .setConsumerKey(credentials.consumerKey)
                .setConsumerSecretKey(credentials.consumerSecret)
                .setLoginListener { onLogin(it) }
                .setExceptionHandler { onException(it) }
                .setUrlCallBack(urlCallback)
                .initiateInActivity(this)
    }

    fun onLogin(r: LoginResult) {
        credentials.authToken = r.oAuthToken
        credentials.authTokenSecret = r.oAuthTokenSecret
        val editor = getPreferences(MODE_PRIVATE).edit()
        editor.putString(authTokenProp, Base64.encodeToString(r.oAuthToken.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
        editor.putString(authTokenSecretProp, Base64.encodeToString(r.oAuthTokenSecret.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
        val committed = editor.commit()
        assert(committed)
        goToDashboard()
    }

    fun goToDashboard() {
        assert(credentials.authToken != null, { "authToken がまだ null のまま Dashboard を開こうとしている" })
        assert(credentials.authTokenSecret != null, { "authTokenSecret がまだ null のまま Dashboard を開こうとしている" })
        val intent = Intent(this, DashboardActivity::class.java)
        intent.putExtra("credentials", credentials)
        startActivity(intent)
    }

    fun onException(r: RuntimeException) {
        Log.e(tag, "Exception occurred on login", r)
    }
}