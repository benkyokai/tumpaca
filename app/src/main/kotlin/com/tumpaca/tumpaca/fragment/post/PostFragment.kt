package com.tumpaca.tumpaca.fragment.post

import android.os.Bundle
import com.tumpaca.tumpaca.fragment.FragmentBase

abstract class PostFragment: FragmentBase() {
    // TODO
    // PostList で対象のポストを管理していると、PostList の先頭に新しい Post がきた場合に対応できないので本当はよくない
    protected var page: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        page = bundle.getInt("pageNum")
    }

}