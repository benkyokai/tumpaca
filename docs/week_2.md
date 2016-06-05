# Week 2
## 内容
- Tumblr Application の登録
- OAuth による認証
- 認証情報の永続化
- Tumblr API による dashboard 表示

## Tumblr Application の登録
- [Tumblr Applications ページ](https://www.tumblr.com/oauth/apps)で Tumpaca for Android として登録し、 OAuth Consumer Key と OAuth Secret Key を取得
- ユーザーは個人メールアドレスで登録したもの。あとからでも変更可
  - TODO: 展開方法
- キーの保存方法
  - 絶対漏洩しない方法はサーバーを立ててすべてのクエリーはサーバーを介して行う → スコープ外
  - 何らかの形で APK に入れなければならないが、せめて git に入れないようにしたい
  - ⇒ `local.properties` に書いて、 Gradle タスクで Base64 で難読化して `.properties` 形式の raw リソースを生成
    - キーは `tumblr.consumer.key`, `tumblr.consumer.secret`
    - `preBuild` タスクの最後に実行されるので、一度ビルドするまでは「リソースが存在しない」エラーが出る
  - TODO: これでいいのか要議論

## OAuth による認証
- Tumblr の OAuth は OAuth 2 ではなく OAuth 1.0a で面倒だが…
- [Loglr](https://github.com/dakshsrivastava/Loglr) という Android 用 Tumblr 認証ライブラリがあった（！）
- コードを確認したが、かなり当たり前のことを当たり前にやっているだけ
- これで簡単に認証フローを実装し、ユーザーの `oauthToken` と `oauthTokenSecret` を取得
- 一連の認証操作は `AuthActivity` で行い、結果を他 Activity に渡す
  - Activity 間の遷移は Intent で行う
  ```
  val intent = Intent(this, NextActivity::class.java)
  startActivity(intent)
  ```
  - Activity 間のデータの受け渡しは Intent に任意のキーで extra を設定して行う
  ```
  // 遷移元
  intent.putExtra("mydata", data)
  // 遷移先
  intent.getStringExtra("mydata")
  // （本当は getIntent() だが Kotlin の syntax sugar のおかげで intent でアクセスできる）
  ```
- 認証情報のカプセル化＆受け渡しのため、先週紹介された `Parcelable` を実装する `Credentials` クラスを導入
  - 別に普通の話だが、 Kotlin だと以外とトリッキー（後述）

## 認証情報の永続化
- OAuth トークンを一度取得したら、次回から再利用できるように永続化している
- 簡単に Base64 で難読化して [`SharedPreferences`](https://developer.android.com/reference/android/content/SharedPreferences.html) という標準の key=value ストレージに保存 (`MODE_PRIVATE`)
  - `SharedPreferences` に値を書き込む際は以下のように `SharedPreferences.Editor` インスタンスを取得して、それに対して値を設定してから `commit()` を呼び出す必要がある
  ```
  val editor = getPreferences(MODE_PRIVATE).edit()
  editor.putString("key", "value")
  editor.commit()
  ```
  - `commit()` を忘れると困るし `Closeable` みたいな匂いがするので try-with-resources っぽく Extension でラップしてみた（後述）
  ```
  editPreferences {
    it.putString("key", "value")
  } // 自動的にコミットされる
  ```

- 永続化されたトークンがあったら、トークン取得を飛ばして直接 dashboard ビューに行く
- 永続化トークンが失効、リボークされた場合などは未対応

## Tumblr API による dashboard 表示
- クライアントキーと OAuth トークンを [Jumblr](https://github.com/tumblr/jumblr) に渡して Tumblr API を叩いて dashboard 項目を取得
  - Activity
- 各項目の slug を簡単にリスト表示
- Android ではネットワーク通信をメインスレッドで行うと例外が発生するので、 `AsyncTask` を使って非同期で実行するのが普通らしい
  - Java Swing でいう `SwingWorker` とほぼ一緒
  - 簡単な使い方だと独立したクラスを作るまでもないし、匿名オブジェクトはなんだか Kotlin っぽくない
  - Kotlin っぽくラムダを渡して使えるラッパーを作成
    ```
    AsyncTaskHelper.first<...> { /* バックグラウンド処理 */ }
        .then { /* メインスレッドでの後処理 */ }
        .go() // 実行開始
    ```

## Weekly Kotlin 1: ラムダの暗黙引数名
- 関数リテラル（ラムダ）の引数は明示的に宣言できる
  ```
  action.setCallback { result -> doStuff(result) }
  ```
- が、その必要がなく、宣言しない場合は [`it` として暗黙的に宣言される](https://kotlinlang.org/docs/reference/lambdas.html#lambda-expression-syntax)
  ```
  action.setCallback { doStuff(it) }
  ```
- Groovy を踏襲？

## Weekly Kotlin 2: `Closeable.use`
- Java 7 でいう [try-with-resources に変わって使える](https://kotlinlang.org/docs/reference/idioms.html#java-7s-try-with-resources)
- `Closeable` を継承するすべてのオブジェクトに [Extension](https://kotlinlang.org/docs/reference/extensions.html) として `use` というメソッドが定義されている
- `use` はラムダを受け取り、ラムダの実行後に `Closeable` オブジェクトを自動的に閉じてくれる
- `Closeable` オブジェクトがラムダに引数として渡される

例

```
openFileInput(myFile).use { props.load(it) }
```

意味

```
var inputStream: FileInputStream?
var closed = false
try {
    inputStream = openFileInput(myFile)
    props.load(inputStream)
} catch (e: Exception) {
    closed = true
    try {
        inputStream?.close()
    } catch (closeException: Exception) {
        // 無視
    }
    throw e
} finally {
    if (!closed) {
        inputStream?.close()
    }
}
```

## Weekly Kotlin 3: [Extensions](https://kotlinlang.org/docs/reference/extensions.html)
- 既存のクラスを拡張して新規メソッドを定義できる
```
fun Int.isOdd(): Boolean {
    return this % 2 != 0
}

1.isOdd() // true
2.isOdd() // false
```
- `SharedPreferences.Editor` のラッパは `Activity` 上の Extension で実装した
```
fun Activity.editPreferences(mode: Int = Context.MODE_PRIVATE, actions: (SharedPreferences.Editor) -> Unit) {
    val prefs = getPreferences(mode).edit()
    actions(prefs)
    val written = prefs.commit()
    assert(written)
}
```

## Weekly Kotlin 4: Companion オブジェクト
- Kotlin には Java の `static` キーワードに相当するものがなく、その概念すら（ほぼ）ない
- インスタンスではなくクラスにメソッドやプロパティを定義したい場合、クラスに「[companion object](https://kotlinlang.org/docs/reference/object-declarations.html#companion-objects)」を定義してそのオブジェクト上に定義する
- すると `static` みたいに `<クラス名>.なんとか` でアクセスできる
- 結果は Java でいう `static` に近いが、いろいろと微妙に違う（companion は別クラスだったりインタフェースを実装したりできる）

例

```
class MyClass {
    companion object Factory {
        fun create(): MyClass = MyClass()
    }
}

var instance = MyClass.create()
```

- Java 互換のため、 `@JvmField` アノテーションで実際の `static` メンバーなどが定義可能
- Android には `Parcelable` など「ほげほげインタフェースを実装するふがふがという静的メンバーが必要」とされるクラスがあって、 Kotlin ではこうしてやっと実装できる（`Credentials` クラスを参照）

```
companion object {
    @JvmField
    val CREATOR = object: Parcelable.Creator<Hoge> {

        override fun createFromParcel(parcel: Parcel): Hoge {
            // Hoge を parcel から復元
        }

        override fun newArray(size: Int): Array<Credentials?> {
            // 新規配列を生成して返す？？？
        }
    }
}
```


## Weekly Kotlin 5: 単純な表記違い
- [Qualified `this`](https://kotlinlang.org/docs/reference/this-expressions.html#qualified)
  - 匿名オブジェクトなどの中で、外のオブジェクトを `this` で指したいけど匿名オブジェクトになってしまう
  - Java だと `<クラス名>.this`
  - Kotlin だと `this@<クラス名>`
- [クラスオブジェクトの参照](https://kotlinlang.org/docs/reference/reflection.html#class-references)
  - Java だと `<クラス名>.class`
  - Kotlin だと `<クラス名>::class` (Kotlin 独自の `KClass`) または `<クラス名>::class.java` (Java クラスオブジェクト)
