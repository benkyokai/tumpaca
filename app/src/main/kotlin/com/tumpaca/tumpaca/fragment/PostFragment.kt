package com.tumpaca.tumpaca.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import com.tumblr.jumblr.JumblrClient
import com.tumblr.jumblr.types.Blog
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.util.AsyncTaskHelper
import com.tumpaca.tumpaca.util.Credentials
import com.tumpaca.tumpaca.util.DownloadImageTask
import java.util.*

/**
 * 色情報を表示する Fragment.
 */
class PostFragment : Fragment() {
    private val credentials = Credentials()

    private val AuthSharedPreferenceName = "activity.AuthActivity"
    private val consumerKeyProp = "tumblr.consumer.key"
    private val consumerSecretProp = "tumblr.consumer.secret"
    private val authTokenProp = "auth.token"
    private val authTokenSecretProp = "auth.token.secret"

    companion object {
        fun getInstance() : PostFragment {
            return PostFragment()
        }
    }

    fun loadCredentials() {
        val authProps = Properties()

        context.resources.openRawResource(R.raw.auth).use { authProps.load(it) }
        credentials.consumerKey = String(Base64.decode(authProps.get(consumerKeyProp) as String, Base64.DEFAULT))
        credentials.consumerSecret = String(Base64.decode(authProps.get(consumerSecretProp) as String, Base64.DEFAULT))

        val prefs = context.getSharedPreferences(AuthSharedPreferenceName, Context.MODE_PRIVATE)
        prefs.getString(authTokenProp, null)?.let { credentials.authToken = String(Base64.decode(it, Base64.DEFAULT)) }
        prefs.getString(authTokenSecretProp, null)?.let { credentials.authTokenSecret = String(Base64.decode(it, Base64.DEFAULT)) }
    }

    fun getClient(): JumblrClient {
        loadCredentials()
        return JumblrClient(credentials.consumerKey, credentials.consumerSecret,
                credentials.authToken, credentials.authTokenSecret)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {

        // データを取得
        val bundle = getArguments()
        val page = bundle.getInt("page")
        val title = bundle.getString("title")
        val blogName = bundle.getString("blogName")
        val subText = bundle.getString("subText")
        val urls = bundle.getStringArrayList("urls")

        // View をつくる
        val view:View? = inflater?.inflate(R.layout.dashboard_post_card, container, false)

        val titleView = view?.findViewById(R.id.title) as? TextView
        val subTextView = view?.findViewById(R.id.sub) as WebView
        val imageView = view?.findViewById(R.id.photo) as ImageView
        val iconView = view?.findViewById(R.id.icon) as ImageView

        titleView?.setText(title)

        val mimeType = "text/html; charset=utf-8"

        val client = getClient()

        AsyncTaskHelper.first<Void, Void, Blog?> {
            client.blogInfo(blogName)
        }.then {blog ->
            AsyncTaskHelper.first<Void, Void, String?> {
                blog?.avatar()
            }.then { avatarUrl ->
                DownloadImageTask(iconView).execute(avatarUrl)
            }.go()
        }.go()

        subTextView.loadData(subText, mimeType, null)

        if (urls != null && urls.size > 0) {
            DownloadImageTask(imageView).execute(urls[0])
        }

        return view as View
    }

}