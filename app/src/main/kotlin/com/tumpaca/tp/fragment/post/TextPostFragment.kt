package com.tumpaca.tp.fragment.post

/**
 * TextPost
 * Created by yabu on 7/11/16.
 */

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import com.tumblr.jumblr.types.TextPost
import com.tumpaca.tp.R
import com.tumpaca.tp.util.UIUtil

class TextPostFragment : PostFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.post_text, container, false)

        getPost {
            if (isAdded && it is TextPost) {
                val titleView = view.findViewById(R.id.post_title) as TextView
                if (it.title == null || it.title.isBlank()) {
                    titleView.visibility = View.GONE
                } else {
                    titleView.setText(it.title)
                }
                update(view, it)
            }
        }

        val webView = view.findViewById(R.id.sub) as WebView
        UIUtil.loadCss(webView)

        return view
    }

    private fun update(view: View, post: TextPost) {
        initStandardViews(view, post.blogName, post.body, post.rebloggedFromName, post.noteCount)
        setIcon(view, post)
    }

}