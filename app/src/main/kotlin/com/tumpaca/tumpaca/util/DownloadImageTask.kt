package com.tumpaca.tumpaca.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.util.DisplayMetrics
import android.util.Log
import android.widget.ImageView
import java.net.URL

/**
 * Created by yabu on 2016/06/13.
 */
class DownloadImageTask(val imageView: ImageView): AsyncTask<String, Void, Bitmap>() {

    val TAG = "DownloadImageTask"

    override fun doInBackground(vararg urls: String): Bitmap? {
        val url = urls[0]
        return loadBitmap(url)
    }

    private fun loadBitmap(url: String): Bitmap? {
        try {
            val stream = URL(url).openStream()
            return BitmapFactory.decodeStream(stream)
        } catch(e: Exception) {
            Log.e("Error", e.message)
            e.printStackTrace()
            return null
        }
    }

    override fun onPostExecute(result: Bitmap) {
        Log.d(TAG, "density ${result.density}, medium ${DisplayMetrics.DENSITY_MEDIUM}")
        result.density = DisplayMetrics.DENSITY_MEDIUM
        this.imageView.setImageBitmap(result)
    }

}