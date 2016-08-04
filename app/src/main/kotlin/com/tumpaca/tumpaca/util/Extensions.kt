package com.tumpaca.tumpaca.util

import android.content.Context
import android.content.SharedPreferences
import com.tumblr.jumblr.types.Post

fun Context.editSharedPreferences(name: String, mode: Int = Context.MODE_PRIVATE, actions: (SharedPreferences.Editor) -> Unit) {
    val editor = getSharedPreferences(name, mode).edit()
    actions(editor)
    val committed = editor.commit()
    assert(committed)
}

fun Post.likeAsync(callback: (Post) -> Unit) {
    AsyncTaskHelper.first<Unit, Unit, Unit> {
        if (isLiked) {
            unlike()
        } else {
            like()
        }
    }.then {
        callback(this)
    }.go()
}

fun Post.reblogAsync(blogName: String, comment: String, callback: (Post) -> Unit) {
    AsyncTaskHelper.first<Unit, Unit, Unit> {
        reblog(blogName, mapOf(Pair("comment", comment)))
    }.then {
        callback(this)
    }.go()
}

