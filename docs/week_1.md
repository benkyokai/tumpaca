# Week One
## 内容
- ビルド高速化
- AndroidManifest.xml
- Activity まわり
- 今週の Kotlin

## ビルド高速化
ビルドを速くしました。([コミット](https://github.com/benkyokai/tumpaca/commit/b219b7145c5e9c50bdf906a10d949518f64b01c9))

- build.grade の minSdkVersion を 21 以上の Flavor を作成。
  - 本当は MultiDex の場合に速くなる
  - 今回は該当しないかと思ったのに速くなった
  - と思ったけど速くなってなかった（速くなったようにみえたのは preDex のおかげ？）
- 速くなりそうなオプションを ON にした。
  - dexProcess: dx を別プロセスで実行。実験機能
  - javaMaxHeapSize: dx 実行時の最大ヒープサイズ
  - preDexLibraries: pre-dex を作成するかどうか。初回ビルド・クリーンは遅くなる代わりに通常時速くなる。
  - ~~incremental: インクリメンタル dx。ただし、実験機能なので動かないかも。~~ => 2.1 で廃止されてた…。
    - 利用している Android Gradle Plugin ver. は build.gradle に書いてある
    - _ref: [DexOptions - Android Plugin 2.1.0 DSL Reference](https://google.github.io/android-gradle-dsl/current/com.android.build.gradle.internal.dsl.DexOptions.html)_
- Gradle 実行時の利用可能メモリ量を大きくした。
  - ``-XX:MaxPermSize=256`` ⇒ ``-XX:MaxPermSize=512m``

## AndroidManifest.xml
- アプリに関する重要な情報を Android OS に提供します
  - パッケージ名
  - 外部に公開するコンポーネント
  - 要求パーミッション
  - 動作 API Level
  - などなど、、、
  - _ref: [アプリマニフェスト](https://developer.android.com/guide/topics/manifest/manifest-intro.html)_
- Tumpaca では、、、
  - ランチャーが起動する Activity を指定。これによってランチャーから起動できるようになる。

## Activity
- ≒ ViewController
  - より ViewController に近い Fragment というものがある。
  - Activity 単体として起動できるでの Application 的な要素もある。
  - アプリケーションとしてのライフサイクルに対応するものに Application クラスがある
- 階層
  - Application（≠ プロセス）
  - Activity
  - Fragment
  - View
- やってること
  - View を作ってモデルとバインド
- View の作り方
  - プログラムで View インスタンスを作成してルートビューにつなげる
  - layout ファイルをパースしてルートビューにつなげる（inflate）

## layout ファイル


- 画面構成を xml で記述
  - プログラムが xml をパースして View 階層を構築（inflate）
- 使った要素
  - LinearLayout: 縦横に重複しないように要素を並べる。（CSS っぽい）
  - EditText: 編集できる Input Text
  - Button: ボタン



## Activity のライフサイクル
代表的なライフサイクルコールバック

- onCreate(): 作られた時に呼ばれます
- onStart(): 画面に表示されるときに呼ばれる
- onResume(): 前面になるときに呼ばれる
- onPause(): バックグラウンドに移動する時によばれる
- onStop(): 画面から消えるときによばれる
- onDestroy(): 終了するときに呼ばれる

参考：[ライフサイクル画像](https://developer.android.com/images/activity_lifecycle.png)

## 対応関係
- onCreate() と onDestroy(): 全体のライフサイクル
- onStart() と onStop(): 可視ライフタイムサイクル
- onResume() と onPause(): フォアグラウンドライフタイムサイクル

## Activity 上のデータの保持

- Android の Activity は破棄されやすい
  - 画面を横にすると破棄される ⇒ メモリ上にデータを保持しておけない
  - DEBUG 用に「Activity を保持しない」モードがある
- コールバックが呼ばれるのでメモリ上のデータを保存する
  - onSaveInstanceState(Bundle outState): outState に情報を保存
  - onCreate(Bundle savedInstanceState): savedInstanceState から情報を復元

## Weekly Kotlin 1：Elvis Operator
構文

```
String title = authorName ?: "UNKNOWN AUTHOR"
```

意味

```
String title = (authorName != null) ? authorName : "UNKNOWN AUTHOR"
```

- C# の Null 合体演算子と同じ。
- 構文的には元々 Groovy。
- なぜ Elvis? ⇒  [回答](http://stackoverflow.com/a/1993455)
- _ref: [Elvis Operator](https://kotlinlang.org/docs/reference/null-safety.html#elvis-operator)_

## Weekly Kotlin ２: 関数リテラル
構文

```
val i = 0
val f1 = { Log.v(TAG, i) }
val f2 = { v: Int -> Log.v(TAG, "$v, $i") }
f1()
f2(1)
```

意味

```
interface Funciton1 { void run(); }
interface Funciton2 { void run(int i); }
　

int i = 0;
Function f1 = new Function1 () {
    public void run() {
        Log.v(TAG, i);
    }
}
Function f2 = new Function1 () {
    public void run(int v) {
        Log.v(TAG, v + ", " + i);
    }
}
f1.run();
f2.run();
```


- 無名関数
- Java だと匿名クラスに該当
- クロージャーなので定義された環境にアクセスできる
  - Java と違って var にもアクセスできるみたい。
- 型は Function<戻り型> 型。
- _ref1: [Lambda Exrepssions and Anonymous Functions](https://kotlinlang.org/docs/reference/lambdas.html#lambda-expressions-and-anonymous-functions)_
- _ref2: [Java 関数型インタフェース](http://www.ne.jp/asahi/hishidama/home/tech/java/functionalinterface.html)_

## Weekly Kotlin3: SAM 変換
構文

```
button.setOnClickListener { v -> Log.v(TAG, v) }
```

意味

```
button.setOnClickListener(new OnClickListener() {
    @Override
    public void onCLick(View v) {
        Log.v(TAG, v);
    }
})
```

- 一定の条件を満たすと、関数リテラルの構文で匿名クラスのインスタンスを生成できる
- 一定の条件
  - 実装のないメソッド宣言が一つだけのインタフェース
  - 抽象クラスには使えません。
- もともと Java8 の機能（知らなかった）
- _ref: [SAM Conversions](https://kotlinlang.org/docs/reference/java-interop.html#sam-conversions)_

## カバーできなかったところ
- フォルダ構成（res など）
- 画面遷移
- Fragment
- DataBindings: 公式のバインディングフレームワーク

## どうでもいい話
API Level の 12 と 21 を間違えちゃった話

https://code.google.com/p/android/issues/detail?id=78495
