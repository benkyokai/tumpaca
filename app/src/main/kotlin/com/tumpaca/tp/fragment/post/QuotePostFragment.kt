package com.tumpaca.tp.fragment.post

/**
 * Created by yabu on 7/11/16.
 */

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.tumblr.jumblr.types.QuotePost
import com.tumpaca.tp.R
import com.tumpaca.tp.util.UIUtil

class QuotePostFragment : PostFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.post_quote, container, false)

        getPost {
            if (isAdded && it is QuotePost) {
                update(view, it)
            }
        }

        val webView = view.findViewById<WebView>(R.id.sub)
        UIUtil.doNotHorizontalScroll(webView)

        return view
    }

    private fun update(view: View, post: QuotePost) {
        initStandardViews(view, post.blogName, post.text, post.rebloggedFromName, post.noteCount)
        setIcon(view, post)
    }

}