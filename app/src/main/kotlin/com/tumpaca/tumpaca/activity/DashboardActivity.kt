package com.tumpaca.tumpaca.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.ListView
import com.tumblr.jumblr.JumblrClient
import com.tumblr.jumblr.types.Post
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.adapter.CardListAdapter
import com.tumpaca.tumpaca.util.AsyncTaskHelper
import com.tumpaca.tumpaca.util.Credentials
import java.util.*

/**
 * Created by amake on 6/1/16.
 */
class DashboardActivity: AppCompatActivity() {

    val tag = "DashboardActivity"

    var client: JumblrClient? = null
    var posts: ListView? = null
    var adapter: CardListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_dashboard)

        val credentials = intent.getParcelableExtra<Credentials>("credentials")
        client = JumblrClient(credentials.consumerKey, credentials.consumerSecret,
                credentials.authToken, credentials.authTokenSecret)

        adapter = CardListAdapter(applicationContext)

        posts = findViewById(R.id.list_posts) as ListView
    }

    override fun onResume() {
        super.onResume()

        AsyncTaskHelper.first<Void, Void, List<Post>?> {
            client?.userDashboard()
        }.then { result ->
            Log.v(tag, "Loaded ${result?.size} dashboard posts")
            //posts?.adapter = ArrayAdapter<Post>(this@DashboardActivity, R.layout.list_item, result?.map { it })
            adapter?.addAll(result)

            val padding = (resources.displayMetrics.density * 8).toInt()
            posts?.setPadding(padding, 0, padding, 0)
            posts?.scrollBarStyle = ListView.SCROLLBARS_OUTSIDE_OVERLAY
            posts?.divider = null

            val inflater = LayoutInflater.from(applicationContext)
            val header = inflater.inflate(R.layout.dashboard_header_footer, posts, false)
            val footer = inflater.inflate(R.layout.dashboard_header_footer, posts, false)
            posts?.addHeaderView(header)
            posts?.addFooterView(footer)
            posts?.adapter = adapter
        }.go()
    }
}
