package com.peatral.anisync.activities;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.peatral.anisync.R;
import com.peatral.anisync.graphql.Sync;
import com.peatral.anisync.lib.AnimeEntry;

import java.util.ArrayList;
import java.util.List;

public class ListFragment extends Fragment {

    private List<AnimeEntry> list = new ArrayList<>();;

    private ListViewAdapter la;

    private SharedPreferences prefs;

    private int state = 0;

    public ListView listView;

    public ListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        listView = view.findViewById(R.id.listView);

        Bundle b = getArguments();
        if(b != null) state = b.getInt("tab");

        reload();

        listView.setOnItemClickListener((adapterView, view1, i, l) -> {
            AnimeEntry anime = (AnimeEntry) adapterView.getItemAtPosition(i);
        });

        listView.setOnItemLongClickListener((adapterView, view1, i, l) -> {
            AnimeEntry anime = (AnimeEntry) adapterView.getItemAtPosition(i);
            if (state != 2) {
                new AlertDialog.Builder(getContext())
                        .setMessage(getString(R.string.dialog_ignore_anime, anime.name))
                        .setNegativeButton(R.string.cancel, (dialog, which) -> {
                        })
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            String listString = prefs.getString("listIgnored", "");
                            Gson gson = new Gson();
                            List<AnimeEntry> ign = new ArrayList<>();
                            if (!listString.equals(""))
                                ign = gson.fromJson(listString, new TypeToken<List<AnimeEntry>>() {
                                }.getType());
                            List<AnimeEntry> toRemove = new ArrayList<>();
                            for (AnimeEntry a : ign) if (a.malId == anime.malId) toRemove.add(a);
                            for (AnimeEntry a : toRemove) ign.remove(a);
                            ign.add(anime);
                            prefs.edit().putString("listIgnored", gson.toJson(ign)).apply();
                            reload();
                        }).create().show();
            } else {
                new AlertDialog.Builder(getContext())
                        .setMessage(getString(R.string.dialog_not_ignore_anime, anime.name))
                        .setNegativeButton(R.string.cancel, (dialog, which) -> {
                        })
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            String listString = prefs.getString("listIgnored", "");
                            Gson gson = new Gson();
                            List<AnimeEntry> ign = new ArrayList<>();
                            if (!listString.equals(""))
                                ign = gson.fromJson(listString, new TypeToken<List<AnimeEntry>>() {
                                }.getType());
                            ign.remove(anime);
                            prefs.edit().putString("listIgnored", gson.toJson(ign)).apply();
                            reload();
                        }).create().show();
            }
            return true;
        });

        return view;
    }

    public void reload(){
        String listString = prefs.getString(new String[]{"lastSynced", "lastFailed", "listIgnored"}[state], "");
        Gson gson = new Gson();
        if (!listString.equals("")) {
            list = gson.fromJson(listString, new TypeToken<List<AnimeEntry>>() {}.getType());
            if (state != 2) list = Sync.filterIgnored(prefs, list);
        } else {
            list = new ArrayList<>();
        }

        la = new ListViewAdapter(list, getContext());
        listView.setAdapter(la);
    }
}

