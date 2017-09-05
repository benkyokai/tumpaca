package com.tumpaca.tp.util

import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import com.tumpaca.tp.R
import com.tumpaca.tp.activity.MainActivity
import com.tumpaca.tp.model.TPRuntime
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.net.URL

class DownloadUtils {
    companion object {
        private const val TAG = "DownloadUtils"

        // 外部ストレージ許可のダイアログから戻ってきたときにダウンロードを再開するために
        // 対象の URL をこれに保存しておく。なお、画面が回転されたりしたら復元しないがそれは諦める。
        private var urlToSave: String? = null

        @JvmStatic fun downloadPhoto(url: String): Observable<Bitmap> {
            return Observable
                    .create { emitter: ObservableEmitter<Bitmap> ->
                        try {
                            val photo = TPRuntime.bitMapCache.getIfNoneAndSet(url, {
                                URL(url).openStream().use { stream ->
                                    val options = BitmapFactory.Options()
                                    options.inDensity = DisplayMetrics.DENSITY_MEDIUM
                                    BitmapFactory.decodeStream(stream, null, options)
                                }
                            })
                            emitter.onNext(photo)
                            emitter.onComplete()
                        } catch (e: Exception) {
                            Log.e("downloadPhoto", e.message.orEmpty(), e)
                            emitter.onError(e)
                        }
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }

        @JvmStatic fun downloadGif(url: String): Observable<ByteArray> {
            return Observable
                    .create { emitter: ObservableEmitter<ByteArray> ->
                        try {
                            val gif = TPRuntime.gifCache.getIfNoneAndSet(url, {
                                URL(url).openStream().use { stream ->
                                    stream.readBytes()
                                }
                            })
                            emitter.onNext(gif)
                            emitter.onComplete()
                        } catch (e: Exception) {
                            Log.e("downloadGif", e.message.orEmpty(), e)
                            emitter.onError(e)
                        }
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }

        @JvmStatic fun saveImage(activity: MainActivity, url: String) {
            if (!activity.checkStoragePermissions()) {
                urlToSave = url
                activity.requestStoragePermissions()
                return
            }

            val target = Uri.parse(url)
            val request = DownloadManager.Request(target)
            val fileName = target.lastPathSegment
            request.setDescription(activity.resources.getString(R.string.download_image))
            request.setTitle(fileName)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            val manager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
            manager?.enqueue(request)
            Log.d(TAG, "startDownload started... fileName=$fileName,url=$url")
        }

        @JvmStatic fun resumeSaveImage(activity: MainActivity) {
            if (urlToSave != null) {
                val url: String = urlToSave as String
                urlToSave = null
                saveImage(activity, url)
            }
        }
    }
}
