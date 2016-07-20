# Week Six
## 内容
- レイアウトファイル修正
- 今週のkotlin

### レイアウト修正
- Fragmentとレイアウトをポストタイプごとに分けた
-- 但し、CHAT, ANSWER, POSTCARDタイプは個人的に見たことないし、ほぼ使われていないだろうということで、レイアウトを用意していない
- 写真がちゃんと大きく表示されるようにした
-- 前回まで画像が小さかった原因
-- ダウンロードしてきたbitmapのdensityに端末のdpiに対応した[DisplayMetrics](https://developer.android.com/reference/android/util/DisplayMetrics.html)が自動で割り当てられる
-- Nexus5Xの場合はDENSITY_420 (2.2625倍) [参照](http://qiita.com/nein37/items/21cf0e98046a0267b158)
--- つまり2.2625分の1のサイズで表示されてた？
-- bitmapのdensityを1倍 (DENSITY_MEDIUM) にセットし直すと大きく表示された
- 写真の上下に空白ができる
-- 写真が大きく表示されるようになったと思ったら、今度は上下に謎の空白ができる
-- APIを眺めてると[adjustViewBounds](https://developer.android.com/reference/android/widget/ImageView.html#attr_android:adjustViewBounds)がそれっぽかったので、trueをセットしてみると空白がなくなった
-- adjustViewBounds=trueはimageViewの境界を画像の縦横比を維持したままぴったりになるように調整するフラグ

### 今週のkotlin
