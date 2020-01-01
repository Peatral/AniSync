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

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.peatral.anisync.R;
import com.peatral.anisync.clients.AnilistApolloClient;
import com.peatral.anisync.graphql.ViewerQuery;

import org.jetbrains.annotations.NotNull;

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
        System.out.println(url);
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

            ViewerQuery viewerQuery = ViewerQuery.builder().build();
            AnilistApolloClient.getApolloClient(getApplicationContext()).query(viewerQuery).enqueue(new ApolloCall.Callback<ViewerQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<ViewerQuery.Data> response) {
                    int userId = response.data().Viewer().id();
                    String userName = response.data().Viewer().name();
                    String avatarUrl = response.data().Viewer().avatar().large();
                    Log.d("USER DATA", userName + " (" + userId + ") " + avatarUrl);

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
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {

                }
            });
        } else {
            wv.loadUrl(url);
        }
    }

}
