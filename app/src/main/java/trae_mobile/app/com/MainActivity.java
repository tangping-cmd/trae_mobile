package trae_mobile.app.com;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowInsetsController;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "trae_solo_prefs";
    private static final String KEY_LAST_URL = "last_url";
    private static final String DEFAULT_URL = "https://solo.trae.cn/";

    private WebView webView;
    private ProgressBar progressBar;
    private View errorLayout;
    private SharedPreferences prefs;
    private boolean isErrorShown = false;
    private int statusBarInsetTop = 0;

    private Handler handler = new Handler(Looper.getMainLooper());

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_main);

        setupImmersiveMode();
        setupWindowInsets();

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        errorLayout = findViewById(R.id.errorLayout);

        setupWebView();
        setupErrorRetryButton();

        String urlToLoad = getSavedUrl();
        loadUrl(urlToLoad);

        handler.postDelayed(fabInjector, 2000);
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

    private void setupImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_DEFAULT);
            }
        }
    }

    private void setLightStatusBar(boolean light) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                if (light) {
                    insetsController.setSystemBarsAppearance(
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    );
                } else {
                    insetsController.setSystemBarsAppearance(
                            0,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    );
                }
            }
        } else {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            if (light) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    private void setupWindowInsets() {
        View contentContainer = findViewById(R.id.contentContainer);
        ProgressBar progressBarView = findViewById(R.id.progressBar);
        ViewCompat.setOnApplyWindowInsetsListener(contentContainer, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            statusBarInsetTop = insets.top;
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            if (progressBarView != null) {
                progressBarView.setTranslationY(insets.top);
            }
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void setupErrorRetryButton() {
        View btnRetry = errorLayout.findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(v -> {
            errorLayout.setVisibility(View.GONE);
            isErrorShown = false;
            loadUrl(getCurrentUrl());
        });
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

    private void detectStatusBarAppearance(String url) {
        boolean isDarkPage = url != null && !url.contains("about:blank");
        setLightStatusBar(!isDarkPage);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
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
        if (webView != null) {
            webView.resumeTimers();
            webView.onResume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveLastUrl();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(fabInjector);
        if (webView != null) {
            webView.destroy();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setupImmersiveMode();
        }
    }

    private final Runnable fabInjector = new Runnable() {
        @Override
        public void run() {
            injectFab();
            handler.postDelayed(this, 3000);
        }
    };

    private void injectFab() {
        String js = "if(!window._traeFabInjected){" +
                "window._traeFabInjected=true;" +
                "const c=document.createElement('div');" +
                "c.id='t-fab';" +
                "const s=document.createElement('style');" +
                "s.innerHTML=`#t-fab{position:fixed;right:0px;bottom:80px;z-index:999999;display:flex;flex-direction:column-reverse;align-items:center;gap:12px;transition:left 0.3s ease-out,top 0.3s ease-out,transform 0.3s,opacity 0.3s;}#t-fab.dragging{transition:none;}#t-fab.half.snap-left{transform:translateX(-28px);opacity:0.5}#t-fab.half.snap-right{transform:translateX(28px);opacity:0.5}.t-btn{border-radius:50%;display:flex;justify-content:center;align-items:center;cursor:pointer;box-shadow:0 4px 12px rgba(0,0,0,0.3);transition:all .3s cubic-bezier(.25,.8,.25,1);border:none;color:#fff;outline:none;-webkit-tap-highlight-color:transparent}.t-main{width:56px;height:56px;background:rgba(92,97,255,.85);backdrop-filter:blur(4px)}.t-sub{width:48px;height:48px;opacity:0;transform:translateY(20px) scale(.8);pointer-events:none}#t-fab.exp .t-sub{opacity:1;transform:translateY(0) scale(1);pointer-events:auto}.t-back{background:#00C853}.t-ref{background:#2979FF}.t-main svg{width:28px;height:28px;transition:transform .3s}#t-fab.exp .t-main svg{transform:rotate(45deg)}`;" +
                "document.head.appendChild(s);" +
                "c.innerHTML=`<div class='t-btn t-main' id='t-main'><svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 34 24' fill='none'><g fill='currentColor'><path d='M.002 0H0v19.549h4.454V4.454h24.864v15.092H4.454V24h29.318V0z'></path><path d='m13.43 8.776-3.149 3.15 3.15 3.149 3.149-3.15zM23.204 8.775l-3.15 3.149 3.15 3.149 3.15-3.15z'></path></g></svg></div><div class='t-btn t-sub t-back' id='t-back'><svg viewBox='0 0 24 24' width='24' height='24' stroke='currentColor' stroke-width='2' fill='none' stroke-linecap='round' stroke-linejoin='round'><line x1='19' y1='12' x2='5' y2='12'></line><polyline points='12 19 5 12 12 5'></polyline></svg></div><div class='t-btn t-sub t-ref' id='t-ref'><svg viewBox='0 0 24 24' width='24' height='24' stroke='currentColor' stroke-width='2' fill='none' stroke-linecap='round' stroke-linejoin='round'><polyline points='23 4 23 10 17 10'></polyline><polyline points='1 20 1 14 7 14'></polyline><path d='M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15'></path></svg></div>`;" +
                "document.body.appendChild(c);" +
                "let m=document.getElementById('t-main');" +
                "let sx,sy,ix,iy,moved=false,ht;" +
                "const K='traeFabPosV1';" +
                "const load=()=>{try{return JSON.parse(localStorage.getItem(K)||'null')}catch(e){return null}};" +
                "const save=()=>{try{localStorage.setItem(K,JSON.stringify({side:c.classList.contains('snap-left')?'L':'R',top:parseFloat(c.style.top)||0}))}catch(e){}};" +
                "const resT=()=>{" +
                "  clearTimeout(ht);" +
                "  if(!c.classList.contains('exp')) ht=setTimeout(()=>c.classList.add('half'),2000);" +
                "};" +
                "const snap=()=>{" +
                "  let r=c.getBoundingClientRect();let cx=r.left+r.width/2;" +
                "  let isL=cx<window.innerWidth/2;" +
                "  c.style.left=isL?'0px':(window.innerWidth-56)+'px';" +
                "  c.classList.remove('snap-left','snap-right');" +
                "  c.classList.add(isL?'snap-left':'snap-right');" +
                "  resT();" +
                "  save();" +
                "};" +
                "m.addEventListener('touchstart',e=>{" +
                "  c.classList.remove('half');" +
                "  let t=e.touches[0];sx=t.clientX;sy=t.clientY;" +
                "  let r=c.getBoundingClientRect();" +
                "  ix=parseFloat(c.style.left); if(isNaN(ix)) ix=window.innerWidth-56;" +
                "  iy=parseFloat(c.style.top); if(isNaN(iy)) iy=r.top;" +
                "  moved=false;c.classList.add('dragging');" +
                "  c.style.right='auto';c.style.bottom='auto';" +
                "  c.style.left=ix+'px';c.style.top=iy+'px';" +
                "  clearTimeout(ht);" +
                "},{passive:false});" +
                "m.addEventListener('touchmove',e=>{" +
                "  let t=e.touches[0];let dx=t.clientX-sx;let dy=t.clientY-sy;" +
                "  if(Math.abs(dx)>5||Math.abs(dy)>5){" +
                "    moved=true;e.preventDefault();" +
                "    let nl=ix+dx,nt=iy+dy;" +
                "    let ml=window.innerWidth-56,mt=window.innerHeight-56;" +
                "    c.style.left=Math.max(0,Math.min(nl,ml))+'px';" +
                "    c.style.top=Math.max(0,Math.min(nt,mt))+'px';" +
                "  }" +
                "},{passive:false});" +
                "m.addEventListener('touchend',e=>{" +
                "  c.classList.remove('dragging');" +
                "  if(!moved){" +
                "    c.classList.toggle('exp');" +
                "    if(c.classList.contains('exp')) clearTimeout(ht);" +
                "    else resT();" +
                "  }else{ snap(); c.classList.remove('exp'); }" +
                "});" +
                "document.getElementById('t-back').onclick=()=>{window.history.back();c.classList.remove('exp');resT();};" +
                "document.getElementById('t-ref').onclick=()=>{window.location.reload();c.classList.remove('exp');resT();};" +
                "document.addEventListener('click',e=>{if(!c.contains(e.target)){c.classList.remove('exp');resT();}});" +
                "window.addEventListener('resize',()=>{let t=parseFloat(c.style.top);if(isNaN(t))t=c.getBoundingClientRect().top;c.style.top=Math.max(0,Math.min(t,window.innerHeight-56))+'px';snap();});" +
                "c.style.right='auto';c.style.bottom='auto';" +
                "const st=load();" +
                "if(st&&typeof st.top==='number'){" +
                "  c.style.top=Math.max(0,Math.min(st.top,window.innerHeight-56))+'px';" +
                "  c.style.left=(st.side==='L')?'0px':(window.innerWidth-56)+'px';" +
                "  c.classList.remove('snap-left','snap-right');" +
                "  c.classList.add((st.side==='L')?'snap-left':'snap-right');" +
                "  resT();" +
                "}else{" +
                "  c.style.top=(window.innerHeight/2 - 28)+'px';" +
                "  c.style.left=(window.innerWidth-56)+'px';" +
                "  c.classList.remove('snap-left','snap-right');" +
                "  c.classList.add('snap-right');" +
                "  resT();" +
                "}" +
                "}";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(js, null);
        } else {
            webView.loadUrl("javascript:" + js);
        }
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
            detectStatusBarAppearance(url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            saveLastUrl();
            injectFab();
            detectStatusBarAppearance(url);
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
