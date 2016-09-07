package com.tumpaca.tumpaca.fragment.post

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.tumblr.jumblr.types.Post
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.fragment.FragmentBase
import com.tumpaca.tumpaca.model.TPRuntime
import com.tumpaca.tumpaca.util.blogAvatarAsync

abstract class PostFragment : FragmentBase() {
    // TODO
    // PostList で対象のポストを管理していると、PostList の先頭に新しい Post がきた場合に対応できないので本当はよくない
    protected var page: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        page = bundle.getInt("pageNum")
    }

    fun getPost(): Post? {
        return TPRuntime.tumblrService!!.postList?.get(page)
    }

    fun initStandardViews(view: View, blogName: String, subText: String, reblogged: String?, noteCount: Long) {
        val titleView = view.findViewById(R.id.title) as TextView
        titleView.text = blogName

        val subTextView = view.findViewById(R.id.sub) as WebView
        val mimeType = "text/html; charset=utf-8"
        subTextView.loadData(subText, mimeType, null)

        val rebloggedView = view.findViewById(R.id.reblogged) as TextView
        if (reblogged != null) {
            rebloggedView.text = reblogged
        } else { // reblogじゃない場合はリブログアイコンを非表示にする
            val reblogInfoLayout = view.findViewById(R.id.post_info) as LinearLayout
            val reblogIcon = view.findViewById(R.id.reblog_icon)
            if (reblogIcon != null) {
                reblogInfoLayout.removeView(reblogIcon)
            }
        }

        val noteCountView = view.findViewById(R.id.notes) as TextView
        if (noteCount == 1L) {
            noteCountView.text = "${noteCount} note"
        } else {
            noteCountView.text = "${noteCount} notes"
        }
    }

    fun setIcon(view: View, post: Post) {
        val iconView = view.findViewById(R.id.icon) as ImageView
        post.blogAvatarAsync { iconView.setImageBitmap(it) }
    }
}