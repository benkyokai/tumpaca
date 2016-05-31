package com.tumpaca.tumpaca.util

import android.text.Editable
import android.text.TextWatcher

//
// シンプルな TextWatcher を独自定義
//
abstract class SimpleTextWatcher(): TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun afterTextChanged(s: Editable) {
    }

    abstract override fun onTextChanged(a: CharSequence, start : Int, before : Int, count : Int)
}
