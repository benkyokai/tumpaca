package com.tumpaca.tp.fragment;

import android.content.BroadcastReceiver
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.tumblr.jumblr.types.Post
import com.tumpaca.tp.R
import com.tumpaca.tp.model.PostList
import com.tumpaca.tp.model.TPRuntime
import com.tumpaca.tp.util.*

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
    var networkReceiver: BroadcastReceiver? = null

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
        postList = TPRuntime.tumblrService.postList

        val settingsButton = view.findViewById(R.id.settings_button)
        settingsButton.setOnClickListener {
            val ft = fragmentManager.beginTransaction()
            ft.addToBackStack(null)
            ft.replace(R.id.fragment_container, SettingsFragment())
            ft.commit()
        }

        if (postList == null) {
            // postListがnullなら何も表示しない
            return view
        }

        changedListener = object : PostList.ChangedListener {
            override fun onChanged() {
                postCount.text = postList?.size.toString()
            }
        }
        postList?.addListeners(changedListener!!)

        dashboardAdapter = DashboardPageAdapter(childFragmentManager, postList!!)
        viewPager?.adapter = dashboardAdapter

        dashboardAdapter?.onBind()

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

        // ダッシュボードをロードしようとした時点でネットワークに接続できない場合、
        // ネットワークが復活したらダッシュボードを丸々リロードする
        // TODO: これはすごくイケてないので、activityを殺さずに表示を更新できるようにする
        // onDestroyView() で networkReceiver を登録解除していることに注意
        if (!context.isOnline()) {
            Toast.makeText(context, R.string.offline_toast, Toast.LENGTH_SHORT).show()
            networkReceiver = context.onNetworkRestored {
                activity.finish()
                activity.startActivity(activity.intent)
            }
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
        changedListener?.let { postList?.removeListeners(it) }
        networkReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (e: Throwable) {
                Log.d(TAG, "Receiver was not registered", e)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_dashboard, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun doLike() {
        val likeMsg = resources.getString(R.string.liked_result)
        val unlikeMsg = resources.getString(R.string.unliked_result)
        val errorMsg = resources.getString(R.string.error_like)
        currentPost?.likeAsync({ post, result ->
            if (result) {
                // ここまで来ると違うPostが表示されているかもしれないのでチェック
                currentPost?.let {
                    if (it == post) {
                        toggleLikeButton(post)
                    }
                }
                if (post.isLiked) {
                    TPToastManager.show(likeMsg)
                } else {
                    TPToastManager.show(unlikeMsg)
                }
            } else {
                TPToastManager.show(errorMsg)
            }
        })
    }

    private fun doReblog() {
        val blogName = TPRuntime.tumblrService.user?.blogs?.first()?.name!!
        val msg = resources.getString(R.string.reblogged_result)
        val errorMsg = resources.getString(R.string.error_reblog)
        currentPost
                ?.reblogAsync(blogName, null)
                ?.subscribe({ post ->
                    TPToastManager.show(msg)
                }) { e ->
                    TPToastManager.show(errorMsg)
                }
    }


    private fun toggleLikeButton(post: Post) {
        val state = android.R.attr.state_checked * if (post.isLiked) 1 else -1
        likeButton?.setImageState(intArrayOf(state), false)
    }
}
