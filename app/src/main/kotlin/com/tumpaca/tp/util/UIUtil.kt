package com.tumpaca.tp.util

import android.webkit.WebView
import com.tumpaca.tp.view.TPWebViewClient

/**
 * UIの便利メソッド集
 * Created by yabu on 2016/10/12.
 */

object UIUtil {

    val TPWebViewClient = TPWebViewClient("style.css")
    val DoNotHorizontalScrollWebViewClient = TPWebViewClient("doNotHorizontalScroll.css")

    fun loadCss(webView: WebView): Unit {
        webView.setWebViewClient(TPWebViewClient)
    }

    fun doNotHorizontalScroll(webView: WebView): Unit {
        webView.setWebViewClient(DoNotHorizontalScrollWebViewClient)
    }

}