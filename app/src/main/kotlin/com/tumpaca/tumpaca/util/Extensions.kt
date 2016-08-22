package com.tumpaca.tumpaca.util

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
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
    object : AsyncTaskHelper<Unit, Unit, Unit>() {
        override fun doTask(params: Array<out Unit>) {
            if (isLiked) {
                unlike()
            } else {
                like()
            }
        }

        override fun onError(e: Exception) {
            // TODO エラー処理
        }

        override fun onSuccess(result: Unit) {
            callback(self)
        }
    }.go()
}

fun Post.reblogAsync(blogName: String, comment: String, callback: (Post) -> Unit) {
    val self = this
    object : AsyncTaskHelper<Unit, Unit, Unit>() {
        override fun doTask(params: Array<out Unit>) {
            reblog(blogName, mapOf(Pair("comment", comment)))
        }

        override fun onError(e: Exception) {
            // TODO エラー処理
        }

        override fun onSuccess(result: Unit) {
            callback(self)
        }
    }.go()
}

fun Post.blogAvatarAsync(callback: (Bitmap) -> Unit) {
    object : AsyncTaskHelper<Void, Void, String?>() {
        override fun doTask(params: Array<out Void>): String? {
            return TPRuntime.avatarUrlCache.getIfNoneAndSet(blogName, {
                client.blogInfo(blogName).avatar()
            })
        }

        override fun onError(e: Exception) {
            // TODO エラー処理
        }

        override fun onSuccess(avatarUrl: String?) {
            DownloadImageTask(callback).execute(avatarUrl)
        }
    }.go()
}
