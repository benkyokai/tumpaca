package com.tumpaca.tumpaca.fragment.post

/**
 * Created by yabu on 7/11/16.
 */

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import com.tumblr.jumblr.types.PhotoPost
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.fragment.post.PostFragment
import com.tumpaca.tumpaca.model.TPRuntime
import com.tumpaca.tumpaca.util.DownloadImageTask
import com.tumpaca.tumpaca.util.blogAvatarAsync
import java.util.*

class PhotoPostFragment : PostFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val post = TPRuntime.tumblrService!!.postList?.get(page) as PhotoPost

        // データを取得
        val blogName = post.blogName
        val subText = post.caption
        val urls = ArrayList(post.photos.map{it.sizes[1].url})

        // View をつくる
        val view = inflater.inflate(R.layout.post_photo, container, false)

        val titleView = view.findViewById(R.id.title) as TextView
        titleView.text = blogName

        val subTextView = view.findViewById(R.id.sub) as WebView
        val mimeType = "text/html; charset=utf-8"
        subTextView.loadData(subText, mimeType, null)

        val iconView = view.findViewById(R.id.icon) as ImageView
        post.blogAvatarAsync { bitmap ->
            iconView.setImageBitmap(bitmap)
        }

        val imageView = view.findViewById(R.id.photo) as ImageView
        if (urls.size > 0) {
            DownloadImageTask({ bitmap ->
                imageView.setImageBitmap(bitmap)
            }).execute(urls[0])
        }

        return view
    }

}