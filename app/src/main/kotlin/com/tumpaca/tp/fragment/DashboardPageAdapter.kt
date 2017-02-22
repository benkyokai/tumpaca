package com.tumpaca.tp.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.util.Log
import com.tumblr.jumblr.types.Post
import com.tumpaca.tp.fragment.post.*
import com.tumpaca.tp.model.AdPost
import com.tumpaca.tp.model.PostList

class DashboardPageAdapter(fm: FragmentManager, private val postList: PostList) : FragmentStatePagerAdapter(fm) {
    companion object {
        private const val TAG = "DashboardPageAdapter"
    }

    private val listener = object : PostList.ChangedListener {
        override fun onChanged() {
            Log.d(TAG, "call notifyDataSetChanged()")
            notifyDataSetChanged()
        }
    }

    // View に接続された
    fun onBind() {
        postList.addListeners(listener)
    }

    // View と切り離された
    fun onUnbind() {
        postList.removeListeners(listener)
    }

    override fun getItem(position: Int): Fragment {
        val bundle = Bundle()
        bundle.putInt("pageNum", position)

        val post = postList.get(position)
        val fragment = createFragment(post!!)
        fragment.arguments = bundle

        return fragment
    }

    override fun getCount(): Int {
        return postList.size
    }

    private fun createFragment(post: Post): PostFragment {
        if (post is AdPost) {
            return AdPostFragment()
        }
        when (post.type) {
            Post.PostType.TEXT -> {
                return TextPostFragment()
            }
            Post.PostType.PHOTO -> {
                return PhotoPostFragment()
            }
            Post.PostType.QUOTE -> {
                return QuotePostFragment()
            }
            Post.PostType.LINK -> {
                return LinkPostFragment()
            }
            Post.PostType.AUDIO -> {
                return AudioPostFragment()
            }
            Post.PostType.VIDEO -> {
                return VideoPostFragment()
            }
            else -> {
                // CHAT, ANSWER, POSTCARDは来ないはず
                throw IllegalArgumentException("post type is invalid: " + post.type.value)
            }
        }
    }

}