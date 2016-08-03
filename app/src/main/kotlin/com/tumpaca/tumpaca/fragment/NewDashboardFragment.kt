package com.tumpaca.tumpaca.fragment;

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.*
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.adapter.NewDashboardPagerAdapter

class NewDashboardFragment : FragmentBase() {
    companion object {
        private const val TAG = "NewDashboardFragment"
    }

    var viewPager: ViewPager? = null
    var dashboardAdapter: NewDashboardPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fr_dashboard, container, false)

        (view.findViewById(R.id.view_pager) as ViewPager).let {
            viewPager = it
        }

        val postList = getMainApplication().tumblrService!!.postList
        dashboardAdapter = NewDashboardPagerAdapter(fragmentManager, postList!!)
        viewPager?.adapter = dashboardAdapter
        dashboardAdapter?.onBind()
        return view
    }

    override fun onResume() {
        super.onResume()

        getActionBar()?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dashboardAdapter?.onUnbind()
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
        getMainApplication().tumblrService!!.logout()
        replaceFragment(AuthFragment(), false)
    }
}
