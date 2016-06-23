package com.tumpaca.tumpaca.util

import android.content.Context
import android.content.SharedPreferences
import android.support.v4.app.FragmentActivity
import android.util.Base64
import android.util.Log
import com.tumblr.jumblr.JumblrClient
import com.tumblr.loglr.LoginResult
import com.tumblr.loglr.Loglr
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.activity.MainActivity
import java.util.*

/**
 * 認証などの Tumbler に関するサービスを提供します。
 */
class TumblerService(val context: Context) {
    companion object {
        const val TAG = "TumblerService"
        const val URL_CALLBACK = "tumpaca://tumblr/auth/ok"
        const val AUTH_SHARED_PREFERENCE_NAME = "TumpacaPreference"
        const val CONSUMER_KEY_PROP = "tumblr.consumer.key"
        const val CONSUMER_SECRET_PROP = "tumblr.consumer.secret"
        const val AUTH_TOKEN_PROP = "auth.token"
        const val AUTH_TOKEN_SECRET_PROP = "auth.token.secret"
    }

    /**
     * Tumbler の API アクセスするための情報を保持します。
     */
    class ConsumerInfo(val key: String, val secret: String)

    /**
     * 特定のユーザーとして Tumbler の API にアクセスするための情報を保持します。
     */
    class AuthInfo(val token: String, val secret: String)

    val consumerInfo: ConsumerInfo
    var authInfo: AuthInfo? = null

    // ログインしているかどうかは authInfo でチェックする。
    // しかし、本当に有効なトークンかどうかは未検証なので注意する。
    val isLogin: Boolean
        get() = authInfo != null

    var jumblerClient: JumblrClient? = null
        get() {
            if (field == null) {
                field = JumblrClient(consumerInfo.key, consumerInfo.secret, authInfo!!.token, authInfo!!.secret)
            }
            return field
        }
        private set

    init {
        consumerInfo = loadConsumerInfo()
        authInfo = loadAuthToken()
    }

    fun auth(activity: FragmentActivity) {
        Loglr.getInstance()
                .setConsumerKey(consumerInfo.key)
                .setConsumerSecretKey(consumerInfo.secret)
                .setLoginListener {
                    onLogin(it)
                }
                .setExceptionHandler {
                    onException(it)
                }
                .setUrlCallBack(URL_CALLBACK)
                .initiateInActivity(activity)
    }

    fun logout() {
        jumblerClient = null
        authInfo = null
        removeAuthToken()
    }

    // バンドルされたファイルから ConsumerInfo を読み取ります。
    private fun loadConsumerInfo(): ConsumerInfo {
        val authProps = Properties()
        context.resources.openRawResource(R.raw.auth).use { authProps.load(it) }
        val key = String(Base64.decode(authProps[CONSUMER_KEY_PROP] as String, Base64.DEFAULT))
        val secret = String(Base64.decode(authProps[CONSUMER_SECRET_PROP] as String, Base64.DEFAULT))
        Log.d(TAG, "Loaded ConsumerInfo: key=$key, secret=$secret")
        // バンドルされたファイルは通常必ず存在するので null チェックなどはしない。
        return ConsumerInfo(key, secret)
    }

    // ローカルに保存されている AuthInfo を読み取ります。
    // AuthInfo がローカルに保存されていない場合には null を返します
    private fun loadAuthToken(): AuthInfo? {
        val prefs = getAuthSharedPreference()
        val token = prefs.getString(AUTH_TOKEN_PROP, null)?.let { String(Base64.decode(it, Base64.DEFAULT)) }
        val secret = prefs.getString(AUTH_TOKEN_SECRET_PROP, null)?.let { String(Base64.decode(it, Base64.DEFAULT)) }
        Log.d(TAG, "Loaded AuthToken: token=$token, secret=$secret")
        return if (token != null && secret != null) AuthInfo(token, secret) else null
    }

    // ローカルに AuthInfo を保存します。
    private fun saveAuthToken(authInfo: AuthInfo) {
        Log.d(TAG, "Saved AuthToken: token=" + authInfo.token + ", secret=" + authInfo.secret)
        context.editSharedPreferences(AUTH_SHARED_PREFERENCE_NAME) {
            it.putString(AUTH_TOKEN_PROP, Base64.encodeToString(authInfo.token.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            it.putString(AUTH_TOKEN_SECRET_PROP, Base64.encodeToString(authInfo.secret.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
        }
    }

    private fun removeAuthToken() {
        context.editSharedPreferences(AUTH_SHARED_PREFERENCE_NAME) {
            it.remove(AUTH_TOKEN_PROP)
            it.remove(AUTH_TOKEN_SECRET_PROP)
        }
    }

    private fun getAuthSharedPreference(): SharedPreferences {
        return context.getSharedPreferences(AUTH_SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    private fun onLogin(result: LoginResult) {
        authInfo = AuthInfo(result.oAuthToken, result.oAuthTokenSecret)
        saveAuthToken(authInfo!!)
    }

    private fun onException(e: RuntimeException) {
        Log.e(TAG, "Exception occurred on login", e)
    }
}
