package trae_mobile.app.com;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "trae_solo_prefs";
    private static final String KEY_LAST_URL = "last_url";
    private static final String DEFAULT_URL = "https://solo.trae.cn/";

    private WebView webView;
    private ProgressBar progressBar;
    private View errorLayout;
    private SharedPreferences prefs;
    private boolean isErrorShown = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_TraeSolo);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        errorLayout = findViewById(R.id.errorLayout);

        MaterialButton btnRetry = errorLayout.findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(v -> {
            errorLayout.setVisibility(View.GONE);
            isErrorShown = false;
            loadUrl(getCurrentUrl());
        });

        setupWebView();

        String urlToLoad = getSavedUrl();
        loadUrl(urlToLoad);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setCacheMode(android.webkit.WebSettings.LOAD_DEFAULT);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setWebViewClient(new TraeWebViewClient());
        webView.setWebChromeClient(new TraeWebChromeClient());
    }

    private String getSavedUrl() {
        return prefs.getString(KEY_LAST_URL, DEFAULT_URL);
    }

    private String getCurrentUrl() {
        String currentUrl = webView.getUrl();
        return (currentUrl != null && !currentUrl.isEmpty()) ? currentUrl : getSavedUrl();
    }

    private void loadUrl(String url) {
        if (url == null || url.isEmpty()) {
            url = DEFAULT_URL;
        }
        errorLayout.setVisibility(View.GONE);
        isErrorShown = false;
        webView.loadUrl(url);
    }

    private void saveLastUrl() {
        String currentUrl = webView.getUrl();
        if (currentUrl != null && !currentUrl.isEmpty()) {
            prefs.edit().putString(KEY_LAST_URL, currentUrl).apply();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    private void showErrorPage() {
        isErrorShown = true;
        webView.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
    }

    private void hideErrorPage() {
        if (isErrorShown) {
            isErrorShown = false;
            errorLayout.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_home) {
            loadUrl(DEFAULT_URL);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveLastUrl();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveLastUrl();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }

    private class TraeWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
            hideErrorPage();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            saveLastUrl();
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (request.isForMainFrame()) {
                showErrorPage();
            }
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, android.webkit.WebResourceResponse errorResponse) {
            if (request.isForMainFrame()) {
                showErrorPage();
            }
        }
    }

    private class TraeWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                progressBar.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);
            }
        }
    }
}
