# Week 3
yabu
## 内容
- DashBoardの取得/表示
- Jumblrのソース組込み
- スワイプによるページ移動
- Weekly Kotlin

## DashBoardの取得/表示

## Jumblrのソース組込み
- AvaterのURLを取得しようとしたら、毎回アプリが落ちる
- 調べてみるとJumblr側の不具合っぽい。
-- HttpURLConnection.setFollowRedirects(false)でリダイレクトをスルーしたいが、marshmallowからはこの設定が無視される[らしい](https://code.google.com/p/android/issues/detail?id=194495)。
-- そのため、リダイレクトが自動で行われ、response.getCode()の結果が301ではなくなり、例外が投げられる
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
-- ↑はすでに修正されているけど、リリースされていない・・・
- そこで上記コミットが行われたブランチからソースを取ってきて、tumpacaに組み込んだ

## スワイプによるページ移動

## Weekly Kotlin
