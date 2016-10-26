package com.tumpaca.tumpaca.view

import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
import com.tumpaca.tumpaca.model.TPRuntime

/**
 * カスタムWebViewクライアント
 * Created by yabu on 2016/10/26.
 */
class TPWebViewClient(val cssFilePath: String) : WebViewClient() {

    /**
     * WebView内のリンクをタップしたときは、外部ブラウザを立ち上げる
     */
    override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
        if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
            view.context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            return true
        } else {
            return false
        }
    }

    /**
     * ページ読み込み完了時にスタイルシート適用
     */
    override fun onPageFinished(view: WebView, url: String) {
        view.loadUrl(generateCSS(cssFilePath))
        super.onPageFinished(view, url)
    }

    private fun generateCSS(cssFile: String): String {
        try {
            val inputStream = TPRuntime.mainApplication.assets.open(cssFile)
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            val encoded = Base64.encodeToString(buffer, Base64.NO_WRAP)
            val scripts = "javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    // Tell the browser to BASE64-decode the string into your script !!!
                    "style.innerHTML = window.atob('$encoded');" +
                    "parent.appendChild(style)" +
                    "})()"
            return scripts
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
}