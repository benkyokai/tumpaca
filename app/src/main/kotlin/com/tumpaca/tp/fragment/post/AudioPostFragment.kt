package com.tumpaca.tp.fragment.post

/**
 * Created by yabu on 7/11/16.
 */

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.tumblr.jumblr.types.AudioPost
import com.tumpaca.tp.R
import com.tumpaca.tp.util.UIUtil

class AudioPostFragment : PostFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.post_audio, container, false)

        getPost({
            if (isAdded && it is AudioPost) {
                update(view, it)
            }
        })

        val webView = view.findViewById(R.id.sub) as WebView
        UIUtil.loadCss(webView)

        return view
    }

    private fun update(view: View, post: AudioPost) {
        initStandardViews(view, post.blogName, post.embedCode, post.rebloggedFromName, post.noteCount)
        setIcon(view, post)
    }

}