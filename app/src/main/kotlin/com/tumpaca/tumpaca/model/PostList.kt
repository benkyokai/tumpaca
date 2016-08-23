package com.tumpaca.tumpaca.model

import android.util.Log
import com.tumblr.jumblr.JumblrClient
import com.tumblr.jumblr.types.Post
import com.tumblr.jumblr.types.User
import com.tumpaca.tumpaca.util.AsyncTaskHelper
import java.util.concurrent.CopyOnWriteArrayList

class PostList(private val client: JumblrClient) {
    companion object {
        private const val TAG = "PostList"
        private const val FIRST_FETCH_UNIT = 300
        private const val FETCH_UNIT = 100
        private const val FETCH_LIMIT = 20
        // 残りのポストがこれ以下になったら fetch する
        private const val FETCH_MIN_POST_NUM = 20
    }

    interface ChangedListener {
        fun onChanged()
    }

    interface FetchedListener {
        fun onFetched(size: Int)
    }

    var listener: ChangedListener? = null

    var fetchedListener: FetchedListener? = null

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


    /**
     * ダッシュボード取得時のオフセット
     * ダウンロードしたpostsはフィルターしてあるので、posts.sizeはオフセットに使えない
     */
    private var offset: Int = 0

    init {
        refreshUser()
        fetch(FIRST_FETCH_UNIT)
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

    // fetch が必要な条件かどうかを判定します。
    private fun needFetch(i: Int): Boolean {
        if (fetching) {
            // すでにフェッチ中なら fetch の必要なし
            return false
        }

        val remain = posts.size - (i + 1)
        return remain < FETCH_MIN_POST_NUM // 最小よりも小さかったら fetch が必要
    }

    private fun fetch(count: Int) {
        fetchImpl(count)
    }

    private fun fetchImpl(fetchSize: Int) {
        fetching = true

        if (fetchSize <= 0) {
            fetching = false
            return
        }

        object : AsyncTaskHelper<Void, Void, List<Post>>() {
            override fun doTask(params: Array<out Void>): List<Post> {
                // ここはバックグラウンドスレッド
                val parameter = hashMapOf(
                        "offset" to offset,
                        "limit" to FETCH_LIMIT,
                        "reblog_info" to true,
                        "notes_info" to true)
                Log.v(TAG, "try to load $offset->${offset + FETCH_LIMIT - 1}")
                return client.userDashboard(parameter)
            }

            override fun onError(e: Exception) {
                // TODO エラー処理
                Log.e(TAG, "PostList fetch error: ${e.message}")
                fetching = false
            }

            override fun onSuccess(result: List<Post>) {
                // ここは UI スレッド
                offset += result.size

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
                fetchedListener?.onFetched(size)
                fetching = false
                fetchImpl(fetchSize - result.size)
            }
        }.go()
    }

    private fun refreshUser() {
        object : AsyncTaskHelper<Void, Void, User>() {
            override fun doTask(params: Array<out Void>): User {
                return client.user()
            }

            override fun onError(e: Exception) {
                // TODO エラー処理
                Log.e(TAG, "PostList refreshUser error: ${e.message}")
            }

            override fun onSuccess(result: User) {
                Log.v(TAG, "Refresh User ${result.name}")
                user = result
            }
        }.go()
    }
}