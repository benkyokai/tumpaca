package com.tumpaca.tumpaca.fragment

import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.*
import com.tumblr.jumblr.JumblrClient
import com.tumblr.jumblr.types.Post
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.adapter.DashboardPagerAdapter
import com.tumpaca.tumpaca.util.AsyncTaskHelper
import java.util.*

/**
 * Created by amake on 6/1/16.
 */
class DashboardFragment: FragmentBase() {

    val TAG = "DashboardFragment"
    var client: JumblrClient? = null

    var viewPager: ViewPager? = null
    var dashboardAdapter: DashboardPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fr_dashboard, container, false)

        client = getMainApplication().tumblerService?.jumblerClient
        viewPager = view?.findViewById(R.id.view_pager) as? ViewPager
        val fm = fragmentManager
        dashboardAdapter = DashboardPagerAdapter(fm)
        return view
    }

    override fun onResume() {
        super.onResume()

        AsyncTaskHelper.first<Void, Void, List<Post>?> {
            client!!.userDashboard()
        }.then { result ->
            Log.v(tag, "Loaded ${result?.size} dashboard posts")
            dashboardAdapter?.addAll(ArrayList(result))
            viewPager?.adapter = dashboardAdapter
        }.go()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_dashboard, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.logout) {
            logout()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun logout() {
        // TODO 本当にログインしたのかダイアログで確認した方がいい
        getMainApplication().tumblerService!!.logout()
        replaceFragment(AuthFragment(), false)
    }
}
