package com.tumpaca.tumpaca.util

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.AsyncTask
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

fun Post.reblogAsync(blogName: String, comment: String, callback: (Post) -> Unit) {
    val self = this
    object : AsyncTask<Unit, Unit, Unit>() {
        override fun doInBackground(vararg args: Unit) {
            reblog(blogName, mapOf(Pair("comment", comment)))
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
