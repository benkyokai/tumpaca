package com.tumpaca.tp.model

import com.tumblr.jumblr.types.Post

/**
 * Created by amake on 2017/02/08.
 */
class AdPost: Post() {
    override fun isLiked(): Boolean {
        return false
    }
}
