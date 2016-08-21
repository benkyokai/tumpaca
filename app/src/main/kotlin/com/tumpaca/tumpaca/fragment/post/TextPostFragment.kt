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
import android.widget.LinearLayout
import android.widget.TextView
import com.tumblr.jumblr.types.TextPost
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.model.TPRuntime
import com.tumpaca.tumpaca.util.blogAvatarAsync

class TextPostFragment : PostFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val post = TPRuntime.tumblrService!!.postList?.get(page) as TextPost

        // データを取得
        val blogName = post.blogName
        val subText = post.body
        val reblogged = post.rebloggedFromName
        val noteCount = post.noteCount

        // View をつくる
        val view = inflater.inflate(R.layout.post_text, container, false)

        val titleView = view.findViewById(R.id.title) as TextView
        titleView.text = blogName

        val subTextView = view.findViewById(R.id.sub) as WebView
        val mimeType = "text/html; charset=utf-8"
        subTextView.loadData(subText, mimeType, null)

        val iconView = view.findViewById(R.id.icon) as ImageView
        post.blogAvatarAsync { bitmap ->
            iconView.setImageBitmap(bitmap)
        }

        val rebloggedView = view.findViewById(R.id.reblogged) as TextView
        if (reblogged != null) {
            rebloggedView.text = reblogged
        } else { // reblogじゃない場合はリブログアイコンを非表示にする
            val reblogInfoLayout = view.findViewById(R.id.post_info) as LinearLayout
            reblogInfoLayout.removeViewAt(R.id.reblog_icon)
        }

        val noteCountView = view.findViewById(R.id.notes) as TextView
        if (noteCount != null && noteCount!! == 1L ) {
            noteCountView.text = "${noteCount!!} note"
        } else {
            noteCountView.text = "${noteCount!!} notes"
        }

        return view
    }

}