package com.tumpaca.tumpaca.util

import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
import com.tumpaca.tumpaca.model.TPRuntime

/**
 * UIの便利メソッド集
 * Created by yabu on 2016/10/12.
 */

object UIUtil {

    fun loadCss(webView: WebView, cssFile: String = "style.css"): Unit {
        webView.settings.javaScriptEnabled = true

        webView.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                webView.loadUrl(generateCSS(cssFile))
                super.onPageFinished(view, url)
            }
        })
    }

    fun doNotHorizontalScroll(webView: WebView): Unit {
        loadCss(webView, "doNotHorizontalScroll.css")
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