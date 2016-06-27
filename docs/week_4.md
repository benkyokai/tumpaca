# Week Four
## 内容
- デモ
- お勉強
  - Application とは何か
  - Context とは何か
  - Fragment とは何か
- 実装説明
  - TumblerService
  - Fragment 導入
- 今週の Kotlin
  - Compile-Time Constants
  - Property Field, Getter, Setter

## デモ
- 認証画面のレイアウト調整しました
- ログイン・ログアウト機能を実装しました
  - アプリを落としてもログイン状態が保持されます
  - ログアウトできます
- ログインしている場合にはダッシュボードから起動します

## お勉強
#### Application とは何か
- アプリの**プロセス**のライフサイクルとひもづくクラス
- プロセスが起動した時に一度だけ onCreate() が呼ばれる
  - アプリ共通で使うデータの初期化などを行う => 重い処理を行うと起動に影響がでます。
  - メモリが足りない時などのコールバックもあります
  - 利用例：droidkaigi2016、 Scene
- 使い方
  - Application を継承したクラスを作り、onCreate() などをオーバーライド
  - AndroidManifest.xml に宣言する
- ライフサイクル階層構造
  - アプリ => Application（=プロセス） => Activity => Fragment
  - 実例: droidkaig2016, Scene

#### Context とは何か
- Android OS 全体でアプリの実行環境をやりとりするための情報を持ったクラス
- 代表的なメソッド
   - getResources(): バンドルされた resources を取得
   - getSharedPreferences(): SharedPreference を取得
   - startActivity(): 新しい Activity を起動
- 実体クラス
   - Application
   - Activity
   - 他に50個ちかくある…
- Application extends Context と Activity extends Context の違い
   - ライフサイクルが違う
     - http://yuki312.blogspot.jp/2012/03/androidcontext.html
   - どちらを渡す？
     - ケースバイケース
     - 渡す先がアプリでグローバルに利用するなら Application extends Context
     - UI などでは Activity extends Context
- 感想：たぶん、よくない API だと思う…分かりづらいし…


#### Fragment とは何か
- Activity 内の部分的な UI を表すクラス
  - Activity 内に複数の Fragment を載せることができる
- **Activity の上にしか存在できない**
- ライフサイクル
  - Activity と似たようなライフサイクルがある（onCreate(), onResume()）
  - Activity と Fragment のライフサイクルの関連（http://i.stack.imgur.com/fRxIQ.png）
- 作り方
  - Fragment.onCreateView() で View を作って返す
- Fragment の追加、削除、置換
  - 置換： Activity 上の Fragment を入れ替える => 通常の画面遷移のように見える
  - 追加： Activity に新しく Fragment を追加する => たとえば Pager など。
  - 削除： Activity から Fragment を削除
  - Activity が起動・終了しかできないのと対照的。


## 実装説明
#### TumblerService
- Tumbler 系の操作をまとめたもの
- 作成動機
  - JumblerClient を一度だけ生成し、それを保存して提供したい
  - ログイン状態の保持・破棄をしたい
  - 各種実装を隠蔽したい
  - UI 側のコードをすっきりさせたい
- 実装
  - Context に依存：getSharedPreference と getRaw を使うので。
  - MainApplication で初期化し、プロパティとして保持。
     - Activity や Fragment は Application を直接取得できる。
  - ログイン・ログアウトの管理
     - ログイン時に AuthToken と AuthTokenSecret を SharedPreference に保存
     - 保存されていればログイン済と判断
     - ログアウト時にはこれらを破棄


#### Fragment の導入
- 現状の構成
  - Activity
     - MainActivity: Tumpaca の唯一の Activity
     - (LoglrActivity: Tumbler 認証 WebView が乗っかる Activity) => Loglr の一部です
  - Fragment
     - AuthFragment: 認証画面
     - DashboardFragment: ダッシュボード画面
       - PostFragment: 各種ポスト画面
