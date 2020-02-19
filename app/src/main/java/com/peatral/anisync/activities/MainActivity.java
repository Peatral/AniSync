package com.peatral.anisync.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.peatral.anisync.R;
import com.peatral.anisync.clients.AnilistClient;
import com.peatral.anisync.fragments.MediaListFragment;
import com.peatral.anisync.graphql.Sync;
import com.peatral.anisync.graphql.SyncListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SyncListener, SharedPreferences.OnSharedPreferenceChangeListener {


    private TextView mal, anilist;
    private ImageView anilistAvatar;
    private SharedPreferences prefs;
    private FloatingActionButton fab;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private int userId;

    private int clickedNavItem = 0;
    private boolean hideDrawerOnResume = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

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
                Sync.getInstance().sync();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        updateScreen();

        Sync.getInstance().setListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hideDrawerOnResume) hideDrawer();
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_clearSyncHistory:
                prefs.edit()
                        .putString("lastSynced", "")
                        .putString("lastFailed", "")
                        .apply();
                Snackbar.make(fab, getString(R.string.snack_clearedHistory), Snackbar.LENGTH_LONG).show();
                return true;
            case R.id.action_clearIgnored:
                prefs.edit().putString("listIgnored", "").apply();
                Snackbar.make(fab, getString(R.string.snack_clearedIgnored), Snackbar.LENGTH_LONG).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        clickedNavItem = item.getItemId();
        switch (clickedNavItem) {
            case R.id.nav_settings:
                hideDrawerOnResume = true;
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.nav_login:
                AnilistClient.logAction(this);
                if (userId == -1) {
                    hideDrawerOnResume = true;
                    break;
                }
            default:
                closeDrawer();
        }

        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        updateScreen();
    }

    public void closeDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void hideDrawer() {
        hideDrawerOnResume = false;
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(Gravity.LEFT, false);
    }

    public void updateScreen() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        userId = prefs.getInt("anilistUserId", -1);
        MenuItem login = navigationView.getMenu().getItem(0);
        if (userId != -1) login.setTitle(R.string.menu_logout);
        else login.setTitle(R.string.menu_login);

        mal = headerView.findViewById(R.id.nav_header_mal);
        String malUsername = prefs.getString("malUsername", getString(R.string.nav_header_mal));
        mal.setText(!malUsername.equals("") ? malUsername : getString(R.string.nav_header_mal));

        anilist = headerView.findViewById(R.id.nav_header_anilist);
        String anilistUsername = prefs.getString("anilistUsername", getString(R.string.nav_header_anilist));
        anilist.setText(!anilistUsername.equals("") ? anilistUsername : getString(R.string.nav_header_anilist));

        anilistAvatar = headerView.findViewById(R.id.nav_header_avatar);
        String url = prefs.getString("anilistAvatarUrl", "");
        if(!url.equals("")) Picasso.get().load(url).into(anilistAvatar);
        else anilistAvatar.setImageResource(R.drawable.ic_tri);
    }

    @Override
    public void message(String text, int id) {
        runOnUiThread(() -> Toast.makeText(this, text, Toast.LENGTH_LONG).show());
        //Snackbar.make(fab, text, Snackbar.LENGTH_LONG).show());//
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            MediaListFragment lf = new MediaListFragment();
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
