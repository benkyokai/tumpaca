package com.tumpaca.tp.fragment.post

/**
 * Created by yabu on 7/11/16.
 */

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tumblr.jumblr.types.VideoPost
import com.tumpaca.tp.R

class VideoPostFragment : PostFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.post_video, container, false)

        getPost({
            if (isAdded && it is VideoPost) {
                update(view, it)
            }
        })

        return view
    }

    private fun update(view: View, post: VideoPost) {
        initStandardViews(view, post.blogName, post.videos[0].embedCode, post.rebloggedFromName, post.noteCount)
        setIcon(view, post)
    }


}