package com.peatral.anisync.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.peatral.anisync.R;
import com.peatral.anisync.graphql.Requests;
import com.peatral.anisync.graphql.classes.Viewer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        String url = getIntent().getExtras().getString("url");

        WebView wv = findViewById(R.id.webView);
        wv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        WebSettings settings = wv.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setDomStorageEnabled(true);
        wv.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                loadUrl(view, url);
                return true;
            }
        });

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(null);
        cookieManager.flush();

        loadUrl(wv, url);
    }

    public void loadUrl(WebView wv, String url) {

        Uri data = Uri.parse(url);

        String scheme = data.getScheme(); // "peatral.app"
        String host = data.getHost(); // "anisync"
        List<String> segments = data.getPathSegments();
        if(scheme.equals("peatral.app") && host.equals("anisync") && segments.get(0).equals("client")) {

            String[] params = data.getEncodedFragment().split("&|=");
            Map<String, String> pmap = new HashMap<>();
            for (int i = 0; i < params.length; i += 2) pmap.put(params[i], params[i+1]);



            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit()
                    .putString("access_token", pmap.get("access_token"))
                    .putLong("token_expires_in", Long.valueOf(pmap.get("expires_in")))
                    .apply();

            new Thread(() -> {
                Viewer viewer = Requests.fetchViewer().getResponse();
                int userId = viewer.getId();
                String userName = viewer.getName();
                String avatarUrl = viewer.getAvatar().getLarge();

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                sp.edit()
                        .putInt("anilistUserId", userId)
                        .putString("anilistUsername", userName)
                        .putString("anilistAvatarUrl", avatarUrl)
                        .apply();
                if (sp.getBoolean("mal_use_anilist_name", false))
                    sp.edit().putString("malUsername", userName).apply();

                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), getString(R.string.logged_in), Toast.LENGTH_LONG).show();
                    finish();
                });
            }).start();
        } else {
            wv.loadUrl(url);
        }
    }

}
