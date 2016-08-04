package com.tumpaca.tumpaca.fragment

import android.os.Bundle

abstract class PostFragment: FragmentBase() {
    // TODO
    // PostList で対象のポストを管理していると、PostList の先頭に新しい Post がきた場合に対応できないので本当はよくない
    protected var postIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        postIndex = bundle.getInt("postIndex")
    }

}