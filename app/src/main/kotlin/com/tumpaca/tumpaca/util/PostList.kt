package com.tumpaca.tumpaca.util

import android.text.Editable
import android.util.Log
import com.tumblr.jumblr.JumblrClient
import com.tumblr.jumblr.types.Post
import com.tumblr.jumblr.types.User
import java.util.concurrent.CopyOnWriteArrayList

class PostList(private val client: JumblrClient) {
    companion object {
        private const val TAG = "PostList"
        private const val FETCH_UNIT = 20
        // 残りのポストがこれ以下になったら fetch する
        private const val FETCH_MIN_POST_NUM = 10
    }

    interface ChangedListener {
        fun onChanged()
    }

    var listener: ChangedListener? = null

    val size: Int
        get() = posts.size

    // CHAT, ANSWER, POSTCARDは対応していないので、postから除く
    private val SUPPORTED_TYPES = setOf(
            Post.PostType.AUDIO,
            Post.PostType.LINK,
            Post.PostType.PHOTO,
            Post.PostType.QUOTE,
            Post.PostType.TEXT,
            Post.PostType.VIDEO)

    // SUPPORTED_TYPES で列挙されたタイプでフィルタリングされたポストリスト
    // バックグラウンドスレッドからもアクセスするのでスレッドセーフリストを使う必要あり。
    private val posts: CopyOnWriteArrayList<Post> = CopyOnWriteArrayList()

    // fetch はいまのところ UI スレッドからのみアクセスするので volatile いらない
    private var fetching: Boolean = false

    // user はいまのところ別スレッドでさわらないので volatile いらない
    private var user: User? = null

    init {
        refreshUser()
        fetch(FETCH_UNIT)
    }

    fun get(i: Int): Post? {
        if (needFetch(i)) {
            Log.v(TAG, "Need fetch $i/$size")
            fetch(FETCH_UNIT)
        }

        if (i < posts.size) {
            return posts[i]
        } else {
            return null
        }
    }

    fun like(i: Int, callback: (Post) -> Unit) {
        AsyncTaskHelper.first<Unit, Unit, Unit> {
            get(i)?.let {
                if (it.isLiked) {
                    Log.v(TAG, "Unliked ${it.slug}")
                    it.unlike()
                } else {
                    Log.v(TAG, "Liked ${it.slug}")
                    it.like()
                }
            }
        }.then {
            val post = get(i)!!
            callback(post)
        }.go()
    }

    fun reblog(i: Int, comment: Editable, callback: (Post) -> Unit) {
        // TODO: 複数のブログに投稿できる場合の対応
        val blogName = user?.blogs?.first()?.name
        AsyncTaskHelper.first<Unit, Unit, Unit> {
            get(i)?.let {
                Log.v(TAG, "Reblogged ${it.slug}")
                it.reblog(blogName, mapOf(Pair("comment", comment)))
            }
        }.then {
            val post = get(i)!!
            callback(post)
        }.go()
    }

    // fetch が必要な条件かどうかを判定します。
    private fun needFetch(i: Int): Boolean {
        if (fetching) {
            // すでにフェッチ中なら fetch の必要なし
            return false
        }

        val remain = posts.size - (i + 1)
        return remain < FETCH_MIN_POST_NUM // 最小よりも小さかったら fetch が必要
    }

    private fun fetch(limit: Int) {
        fetching = true
        AsyncTaskHelper.first<Void, Void, List<Post>> {
            // ここはバックグラウンドスレッド
            val offset = posts.size
            val parameter = hashMapOf(
                    "offset" to offset,
                    "limit" to limit)
            Log.v(TAG, "try to load $offset->${offset + limit - 1}")
            client.userDashboard(parameter)
        }.then { result ->
            // ここは UI スレッド
            val filteredResult = result.filter {
                SUPPORTED_TYPES.contains(it.type)
            }

            if (result.size != filteredResult.size) {
                // TODO fetch の結果 filter されると、現状の post サイズとインターネット上の post のインデックスが
                // 合わなくなるのでフィルタリングは外側でやったほうがいい
                Log.w(TAG, "Some posts are filtered: ${result.size}=>${filteredResult.size}");
            }

            posts.addAll(filteredResult)
            Log.v(TAG, "Loaded ${result.size} posts, size=$size")
            listener?.onChanged()
            fetching = false
        }.go()
    }

    private fun refreshUser() {
        AsyncTaskHelper.first<Void, Void, User> {
            client.user()
        }.then { result ->
            Log.v(TAG, "Refresh User ${result.name}")
            user = result
        }.go()
    }
}