package com.peatral.anisync.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.google.android.material.snackbar.Snackbar;
import com.peatral.anisync.BuildConfig;
import com.peatral.anisync.R;
import com.peatral.anisync.clients.AnilistClient;

public class SettingsActivity extends AppCompatActivity {

    private LinearLayout layout;
    private SettingsFragment fragment;
    public SharedPreferences prefs;
    private int userId;

    public void snack(String text, int dur){
        Snackbar.make(layout, text, dur);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsFragment.settingsActivity = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener((sharedPreferences, s) -> recreate());

        userId = prefs.getInt("anilistUserId", -1);

        AppCompatDelegate.setDefaultNightMode(prefs.getBoolean("nightMode", false) ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.settings_activity);

        fragment = new SettingsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, fragment)
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        layout = findViewById(R.id.settings_linearLayout);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private static SettingsActivity settingsActivity;

        Preference creator;
        Preference version;
        Preference aniList;
        Preference mal_use_anilist_name;


        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            creator = findPreference("creator");
            version = findPreference("version");
            aniList = findPreference("userAniList");
            mal_use_anilist_name = findPreference("mal_use_anilist_name");

            SwitchPreference themeList = findPreference("nightMode");

            version.setSummary(BuildConfig.VERSION_NAME);

            String summary = settingsActivity.prefs.getString("anilistUsername", getString(R.string.anilist_summary));
            aniList.setSummary(!summary.equals("") ? summary : getString(R.string.anilist_summary));

            creator.setOnPreferenceClickListener(preference -> {
                Log.d("SharedPreferenceClicked", "Creator");
                settingsActivity.snack("Button", Snackbar.LENGTH_LONG);
                return false;
            });

            aniList.setOnPreferenceClickListener(preference -> {
                AnilistClient.logAction(settingsActivity);
                return true;
            });

            mal_use_anilist_name.setOnPreferenceChangeListener((preference, newValue) -> {
                SharedPreferences sp = getPreferenceManager().getSharedPreferences();
                if ((boolean) newValue) sp.edit().putString("malUsername", sp.getString("anilistUsername", "")).apply();
                settingsActivity.recreate();
                return true;
            });

            themeList.setOnPreferenceChangeListener((preference, newValue) -> {
                Log.d("SharedPreferenceChanged", newValue.toString());
                settingsActivity.recreate();
                return true;
            });


        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userId != prefs.getInt("anilistUserId", -1)) recreate();
    }
}