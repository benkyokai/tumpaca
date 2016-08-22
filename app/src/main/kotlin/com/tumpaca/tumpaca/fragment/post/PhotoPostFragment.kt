package com.tumpaca.tumpaca.fragment.post

/**
 * Created by yabu on 7/11/16.
 */

import android.graphics.Color
import android.os.Bundle
import android.view.*
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
import com.tumpaca.tumpaca.view.GifSquareImageView
import java.net.URL
import java.util.*

class PhotoPostFragment : PostFragment() {
    private val LOADING_VIEW_ID = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val post = TPRuntime.tumblrService!!.postList?.get(page) as PhotoPost

        // データを取得
        val blogName = post.blogName
        val subText = post.caption
        val reblogged = post.rebloggedFromName
        val noteCount = post.noteCount
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
        if (noteCount != null && noteCount!! == 1L ) {
            noteCountView.text = "${noteCount!!} note"
        } else {
            noteCountView.text = "${noteCount!!} notes"
        }

        // ImageViewを挿入するPhotoListLayoutを取得
        val imageLayout = view.findViewById(R.id.photo_list) as LinearLayout

        val LOADING_GIF_BYTES = resources.openRawResource(R.raw.tumpaca_run).readBytes()

        val loadingGifView = createLoadingGifImageView()
        loadingGifView.id = LOADING_VIEW_ID
        loadingGifView.setBackgroundColor(Color.parseColor("#35465c"))
        imageLayout.addView(loadingGifView)
        loadingGifView.setBytes(LOADING_GIF_BYTES)
        loadingGifView.startAnimation()

        /**
         * urls.size個の画像があるので、個数分のImageViewを生成して、PhotoListLayoutに追加する
         */
        Array<Int>(urls.size, {i -> i}).map {
            val url = urls[it]
            // gifだった場合はGif用のcustom image viewを使う
            if (url.endsWith(".gif")) {
                val gifView = createGifImageView(it != 0)
                imageLayout.addView(gifView)

                object: AsyncTaskHelper<Void, Void, ByteArray>() {
                    override fun doTask(params: Array<out Void>): ByteArray {
                        return URL(url).openStream().readBytes()
                    }

                    override fun onError(e: Exception) {
                        // TODO エラー処理
                    }

                    override fun onSuccess(result: ByteArray) {
                        gifView.setBytes(result)
                        gifView.startAnimation()
                        imageLayout.removeView(loadingGifView)
                    }
                }.go()
            } else {
                val iView = createImageView(it != 0)
                imageLayout.addView(iView)
                DownloadImageTask { bitmap ->
                    iView.setImageBitmap(bitmap)
                    imageLayout.removeView(loadingGifView)
                }.execute(urls[it])
            }
        }

        return view
    }

    private fun createLoadingGifImageView(): GifSquareImageView {
        val gifView = GifSquareImageView(context)
        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        gifView.layoutParams = layoutParams
        gifView.scaleType = ImageView.ScaleType.CENTER
        return gifView
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