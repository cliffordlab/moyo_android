package com.cliffordlab.amoss.gui.misc

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.cliffordlab.amoss.R

class MoyoBrowser : AppCompatActivity() {
    private var myWebView: WebView? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.moyo_browser)

        val url = intent.getStringExtra("url")
        myWebView = findViewById(R.id.webView)
        myWebView!!.webViewClient = WebViewClient()
        val webSettings = myWebView!!.settings
        webSettings.javaScriptEnabled = true
        myWebView!!.loadUrl(url.toString())
    }
}
