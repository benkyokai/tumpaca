package com.tumpaca.tumpaca.fragment.post

/**
 * Created by yabu on 7/11/16.
 */

import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.felipecsl.gifimageview.library.GifImageView
import com.tumblr.jumblr.types.PhotoPost
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.model.TPRuntime
import com.tumpaca.tumpaca.util.DownloadImageTask
import com.tumpaca.tumpaca.util.blogAvatarAsync
import com.tumpaca.tumpaca.util.getBestSizeForScreen
import com.tumpaca.tumpaca.view.GifSquareImageView
import java.net.URL
import java.util.*

class PhotoPostFragment : PostFragment() {

    companion object {
        private const val LOADING_VIEW_ID = 1
        private var loadingGifBytes: ByteArray? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val post = TPRuntime.tumblrService!!.postList?.get(page) as PhotoPost

        // データを取得
        val urls = ArrayList(post.photos.map { it.getBestSizeForScreen(resources.displayMetrics).url })

        // View をつくる
        val view = inflater.inflate(R.layout.post_photo, container, false)

        initStandardViews(view, post.blogName, post.caption, post.rebloggedFromName, post.noteCount)
        post.blogAvatarAsync { setIcon(view, it) }

        // ImageViewを挿入するPhotoListLayoutを取得
        val imageLayout = view.findViewById(R.id.photo_list) as LinearLayout


        if (loadingGifBytes == null) {
            loadingGifBytes = resources.openRawResource(R.raw.tumpaca_run).readBytes()
        }
        val loadingGifView = createLoadingGifImageView()
        loadingGifView.id = LOADING_VIEW_ID
        loadingGifView.setBackgroundColor(Color.parseColor("#35465c"))
        imageLayout.addView(loadingGifView)
        loadingGifView.setBytes(loadingGifBytes)
        loadingGifView.startAnimation()

        /**
         * urls.size個の画像があるので、個数分のImageViewを生成して、PhotoListLayoutに追加する
         */
        0.until(urls.size).forEach {
            val url = urls[it]
            // gifだった場合はGif用のcustom image viewを使う
            if (url.endsWith(".gif")) {
                val gifView = createGifImageView(it != 0)
                imageLayout.addView(gifView)
                object: AsyncTask<Unit, Unit, ByteArray>() {

                    override fun doInBackground(vararg args: Unit): ByteArray {
                        // TODO: 失敗した場合のエラーハンドリング
                        return URL(urls[it]).openStream().readBytes()
                    }

                    override fun onPostExecute(result: ByteArray) {
                        gifView.setBytes(result)
                        gifView.startAnimation()
                        imageLayout.removeView(loadingGifView)
                    }
                }.execute()
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
            /* なぜかmarginが効かない (多分何か間違ってる)
            val marginLayoutParams = ViewGroup.MarginLayoutParams(layoutParams)
            marginLayoutParams.topMargin = 20
            iView.layoutParams = marginLayoutParams
            */
            iView.layoutParams = layoutParams
            iView.setPadding(0, 20, 0, 0)
        } else {
            iView.layoutParams = layoutParams
        }
        iView.scaleType = ImageView.ScaleType.FIT_CENTER
        iView.adjustViewBounds = true
    }

}