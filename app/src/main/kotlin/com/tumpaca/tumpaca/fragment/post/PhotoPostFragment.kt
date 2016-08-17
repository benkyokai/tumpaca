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
import com.felipecsl.gifimageview.library.GifImageView
import com.tumblr.jumblr.types.PhotoPost
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.model.TPRuntime
import com.tumpaca.tumpaca.util.AsyncTaskHelper
import com.tumpaca.tumpaca.util.DownloadImageTask
import com.tumpaca.tumpaca.util.blogAvatarAsync
import java.net.URL
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

        // ImageViewを挿入するPhotoListLayoutを取得
        val imageLayout = view.findViewById(R.id.photo_list) as LinearLayout

        /**
         * urls.size個の画像があるので、個数分のImageViewを生成して、PhotoListLayoutに追加する
         */
        Array<Int>(urls.size, {i -> i}).map {
            val url = urls[it]
            // gifだった場合はGif用のcustom image viewを使う
            if (url.endsWith(".gif")) {
                val gifView = createGifImageView(it != 0)
                imageLayout.addView(gifView)

                AsyncTaskHelper.first<Void, Void, ByteArray> {
                    URL(url).openStream().readBytes()
                }.then {byteArray ->
                    gifView.setBytes(byteArray)
                    gifView.startAnimation()
                }.go()
            } else {
                val iView = createImageView(it != 0)
                imageLayout.addView(iView)

                DownloadImageTask { bitmap ->
                    iView.setImageBitmap(bitmap)
                }.execute(urls[it])
            }
        }

        return view
    }

    private fun createGifImageView(withTopMargin: Boolean): GifImageView {
        val gifView = GifImageView(context)
        setParameterToImageView(gifView, withTopMargin)
        return gifView
    }

    private fun createImageView(withTopMargin: Boolean): ImageView {
        val iView = ImageView(context)
        setParameterToImageView(iView, withTopMargin)
        return iView
    }

    private fun setParameterToImageView(iView: ImageView, withTopMargin: Boolean) {
        // レイアウト生成
        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        if (withTopMargin) {
            val marginLayoutParams = ViewGroup.MarginLayoutParams(layoutParams)
            marginLayoutParams.topMargin = 20
            iView.layoutParams = marginLayoutParams
        } else {
            iView.layoutParams = layoutParams
        }
        iView.scaleType = ImageView.ScaleType.FIT_CENTER
        iView.adjustViewBounds = true
    }

}