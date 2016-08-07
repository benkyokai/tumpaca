package com.tumpaca.tumpaca.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.util.Log
import com.tumblr.jumblr.types.Post
import com.tumpaca.tumpaca.fragment.*
import com.tumpaca.tumpaca.fragment.post.*
import com.tumpaca.tumpaca.model.PostList

class DashboardPageAdapter(fm: FragmentManager, private val postList: PostList): FragmentStatePagerAdapter(fm) {
    companion object {
        private const val TAG = "DashboardPageAdapter"
    }

    private val listener = object: PostList.ChangedListener {
        override fun onChanged() {
            Log.d(TAG, "call notifyDataSetChanged()")
            notifyDataSetChanged()
        }
    }

    // View に接続された
    fun onBind() {
        postList.listener = listener
    }

    // View と切り離された
    fun onUnbind() {
        postList.listener = null
    }

    override fun getItem(position: Int): Fragment {
        val bundle = Bundle()
        bundle.putInt("pageNum", position)

        val post = postList.get(position)
        val fragment = createFragment(post!!.type)
        fragment.arguments = bundle

        return fragment
    }

    override fun getCount(): Int {
        return postList.size
    }

    private fun createFragment(postType: Post.PostType): PostFragment {
        when (postType) {
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
                throw IllegalArgumentException("post type is invalid: " + postType.value)
            }
        }
    }

}