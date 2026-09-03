package com.example.ledger;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.widget.FrameLayout;
import android.view.WindowInsets;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import android.content.Intent;
import android.net.Uri;

public class MainActivity extends Activity {
    private WebView webView;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(255, 218, 62));
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setBackgroundColor(Color.rgb(255, 218, 62));
        int systemUiFlags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            systemUiFlags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        getWindow().getDecorView().setSystemUiVisibility(systemUiFlags);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.WHITE);
        webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface public void startUpdate(String url) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        }, "AndroidBridge");
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDefaultTextEncodingName("utf-8");
        root.addView(webView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        ));
        View statusBarBackground = new View(this);
        statusBarBackground.setBackgroundColor(Color.rgb(255, 218, 62));
        root.addView(statusBarBackground, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, 0, android.view.Gravity.TOP
        ));
        View navigationBarBackground = new View(this);
        navigationBarBackground.setBackgroundColor(Color.WHITE);
        root.addView(navigationBarBackground, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, 0, android.view.Gravity.BOTTOM
        ));
        getWindow().getDecorView().setOnApplyWindowInsetsListener((view, insets) -> {
            int top = insets.getSystemWindowInsetTop();
            int bottom = insets.getSystemWindowInsetBottom();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                android.graphics.Insets bars = insets.getInsets(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout()
                );
                top = bars.top;
                bottom = bars.bottom;
            }
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) webView.getLayoutParams();
            params.setMargins(0, top, 0, bottom);
            webView.setLayoutParams(params);
            FrameLayout.LayoutParams statusParams = (FrameLayout.LayoutParams) statusBarBackground.getLayoutParams();
            statusParams.height = top;
            statusBarBackground.setLayoutParams(statusParams);
            FrameLayout.LayoutParams navigationParams = (FrameLayout.LayoutParams) navigationBarBackground.getLayoutParams();
            navigationParams.height = bottom;
            navigationBarBackground.setLayoutParams(navigationParams);
            return insets;
        });
        webView.loadUrl("file:///android_asset/index.html");
        setContentView(root);
        getWindow().getDecorView().requestApplyInsets();
    }
    @Override public void onBackPressed() {
        if (webView == null) { super.onBackPressed(); return; }
        webView.evaluateJavascript("window.handleAndroidBack ? window.handleAndroidBack() : false", value -> {
            if (!"true".equals(value == null ? "" : value.trim())) super.onBackPressed();
        });
    }
}
