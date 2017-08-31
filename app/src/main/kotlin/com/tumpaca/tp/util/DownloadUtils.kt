package com.tumpaca.tp.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.DisplayMetrics
import android.util.Log
import com.tumpaca.tp.model.TPRuntime
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.net.URL

class DownloadUtils {
    companion object {
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
                            URL(url).openStream().use { stream ->
                                val bytes = stream.readBytes()
                                emitter.onNext(bytes)
                                emitter.onComplete()
                            }
                        } catch (e: Exception) {
                            Log.e("downloadGif", e.message.orEmpty(), e)
                            emitter.onError(e)
                        }
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }
}
