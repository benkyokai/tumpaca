package com.tumpaca.tumpaca.adapter

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.tumblr.jumblr.types.PhotoPost
import com.tumblr.jumblr.types.Post
import com.tumblr.jumblr.types.QuotePost
import com.tumblr.jumblr.types.TextPost
import com.tumpaca.tumpaca.fragment.PostFragment
import java.util.*

class DashboardPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {

    private val mList: ArrayList<Post> = arrayListOf()

    override fun getItem(position: Int): Fragment {

        val post = mList[position]

        val bundle = Bundle()
        bundle.putInt("page", position)
        bundle.putString("title", post.type.value)
        bundle.putString("blogName", post.blogName)

        when (post.type) {
            Post.PostType.TEXT -> {
                val textPost = post as TextPost
                bundle.putString("subText", textPost.body)
            }
            Post.PostType.PHOTO -> {
                val photoPost = post as PhotoPost
                bundle.putStringArrayList("urls", ArrayList(photoPost.photos.map{it.sizes[0].url}))
                bundle.putString("subText", photoPost.caption)
            }
            Post.PostType.QUOTE -> {
                val quotePost = post as QuotePost
                bundle.putString("subText", quotePost.text)
            }
            Post.PostType.LINK -> {}
            Post.PostType.CHAT -> {}
            Post.PostType.AUDIO -> {}
            Post.PostType.VIDEO -> {}
            Post.PostType.ANSWER -> {}
            Post.PostType.POSTCARD -> {}
            else -> {}
        }

        val frag = PostFragment()
        frag.arguments = bundle

        return frag
    }

    override fun getCount(): Int {
        return mList.size
    }


    fun add(item: Post) {
        mList.add(item)
    }

    fun addAll(list: List<Post>) {
        mList.addAll(list)
    }

}