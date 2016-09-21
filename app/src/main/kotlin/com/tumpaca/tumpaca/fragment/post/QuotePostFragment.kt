package com.tumpaca.tumpaca.fragment.post

/**
 * Created by yabu on 7/11/16.
 */

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.tumblr.jumblr.types.QuotePost
import com.tumpaca.tumpaca.R

class QuotePostFragment : PostFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.post_quote, container, false)

        getPostAsync({
            if (it is QuotePost) {
                update(view, it)
            }
        })

        return view
    }

    private fun update(view: View, post: QuotePost) {
        initStandardViews(view, post.blogName, post.text, post.rebloggedFromName, post.noteCount)
        setIcon(view, post)
    }

}