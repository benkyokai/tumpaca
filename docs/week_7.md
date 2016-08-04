# Week Seven
## 内容
- デモ
- 修正内容
  - PostList の導入
  - Post へのメソッド追加
  - PostFragment 系生成方法を bundle から変更
  - FragmentStatePagerAdapter の利用
  - その他
  - 失敗したこと
- 問題
  - 最新のポストをロードしてくる戦略
  - ポストのフィルタリング問題
- 今週の Kotlin
  - Delegation

## デモ
- 前後の先読み数を増やしました

## 修正内容の説明
### PostList を導入
- ポストのリストを抽象的に表現する PostList というクラスを作りました
- 目的
  - filter 作業の隠蔽
  - fetch 作業の隠蔽
  - 非同期操作の隠蔽
- 機能
  - get(i) で Post を取得
  - 値に応じて自動的にサーバー上から次の Post リストを取得しておく


### Post へのメソッド追加
- Post に Extensions で各種操作を追加しました
- 目的
  - 非同期操作の隠蔽
- 機能
  - like
  - reblog
  - getAvatar


### Post 系 Fragment の生成方法の変更
- bundle での引数受け渡しを辞めました。
- position を元にして PostList から post を取得し、それに応じて表示します
- 目的
  - コードの煩雑性除去
  - avatar 取得部分を post.getAvatar() としてシンプル化
  - Post 系 Fragment 生成部分の煩雑性除去


### FragmentStatePagerAdapter の利用
- FragmentStatePagerAdapter を利用するようにしました
- Why
  - FragmentPagerAdapter: 作った Fragment を永遠に保持 => Page が多いとメモリを圧迫
  - FragmentStatePagerAdapter: 表示中ページから遠いページの Fragment は破棄。ただし、state は保存され、表示時には復元される


### その他
- TpRuntime 経由で TumblerService を取得するように変更しました
- キャッシュを LruCache で実装しました
- WebView ロード時に勝手に変更しないように修正しました。

### 失敗したこと
- Post や User をラップした非同期層を提供
  - Post を継承した PhotoPost などがいくつもあるので、うまくラップできず


## 問題
### ポストフィルタリング問題
- 特定のポスト種別をフィルタリングして保持していると、ローカルのポスト配列とサーバーのポスト配列が異なってしまう

### 最新のポスト問題
- offset を基準にすると、サーバー上で新しいポストがリストの先頭に入ると、offset がずれる
- dashboard の API 的にいい方法があるか？
  - https://www.tumblr.com/docs/en/api/v2#m-ug-dashboard

## 今週の Kotlin
### Delegation
- 委譲する際の処理のコードを書かなくてよい機能
- https://kotlinlang.org/docs/reference/delegation.html
- in Java
```
interface Base {
   void add();
   void remove();
   void print();
}

class BaseImpl implements Base {
   public void add() { ... }
   public void remove() { ... }
   public void print() { ... }
}

class Derived implements Base {
   private final Base base;
   Derived(Base base) {
       this.base = base;
   }

   @Override
   public void add() {
       base.add();
   }

   @Override
   public void remove() {
      base.remove();
   }

   @Override
   public void print() {
      base.print();
   }
}
```

- in Kotlin

```
interface Base {
  fun add()
  fun remove()
  fun print()
}

class BaseImpl() : Base {
  override fun add() { ... }
  override fun remove() { ... }
  override fun print() { ... }
}

// 1 行で OK
class Derived(b: Base) : Base by b
```

- （Post のラッパーをつくるところで使おうとしたが失敗）
