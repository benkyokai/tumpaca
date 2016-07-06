# Week Five
## 内容
- デモ
- お勉強
  - StateListDrawable
  - Material Design コンポーネント
  - RTL言語のレイアウトサポート
- 実装説明
  - Like と Reblog
  - FAB とそのレイアウト
- イラッときた問題の紹介
- 今週の Kotlin

## デモ
- jumblr 関連のレポジトリ変更
- Like と Reblog
- post 表示のスクロール（横画面など）

## お勉強
### [StateListDrawable](https://developer.android.com/reference/android/graphics/drawable/StateListDrawable.html)
- 複数の PNG などの drawable 実体をまとめて 1 つの drawable として扱う
  例： `button_normal.png` + `button_active.png` → `R.drawable.button`
- XML で記述
  ```
  <selector xmlns:android="http://schemas.android.com/apk/res/android">
      <item android:drawable="@drawable/button_active" android:state_active="true"/>
      <item android:drawable="@drawable/button_normal"/>
  </selector>
  ```
- [`setImageState(int[], boolean)`](https://developer.android.com/reference/android/widget/ImageView.html#setImageState(int[],%20boolean))
  でステートを切り替える
  ```
  val flags = intArrayOf(android.R.attr.state_active, -android.R.attr.state_checked)
  button.setImageState(flags, false)
  ```
  - 正の値は ON、負の値は OFF
  - boolean の引数は既存のステートとマージするかどうかを表す

### Material Design コンポーネント

#### [FloatingActionButton](https://developer.android.com/reference/android/support/design/widget/FloatingActionButton.html)
- Material Design で導入されたお馴染みのアクションボタン
- アイコンに StateListDrawable が使用可能
- 以下「FAB」と略す

#### [Snackbar](https://developer.android.com/reference/android/support/design/widget/Snackbar.html)
- Toast に代わる、画面下からぽこっと現れてくるコンポーネント
- Toast と違って通知だけでなく、アクションを起こすボタンも搭載可能（直近の操作の undo など）

#### [CoordinatorLayout](https://developer.android.com/reference/android/support/design/widget/CoordinatorLayout.html)
- Material Design の定番コンポーネントを操る view（「super-powered FrameLayout」だそうだ）
  - FrameLayout は 1 だけの view を格納するためのコンテナ
- [Behavior](https://developer.android.com/reference/android/support/design/widget/CoordinatorLayout.Behavior.html)
  クラスでチャイルド view の動きを動的に制御できる
- 一部のコンポーネントはデフォルトで Behavior を持っている
  例：FAB を直に格納すると、 Snackbar の出現に応じて FAB の位置を自動的に調整する

### RTL言語のサポート
- 〈左から右〉ではなく、〈右から左〉へ流れる自然言語がある（アラビア語など）
- レイアウトを定義するとき「左」と「右」の概念を「起点」と「終点」に置き換えれば、あとは裏でいろいろと自動的に調整できる
- Android 4.2 には [RTL レイアウトの対応](https://developer.android.com/about/versions/android-4.2.html#RTL)
  を助ける APIの工夫が追加された
- 基本的に XML レイアウトの属性名の「left」、「right」を「start」、「end」に置き換える
  例：`android:layout_marginLeft` → `android:layout_marginStart`

## 実装説明
### Like と Reblog
- FAB のアイコンは [Material icons](https://design.google.com/icons/) から取得
  - Like: `favorite`, `favorite border`
  - Reblog: `repeat`
- jumblr では、 API 呼び出しは JumblrClient オブジェクトからでも実体オブジェクト（Post など)からでも実行可能
  例：`client.like(post.id, post.reblogKey)` or `post.like()`
#### Like
- Post オブジェクトは `isLiked` フラグを持っているけれど、setter がなく get しかできない
- `post.like()` をしても値が変わらない
- API 呼び出しで Post をとり直すともったいないので `post.like()` で true に設定することにした
- こういうのがいろいろ出てきそうなので jumblr は好きに改造するのもありかと
- like 済みの項目は FAB のアイコンが塗りつぶされ、like/unlike 時にアイコンが反転する
#### Reblog
- Tumblr では一人のユーザーは複数の blog に投稿できる場合があるので、reblog するときは投稿先のブログ名を指定する必要がある
  例：`post.reblog(blogName)`
- ユーザーのブログリストは `client.user().blogs()` で取得可能
- ⇒ User オブジェクトが必要なので `onResume` で dashboard の取得と並列でとっておく
- オプションでコメントをつけて reblog できるので、 FAB を押すと入力ダイアログが表示される
  - 空白のまま OK → コメントなし

### FAB とそのレイアウト
- 1 つだけの FAB は plug-and-play な感じで非常に簡単だが、2 つだとレイアウトが非常に厄介
- 常識的に RelativeLayout にネストすると、 FAB と CoordinatorLayout の連携ができない（Snackbar の出現に反応しない）
- 安直に margin を指定して配置すると下の FAB だけ Snackbar に反応する（上のは重ならないので動かなくてもいいらしい）
- `app:layout_anchor` で FAB を別のコンポーネントにアンカーできるが、コンポーネントの縁にまたがるデザインになっていて、意図どおりのレイアウトにならない
  - margin でごまかすと、動くときに崩れる → [StackOverflow のだめな回答例](http://stackoverflow.com/a/32026915/448068)
- ダミーの spacer view を挟んで 上FAB → spacer → 下FAB とアンカーをつないでやっと正しい挙動に
  - [正解につながった StackOverflow 回答](http://stackoverflow.com/a/31523517/448068)

## イラッときた問題の紹介
- 上記の FAB レイアウト問題
- Tumblr API における blogName の件は理解するのにだいぶ時間がかかった
- Gradle の `settings.gradle` はオプションで、サブプロジェクトにあってもなくても大丈夫なはずなのに、サブプロジェクトにあると
  Gradle 自身は問題ないけど Android Studio は混乱してサブプロジェクトを意識しなくなる
  - jumblr の `settings.gradle` を削除して解決
- Android Studio の JDK によっていろいろ左右される
  - Android Studio 自体が使う JDK は `Android Studio.app/Contents/Info.plist` で設定可能
  - 家では 1.8 だけど会社では 1.7
  - `.idea` 配下のいろんな設定ファイルが毎回書き換わる
  - jumblr サブプロジェクトに Java バージョンを指定しないと、デフォルトの JDK でコンパイルされる → 家では 1.8 でエラー発生
    - `targetCompatibility = '1.6'` で解決
  - `.idea` などのファイルはそもそもコミットするべき？一般にはコミットするらしいけどめんどい

## 今週の Kotlin
### Method Cascade もどき
- Kotlin では、 ? 型のメンバーにオブジェクトを代入しても、その後また null が代入されるので毎回 unwrap しないといけない
  ```
  view = inflater.inflate(...) as View
  view!!.setFoo()
  view!!.setBar()
  ```
- Method Cascade とは、オブジェクト生成直後などの設定を簡潔に記述する手法。
  [Dart](http://news.dartlang.org/2012/02/method-cascades-in-dart-posted-by-gilad.html) の例：
  ```
  // 通常： add() は自身を返さないので fluent に呼び出せない
  myTokenTable.add("aToken");
  myTokenTable.add("anotherToken");
  myTokenTable.add("theUmpteenthToken");

  // Cascade： `..` 演算子はメソッドの返り値を捨てて元のレシーバーを返す
  myTokenTable
    ..add("aToken");
    ..add("anotherToken");
    ..add("theUmpteenthToken");
  ```
- Swift に似たようなものを追加する [proposal](https://gist.github.com/erica/6794d48d917e2084d6ed) が出ている（未採用）
- Kotlin でこうして Method Cascade もどきを使うとちょっと綺麗になる（？）
  ```
  (inflater.inflate(...) as View).let {
    view = it
    it.setFoo()
    it.setBar()
  }
  ```

### 配列の参照は [] で
- `myList.get(i)` はもちろんできるけど、Android Studio が `myList[i]` にしてね、と警告する
