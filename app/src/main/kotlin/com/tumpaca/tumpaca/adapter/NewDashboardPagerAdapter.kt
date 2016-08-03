package com.tumpaca.tumpaca.adapter

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.Log
import com.tumblr.jumblr.types.*
import com.tumpaca.tumpaca.fragment.*
import com.tumpaca.tumpaca.util.PostList
import java.util.*

class NewDashboardPagerAdapter(fm: FragmentManager, private val postList: PostList): FragmentPagerAdapter(fm) {
    companion object {
        private const val TAG = "NewDashboardPagerAdapter"
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
        val post = postList.get(position)
        return genFragment(position, post!!)
    }

    override fun getCount(): Int {
        Log.d(TAG, "count: ${postList.size}")
        return postList.size
    }

    fun genFragment(position: Int, post: Post): PostFragment {

        val bundle = Bundle()
        bundle.putInt("page", position)
        bundle.putString("blogName", post.blogName)

        when (post.type) {
            Post.PostType.TEXT -> {
                val textPost = post as TextPost
                bundle.putString("subText", textPost.body)
                val fragment = TextPostFragment()
                fragment.arguments = bundle
                return fragment
            }
            Post.PostType.PHOTO -> {
                val photoPost = post as PhotoPost
                bundle.putStringArrayList("urls", ArrayList(photoPost.photos.map{it.sizes[1].url}))
                bundle.putString("subText", photoPost.caption)
                val fragment = PhotoPostFragment()
                fragment.arguments = bundle
                return fragment
            }
            Post.PostType.QUOTE -> {
                val quotePost = post as QuotePost
                bundle.putString("subText", quotePost.text)
                val fragment = QuotePostFragment()
                fragment.arguments = bundle
                return fragment
            }
            Post.PostType.LINK -> {
                val linkPost = post as LinkPost
                bundle.putString("subText", linkPost.linkUrl)
                val fragment = LinkPostFragment()
                fragment.arguments = bundle
                return fragment
            }
            Post.PostType.AUDIO -> {
                val audioPost = post as AudioPost
                bundle.putString("subText", audioPost.embedCode)
                val fragment = AudioPostFragment()
                fragment.arguments = bundle
                return fragment
            }
            Post.PostType.VIDEO -> {
                val videoPost = post as VideoPost
                bundle.putString("subText", videoPost.videos[0].embedCode)
                val fragment = VideoPostFragment()
                fragment.arguments = bundle
                return fragment
            }
            else -> {
                // CHAT, ANSWER, POSTCARDは来ないはず
                throw RuntimeException("post type is invalid: " + post.type.value)
            }
        }
    }

}