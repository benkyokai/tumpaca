package com.tumpaca.tumpaca.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

/**
 * Created by amake on 6/8/16.
 */

fun Activity.editPreferences(mode: Int = Context.MODE_PRIVATE, actions: (SharedPreferences.Editor) -> Unit) {
    val editor = getPreferences(mode).edit()
    actions(editor)
    val committed = editor.commit()
    assert(committed)
}
