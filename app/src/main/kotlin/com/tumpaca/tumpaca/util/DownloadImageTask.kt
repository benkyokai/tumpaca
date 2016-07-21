package com.tumpaca.tumpaca.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.DisplayMetrics
import android.util.Log
import android.widget.ImageView
import com.tumpaca.tumpaca.TPRuntime
import java.net.URL

/**
 * Created by yabu on 2016/06/13.
 */
class DownloadImageTask(val imageView: ImageView) : AsyncTask<String, Void, Bitmap>() {

    override fun doInBackground(vararg urls: String): Bitmap? {
        val url = urls[0]
        return loadBitmap(url)
    }

    private fun loadBitmap(url: String): Bitmap? {
        try {
            return TPRuntime.bitMapCache.getIfNoneAndSet(url, {
                val stream = URL(url).openStream()
                BitmapFactory.decodeStream(stream)
            })
        } catch(e: Exception) {
            Log.e("Error", e.message)
            e.printStackTrace()
            return null
        }
    }

    override fun onPostExecute(result: Bitmap) {
        // densityが画面のdpiに応じて勝手に設定されるので、倍率1に戻す
        result.density = DisplayMetrics.DENSITY_MEDIUM
        this.imageView.setImageBitmap(result)
    }

}