package com.mad.fruityloopy;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.appsflyer.AppsFlyerLib;
import com.appsflyer.attribution.AppsFlyerRequestListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.security.GeneralSecurityException;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "WebApp";
    private WebView webView;
    private BottomNavigationView botNav;
    private SecurePreferencesHelper securePreferencesHelper;

    private static final String AppsFlyerDevID = "<YOUR_AF_ID>";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the activity to fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        securePreferencesHelper = new SecurePreferencesHelper(this);

        try {
            securePreferencesHelper.initialize();
            Log.d(TAG, "Secured Preferences Initialized");
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        AppsFlyerLib.getInstance().start(getApplicationContext(), AppsFlyerDevID, new AppsFlyerRequestListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Launch sent successfully, got 200 response code from server");
            }

            @Override
            public void onError(int i, @NonNull String s) {
                Log.d(TAG, "Launch failed to be sent:\n" +
                        "Error code: " + i + "\n"
                        + "Error description: " + s);
            }
        });

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

        // Added: Dev.Jo 04292024
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(TAG, consoleMessage.message());

                String newMsg = null;
                newMsg = consoleMessage.message();

                if(newMsg != null && consoleMessage.message().contains("tracker:dispatch") && !consoleMessage.message().contains("window.Android.tracking"))
                {
                    newMsg = consoleMessage.message().replace("tracker:dispatch ", "");
                    Log.d(TAG, "Message: " + newMsg);
                }

                assert newMsg != null;
                if(!newMsg.contains("window.Android.tracking"))
                {
                    AppsFlyerLib.getInstance().logEvent(MainActivity.this, consoleMessage.message().replace("tracker:dispatch",""), null);

                    if(newMsg.contains("firstDepositArrival") || consoleMessage.message().contains("firstrecharge"))
                    {
                        Handler handler = new Handler();
                        handler.postDelayed(() -> {

                            // Create your Dialog to call Download Link

                        }, 5000);
                    }
                }

                return true;
            }
        });

        webView.setWebViewClient(new WebViewClient());

        webView.loadUrl("https://www.777d.one/m/index.html?affiliateCode=google");
        //webView.loadUrl("https://fruityloopypg.web.app/");

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


