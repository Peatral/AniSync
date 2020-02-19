package com.peatral.anisync.fragments;

import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.peatral.anisync.App;
import com.peatral.anisync.R;
import com.peatral.anisync.graphql.classes.MediaList;

import java.util.ArrayList;
import java.util.List;

import mva3.adapter.ListSection;
import mva3.adapter.MultiViewAdapter;
import mva3.adapter.util.Mode;

class MediaListAdapter extends MultiViewAdapter {

    private Mode selectionMode;

    private ListSection<MediaList> section;

    private ActionMode actionMode;

    private int state;

    private List<MediaListBinder.ViewHolder> holders = new ArrayList<>();

    private RecyclerView recyclerView;

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());

    public MediaListAdapter(List<MediaList> list, int state, RecyclerView recyclerView) {
        this.state = state;
        this.recyclerView = recyclerView;

        MediaListBinder binder = new MediaListBinder(this);
        registerItemBinders(binder);

        section = new ListSection();
        section.addAll(list);
        section.setOnSelectionChangedListener((item, isSelected, selectedItems) -> updateActionModeTitle());
        addSection(section);

        setSelectionMode(Mode.NONE);
    }



    @Override
    public void setSelectionMode(Mode selectionMode) {
        super.setSelectionMode(selectionMode);
        this.selectionMode = selectionMode;
    }

    public Mode getSelectionMode() {
        return selectionMode;
    }

    public int getState() {
        return state;
    }

    public void startActionMode(AppCompatActivity a) {
        startActionMode();
        a.startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.list_context, menu);
                actionMode = mode;
                setSelectionMode(Mode.MULTIPLE);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_mode_delete:
                        if (state != 2) addToIgnore(section.getSelectedItems());
                        else removeFromIgnore(section.getSelectedItems());
                        mode.finish();
                        return true;
                    case R.id.action_mode_select_all:
                        if (section.getSelectedItems().size() == section.getData().size()) deselectAll();
                        else selectAll();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                deselectAll();
                setSelectionMode(Mode.NONE);
                stopActionMode();
            }
        });
    }

    public void addToIgnore(MediaList listMedia) {
        List<MediaList> medias = new ArrayList<>();
        medias.add(listMedia);
        removeFromIgnore(medias);
    }

    public void addToIgnore(List<MediaList> listMedia) {
        String listString = prefs.getString("listIgnored", "");
        Gson gson = new Gson();
        List<MediaList> ign = new ArrayList<>();
        if (!listString.equals(""))
            ign = gson.fromJson(listString, new TypeToken<List<MediaList>>() {
            }.getType());

        List<MediaList> toRemove = new ArrayList<>();
        for (MediaList media : listMedia) {
            for (MediaList a : ign) if (a.getMedia().getIdMal() == media.getMedia().getIdMal()) toRemove.add(a);
            for (MediaList a : toRemove) ign.remove(a);
            ign.add(media);

            for (int i = 0; i < section.getData().size(); i++) {
                if (section.get(i) == media) {
                    section.remove(i);
                }
            }


        }

        prefs.edit().putString("listIgnored", gson.toJson(ign)).apply();

        notifyDataSetChanged();
    }

    public void removeFromIgnore(MediaList listMedia) {
        List<MediaList> medias = new ArrayList<>();
        medias.add(listMedia);
        removeFromIgnore(medias);
    }

    public void removeFromIgnore(List<MediaList> listMedia) {

        String listString = prefs.getString("listIgnored", "");
        Gson gson = new Gson();
        List<MediaList> ign = new ArrayList<>();
        if (!listString.equals(""))
            ign = gson.fromJson(listString, new TypeToken<List<MediaList>>() {
            }.getType());

        for (MediaList media : listMedia) {
            for (MediaList a : ign) if (a.getMedia().getIdMal() == media.getMedia().getIdMal()) {
                ign.remove(a);
                break;
            }


            for (int i = 0; i < section.getData().size(); i++) {
                if (section.get(i) == media) {
                    section.remove(i);
                }
            }
        }

        prefs.edit().putString("listIgnored", gson.toJson(ign)).apply();

        notifyDataSetChanged();
    }

    public void addHolder(MediaListBinder.ViewHolder holder) {
        if (!holders.contains(holder)) holders.add(holder);
    }

    public void selectAll() {
        for (MediaListBinder.ViewHolder holder : holders) if (!holder.isItemSelected()) holder.toggleItemSelection();
        updateActionModeTitle();
    }

    public void deselectAll() {
        clearAllSelections();
        updateActionModeTitle();
    }

    public void reload(List<MediaList> list) {
        if (!section.getData().equals(list)) {
            section.clear();
            section.addAll(list);
            notifyDataSetChanged();
        }

        holders = new ArrayList<>();
        if (actionMode != null) {
            actionMode.finish();
            actionMode = null;
        }
    }

    public void updateActionModeTitle() {
        actionMode.setTitle(App.getContext().getString(R.string.selected, section.getSelectedItems().size()));
    }


}
