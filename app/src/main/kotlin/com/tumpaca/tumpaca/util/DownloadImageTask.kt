package com.tumpaca.tumpaca.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.DisplayMetrics
import android.util.Log
import com.tumpaca.tumpaca.model.TPRuntime
import java.net.URL

/**
 * Created by yabu on 2016/06/13.
 */
class DownloadImageTask(val callback: (Bitmap) -> Unit) : AsyncTask<String, Void, Bitmap>() {
    companion object {
        private const val TAG = "DownloadImageTask"
    }

    override fun doInBackground(vararg urls: String): Bitmap? {
        val url = urls[0]
        return loadBitmap(url)
    }

    private fun loadBitmap(url: String): Bitmap? {
        try {
            return TPRuntime.bitMapCache.getIfNoneAndSet(url, {
                val stream = URL(url).openStream()
                val options = BitmapFactory.Options()
                options.inDensity = DisplayMetrics.DENSITY_MEDIUM
                BitmapFactory.decodeStream(stream, null, options)
            })
        } catch(e: Exception) {
            Log.e("Error", e.message)
            e.printStackTrace()
            return null
        }
    }

    override fun onPostExecute(result: Bitmap) {
        callback(result)
    }

}