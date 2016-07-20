package com.tumpaca.tumpaca.adapter

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.tumblr.jumblr.types.*
import com.tumpaca.tumpaca.fragment.*
import java.util.*

class DashboardPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {

    private val mList: ArrayList<Post> = arrayListOf()

    override fun getItem(position: Int): Fragment {
        val post = mList[position]
        return genFragment(position, post)
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

    override fun getCount(): Int {
        return mList.size
    }

    fun getPost(index: Int): Post {
        return mList[index]
    }

    fun add(item: Post) {
        mList.add(item)
    }

    fun addAll(list: List<Post>) {
        mList.addAll(list)
    }

}