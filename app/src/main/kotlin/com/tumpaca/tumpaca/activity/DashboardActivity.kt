package com.tumpaca.tumpaca.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import com.tumblr.jumblr.JumblrClient
import com.tumblr.jumblr.types.Post
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.util.AsyncTaskHelper
import com.tumpaca.tumpaca.util.Credentials

/**
 * Created by amake on 6/1/16.
 */
class DashboardActivity: AppCompatActivity() {

    val tag = "DashboardActivity"

    var client: JumblrClient? = null
    var posts: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_dashboard)

        val credentials = intent.getParcelableExtra<Credentials>("credentials")
        client = JumblrClient(credentials.consumerKey, credentials.consumerSecret,
                credentials.authToken, credentials.authTokenSecret)

        posts = findViewById(R.id.list_posts) as ListView
    }

    override fun onResume() {
        super.onResume()

        AsyncTaskHelper.first<Void, Void, List<Post>?> {
            client?.userDashboard()
        }.then { result ->
            Log.v(tag, "Loaded ${result?.size} dashboard posts")
            posts?.adapter = ArrayAdapter<String>(this@DashboardActivity, R.layout.list_item, result?.map { it.slug })
        }.go()
    }
}
