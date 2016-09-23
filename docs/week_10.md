# Week 10

## 内容
- デモ
- 修正箇所
- 今後
- 今週の Kotlin

## デモ
 - 回転によるクラッシュ修正
 - status bar と背景の色変更
 - Settings 画面

## 修正箇所
### 回転によるクラッシュ
- 回転するとクラッシュしていた
- 回転すると PostList が空になり、Fragment の getPost() が null が帰ってきていた
- 常に非同期でとれるように修正
  - 当該 index の post がなかったら、サーバーにその post を取りに行く
  - 取りに行き終わったら callback を呼ぶ
- 重いので最終的には Activity が再生成されないようにした。

### Rounded Corner
- View の角を丸くする方法
- API Level 21（Android 5.0）から簡単にできる
-- shape を xml として定義
-- 対象の View の xml に `android:background="@drawable/round"` を定義
-- コード上で `view.clipToOutline = true` を設定

### Listener 系のリファクタ
- Settings 画面のボタンイベントの処理実行を Listener で実行していた
  - Activity のメソッドを呼び出したいため？
- Fragment 内で FragmentManager は取得できる
- Fragment 内で Activity は取得できる

### スモークテスト
- クリーンインストール
- ログインする
- Dashboard を 50 件くらいみる
-- 動画は見られるか？
-- GIF は再生されるか？
-- 端末を回転しても問題ないか
- Like する
- Reblog する
- Dashboad をリロードする
- ログアウトする
- 再ログインする

## 今後
- リリースまでの道のり
  - Dashboard 取得制限問題
- 個人的にいれたいもの
  - Code License
  - バージョニング
  - Fabric


## 今週の Kotlin
### is 演算子
- 実行時の方を調べる演算子
- Java の instanceof
- !is もあるよ => `x !is String` と `!(x is String)` は同じ
- null is String => false （Java と同じ）


### smartcast
- is 演算子の後は明示的キャストを書く必要がない（コンパイラが挿入）

```
// こういうメソッドがあると仮定する
fun update(view: View, post: QuotePost) {
  ...
}
```

```
val post: Post = getPost()
if (post is QuotePost) {
    // post を明示的にキャストする必要なし
    update(view, post)
}
```

```
val post: Post = getPost()
if (it !is QuotePost) {
  return
}
// post を明示的にキャストする必要なし
update(view, post)
```

