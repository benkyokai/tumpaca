package com.tumpaca.tumpaca.fragment.post

/**
 * Created by yabu on 7/11/16.
 */

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.tumblr.jumblr.types.TextPost
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.util.UIUtil

class TextPostFragment : PostFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.post_text, container, false)

        getPost({
            if (isAdded && it is TextPost) {
                update(view, it)
            }
        })

        val webView = view.findViewById(R.id.sub) as WebView
        UIUtil.loadCss(webView)

        return view
    }

    private fun update(view: View, post: TextPost) {
        initStandardViews(view, post.blogName, post.body, post.rebloggedFromName, post.noteCount)
        setIcon(view, post)
    }

}