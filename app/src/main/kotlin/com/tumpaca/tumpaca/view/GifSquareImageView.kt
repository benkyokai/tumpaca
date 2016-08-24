package com.tumpaca.tumpaca.view

import android.content.Context
import com.felipecsl.gifimageview.library.GifImageView

/**
 * GifImageViewのカスタムビュー
 * Created by yabu on 8/17/16.
 */
class GifSquareImageView(context: Context) : GifImageView(context) {
    /**
     * 表示時に横幅を縦幅にセットして正方形にする
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = measuredWidth
        setMeasuredDimension(width, width)
    }
}
