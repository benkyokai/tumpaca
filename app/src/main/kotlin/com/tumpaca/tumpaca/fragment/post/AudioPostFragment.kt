package com.tumpaca.tumpaca.fragment.post

/**
 * Created by yabu on 7/11/16.
 */

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tumblr.jumblr.types.AudioPost
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.model.TPRuntime
import com.tumpaca.tumpaca.util.blogAvatarAsync

class AudioPostFragment : PostFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val post = TPRuntime.tumblrService!!.postList?.get(page) as AudioPost

        // View をつくる
        val view = inflater.inflate(R.layout.post_audio, container, false)

        initStandardViews(view, post.blogName, post.embedCode, post.rebloggedFromName, post.noteCount)
        setIcon(view, post)

        return view
    }

}