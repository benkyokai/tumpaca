package com.tumpaca.tumpaca.util

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.tumblr.jumblr.types.Photo
import com.tumblr.jumblr.types.PhotoSize
import com.tumblr.jumblr.types.Post
import com.tumpaca.tumpaca.model.TPRuntime

fun Context.editSharedPreferences(name: String, mode: Int = Context.MODE_PRIVATE, actions: (SharedPreferences.Editor) -> Unit) {
    val editor = getSharedPreferences(name, mode).edit()
    actions(editor)
    val committed = editor.commit()
    assert(committed)
}

fun Post.likeAsync(callback: (Post) -> Unit) {
    val self = this
    object : AsyncTask<Unit, Unit, Unit>() {
        override fun doInBackground(vararg args: Unit) {
            if (isLiked) {
                unlike()
            } else {
                like()
            }
            // TODO エラー処理
        }

        override fun onPostExecute(result: Unit) {
            callback(self)
        }
    }.execute()
}

fun Post.reblogAsync(blogName: String, comment: String?, callback: (Post) -> Unit) {
    val self = this
    object : AsyncTask<Unit, Unit, Unit>() {
        override fun doInBackground(vararg args: Unit) {
            val option = mapOf<String, String>()
            if (comment != null) {
                option.plus(Pair("comment", comment))
            }
            reblog(blogName, option)
            // TODO エラー処理
        }

        override fun onPostExecute(result: Unit) {
            callback(self)
        }
    }.execute()
}

fun Post.blogAvatarAsync(callback: (Bitmap) -> Unit) {
    object : AsyncTask<Void, Void, String?>() {
        override fun doInBackground(vararg args: Void): String? {
            return TPRuntime.avatarUrlCache.getIfNoneAndSet(blogName, {
                client.blogInfo(blogName).avatar()
            })
            // TODO エラー処理
        }

        override fun onPostExecute(avatarUrl: String?) {
            DownloadImageTask(callback).execute(avatarUrl)
        }
    }.execute()
}

fun Photo.getBestSizeForScreen(metrics: DisplayMetrics): PhotoSize {
    val w = metrics.widthPixels
    val biggest = sizes.first()
    val optimal = sizes.lastOrNull { it.width >= w }

    if (optimal != null && optimal != biggest) {
        Log.d("Util", "画面の解像度：${metrics.widthPixels}x${metrics.heightPixels}　採択した解像度：")
        sizes.forEach { Log.d("Util", (if (it == optimal) "=>" else "  ") + it.debugString()) }
    }

    return optimal ?: biggest
}

fun PhotoSize.debugString(): String {
    return "${width}x${height}"
}

fun ViewGroup.children(): List<View> {
    return 0.until(childCount).map { getChildAt(it) }
}

fun <T> List<T>.enumerate(): List<Pair<Int, T>> {
    return (0 until size).map { it to get(it) }
}
