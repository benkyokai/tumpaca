package com.tumpaca.tumpaca.fragment.post

/**
 * Created by yabu on 7/11/16.
 */

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tumblr.jumblr.types.VideoPost
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.model.TPRuntime
import com.tumpaca.tumpaca.util.blogAvatarAsync

class VideoPostFragment : PostFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val post = TPRuntime.tumblrService!!.postList?.get(page) as VideoPost

        // View をつくる
        val view = inflater.inflate(R.layout.post_video, container, false)

        initStandardViews(view, post.blogName, post.videos[0].embedCode, post.rebloggedFromName, post.noteCount)
        post.blogAvatarAsync { setIcon(view, it) }

        return view
    }

}