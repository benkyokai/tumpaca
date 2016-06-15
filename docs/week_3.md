# Week 3
yabu
## 内容
- DashBoardの取得/表示
- Jumblrのソース組込み
- スワイプによるページ移動
- DownloadImageTaskの実装
- Weekly Kotlin

## DashBoardの取得/表示

## Jumblrのソース組込み
- AvaterのURLを取得しようとしたら、毎回アプリが落ちる
- 調べてみるとJumblr側の不具合っぽい。
    - HttpURLConnection.setFollowRedirects(false)でリダイレクトをスルーしたいが、marshmallowからはこの設定が無視される[らしい](https://code.google.com/p/android/issues/detail?id=194495)。
    - そのため、リダイレクトが自動で行われ、response.getCode()の結果が301ではなくなり、例外が投げられる

```
public String getRedirectUrl(String path) {
    OAuthRequest request = this.constructGet(path, null);
    sign(request);
    boolean presetVal = HttpURLConnection.getFollowRedirects();
    HttpURLConnection.setFollowRedirects(false); // ここでリダイレクトをスルーしようとするが、marshmallowからは無視される
    Response response = request.send();
    HttpURLConnection.setFollowRedirects(presetVal);
    if (response.getCode() == 301) {
        return response.getHeader("Location");
    } else {
        throw new JumblrException(response); // ここに落ちる
    }
}
```
- githubのissueを見てみると、[報告](https://github.com/tumblr/jumblr/issues/94)されてた
    - ↑はすでに修正されているけど、リリースされていない・・・
- そこで上記コミットが行われたブランチからソースを取ってきて、tumpacaに組み込んだ
    - jarにして組み込んでもいいが、他にも修正する可能性がありそうなので、ソースで組み込んだ

## スワイプによるページ移動
- 必要なコンポーネント
    - ViewPager: 横スクロールしてページを切り替えるためのview
        - ListView: 縦スクロール
    - DashboardPagerAdapter extends FragmentPagerAdapter: 横スクロールする各ページを管理する
    - PostFragment extends Fragment: 各ページのUIの表示とコントロール
    - dashboard_post_card: 各ページのlayout

1. DashboardActivity
    1. ViewPagerを生成
    2. DashboardPagerAdapterを生成し、Dashboardのポスト一覧を渡す
    3. ViewPagerにDashboardAdapterをセット
2. DashboardPagerAdapter
    1. ポスト一覧がどの種類のポストか判別して、各種ポストに対応したデータをPostFragmentに渡す
3. PostFragment
    1. 渡されたBundleから必要な情報を取得し、dashboard_post_cardレイアウトに必要なデータを埋め込んでいく
        - TumblrのPostのbodyはhtmlになっているので、表示にはWebviewと使う
        
## DownloadImageTaskの実装
- ImageViewに画像を表示するためのAsyncTask
- Tumblrのポストの画像は全てURLからダウンロードして表示する
    - httpアクセスして画像をダウンロードするためのAsyncTaskが必要
- ImageViewとURLを渡すと、画像をダウンロードして、ImageViewにセットしてくれるAsyncTaskを実装

```
class DownloadImageTask(val imageView: ImageView): AsyncTask<String, Void, Bitmap>() {

    override fun doInBackground(vararg urls: String): Bitmap? {
        val url = urls[0] // このへんのassertionはちゃんとしてない
        return loadBitmap(url)
    }

    private fun loadBitmap(url: String): Bitmap? {
        try {
            val stream = URL(url).openStream();
            return BitmapFactory.decodeStream(stream)
        } catch(e: Exception) {
            Log.e("Error", e.message)
            e.printStackTrace()
            return null
        }
    }

    override fun onPostExecute(result: Bitmap) {
        this.imageView.setImageBitmap(result)
    }

}
```

## Weekly Kotlin
- LoginActivityViewModelを使ってCake PatternによるDIを実装してみた
    - 無理やりやってみただけなので、ここでDIするのが有用かどうかは考えない
- CakePatternはScalaコンパイラの中で出てくるDIの実装パターン
    - http://eed3si9n.com/ja/real-world-scala-dependency-injection-di
- ただ、本家のCakePatternは複雑すぎるので、簡素化したMinimal Cake Patternをkotlinで実装する
    - http://qiita.com/pab_tech/items/1c0bdbc8a61949891f1f

- LoginViewModelのインタフェースとViewModelを保持するHasLoginActivityViewModelを定義

```
interface HasLoginActivityViewModel {
    val loginActivityViewModel: LoginActivityViewModel
}

interface LoginActivityViewModel {
    var mail: String?
    var password: String?
    fun set(mail: String, password: String)
    fun login()
}
```

- 上記のインタフェースの実装

```
object MixInLoginActivityViewModel: HasLoginActivityViewModel {
    override val loginActivityViewModel: LoginActivityViewModel
        get() = LoginActivityViewModelImpl
}

object LoginActivityViewModelImpl : LoginActivityViewModel {
    final val TAG = "LoginActivityViewModel"

    override var mail: String? = null
    override var password: String? = null

    override fun set(mail: String, password: String) {
        this.mail = mail
        this.password = password
    }

    override fun login() {
        Log.i(TAG, "login, mail=$mail, pass=${password?.length}")
    }
}
```

- 利用側はClass Delegationを使って、HasLoginActivityViewModelの実装をMixInLoginActivityViewModelに移譲する

```
class LoginActivity : AppCompatActivity(), HasLoginActivityViewModel by MixInLoginActivityViewModel { 
    override fun onCreate(savedInstanceState: Bundle?) {
        ...
        
        loginActivityViewModel.set(mail ?: "", password ?: "") // 実装を注入されたview modelを利用
        
        ...
    }
}
```