package com.mad.fruityloopy;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private BottomNavigationView botNav;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the activity to fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        webView = findViewById(R.id.webView);
        botNav = findViewById(R.id.btmNav);

        WebSettings h5settings = webView.getSettings();
        h5settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        h5settings.setDomStorageEnabled(true);
        h5settings.setLoadsImagesAutomatically(true);
        h5settings.setMediaPlaybackRequiresUserGesture(false);
        h5settings.setJavaScriptEnabled(true);
        h5settings.setJavaScriptCanOpenWindowsAutomatically(true);
        h5settings.setSupportMultipleWindows(true);
        h5settings.setSafeBrowsingEnabled(true);


        webView.setWebViewClient(new WebViewClient());

        webView.addJavascriptInterface(this, "jsBridge");

        // Load the game content from index.html file
        webView.loadUrl("https://fruityloopypg.web.app/");

        botNav.setOnNavigationItemSelectedListener(menuItem -> {

            if (menuItem.getItemId() == R.id.home) {
                openHome();
                Log.d("FruityLoopy", "Home");
            }
            else if (menuItem.getItemId() == R.id.help)
                openGameHelp();
            else if (menuItem.getItemId() == R.id.settings)
                openGameMenu();
            else if (menuItem.getItemId() == R.id.exit)
                finish();

            return false;
        });
    }

    @JavascriptInterface
    public void postMessage(String name, String data) {
        Log.d("WantedWager", "Event:" + name + "\n" + "Data:" + data);
    }

    private void openHome() {
        webView.reload();
    }

    private void openGameHelp() {
        webView.post(() -> webView.evaluateJavascript("SceneGame.instance.GamePaytablePressed()", null));
    }

    private void openGameMenu() {
        webView.post(() -> webView.evaluateJavascript("SceneGame.instance.GamePausePressed()", null));
    }
}