- 処理（流れは図で説明します）
  - MainApplication (システムが AndroidManifest.xml を見て勝手に起動)
  - MainActivity (システムが AndroidManifest.xml を見て勝手に起動)
      - AuthFragment に置換（実際には追加）
  - AuthFragment
    - ログイン済なら DashboardFragment を置換
    - 未ログインなら画面を表示
    - ボタンが押されたら、LoglrActivity **を起動**
  - LoglrActivity
    - WebView が開いて Tumbler 認証
    - 認証が終わると Activity 終了 => AuthFragment に戻る
  - DashboardFragment
    - Dashboard を表示
    - ログアウトが押されたら
       - TumblerService.logout()
       - AuthFragment に置換

## 今週の Kotlin
#### Compile-Time Constants
- spec: https://kotlinlang.org/docs/reference/properties.html#compile-time-constants
- コンパイル時に値が決まる定数
- Java でいう static final
- Compile-Time Constants になる条件
  - トップレベル変数 or object 内
  - primitive か String で初期化されている
  - カスタム getter がない

```
class MainActivity: AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }
}
```

- 証拠（MainApplication.class をデコンパイル）

```
package com.tumpaca.tumpaca;

@Metadata(mv={1, 1, 1}, bv={1, 0, 0}, k=1, d1={""}, d2={"Lcom/tumpaca/tumpaca/MainApplication;", "Landroid/app/Application;", "()V", "<set-?>", "Lcom/tumpaca/tumpaca/util/TumblerService;", "tumblerService", "getTumblerService", "()Lcom/tumpaca/tumpaca/util/TumblerService;", "setTumblerService", "(Lcom/tumpaca/tumpaca/util/TumblerService;)V", "onCreate", "", "Companion", "app-compileEnvDevDebugKotlin"})
public final class MainApplication extends Application
{

  @Nullable
  private TumblerService tumblerService;

  @NotNull
  // => private にならない…
  public static final String TAG = "MainApplication";
  public static final Companion Companion = new Companion(null);

  @Nullable
  public final TumblerService getTumblerService()
  {
    return this.tumblerService; }
  private final void setTumblerService(TumblerService <set-?>) { this.tumblerService = <set-?>; }

  public void onCreate()
  {
    super.onCreate();
    Log.d(TAG, "MainApplication:onCreate()");
    this.tumblerService = new TumblerService((Context)this);

    getContentResolver();
  }

  @Metadata(mv={1, 1, 1}, bv={1, 0, 0}, k=1, d1={""}, d2={"Lcom/tumpaca/tumpaca/MainApplication$Companion;", "", "()V", "TAG", "", "app-compileEnvDevDebugKotlin"})
  public static final class Companion
  {
  }
}
```


#### Getter と Setter
- spec: https://kotlinlang.org/docs/reference/properties.html#properties-and-fields
- メンバ変数の var と val
  - var: setter and getter (read and write)
  - val: only getter (read-only)

```
var allByDefault: Int?
// => デフォルトゲッタとセッタが作成される
// => ただし、コンパイルエラー。初期化が必要
var initialized = 1
// => デフォルトゲッタとセッタが作成される
```

```
val simple: Int?
// => デフォルトゲッタのみ作成
// => コンストラクタで初期化しないとエラー
val inferredType = 1
// => デフォルトゲッタのみ作成
```

- Custom Getter and Setter

```
val isEmpty: Boolean
  get() = this.size == 0
// => getter を定義。
// => val だが実行時に値は変わる。val は immutable を意味しない
```

```
var stringRepresentation: String
  get() = this.toString()
  set(value) {
    setDataFromString(value)
  }
// => setter を定義
// => 引数は value という名前を使う
```

- Backing Field
  - 実際にデータが置かれるフィールドのこと
    - Setter 内で field としてのみ参照可能
    - Kotlin 上では通常意識されない。
  - val isEmpty のように Backing Field が必要ない場合もある
  - Android Studio でなぜかハイライトされる。。。

```
var counter = 0
  set(value) {
    if (value >= 0)
      field = value
  }
=> field は counter の実際の値（= Backing Field）を参照している
```


## その他
- adb shell で実機にログインして SharePreference とか見られる。

```
> adb shell

adb-shell > run-as com.tumpaca.tumpaca
```


## できなかったこと
- like と reblog
- Jumbler ソースをサブプロジェクトへと分割
- PostFragment が落ちる
  - ログイン ⇒ ログアウト ⇒ アプリをバックグラウンドに => アプリを再度フォアグラウンドに

## 今後の予定など
