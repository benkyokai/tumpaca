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
    private val tmpPosts: CopyOnWriteArrayList<Post> = CopyOnWriteArrayList()

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

    fun getAsync(i: Int, callback: (Post?) -> Unit) {
        val post = get(i)
        if (post != null) {
            return callback(post)
        } else {
            // 全部 UI スレッドで実行されているため、リスナーをしかける下記のコードの方が
            // onChanged() 呼び出しよりも先に呼ばれるので問題なし
            val listener = object : PostList.ChangedListener {
                override fun onChanged() {
                    if (i < posts.size) {
                        removeListeners(this)
                        callback(posts[i])
                    }
                }
            }
            addListeners(listener)
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

    /**
     * fetchSizeは目安です。fetchSize以上の件数を取得することもあります。
     */
    private fun fetch(fetchSize: Int) {
        fetchImpl(fetchSize)
    }

    private fun fetchImpl(fetchSize: Int) {
        fetching = true

        if (fetchSize <= 0) {
            tmpPosts.removeAll(tmpPosts)
            fetching = false
            return
        }

        //fetchByOffset(fetchSize)
        if (offset <= 20) {
            fetchByOffset(fetchSize)
        } else {
            fetchBySinceId(fetchSize)
        }
    }

    private fun fetchByOffset(fetchSize: Int) {
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

    private fun fetchBySinceId(fetchSize: Int) {
        val sinceId = nextId()
        Log.v(TAG, "sinceId fetching")

        object : AsyncTask<Void, Void, List<Post>>() {
            override fun doInBackground(vararg args: Void): List<Post> {
                // ここはバックグラウンドスレッド
                val parameter = hashMapOf(
                        "since_id" to sinceId,
                        "limit" to FETCH_LIMIT,
                        "reblog_info" to true,
                        "notes_info" to true)
                Log.v(TAG, "try to load since_id -> $sinceId")
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
                val filteredResult = result.filter {
                    SUPPORTED_TYPES.contains(it.type)
                }

                val r = checkResult(filteredResult)

                when (r) {
                    Result.EMPTY -> {
                        Log.v(TAG, "EMPTY")
                        tmpPosts.removeAll(tmpPosts)
                    }
                    Result.FAR_AWAY -> {
                        tmpPosts.addAll(0, filteredResult)
                        Log.v(TAG, "FAR_AWAY tmpPosts=${tmpPosts.size}")
                    }
                    Result.DUPLICATE -> {
                        Log.v(TAG, "DUPLICATE")
                    }
                    Result.GOOD -> {
                        val i = filteredResult.indexOf(posts.last())
                        tmpPosts.addAll(0, filteredResult.drop(i + 1))
                        Log.v(TAG, "GOOD tmpPosts=${tmpPosts.size}")
                    }
                }

                Log.v(TAG, "Loaded ${result.size} posts, size=$size")
                //listeners.forEach { it.onChanged() }
                fetching = false
                if (r == Result.GOOD || (r == Result.DUPLICATE && tmpPosts.isNotEmpty()) || (r == Result.EMPTY && tmpPosts.isNotEmpty())) {
                    rate = 1.0
                    posts.addAll(tmpPosts)
                    val tmpPostsCount = tmpPosts.size
                    tmpPosts.removeAll(tmpPosts)
                    listeners.forEach { it.onChanged() }
                    fetchImpl(fetchSize - tmpPostsCount)
                } else if (r == Result.DUPLICATE && tmpPosts.isEmpty()) {
                    rate = rate + 1.0
                    Log.v(TAG, "DUPLICATE and tmpPosts is empty, rate = ${rate}")
                    fetchImpl(fetchSize)
                } else if (r == Result.EMPTY && tmpPosts.isEmpty()) {
                    rate = 1.0
                    return
                } else if (r == Result.FAR_AWAY) {
                    if (fetchSize - tmpPosts.size < 0) {
                        tmpPosts.removeAll(tmpPosts)

                        if (rate > 1.0) {
                            rate = rate - 0.7
                        } else {
                            rate = rate * 0.5
                        }
                        Log.v(TAG, "FAR_AWAY fetchSize < 0, rate = ${rate}")
                        fetchImpl(fetchSize)
                    } else {
                        rate = 1.0
                        fetchImpl(fetchSize - tmpPosts.size)
                    }
                } else {
                    Log.v(TAG, "ここには来ないはず")
                    throw RuntimeException("PostList: Result is invalid. ${r}, tmpPosts.size == ${tmpPosts.size}")
                }
            }
        }.execute()
    }

    private var rate = 1.0

    private fun nextId(): Long {
        if (tmpPosts.size > 0) {
            return tmpPosts.first().id
        }

        if (posts.size <= 0) {
            return 0
        }

        val diff = ((posts.first().id - posts.last().id) / posts.size) * FETCH_LIMIT
        Log.v(TAG, "diff: ${diff}")

        val sinceId = posts.last().id - (diff * rate).toLong()

        return if (sinceId <= 0) 0 else sinceId
    }

    enum class Result {
        GOOD, FAR_AWAY, DUPLICATE, EMPTY
    }

    private fun checkResult(result: List<Post>): Result {
        if (result.isEmpty()) {
            return Result.EMPTY
        }

        val firstPost = result[0]
        if (posts.last().id > firstPost.id) {
            return Result.FAR_AWAY
        } else if (posts.last().id >= result.last().id) {
            return Result.DUPLICATE
        } else {
            return Result.GOOD
        }
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