package com.peatral.anisync.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.peatral.anisync.App;
import com.peatral.anisync.R;
import com.peatral.anisync.graphql.Sync;
import com.peatral.anisync.graphql.classes.MediaList;

import java.util.ArrayList;
import java.util.List;

public class MediaListFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {


    public static final String[] TABS = new String[]{"lastSynced", "lastFailed", "listIgnored"};

    private SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());

    private int state = 0;

    private RecyclerView listView;
    private SwipeRefreshLayout refreshlayout;

    private List<MediaList> list = new ArrayList<>();

    private MediaListAdapter adapter;

    public MediaListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        listView = view.findViewById(R.id.listView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        listView.setLayoutManager(layoutManager);
        refreshlayout = view.findViewById(R.id.swipeRefreshLayout);
        refreshlayout.setEnabled(true);

        prefs.registerOnSharedPreferenceChangeListener(this);

        Bundle b = getArguments();
        if (b != null) state = b.getInt("tab");

        list = getMediaLists();
        setUpAdapter(list);


        refreshlayout.setOnRefreshListener(() -> {
            refreshlayout.setRefreshing(false);
            reload();
        });


        return view;
    }

    public List<MediaList> getMediaLists() {
        List<MediaList> mediaLists = new ArrayList<>();
        String listString = prefs.getString(TABS[state], "");
        Gson gson = new Gson();
        if (!listString.equals("")) {
            List<MediaList> newList = gson.fromJson(listString, new TypeToken<List<MediaList>>() {
            }.getType());
            if (state != 2) {
                mediaLists.addAll(Sync.filterIgnored(prefs, newList));
            } else {
                mediaLists.addAll(newList);
            }
        }
        return mediaLists;
    }


    public void setUpAdapter(List<MediaList> mediaList) {
        adapter = new MediaListAdapter(mediaList, state, listView);
        listView.setAdapter(adapter);
    }

    public void reload() {
        list = getMediaLists();
        adapter.reload(list);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        reload();
    }
}

