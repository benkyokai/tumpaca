package com.tumpaca.tumpaca.activity

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import com.tumblr.jumblr.JumblrClient
import com.tumblr.jumblr.types.Post
import com.tumpaca.tumpaca.R

/**
 * Created by amake on 6/1/16.
 */
class DashboardActivity: AppCompatActivity() {

    var tag = "DashboardActivity"

    var client: JumblrClient? = null
    var posts: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_dashboard)

        var consumerKey = intent.getStringExtra("consumerKey")
        var consumerSecret = intent.getStringExtra("consumerSecret")
        var authToken = intent.getStringExtra("authToken")
        var authTokenSecret = intent.getStringExtra("authTokenSecret")
        client = JumblrClient(consumerKey, consumerSecret, authToken, authTokenSecret)

        posts = findViewById(R.id.list_posts) as ListView
    }

    override fun onResume() {
        super.onResume()

        object: AsyncTask<Void, Void, List<Post>>() {
            override fun doInBackground(vararg p0: Void?): List<Post>? {
                return client?.userDashboard()
            }

            override fun onPostExecute(result: List<Post>?) {
                Log.v(tag, "Loaded ${result?.size} dashboard posts")
                posts?.adapter = ArrayAdapter<String>(this@DashboardActivity, R.layout.list_item, result?.map { it.slug })
            }
        }.execute()
    }
}
