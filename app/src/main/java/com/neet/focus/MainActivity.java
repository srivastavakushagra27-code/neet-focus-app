package com.neet.focus;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.webkit.WebViewAssetLoader;

public class MainActivity extends Activity {
  private WebView w;
  private ValueCallback<Uri[]> cb;

  @Override protected void onCreate(Bundle b) {
    super.onCreate(b);
    getWindow().setStatusBarColor(0xFF0D0F16);
    getWindow().setNavigationBarColor(0xFF0D0F16);
    if (Build.VERSION.SDK_INT >= 33) requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
    w = new WebView(this);
    w.setBackgroundColor(0xFF0D0F16);
    setContentView(w);
    WebSettings s = w.getSettings();
    s.setJavaScriptEnabled(true);
    s.setDomStorageEnabled(true);
    final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
        .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
    w.setWebViewClient(new WebViewClient() {
      @Override public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
        return loader.shouldInterceptRequest(r.getUrl());
      }
    });
    w.setWebChromeClient(new WebChromeClient() {
      @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> c, FileChooserParams p) {
        cb = c;
        try { startActivityForResult(p.createIntent(), 7); } catch (Exception e) { cb = null; return false; }
        return true;
      }
    });
    w.addJavascriptInterface(new Bridge(this), "Android");
    w.loadUrl("https://appassets.androidplatform.net/assets/index.html");
  }

  @Override protected void onActivityResult(int rq, int rs, Intent d) {
    if (rq == 7 && cb != null) {
      cb.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(rs, d));
      cb = null;
    } else {
      super.onActivityResult(rq, rs, d);
    }
  }

  @Override public void onBackPressed() { moveTaskToBack(true); }
}
