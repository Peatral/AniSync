package com.peatral.anisync.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
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

public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    public static SharedPreferences prefs;

    private static final String TITLE_TAG = "settingsActivityTitle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        AppCompatDelegate.setDefaultNightMode(Integer.parseInt(prefs.getString("nightMode", String.valueOf(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM))));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new HeaderFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
                    if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                        setTitle(R.string.settings);
                    }});
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit();
        setTitle(pref.getTitle());
        return true;
    }

    public static class HeaderFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.header_preferences, rootKey);
        }
    }

    public static class VisualsFragment extends PreferenceFragmentCompat {

        ListPreference nightMode;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.visuals_preferences, rootKey);

            nightMode = findPreference(Settings.PREF_NIGHTMODE);

            nightMode.setOnPreferenceChangeListener((preference, newValue) -> {
                getActivity().recreate();
                return true;
            });
        }
    }

    public static class AccountsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        Preference aniList;
        SwitchPreference mal_use_anilist_name;
        EditTextPreference malUsername;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.accounts_preferences, rootKey);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
            prefs.registerOnSharedPreferenceChangeListener(this);

            aniList = findPreference(Settings.PREF_USER_ANILIST);
            mal_use_anilist_name = findPreference(Settings.PREF_MAL_USE_ANILIST_NAME);
            malUsername = findPreference(Settings.PREF_MAL_USERNAME);

            setSummaryProvider();

            aniList.setOnPreferenceClickListener(preference -> {
                AnilistClient.logAction(getActivity());
                return true;
            });

            mal_use_anilist_name.setOnPreferenceChangeListener((preference, newValue) -> {
                SharedPreferences sp = getPreferenceManager().getSharedPreferences();
                if ((boolean) newValue) sp.edit().putString(Settings.PREF_MAL_USERNAME, sp.getString(Settings.PREF_ANILIST_USERNAME, "")).apply();
                else sp.edit().putString(Settings.PREF_MAL_USERNAME, malUsername.getText()).apply();
                return true;
            });
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            setSummaryProvider();
        }

        private void setSummaryProvider() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
            aniList.setSummaryProvider(preferece -> {
                String s = prefs.getString(Settings.PREF_ANILIST_USERNAME, getString(R.string.anilist_summary));
                return !s.equals("") ? getString(R.string.anilist_logged_in, s) : getString(R.string.anilist_summary);
            });
            malUsername.setSummaryProvider(preference -> {
                String s;
                if (prefs.getBoolean(Settings.PREF_MAL_USE_ANILIST_NAME, false)) s = prefs.getString(Settings.PREF_ANILIST_USERNAME, "");
                else s = prefs.getString(Settings.PREF_MAL_USERNAME, "");
                return s.equals("") ? getString(R.string.not_set) : s;
            });
        }
    }

    public static class SyncFragment extends PreferenceFragmentCompat {

        ListPreference periodicSync;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.sync_preferences, rootKey);

            periodicSync = findPreference(Settings.PREF_PERIODIC_SYNC);

            periodicSync.setOnPreferenceChangeListener((preference, newValue) -> {
                SyncJobService.scheduleByPreference();

                return true;
            });
        }
    }

    public static class AboutFragment extends PreferenceFragmentCompat {

        Preference creator;
        Preference version;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.about_preferences, rootKey);

            creator = findPreference("creator");
            version = findPreference("version");

            version.setSummary(BuildConfig.VERSION_NAME);
        }
    }

}