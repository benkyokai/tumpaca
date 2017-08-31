package com.tumpaca.tp.fragment.post

/**
 * Created by yabu on 7/11/16.
 */

import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import com.felipecsl.gifimageview.library.GifImageView
import com.tumblr.jumblr.types.PhotoPost
import com.tumpaca.tp.R
import com.tumpaca.tp.util.*
import com.tumpaca.tp.view.GifSquareImageView

class PhotoPostFragment : PostFragment() {

    companion object {
        private const val LOADING_VIEW_ID = 1
        private var loadingGifBytes: ByteArray? = null
        private const val TAG = "PhotoPost"
        private val tmpRect = Rect()
    }


    // このViewが実際に画面に表示されているかどうか。
    // ViewPagerでの使用を想定しているので、isVisible()は信用できない
    // （ViewPagerのcurrentItemでなくても事前ロードされるときからtrueが返るため）
    // この値はsetUserVisibleHint()で受け取って、onPause()やonResume()で
    // 変更しない（アプリがバックグラウンドにいるときなど実際に画面に描画されて
    // いなくてもこのステートはそのまま。
    var isVisibleToUser = false
    var imageLayout: LinearLayout? = null
    // GIFの可視判定を行う呼び出しに渡す必要があるが、中身は使っていない
    var mView: View? = null
    val onScrollChangedListener = object : ViewTreeObserver.OnScrollChangedListener {
        override fun onScrollChanged() {
            startStopAnimations()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.post_photo, container, false)
        mView = view

        getPost {
            if (isAdded && it is PhotoPost) {
                update(view, it)
            }
        }

        val webView = view.findViewById(R.id.sub) as WebView
        UIUtil.loadCss(webView)

        return view
    }

    override fun onDetach() {
        mView?.viewTreeObserver?.removeOnScrollChangedListener(onScrollChangedListener)
        super.onDetach()
    }

    private fun update(view: View, post: PhotoPost) {
        // データを取得
        val urls = post.photos.map { it.getBestSizeForScreen(resources.displayMetrics).url }

        initStandardViews(view, post.blogName, post.caption, post.rebloggedFromName, post.noteCount)
        setIcon(view, post)


        // ImageViewを挿入するPhotoListLayoutを取得
        imageLayout = view.findViewById(R.id.photo_list) as LinearLayout

        // このポストにGIFがあったら、再生／停止判定を行うリスナーを追加する
        if (urls.any { it.endsWith(".gif") }) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                view.setOnScrollChangeListener { _, _, _, _, _ ->
                    // スクロール位置によって見えてきたものを再生、見えなくなったものを停止
                    startStopAnimations()
                }
            } else {
                view.viewTreeObserver.addOnScrollChangedListener {
                    onScrollChangedListener
                }
            }

            imageLayout?.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                // ローディングなどでレイアウトが変わると見えるものも変わるので再判定
                startStopAnimations()
            }
        }

        if (loadingGifBytes == null) {
            loadingGifBytes = resources.openRawResource(R.raw.tumpaca_run).readBytes()
        }
        val loadingGifView = createLoadingGifImageView()
        loadingGifView.id = LOADING_VIEW_ID
        loadingGifView.setBackgroundColor(Color.parseColor("#35465c"))
        imageLayout?.addView(loadingGifView)
        loadingGifView.setBytes(loadingGifBytes)
        loadingGifView.gotoFrame(0)

        /**
         * urls.size個の画像があるので、個数分のImageViewを生成して、PhotoListLayoutに追加する
         */
        for ((i, url) in urls.enumerate()) {
            // gifだった場合はGif用のcustom image viewを使う
            if (url.endsWith(".gif")) {
                val gifView = createGifImageView(i != 0)
                imageLayout?.addView(gifView)
                post.downloadGif(url)
                        .subscribe { gif: ByteArray ->
                            gifView.setBytes(gif)
                            if (isVisibleToUser) {
                                // すでに見えているので今すぐアニメーションを開始
                                gifView.startAnimation()
                            } else {
                                // まだ見えていないけれど、何も描画しないと可視判定ができないので
                                // とりあえず最初のコマだけ表示しておく
                                gifView.gotoFrame(0)
                            }
                            imageLayout?.removeView(loadingGifView)
                        }
            } else {
                val iView = createImageView(i != 0)
                imageLayout?.addView(iView)
                post.downloadPhoto(url)
                        .subscribe { photo ->
                            iView.setImageBitmap(photo)
                            imageLayout?.removeView(loadingGifView)
                        }
            }
        }
    }


    private fun startStopAnimations() {
        imageLayout?.children()?.forEach {
            (it as? GifImageView)?.let { startStopByVisibility(it) }
        }
    }

    private fun startStopByVisibility(view: GifImageView) {
        if (isVisibleToUser && view.getLocalVisibleRect(tmpRect)) {
            if (!view.isAnimating) {
                view.startAnimation()
                Log.d(TAG, "Page $page: アニメーション開始")
            }
        } else if (view.isAnimating) {
            view.stopAnimation()
            Log.d(TAG, "Page $page: アニメーション停止")
        }
    }

    override fun onPause() {
        super.onPause()
        if (isVisibleToUser) {
            imageLayout?.children()?.forEach { (it as? GifImageView)?.stopAnimation() }
        }
    }


    override fun onResume() {
        super.onResume()
        startStopAnimations()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        // FragmentをViewPagerの中で使うとisVisible()はほぼ常にtrueになる。
        // 実際表示されているかどうかはこのメソッドでリスンする
        super.setUserVisibleHint(isVisibleToUser)
        this.isVisibleToUser = isVisibleToUser
        startStopAnimations()
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
