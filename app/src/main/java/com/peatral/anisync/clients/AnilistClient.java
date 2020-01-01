package com.peatral.anisync.clients;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.peatral.anisync.App;
import com.peatral.anisync.R;
import com.peatral.anisync.activities.WebViewActivity;

public class AnilistClient {
    public static final String clientID = "2383";
    public static final String clientSecret = "msmgJsW9hN8nSVgzfJuq8qbGW2rsSEjzSsQZKmno";
    public static final String redirectURI = "peatral.app://anisync/client";

    public static String getAuthCodeLink(){
        //return "https://anilist.co/api/v2/oauth/authorize?client_id=" + clientID + "&redirect_uri="+ redirectURI + "&response_type=code";
        return "https://anilist.co/api/v2/oauth/authorize?client_id=" + clientID + "&response_type=token";
    }

    public static void logAction(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        int userId = prefs.getInt("anilistUserId", -1);

        if (userId == -1) {
            openLogin();
        } else {
            openLogout(c);
        }
    }



    public static void openLogin() {
        Intent i = new Intent(App.getContext(), WebViewActivity.class);
        i.putExtra("url", AnilistClient.getAuthCodeLink());
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        App.getContext().startActivity(i);
    }

    public static void openLogout(Context c) {
        new AlertDialog.Builder(c)
                .setMessage(R.string.dialog_logout)
                .setNegativeButton(R.string.cancel, (dialog, which) -> {})
                .setPositiveButton(R.string.logout, (dialog, which) -> logout()).create().show();
    }

    public static void logout() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        if (sp.getBoolean("mal_use_anilist_name", false))
            sp.edit().putString("malUsername", "").apply();
        sp.edit()
                .putInt("anilistUserId", -1)
                .putString("anilistUsername", "")
                .putString("anilistAvatarUrl", "")
                .putString("access_token", "")
                .putLong("token_expires_in", 0L)
                .apply();
    }
}
