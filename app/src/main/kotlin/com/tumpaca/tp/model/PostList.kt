package com.tumpaca.tp.model

import android.os.AsyncTask
import android.util.Log
import com.tumblr.jumblr.JumblrClient
import com.tumblr.jumblr.types.Post
import com.tumblr.jumblr.types.User
import com.tumpaca.tp.util.lastNonAdId
import java.util.concurrent.CopyOnWriteArrayList

class PostList(private val client: JumblrClient) {
    companion object {
        private const val TAG = "PostList"
        private const val FIRST_FETCH_UNIT = 250
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
        get() = filteredPosts.size

    // CHAT, ANSWER, POSTCARDは対応していないので、postから除く
    private val SUPPORTED_TYPES = setOf(
            Post.PostType.AUDIO,
            Post.PostType.LINK,
            Post.PostType.PHOTO,
            Post.PostType.QUOTE,
            Post.PostType.TEXT,
            Post.PostType.VIDEO)

    private val SUPPORTED_TYPES_WITHOUT_PHOTO = setOf(
            Post.PostType.LINK,
            Post.PostType.QUOTE,
            Post.PostType.TEXT
    )

    // SUPPORTED_TYPES で列挙されたタイプでフィルタリングされたポストリスト
    // バックグラウンドスレッドからもアクセスするのでスレッドセーフリストを使う必要あり。
    private val posts: CopyOnWriteArrayList<Post> = CopyOnWriteArrayList()
    private val filteredPosts: CopyOnWriteArrayList<Post> = CopyOnWriteArrayList()
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

        if (i < filteredPosts.size) {
            return filteredPosts[i]
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
                    if (i < filteredPosts.size) {
                        removeListeners(this)
                        callback(filteredPosts[i])
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

        val remain = filteredPosts.size - (i + 1)
        return remain < FETCH_MIN_POST_NUM // 最小よりも小さかったら fetch が必要
    }

    /**
     * fetchSizeは目安です。fetchSize以上の件数を取得することもあります。
     */
    private fun fetch(fetchSize: Int) {
        fetchImpl(fetchSize)
    }

    private fun fetchImpl(fetchSize: Int, retryCount: Int = 20) {
        fetching = true

        if (retryCount <= 0) {
            Log.v(TAG, "retryCount is 0")
            return;
        }

        if (fetchSize <= 0) {
            Log.v(TAG, "fetchSize: ${fetchSize}")
            tmpPosts.clear()
            fetching = false
            return
        }

        if (offset <= 200) {
            fetchByOffset(fetchSize)
        } else {
            fetchBySinceId(fetchSize, retryCount)
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

                posts.addAll(result)
                filteredPosts.addAll(filterPosts(result))
                Log.v(TAG, "Loaded ${result.size} posts, size=$size")
                maybeInsertAd()
                listeners.forEach { it.onChanged() }
                fetching = false
                if (result.isNotEmpty()) {
                    fetchImpl(fetchSize - result.size)
                } // 取得したポストの数が0なら次回のロードはしない
            }
        }.execute()
    }

    /**
     * サポート対象外のポストを除外
     * また、設定で「自分のポストを表示しない」にしている場合は、
     * 自分のポストを除外する
     */
    private fun filterPosts(posts: List<Post>): List<Post> {
        // 自分のポストを表示するかどうか
        val posts1 =
                if (TPRuntime.settings.excludeMyPosts) {
                    posts.filter {
                        // 自分のブログ名一覧にポストのブログ名が含まれていれば除外する
                        val blogs = TPRuntime.tumblrService.user?.blogs?.map { it.name }
                        blogs?.contains(it.blogName)?.not() ?: true
                    }
                } else {
                    posts
                }

        // 写真ポストを除外するかどうか
        val posts2 =
                if (TPRuntime.settings.excludePhoto) {
                    posts1.filter { SUPPORTED_TYPES_WITHOUT_PHOTO.contains(it.type) }
                } else {
                    posts1.filter { SUPPORTED_TYPES.contains(it.type) }
                }

        return posts2
    }

    /**
     * SinceIdによるダッシュボード取得
     * Offsetによるポストの取得は260件目までしか対応していないため、
     * 260件以上遡るためにはSinceIdから取ってくるしかない
     */
    private fun fetchBySinceId(fetchSize: Int, retryCount: Int) {
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
                // 取得結果の状態をチェック
                val r = checkResult(result)

                when (r) {
                    Result.EMPTY -> {
                        Log.v(TAG, "EMPTY")
                        rate = 1.0
                        return
                    }
                    Result.FAR_AWAY -> {
                        // すべてtmpPostsに追加
                        tmpPosts.addAll(0, result)
                        Log.v(TAG, "FAR_AWAY tmpPosts=${tmpPosts.size}")
                    }
                    Result.DUPLICATE -> {
                        Log.v(TAG, "DUPLICATE tmpPosts=${tmpPosts.size}")
                    }
                    Result.GOOD -> {
                        // 重なっている分を除外して、残りをtmpPostsに追加
                        val i = result.indexOf(posts.last())
                        tmpPosts.addAll(0, result.drop(i + 1))
                        Log.v(TAG, "GOOD tmpPosts=${tmpPosts.size}")
                    }
                }

                Log.v(TAG, "Loaded ${result.size} posts, size=$size")
                fetching = false

                /**
                 * 結果がGOOD, もしくはDUPLICATEかつtmpPostsが空ではない、ときは
                 * postsに追加すべきポストがtmpPostsに入っているはずなので成功
                 */
                if (r == Result.GOOD || (r == Result.DUPLICATE && tmpPosts.isNotEmpty())) {
                    rate = 1.0
                    // tmpPostsからpostsの重複分を除く
                    val index = tmpPosts.map { it.id }.indexOf(posts.lastNonAdId()!!)
                    Log.v(TAG, "duplicate index: $index")
                    val postsExcludeDuplicate = tmpPosts.drop(index + 1)

                    // postsに追加
                    posts.addAll(postsExcludeDuplicate)
                    filteredPosts.addAll(filterPosts(postsExcludeDuplicate))
                    maybeInsertAd()
                    val tmpPostsCount = postsExcludeDuplicate.size

                    // ここで一旦成功なので、tmpPostsを空にして、再度最初からフェッチする
                    tmpPosts.clear()
                    listeners.forEach { it.onChanged() }
                    fetchImpl(fetchSize - tmpPostsCount)
                } else if (r == Result.DUPLICATE && tmpPosts.isEmpty()) {
                    rate = rate * 2.0
                    Log.v(TAG, "DUPLICATE and tmpPosts is empty, rate = ${rate}")
                    fetchImpl(fetchSize, retryCount - 1)
                } else if (r == Result.FAR_AWAY) {
                    rate = 1.0
                    fetchImpl(fetchSize, retryCount - 1)
                } else {
                    Log.v(TAG, "ここには来ないはず")
                    throw RuntimeException("PostList: Result is invalid. ${r}, tmpPosts.size == ${tmpPosts.size}")
                }
            }
        }.execute()
    }

    private fun maybeInsertAd(): Unit {
        if (!TPRuntime.settings.showAdPosts) {
            return
        }
        Log.d(TAG, "AMK filteredPosts: ${filteredPosts.size}, last ad: ${filteredPosts.indexOfLast { it is AdPost }}")
        if (filteredPosts.size - filteredPosts.indexOfLast { it is AdPost } >= 20) {
            Log.d(TAG, "AMK inserted ad at ${filteredPosts.size}")
            val adPost = AdPost()
            posts.add(adPost)
            filteredPosts.add(adPost)
        }
        // テスト用に先頭に近いところにも挟む
        if (filteredPosts.get(2) !is AdPost) {
            val adPost = AdPost()
            filteredPosts.add(2, adPost)
            posts.add(2, adPost)
        }
    }

    private var rate = 1.0

    /**
     * 次に指定するSinceIdを取得
     */
    private fun nextId(): Long {
        // tmpPostsが空でなければ、tmpPostsの先頭からもう一度ダッシュボードを取得
        if (tmpPosts.isNotEmpty()) {
            return tmpPosts.first().id
        }

        // 取得済みポストリストが空（ありえないはず）
        if (posts.isEmpty()) {
            return 0
        }

        /**
         * 取得済みポストの先頭と最後のIDの差をポストの数で割った値 = ポストと次のポストのIDの差の平均
         * この平均に取得したいポスト数を掛ければ、どれだけ遡ればいいかが分かる
         */
        val diff = ((posts.first().id - posts.lastNonAdId()!!) / posts.size) * FETCH_LIMIT
        Log.v(TAG, "diff: $diff")

        /**
         * うまくダッシュボードが取得できないときに、
         * 上で求めた値にrate分の補正をかける
         * もっと遡りたければrate > 1にし、遡りを抑制したければ、 0 < rate < 1 にする
         */
        val sinceId = posts.lastNonAdId()!! - (diff * rate).toLong()

        return if (sinceId <= 0) 0 else sinceId
    }

    enum class Result {
        GOOD, FAR_AWAY, DUPLICATE, EMPTY
    }

    /**
     * SinceIdによるダッシュボードの取得結果の状態をチェック
     * EMPTY: 取得結果が空（ありえないはず）
     * FAR_AWAY: 取得ポストリストの先頭が取得済みポストリストの最後よりも前のポスト（間にまだポストがあるかもしれない）
     * DUPLICATE: 取得ポストリストがすべて取得済みポストリストに含まれている
     * GOOD: 取得ポストリストの一部が取得済みポストリストに含まれている
     */
    private fun checkResult(result: List<Post>): Result {
        if (result.isEmpty()) {
            return Result.EMPTY
        }

        val firstPost = result[0]
        if (posts.lastNonAdId()!! > firstPost.id) {
            return Result.FAR_AWAY
        } else if (result.lastNonAdId()!! >= posts.lastNonAdId()!!) {
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