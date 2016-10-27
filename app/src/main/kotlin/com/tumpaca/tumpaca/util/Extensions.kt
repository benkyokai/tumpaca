package com.tumpaca.tumpaca.util

import android.content.*
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.tumblr.jumblr.types.Photo
import com.tumblr.jumblr.types.PhotoSize
import com.tumblr.jumblr.types.Post
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.model.TPRuntime

fun Context.editSharedPreferences(name: String, mode: Int = Context.MODE_PRIVATE, actions: (SharedPreferences.Editor) -> Unit) {
    val editor = getSharedPreferences(name, mode).edit()
    actions(editor)
    val committed = editor.commit()
    assert(committed)
}

fun Post.likeAsync(callback: (Post) -> Unit) {
    if (this.isLiked) {
        TPToastManager.show(TPRuntime.mainApplication.resources.getString(R.string.unlike))
    } else {
        TPToastManager.show(TPRuntime.mainApplication.resources.getString(R.string.like))
    }

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
            callback(this@likeAsync)
        }
    }.execute()
}

fun Post.reblogAsync(blogName: String, comment: String?, callback: (Post) -> Unit) {
    TPToastManager.show(TPRuntime.mainApplication.resources.getString(R.string.reblog))
    object : AsyncTask<Unit, Unit, Unit>() {
        override fun doInBackground(vararg args: Unit) {
            val option = if (comment == null) {
                emptyMap<String, String>()
            } else {
                mapOf("comment" to comment)
            }
            reblog(blogName, option)
            // TODO エラー処理
        }

        override fun onPostExecute(result: Unit) {
            callback(this@reblogAsync)
        }
    }.execute()
}

fun Post.blogAvatarAsync(callback: (Bitmap?) -> Unit) {
    object : AsyncTask<Void, Void, String?>() {
        override fun doInBackground(vararg args: Void): String? {
            return TPRuntime.avatarUrlCache.getIfNoneAndSet(blogName, {
                client.blogInfo(blogName).avatar()
            })
            // TODO エラー処理
        }

        override fun onPostExecute(avatarUrl: String?) {
            avatarUrl?.let {
                DownloadImageTask(callback).execute(it)
            }
        }
    }.execute()
}

/**
 * PHOTOポストの写真に対して最適なサイズを取得する
 * 高画質写真設定がtrueの場合:
 *   画面解像度の幅を超える最小の画像を取得
 * 高画質写真設定がfalseの場合:
 *   幅500px以下の最大の画像を取得
 * 上記条件で取得できない場合:
 *   サイズリストの最大の画像を取得
 */
fun Photo.getBestSizeForScreen(metrics: DisplayMetrics): PhotoSize {
    val w = metrics.widthPixels
    val biggest = sizes.first()
    val optimal = sizes.lastOrNull { it.width >= w }
    val better = sizes.firstOrNull { it.width <= 500 }

    if (optimal != null && optimal != biggest) {
        Log.d("Util", "画面の解像度：${metrics.widthPixels}x${metrics.heightPixels}　採択した解像度：")
        sizes.forEach { Log.d("Util", it.debugString() + (if (it == optimal) " optimal" else if (it == better) " better" else "")) }
    }

    if (TPRuntime.settings.isHighResolutionPhoto()) {
        return optimal ?: biggest
    } else {
        return better ?: biggest
    }
}

fun PhotoSize.debugString(): String {
    return "${width}x${height}"
}

fun ViewGroup.children(): List<View> {
    return (0 until childCount).map { getChildAt(it) }
}

fun <T> List<T>.enumerate(): List<Pair<Int, T>> {
    return (0 until size).map { it to get(it) }
}

fun Context.isOnline(): Boolean {
    val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connMgr.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

fun Context.onNetworkRestored(callback: () -> Unit): BroadcastReceiver {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.let {
                if (it.isOnline()) {
                    Log.d("Util", "インターネット接続が復活した")
                    callback()
                }
            }
        }
    }
    registerReceiver(receiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    return receiver
}
