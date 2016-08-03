package com.tumpaca.tumpaca.fragment

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
import com.tumblr.jumblr.types.Blog
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.model.TPRuntime
import com.tumpaca.tumpaca.util.AsyncTaskHelper
import com.tumpaca.tumpaca.util.DownloadImageTask

class PhotoPostFragment : PostFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        // データを取得
        val bundle = arguments
        val blogName = bundle.getString("blogName")
        val subText = bundle.getString("subText")
        val urls = bundle.getStringArrayList("urls")

        // View をつくる
        val view = inflater.inflate(R.layout.post_photo, container, false)

        val titleView = view.findViewById(R.id.title) as TextView
        val subTextView = view.findViewById(R.id.sub) as WebView
        val imageView = view.findViewById(R.id.photo) as ImageView
        val iconView = view.findViewById(R.id.icon) as ImageView

        titleView.text = blogName

        val mimeType = "text/html; charset=utf-8"

        val client = TPRuntime.tumblrService!!.jumblerClient!!

        AsyncTaskHelper.first<Void, Void, Blog?> {
            client.blogInfo(blogName)
        }.then {blog ->
            AsyncTaskHelper.first<Void, Void, String?> {
                blog?.avatar()
            }.then { avatarUrl ->
                DownloadImageTask(iconView).execute(avatarUrl)
            }.go()
        }.go()

        subTextView.loadData(subText, mimeType, null)

        if (urls != null && urls.size > 0) {
            DownloadImageTask(imageView).execute(urls[0])
        }

        return view
    }

}