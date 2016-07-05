package com.tumpaca.tumpaca.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.*
import android.widget.ImageButton
import com.tumblr.jumblr.JumblrClient
import com.tumblr.jumblr.types.Post
import com.tumblr.jumblr.types.User
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
    var user: User? = null

    var viewPager: ViewPager? = null
    var dashboardAdapter: DashboardPagerAdapter? = null
    var likeButton: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fr_dashboard, container, false)

        client = getMainApplication().tumblrService!!.jumblerClient

        (view.findViewById(R.id.view_pager) as ViewPager).let {
            viewPager = it
            it.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    dashboardAdapter?.getPost(position)?.let { toggleLikeButton(it) }
                }
            })
        }

        dashboardAdapter = DashboardPagerAdapter(fragmentManager)

        (view.findViewById(R.id.like_button) as ImageButton).let {
            likeButton = it
            it.setOnClickListener { currentPost?.let { doLike(it) } }
        }

        val reblogButton = view.findViewById(R.id.reblog_button)
        reblogButton.setOnClickListener { currentPost?.let { doReblog(it) } }

        return view
    }

    override fun onResume() {
        super.onResume()

        getActionBar()?.title = resources.getString(R.string.dashboard)
        getActionBar()?.show()

        AsyncTaskHelper.first<Void, Void, List<Post>> {
            client!!.userDashboard()
        }.then { result ->
            Log.v(tag, "Loaded ${result.size} dashboard posts")
            dashboardAdapter?.addAll(result)
            viewPager?.adapter = dashboardAdapter
        }.go()

        AsyncTaskHelper.first<Void, Void, User> {
            client!!.user()
        }.then { result ->
            Log.v(tag, "Loaded user info for ${result.name}")
            user = result
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

    val currentPost: Post?
        get() = viewPager?.let { dashboardAdapter?.getPost(it.currentItem) }

    private fun doLike(post: Post) {
        AsyncTaskHelper.first<Unit, Unit, Unit> {
            if (post.isLiked) {
                Log.v(tag, "Unliked ${post.slug}")
                post.unlike()
            } else {
                Log.v(tag, "Liked ${post.slug}")
                post.like()
            }
        }.then {
            toggleLikeButton(post)
        }.go()
    }

    private fun toggleLikeButton(post: Post) {
        val state = android.R.attr.state_checked * if (post.isLiked) 1 else -1
        likeButton?.setImageState(intArrayOf(state), false)
    }

    private fun doReblog(post: Post) {
        user!!.blogs.first().name.let { blogName ->
            AsyncTaskHelper.first<Unit, Unit, Unit> {
                Log.v(tag, "Reblogged ${post.slug}")
                post.reblog(blogName)
            }.go()
        }
    }

    private fun logout() {
        // TODO 本当にログインしたのかダイアログで確認した方がいい
        getMainApplication().tumblrService!!.logout()
        replaceFragment(AuthFragment(), false)
    }
}
