package com.peatral.anisync.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.peatral.anisync.R;
import com.peatral.anisync.clients.AnilistClient;
import com.peatral.anisync.graphql.Sync;
import com.peatral.anisync.graphql.SyncListener;
import com.peatral.anisync.lib.AnimeEntry;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SyncListener {


    private TextView mal, anilist;
    private ImageView anilistAvatar;
    private SharedPreferences prefs;
    private FloatingActionButton fab;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener((sharedPreferences, s) -> recreate());

        userId = prefs.getInt("anilistUserId", -1);

        AppCompatDelegate.setDefaultNightMode(prefs.getBoolean("nightMode", true) ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.view_pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        //tabLayout.setupWithViewPager(mViewPager);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            if (!Sync.getInstance().isSyncing()) {
                Sync.getInstance().fetchAnimeList();
                //Snackbar.make(view, getString(R.string.sync_started), Snackbar.LENGTH_LONG).show();
            }
            else {
                Sync.getInstance().reset(Sync.ID_STOPPED);
                //Snackbar.make(view, getString(R.string.sync_stopped), Snackbar.LENGTH_LONG).show();
            }
            //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            //        .setAction("Action", null).show();
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


        View headerView = navigationView.getHeaderView(0);

        MenuItem login = navigationView.getMenu().getItem(0);
        if (userId != -1) login.setTitle(R.string.menu_logout);

        mal = headerView.findViewById(R.id.nav_header_mal);
        String malUsername = prefs.getString("malUsername", getString(R.string.nav_header_mal));
        mal.setText(!malUsername.equals("") ? malUsername : getString(R.string.nav_header_mal));

        anilist = headerView.findViewById(R.id.nav_header_anilist);
        String anilistUsername = prefs.getString("anilistUsername", getString(R.string.nav_header_anilist));
        anilist.setText(!anilistUsername.equals("") ? anilistUsername : getString(R.string.nav_header_anilist));

        anilistAvatar = headerView.findViewById(R.id.nav_header_avatar);
        String url = prefs.getString("anilistAvatarUrl", "");
        if(!url.equals("")) Picasso.get().load(url).into(anilistAvatar);

        Sync.getInstance().setListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_clearSyncHistory:
                prefs.edit()
                        .putString("lastSynced", "")
                        .putString("lastFailed", "")
                        .apply();
                mSectionsPagerAdapter.notifyDataSetChanged();
                Snackbar.make(fab, getString(R.string.snack_clearedHistory), Snackbar.LENGTH_LONG).show();
                return true;
            case R.id.action_clearIgnored:
                prefs.edit().putString("listIgnored", "").apply();
                Snackbar.make(fab, getString(R.string.snack_clearedIgnored), Snackbar.LENGTH_LONG).show();
                mSectionsPagerAdapter.notifyDataSetChanged();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_login) {
            AnilistClient.logAction(this);

        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userId != prefs.getInt("anilistUserId", -1)) recreate();

    }

    @Override
    public void message(String text, int id) {
        Snackbar.make(fab, text, Snackbar.LENGTH_LONG).show();
        runOnUiThread(() -> mSectionsPagerAdapter.notifyDataSetChanged());
    }

    @Override
    public void animeSynced(AnimeEntry anime) {

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            ListFragment lf = new ListFragment();
            Bundle b = new Bundle();
            b.putInt("tab", position);
            lf.setArguments(b);
            return lf;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
