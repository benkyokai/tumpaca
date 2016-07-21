package com.tumpaca.tumpaca.fragment

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageButton
import com.tumblr.jumblr.JumblrClient
import com.tumblr.jumblr.types.Post
import com.tumblr.jumblr.types.Post.PostType.*
import com.tumblr.jumblr.types.User
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.adapter.DashboardPagerAdapter
import com.tumpaca.tumpaca.util.AsyncTaskHelper

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
    var downloading: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fr_dashboard, container, false)

        client = getMainApplication().tumblrService!!.jumblerClient

        (view.findViewById(R.id.view_pager) as ViewPager).let {
            viewPager = it
            it.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                    Log.d(TAG, "onPageScrollStateChanged")
                    if (state == ViewPager.SCROLL_STATE_IDLE) {
                        val position = viewPager!!.currentItem
                        (viewPager?.adapter as DashboardPagerAdapter).let {
                            val adapter = it
                            val postCount = adapter.count
                            Log.d(TAG, "position: $position, postCount: $postCount")
                            if (position + 1 > postCount - 5 && !downloading) {
                                downloading = true
                                AsyncTaskHelper.first<Void, Void, List<Post>> {
                                    client!!.userDashboard(hashMapOf("offset" to postCount))
                                }.then { result ->
                                    val currentPosition = viewPager!!.currentItem
                                    Log.v(tag, "Loaded ${result.size} dashboard posts")
                                    // CHAT, ANSWER, POSTCARDは対応していないので、postから除く
                                    val filteredResult = result.filter { setOf(AUDIO, LINK, PHOTO, QUOTE, TEXT, VIDEO).contains(it.type) }
                                    adapter.addAll(filteredResult)
                                    adapter.notifyDataSetChanged()
                                    getActionBar()?.title = (currentPosition + 1).toString() + "/" + adapter.count
                                    downloading = false
                                }.go()
                            }
                            getActionBar()?.title = (position + 1).toString() + "/" + adapter.count
                        }
                    }
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

        getActionBar()?.title = "0/0"
        getActionBar()?.show()

        AsyncTaskHelper.first<Void, Void, List<Post>> {
            client!!.userDashboard()
        }.then { result ->
            Log.v(tag, "Loaded ${result.size} dashboard posts")
            // CHAT, ANSWER, POSTCARDは対応していないので、postから除く
            val filteredResult = result.filter{ setOf(AUDIO, LINK, PHOTO, QUOTE, TEXT, VIDEO).contains(it.type) }
            dashboardAdapter?.addAll(filteredResult)
            viewPager?.adapter = dashboardAdapter
            toggleLikeButton(filteredResult.first())
            getActionBar()?.title = "1/" + filteredResult.size
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
            val msg = if (post.isLiked) R.string.liked_result else R.string.unliked_result
            Snackbar.make(view!!, msg, Snackbar.LENGTH_SHORT).show()
        }.go()
    }

    private fun toggleLikeButton(post: Post) {
        val state = android.R.attr.state_checked * if (post.isLiked) 1 else -1
        likeButton?.setImageState(intArrayOf(state), false)
    }

    private fun doReblog(post: Post) {
        val input = EditText(context)
        input.setHint(R.string.comment_input_hint)
        AlertDialog.Builder(context)
                .setTitle(R.string.reblog_dialog_header)
                .setView(input)
                .setPositiveButton(android.R.string.ok) { dialog, which ->
                    val comment = input.text
                    // TODO: 複数のブログに投稿できる場合の対応
                    val blogName = user!!.blogs.first().name
                    AsyncTaskHelper.first<Unit, Unit, Unit> {
                        Log.v(tag, "Reblogged ${post.slug}")
                        post.reblog(blogName, mapOf(Pair("comment", comment)))
                    }.then {
                        Snackbar.make(view!!, R.string.reblogged_result, Snackbar.LENGTH_SHORT).show()
                    }.go()
                }
                .setNegativeButton(android.R.string.cancel) { d, w -> }
                .show()
    }

    private fun logout() {
        // TODO 本当にログインしたのかダイアログで確認した方がいい
        getMainApplication().tumblrService!!.logout()
        replaceFragment(AuthFragment(), false)
    }
}
