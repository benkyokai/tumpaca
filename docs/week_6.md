# Week Six
## 内容
- レイアウトファイル修正
- 簡易的なキャッシュを実装
- MainApplicationとTPRuntime
- 今週のkotlin

### レイアウト修正
- Fragmentとレイアウトをポストタイプごとに分けた
    - 但し、CHAT, ANSWER, POSTCARDタイプは個人的に見たことないし、ほぼ使われていないだろうということで、レイアウトを用意していない
- 写真がちゃんと大きく表示されるようにした
    - 前回まで画像が小さかった原因
    - ダウンロードしてきたbitmapのdensityに端末のdpiに対応した[DisplayMetrics](https://developer.android.com/reference/android/util/DisplayMetrics.html)が自動で割り当てられる
    - Nexus5Xの場合はDENSITY_420 (2.2625倍) [参照](http://qiita.com/nein37/items/21cf0e98046a0267b158)
        - つまり2.2625分の1のサイズで表示されてた？
    - bitmapのdensityを1倍 (DENSITY_MEDIUM) にセットし直すと大きく表示された
- 写真の上下に空白ができる
    - 写真が大きく表示されるようになったと思ったら、今度は上下に謎の空白ができる
    - APIを眺めてると[adjustViewBounds](https://developer.android.com/reference/android/widget/ImageView.html#attr_android:adjustViewBounds)がそれっぽかったので、trueをセットしてみると空白がなくなった
    - adjustViewBounds=trueはimageViewの境界を画像の縦横比を維持したままぴったりになるように調整するフラグ

### 簡易的なキャッシュを実装
- DownloadImageTaskにキャッシュを実装
- Cache Interfaceとその実装TPCacheを作った
- TPCache<T>
    - HashMapに保存するだけ
    - getIfNoneAndSet(key: String, f: () -> T?): T?
        - keyで取得しようとするが、もしキャッシュに存在しなければf()の結果をキャッシュにセットする
        - get() -> なければ別の方法で取得 -> set()をひとまとめにしている


### MainApplicationとTPRuntime
- 今回キャッシュを生成し、保持しておくためにTPRuntimeというシングルトンを作成した
- 当初はMainApplicationにもたせて (TumblrServiceと同じように) 、DownloadImageTaskのコンストラクタにキャッシュを渡していた
- ただ、DownloadImageTaskを呼ぶたびに DownloadImageTask(imageView, cache)とやるのもめんどくさい
- シングルトンのTPRuntimeを作りcacheをそこの持たせ、DownloadImageTask内でTPRuntime.cacheとすることで、キャッシュを利用できるようにした
- アプリケーション全体で使いたいインスタンスは、MainApplicationに持たせたほうがいいのか、TPRuntimeのようなシングルトンに持たせたほうがいいのかよくわからない

### 今週のkotlin
- Android StudioのInspect Codeを使ってみた
- 結果42件の指摘
- Android Lint for Kotlin (14件)
    - MainActivityの27行目で"transaction開始したらcommitして完了しろ"と指摘されているが、実際はcommitしているので、analyzernのバグ？
    - Extensions.ktでeditor.commit()は同期処理なので、呼び出し側で注意が必要 (UIスレッドを止めてしまわないかとか) という指摘。
        - 非同期でもいいならapply()を利用すべき
        - 認証トークンの書込で使われていて、同期処理すべきなのでここはそのまま？
    - The resource @id/title is marked as private in com.android.support:appcompat-v7 (12件)
        - よくわからない。無視してよさそう。
- General (12件)
    - デフォルトのファイルテンプレートが使われているよという指摘
    - 無視
- Kotlin (16件)
    - TumblrService.ktの38行目
        - loglrは[platform type](https://kotlinlang.org/docs/reference/java-interop.html#null-safety-and-platform-types)なので、型を明示しておくべき。という指摘
        - Java宣言の型は、kotlinではplatform typeと呼ばれる型として扱われ、nullチェックが緩和される
        ```
        val list = ArrayList<String>()
        list.add("Item")
        val size = list.size()
        val item = list[0] // プラットフォーム型
        item.substring(1) // 許可される。例外が投げられる可能性がある
        val nullable: String? = item // 許可されるし、常に動作する
        val notNull: String = item   // 許可されるけど、実行時に失敗するかもしれない。コンパイラが代入前にassertionを挿入。
        ```
        - ここではLoglr.getInstance()はnon-nullのはず
    - Credentials.ktの26行目
        - arrayOfNulls<Credentials>(size)の型引数はいらない
        - あってもいいと思うので残しておく
    - 使われていないImport文
        - 削除
    - 使われていない変数等 (13件)
        - 消したり、消さなかったり