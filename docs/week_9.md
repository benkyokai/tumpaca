# Week 9
## 内容
- デモ
    - 画面サイズに合わせて画像をロード
    - GIF再生の負荷を低減
    - Snackbar を Toast に
    - FAB を親 FAB で show/hide
- 修正内容
    - 画面サイズに合わせて画像をロード
    - GIF再生の負荷を低減
    - Snackbar を Toast に
    - FAB を親 FAB で show/hide
    - リファクタリング
- バグ
    - Like ボタンの状態が実際に表示中の post とはぐれる
- 問題点
    - avatar のロードが案外遅い
- 今週の Kotlin
    - Range のおさらい
    - lateinit
  

## デモ

## 修正内容の説明
### 画面サイズに合わせて画像をロード
- `Photo` には複数の `PhotoSize` が格納されているが、これまでは最大から 1 つ小さいのを固定でロードしていた
- 画面の px 幅より大きい最小サイズを選ぶ（これで合っているよね？dp じゃなくてもいいよね？）
- Nexus 5 (1080x1920) だとほとんどの場合最大サイズになる
- Nexus S や Nexus One (480x800) はたいてい最大より 1 つ小さいサイズ
- あまりパフォーマンスネックにはなっていなかった

### GIF再生の負荷を低減
- 実際に画面に表示されている GIF 以外をすべて止める仕組みに
- `ViewPager` で `currentItem` になるまでは `startAnimation()` しない
- でも `startAnimation()` するまでは何も描画されず、サイズが 0x0 なので可視判定ができない
- GifImageView ライブラリの現行リリースではどうしようもないが、最新の master だと `gotoFrame(int)` が最近追加された
  - アニメーションが開始されず、指定されたコマを静止画として描画するだけ
- GifImageView をフォークして submodule として取り入れた
  - Android SDK Build-tools 23.0.2 が必要なのでない場合はインストールよろしく
- ロード時に GIF のアニメーションを開始せず最初のフレームだけ表示しておく（loading GIF を含む）
- 以下のタイミングで可視判定を行い、見える GIF だけ再生する
  - `PhotoPostFragment` が `ViewPager` の `currentItem` になり実際に表示されるとき
    - `ViewPager` のプリロードを許すと実際に表示される前から `onCreate()` が呼ばれ `isVisible()` が `true` を返すので困った
    - 実際の表示／非表示のときに `Fragment.setUserVisibleHint()` が呼ばれるのでこれで状態がとれる
  - アプリがフォアグラウンドに戻ったとき (`onResume()`)
- 以下のタイミングで可視判定を行い、見えない GIF を停止する
  - `PhotoPostFragment` が `ViewPager`
  - `PhotoPostFragment` のルート `ScrollView` のスクロール位置が変わったとき (`OnScrollChangeListener`)
  - GIF が格納される `LinearLayout` のレイアウトが変わったとき (`OnLayoutChangeListener`)
    - 最初のフレームが描画されるまでは各 `ImageView` のサイズは 0x0 なので描画によって見えていたものが見えなくなる可能性がある
  - アプリがバックグラウンドに行くとき (`onPause()`)
- 効果抜群！CPU 負荷がぐんと下がった
  - Nexus 5 だと通常サイズの GIF は最大 3~4 枚しか同時再生されることがなく、それでも CPU 使用率は 50% 前後

### Snackbar を Toast に
- 特に最初の 300 個の post をロードしている最中、 like や reblog をしても反応が遅く、次の post に行ってから結果の Snackbar が操作を邪魔してくる
- Toast に変更して解決
- API はほぼ一緒なので楽

### FAB を親 FAB で show/hide
- FAB が 3 つに増えて邪魔なので、1 つだけの親 FAB を置いてその”中”に従来の FAB を”格納”することに
- 初期状態では「＋」の大きい FAB だけがあって、これをタップするとほかのミニ FAB が上に現れる
- チュートリアルどおりのアニメーションなので調整してカッコよくしたい
- これでも FAB の使い方としてかなり間違っているが、いい方法が思いつかない

### リファクタリング
- もともと object リテラルを書かずに済むように作った `AsyncTaskHelper` が abstract class になってしまい存在価値がゼロに
  - まるまる削除して標準の `AsyncTask` に
- 各種 `PostFragment` に共通パーツのセットアップを行うコードが重複していたので `PostFragment` に集約
- キャッシュクラスの型パラメータを固定した
  - 例
  ```
  class BitmapCache<T : Bitmap> : Cache<T> {
  ```
  を以下に変更
  ```
  class BitmapCache() : Cache<Bitmap> {
  ```
  - Suppress していた警告を suppress しなくて済む
- `lateinit`（後述）

## バグ
### Like ボタンの状態が実際に表示中の post とはぐれる
- Like してすぐ移動すると、応答が来るときには別の post を見ているのに like ボタンのアイコンが反転する
- Like した post が現在の post であることをチェックすることで解決

## 問題点
### avatar のロードが案外遅い
- ロード済みの avatar でも早送りで post を繰ると出てくるのにだいぶ時間がかかる
- キャッシュのプリロードが重すぎる？Async タスクが飛びすぎる？
- `PostFragment` が破棄されるときに実行中の `AsyncTask` をキャンセルすれば早くなるかもと思ったが、あまり効果はなかった
  - feature/aaron/cancel-async ブランチに実装が残してある

## 今週の Kotlin
### Range のおさらい
- こんなコードが…
  ```
  Array<Int>(urls.size, { i -> i }).map { ... urls[it] ... }
  ```
  インデックスは実際使っているので `urls.map { ... }` がいいわけではない
- この方がすっきりするよね？
  ```
  (0..urls.size).map { ... } // 0.rangeTo(urls.size)のシンタックスシュガー
  ```
- `Int.rangeTo(Int)` は最大値が含まれるからすっきりしない！！！残念！！！
- この方が正しくてすっきり
  ```
  (0 until urls.size).map { ... }
  ```
  （最大値が含まれない）
- Python の方がすごくすっきり
  ```
  [ ... for i, url in enumerate(urls)]
  ```

### [lateinit](https://kotlinlang.org/docs/reference/properties.html#late-initialized-properties)
- 非ヌルプロパティは本当はコンストラクタで初期化しないといけないが、それが不便なケースが多い（特に Android では）
- その時のために `lateinit` という修飾子がある
  ```
  class MyFragment : FragmentBase() {
    lateinit var mHoge: Hoge

    override fun onCreate(savedInstanceState: Bundle?) {
        mHoge = bundle.getInt("hoge")
        mHoge.fuga() // ← !! しなくても大丈夫
    }
  ```
- 初期化される前に参照されてしまうと例外が投げられる
- `TPRuntime.tumblrService` は必ず初期化されているのに毎回 `!!` しているのでちょうどいいユースケース
