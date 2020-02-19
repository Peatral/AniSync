package com.peatral.anisync.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.google.android.material.snackbar.Snackbar;
import com.peatral.anisync.App;
import com.peatral.anisync.BuildConfig;
import com.peatral.anisync.R;
import com.peatral.anisync.Settings;
import com.peatral.anisync.SyncJobService;
import com.peatral.anisync.clients.AnilistClient;
import com.peatral.anisync.graphql.Sync;

public class SettingsActivity extends AppCompatActivity {

    private LinearLayout layout;
    private SettingsFragment fragment;
    public SharedPreferences prefs;

    public void snack(String text, int dur){
        Snackbar.make(layout, text, dur);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        AppCompatDelegate.setDefaultNightMode(prefs.getBoolean("nightMode", false) ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.settings_activity);

        SettingsFragment.settingsActivity = this;


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

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        private static SettingsActivity settingsActivity;



        Preference creator;
        Preference version;
        Preference aniList;
        SwitchPreference mal_use_anilist_name;
        SwitchPreference nightMode;
        EditTextPreference malUsername;
        ListPreference periodicSync;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            settingsActivity.prefs.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            creator = findPreference("creator");
            version = findPreference("version");
            aniList = findPreference(Settings.PREF_USER_ANILIST);
            mal_use_anilist_name = findPreference(Settings.PREF_MAL_USE_ANILIST_NAME);
            malUsername = findPreference(Settings.PREF_MAL_USERNAME);
            periodicSync = findPreference(Settings.PREF_PERIODIC_SYNC);

            nightMode = findPreference(Settings.PREF_NIGHTMODE);

            version.setSummary(BuildConfig.VERSION_NAME);

            setSummaryProvider();

            aniList.setOnPreferenceClickListener(preference -> {
                AnilistClient.logAction(settingsActivity);
                return true;
            });



            mal_use_anilist_name.setOnPreferenceChangeListener((preference, newValue) -> {
                SharedPreferences sp = getPreferenceManager().getSharedPreferences();
                if ((boolean) newValue) sp.edit().putString(Settings.PREF_MAL_USERNAME, sp.getString(Settings.PREF_ANILIST_USERNAME, "")).apply();
                else sp.edit().putString(Settings.PREF_MAL_USERNAME, malUsername.getText()).apply();
                return true;
            });

            nightMode.setOnPreferenceChangeListener((preference, newValue) -> {
                settingsActivity.recreate();
                return true;
            });

            creator.setOnPreferenceClickListener(preference -> {
                settingsActivity.snack("Button", Snackbar.LENGTH_LONG);
                return false;
            });

            periodicSync.setOnPreferenceChangeListener((preference, newValue) -> {
                SyncJobService.scheduleByPreference();

                return true;
            });
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            setSummaryProvider();
        }

        private void setSummaryProvider() {
            aniList.setSummaryProvider(preferece -> {
                String s = settingsActivity.prefs.getString(Settings.PREF_ANILIST_USERNAME, getString(R.string.anilist_summary));
                return !s.equals("") ? getString(R.string.anilist_logged_in, s) : getString(R.string.anilist_summary);
            });
            malUsername.setSummaryProvider(preference -> {
                String s;
                if (settingsActivity.prefs.getBoolean(Settings.PREF_MAL_USE_ANILIST_NAME, false)) s = settingsActivity.prefs.getString(Settings.PREF_ANILIST_USERNAME, "");
                else s = settingsActivity.prefs.getString(Settings.PREF_MAL_USERNAME, "");
                return s.equals("") ? getString(R.string.not_set) : s;
            });
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }
}