# 0週目
5/25 yabu

## github organizationを作成
名前: benkyokai
https://github.com/benkyokai/

## リポジトリ作成
名前: tumpaca
https://github.com/benkyokai/tumpaca/tree/develop

## プロジェクトダウンロード
git clone https://github.com/benkyokai/tumpaca.git
Android Studioで開く

## kotlinプラグインインストール

Preferences -> Plugins -> Install JetBrains plugins
で、kotlinを検索してインストール

kotlinでの始め方
http://crossbridge-lab.hatenablog.com/entry/2015/12/03/220000

## run
プロジェクト生成時に起動用の設定 (app) が作られているので、そのまま実行する

yabuの環境だと以下のエラーが出てエミュレータが立ち上がらなかった
```
Failed to sync vcpu reg
Internal error: initial hax sync failed
```
↑dockerを起動してたのが原因（dockerのvmとAVDのvmは共存できないっぽい）

## アイコン変更
とりあえず、出来ているのはアプリ名とアイコンだけなので、アイコンを設定してみた。
AndroidManifest.xmlにアイコンに関する設定が書いてある。

``` xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="com.tumpaca.tumpaca">
    <application android:allowBackup=“true”
                 android:icon="@mipmap/ic_launcher” // アプリアイコンのファイル名と場所
                 android:label="@string/app_name”  // アプリケーション名
                 android:supportsRtl=“true” // Right-To-Left 右始まりの言語への対応
                 android:theme="@style/AppTheme”> // 画面共通のスタイル定義 -> styles.xml, colors.xml
        <activity android:name=".MainActivity”> // 画面
            <intent-filter>
                <action android:name="android.intent.action.MAIN" /> // アプリ起動時のメイン画面
                <category android:name="android.intent.category.LAUNCHER" /> // ホーム画面のアイコンから起動可
            </intent-filter>
        </activity>
    </application>
</manifest>
```

applicationタグのandroid:iconにアイコンのパスが書かれているのでそれに合わせてアイコンを配置した。
各画面サイズに応じて[directory-name]-[size]の形式でディレクトリを作り、そのなかにic_launcher.pngという名前でアイコンを配置した。
res/mipmap-hdpi 72px
res/mipmap-mdpi 48px
res/mipmap-xhdpi 96px
res/mipmap-xxhdpi 144px
res/mipmap-xxxhdpi 192px



