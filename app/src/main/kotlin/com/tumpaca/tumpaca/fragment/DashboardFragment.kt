package com.tumpaca.tumpaca.fragment;

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.view.*
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.tumblr.jumblr.types.Post
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.model.PostList
import com.tumpaca.tumpaca.model.TPRuntime
import com.tumpaca.tumpaca.util.likeAsync
import com.tumpaca.tumpaca.util.reblogAsync

class DashboardFragment : FragmentBase() {
    companion object {
        const val TAG = "DashboardFragment"
        private const val OFFSCREEN_PAGE_LIMIT = 4
    }

    var postList: PostList? = null
    var likeButton: ImageButton? = null
    var isFabOpen = false
    var viewPager: ViewPager? = null
    var dashboardAdapter: DashboardPageAdapter? = null
    var changedListener: PostList.ChangedListener? = null

    var currentPost: Post? = null
        get() = postList?.get(viewPager!!.currentItem)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fr_dashboard, container, false)

        val page = view.findViewById(R.id.page) as TextView
        val postCount = view.findViewById(R.id.post_count) as TextView

        (view.findViewById(R.id.view_pager) as ViewPager).let {
            viewPager = it
            it.offscreenPageLimit = OFFSCREEN_PAGE_LIMIT
            it.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    page.text = "${position + 1}"
                    postCount.text = "${postList?.size}"
                }

                override fun onPageSelected(position: Int) {
                    toggleLikeButton(postList?.get(position)!!)
                }
            })
        }

        // PostList と ViewPage のバインド
        TPRuntime.tumblrService.resetPosts()
        postList = TPRuntime.tumblrService.postList

        changedListener = object : PostList.ChangedListener {
            override fun onChanged() {
                postCount.text = postList?.size.toString()
            }
        }
        postList?.addListeners(changedListener!!)

        dashboardAdapter = DashboardPageAdapter(fragmentManager, postList!!)
        viewPager?.adapter = dashboardAdapter

        dashboardAdapter?.onBind()

        /*
        (view.findViewById(R.id.main_menu_button) as ImageButton).let { mainFab ->
            val fabOpen = AnimationUtils.loadAnimation(context, R.anim.fab_open)
            val fabClose = AnimationUtils.loadAnimation(context, R.anim.fab_close)
            val rotateForward = AnimationUtils.loadAnimation(context, R.anim.rotate_forward)
            val rotateBackward = AnimationUtils.loadAnimation(context, R.anim.rotate_backward)
            val fabs = arrayOf(R.id.settings_button, R.id.reblog_button, R.id.like_button).map { view.findViewById(it) }
            mainFab.setOnClickListener {
                if (isFabOpen) {
                    fabs.forEach {
                        it.startAnimation(fabClose)
                        it.isClickable = false
                    }
                    mainFab.startAnimation(rotateBackward)
                    isFabOpen = false
                } else {
                    fabs.forEach {
                        it.startAnimation(fabOpen)
                        it.isClickable = true
                    }
                    mainFab.startAnimation(rotateForward)
                    isFabOpen = true
                }
            }
        }*/

        // Like
        (view.findViewById(R.id.like_button) as ImageButton).let {
            likeButton = it
            it.setOnClickListener {
                doLike()
            }
        }

        // reblog
        val reblogButton = view.findViewById(R.id.reblog_button)
        reblogButton.setOnClickListener {
            doReblog()
        }

        val settingsButton = view.findViewById(R.id.settings_button)
        settingsButton.setOnClickListener {
            val ft = fragmentManager.beginTransaction()
            ft.addToBackStack(null)
            ft.replace(R.id.fragment_container, SettingsFragment())
            ft.commit()
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        getActionBar()?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dashboardAdapter?.onUnbind()
        if (changedListener != null) {
            postList?.removeListeners(changedListener!!)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_dashboard, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun doLike() {
        currentPost?.likeAsync({ post ->
            // ここまで来ると違うPostが表示されているかもしれないのでチェック
            currentPost?.let {
                if (it == post) {
                    toggleLikeButton(post)
                }
            }
            val msg = if (post.isLiked) R.string.liked_result else R.string.unliked_result
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        })
    }

    private fun doReblog() {
        val post = currentPost!!
        val input = EditText(context)
        input.setHint(R.string.comment_input_hint)
        AlertDialog.Builder(context)
                .setTitle(R.string.reblog_dialog_header)
                .setView(input)
                .setPositiveButton(android.R.string.ok) { dialog, which ->
                    val comment = input.text.toString()
                    val blogName = TPRuntime.tumblrService!!.user?.blogs?.first()?.name!!
                    post.reblogAsync(blogName, comment, {
                        Toast.makeText(context, R.string.reblogged_result, Toast.LENGTH_SHORT).show()
                    })
                }
                .setNegativeButton(android.R.string.cancel) { d, w -> }
                .show()
    }


    private fun toggleLikeButton(post: Post) {
        val state = android.R.attr.state_checked * if (post.isLiked) 1 else -1
        likeButton?.setImageState(intArrayOf(state), false)
    }
}
