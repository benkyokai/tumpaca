package com.tumpaca.tumpaca.model

import android.os.AsyncTask
import android.util.Log
import com.tumblr.jumblr.JumblrClient
import com.tumblr.jumblr.types.Post
import com.tumblr.jumblr.types.User
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

    // マルチスレッドでは使わないが、リスナーのイテレート中に自身を消したりするので CopyOnWriteArrayList を利用
    val listeners: CopyOnWriteArrayList<ChangedListener> = CopyOnWriteArrayList()

    fun addListeners(listener: ChangedListener) {
        listeners.add(listener)
    }

    fun removeListeners(listener: ChangedListener) {
        listeners.remove(listener)
    }

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

            // 渡されたインデックスの post が次回の fetch に確実に含まれるようにするために
            // 渡されたインデックスと現在の offset の差を計算する。そして、その値と FETCH_UNIT
            // を比較して大きい方を次回の fetch 単位とする
            val remain = i - offset + 1
            val unit = if (remain > FETCH_UNIT) remain else FETCH_UNIT
            fetch(unit)
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

        // TODO: PostListの参照が漏れないようにこのタスクを独立クラス化する
        object : AsyncTask<Void, Void, List<Post>>() {
            override fun doInBackground(vararg args: Void): List<Post> {
                // ここはバックグラウンドスレッド
                val parameter = hashMapOf(
                        "offset" to offset,
                        "limit" to FETCH_LIMIT,
                        "reblog_info" to true,
                        "notes_info" to true)
                Log.v(TAG, "try to load $offset->${offset + FETCH_LIMIT - 1}")
                try {
                    return client.userDashboard(parameter)
                } catch (e: Throwable) {
                    // TODO エラー処理
                    Log.e(TAG, "PostList fetch error: ${e.message}")
                    fetching = false
                    return emptyList()
                }
            }

            override fun onPostExecute(result: List<Post>) {
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
                listeners.forEach { it.onChanged() }
                fetching = false
                if (result.size > 0) {
                    fetchImpl(fetchSize - result.size)
                } // 取得したポストの数が0なら次回のロードはしない
            }
        }.execute()
    }

    private fun refreshUser() {
        // TODO: PostListの参照が漏れないようにこのタスクを独立クラス化する
        object : AsyncTask<Void, Void, User>() {
            override fun doInBackground(vararg args: Void): User? {
                try {
                    return client.user()
                } catch (e: Throwable) {
                    // TODO エラー処理
                    Log.e(TAG, "PostList refreshUser error: ${e.message}")
                    return null
                }
            }

            override fun onPostExecute(result: User?) {
                result?.let {
                    Log.v(TAG, "Refresh User ${result.name}")
                    user = result
                }
            }
        }.execute()
    }
}