package com.tumpaca.tumpaca.activity

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.tumblr.jumblr.JumblrClient
import com.tumblr.jumblr.types.Post
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.adapter.DashboardPagerAdapter
import com.tumpaca.tumpaca.util.AsyncTaskHelper
import com.tumpaca.tumpaca.util.Credentials
import java.util.*

/**
 * Created by amake on 6/1/16.
 */
class DashboardActivity: AppCompatActivity() {

    val tag = "DashboardActivity"
    var viewPager: ViewPager? = null

    var client: JumblrClient? = null
    var dashboardAdapter: DashboardPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val credentials = intent.getParcelableExtra<Credentials>("credentials")
        client = JumblrClient(credentials.consumerKey, credentials.consumerSecret,
                credentials.authToken, credentials.authTokenSecret)

        setContentView(R.layout.activity_dashboard)
        viewPager = findViewById(R.id.view_pager) as? ViewPager
        val fm = supportFragmentManager
        dashboardAdapter = DashboardPagerAdapter(fm)
    }

    override fun onResume() {
        super.onResume()

        AsyncTaskHelper.first<Void, Void, List<Post>?> {
            client?.userDashboard()
        }.then { result ->
            Log.v(tag, "Loaded ${result?.size} dashboard posts")
            dashboardAdapter?.addAll(ArrayList(result))
            viewPager?.adapter = dashboardAdapter
        }.go()
    }
}
