package com.peatral.anisync.graphql;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.peatral.anisync.App;
import com.peatral.anisync.QuickSettingsService;
import com.peatral.anisync.R;
import com.peatral.anisync.Settings;
import com.peatral.anisync.activities.MainActivity;
import com.peatral.anisync.activities.SettingsActivity;
import com.peatral.anisync.graphql.classes.Media;
import com.peatral.anisync.graphql.classes.MediaList;
import com.peatral.anisync.graphql.classes.MediaListGroup;
import com.peatral.anisync.graphql.classes.Page;
import com.peatral.anisync.graphql.classes.SaveMediaListEntry;

import org.javatuples.Pair;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.aexp.nodes.graphql.GraphQLRequestEntity;
import io.aexp.nodes.graphql.exceptions.GraphQLException;

public class Sync {
    private static Sync INSTANCE;

    public static final int ID_STARTED = 0;
    public static final int ID_FINISHED = 1;
    public static final int ID_ERROR = 2;
    public static final int ID_STOPPED = 3;

    public static final String CHANNEL_ID = "com.peatral.maltoanilist";
    public static final int notificationId = 0;

    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder builder;

    private SyncListener listener;
    private QuickSettingsService tileService;
    private boolean syncing = false;

    private List<MediaList> synced;
    private List<MediaList> failed;

    public Sync() {
        createNotificationChannel();
    }

    public static Sync getInstance() {
        if (INSTANCE == null) INSTANCE = new Sync();
        return INSTANCE;
    }

    public void setListener(SyncListener listener) {
        this.listener = listener;
    }

    public QuickSettingsService getTileService() {
        return tileService;
    }

    public void setTileService(QuickSettingsService tileService) {
        this.tileService = tileService;
    }

    public boolean isSyncing() {
        return syncing;
    }

    private SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) App.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void sync() {
        new Thread(this::startSync).start();
    }

    public void startSync() {


        String userNameMal = prefs.getString("malUsername", "");
        int userId = prefs.getInt("anilistUserId", -1);

        if (userId == -1 || !isNetworkAvailable() || userNameMal.equals("")) {
            fail();
            return;
        }


        synced = new ArrayList<>();
        failed = new ArrayList<>();

        start();

        List<MediaList> animeListMal = Requests.getMalList(userNameMal);
        if (animeListMal == null) {
            fail();
            return;
        }

        try {
            List<MediaList> animeListAni = new ArrayList<>();
            for (MediaListGroup mlg : Requests.fetchMediaListCollection(userId).getResponse().getLists())
                animeListAni.addAll(mlg.getEntries());
            animeListMal = filterIgnored(prefs, animeListMal);
            List<MediaList> changes = compareLists(animeListMal, animeListAni);

            if (changes.size() > 0) {
                List<GraphQLRequestEntity> changesRequests = new ArrayList<>();
                for (MediaList change : changes) {
                    changesRequests.add(Requests.mediaListEntryRequest(
                            change.getMedia().getId(),
                            change.getStatus(),
                            change.getProgress(),
                            change.getScore()
                    ));
                    System.out.println(change.getMedia().getTitle().getRomajiTitle() + " changed");
                }
                List<String> changesStrings = new ArrayList<>();
                for (GraphQLRequestEntity requestEntity : changesRequests) {
                    changesStrings.add(requestEntity.getRequest());
                }

                String s = Requests.batchMutation(changesStrings).body().string();

                org.json.simple.JSONObject response = (org.json.simple.JSONObject) JSONValue.parse(s);
                org.json.simple.JSONObject data = (JSONObject) response.get("data");
                ObjectMapper mapper = new ObjectMapper();
                for (int i = 0; i < changes.size(); i++) {
                    SaveMediaListEntry saveMediaListEntry = mapper.readValue(((org.json.simple.JSONObject) (data).get("q" + i)).toJSONString(), SaveMediaListEntry.class);
                    MediaList mediaList = changes.get(i);
                    if (saveMediaListEntry.getMediaId() == mediaList.getMedia().getId()) {
                        if (
                                saveMediaListEntry.getProgress() != mediaList.getProgress() ||
                                        saveMediaListEntry.getScore() != mediaList.getScore() ||
                                        saveMediaListEntry.getStatus() != mediaList.getStatus()
                        ) {
                            failed.add(mediaList);
                        } else {
                            synced.add(mediaList);
                        }
                    }
                }
            }
            finish();
        } catch (GraphQLException | IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    private List<MediaList> compareLists(List<MediaList> mal, List<MediaList> ani) {
        List<MediaList> notFound = new ArrayList<>();
        List<Pair<MediaList, MediaList>> difference = new ArrayList<>();

        List<MediaList> aniClone = new ArrayList<>();
        aniClone.addAll(ani);

        for (MediaList mediaList : mal) {
            int idx = 0;
            boolean found = false;
            while (idx < aniClone.size() && !found) {
                MediaList aniEntry = aniClone.get(idx);

                if (mediaList.getMedia().getIdMal() == aniEntry.getMedia().getIdMal()) {
                    found = true;
                    aniClone.remove(aniEntry);
                    if (!mediaListsAreEqual(mediaList, aniEntry)) {
                        difference.add(new Pair<>(mediaList, aniEntry));
                    }
                }
                idx++;
            }
            if (!found) {
                notFound.add(mediaList);
            }
        }

        List<MediaList> updates = new ArrayList<>();
        List<Media> medias = new ArrayList<>();
        for (MediaList mediaList : notFound) medias.add(mediaList.getMedia());
        medias = Requests.getAniIds(medias);
        for (MediaList mediaList : notFound) {
            boolean found = false;
            for (Media media : medias) {
                if (media.getIdMal() == mediaList.getMedia().getIdMal()) {
                    mediaList.setMedia(media);
                    updates.add(mediaList);
                    found = true;
                }
            }
            if (!found) failed.add(mediaList);
        }


        for (Pair<MediaList, MediaList> pair : difference) {
            MediaList malValue = pair.getValue0();
            MediaList aniValue = pair.getValue1();

            MediaList result = new MediaList();
            result.setMedia(aniValue.getMedia());

            result.setScore(malValue.getScore());// > aniValue.getScore() ? malValue.getScore() : aniValue.getScore());
            result.setStatus(malValue.getStatus());
            result.setProgress(malValue.getProgress());// > aniValue.getProgress() ? malValue.getProgress() : aniValue.getProgress());

            updates.add(result);
        }

        return updates;
    }

    public static boolean mediaListsAreEqual(MediaList a, MediaList b) {
        boolean equal = true;

        if (a.getMedia().getIdMal() != b.getMedia().getIdMal()) equal = false;

        if (a.getScore() != b.getScore()) equal = false;
        if (a.getStatus() != b.getStatus()) equal = false;
        if (a.getProgress() != b.getProgress()) equal = false;

        return equal;
    }

    public static List<MediaList> filterIgnored(SharedPreferences prefs, List<MediaList> listIn) {
        Gson gson = new Gson();
        String ignString = prefs.getString("listIgnored", "");
        if (!ignString.equals("")) {
            List<MediaList> ign = gson.fromJson(ignString, new TypeToken<List<MediaList>>() {}.getType());
            List<MediaList> toRemove = new ArrayList<>();
            for (MediaList a1 : listIn) for (MediaList a2 : ign) if (a1.getMedia().getIdMal() == a2.getMedia().getIdMal()) toRemove.add(a1);
            for (MediaList a1 : toRemove) listIn.remove(a1);
        }
        return listIn;
    }

    private void start() {
        syncing = true;

        update();
        notification(ID_STARTED);
    }

    private void fail() {
        syncing = false;

        update();
        notification(ID_ERROR);
    }

    private void finish() {
        syncing = false;

        commit();
        update();
        notification(ID_FINISHED);

    }

    private void update() {
        if (tileService != null) {
            tileService.updateTile();
        }
    }

    private void commit() {
        if (synced.size() > 0 || failed.size() > 0) {
            Gson gson = new Gson();
            String jsonFailed = gson.toJson(failed);
            String jsonSynced = gson.toJson(synced);
            prefs.edit()
                    .putString("lastSynced", jsonSynced)
                    .putString("lastFailed", jsonFailed)
                    .commit();
        }
    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = App.getContext().getString(R.string.progress_channel_name);
            String description = App.getContext().getString(R.string.progress_channel_desc);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = App.getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void notification(int id) {


        String text = "";

        switch (id) {
            case ID_STARTED:
                Intent intent = new Intent(App.getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(App.getContext(), 0, intent, 0);

                text = App.getContext().getString(R.string.sync_started);
                notificationManager = NotificationManagerCompat.from(App.getContext());
                builder = new NotificationCompat.Builder(App.getContext(), CHANNEL_ID);
                builder.setContentTitle(App.getContext().getString(R.string.tile_label))
                        .setSmallIcon(R.drawable.ic_fab_sync)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setOngoing(true).setContentIntent(pendingIntent);
                break;
            case ID_FINISHED:
                text = App.getContext().getString(R.string.notification_text_complete, failed.size(), synced.size());
                break;
            case ID_ERROR:
                text = App.getContext().getString(R.string.sync_error);
                break;
            case ID_STOPPED:
                text = App.getContext().getString(R.string.sync_stopped);
                break;
        }
        if (builder != null && notificationManager != null && prefs.getBoolean(Settings.PREF_NOTIFICATION, true)) {
            builder.setContentText(text);
            if(id == ID_STARTED) {

            } else {
                builder.setOngoing(false).setAutoCancel(true);
            }
            notificationManager.notify(getClass().getPackage().getName(), notificationId, builder.build());
        }
        if(id != ID_STARTED){
            builder = null;
            notificationManager = null;
        }
        if (listener != null) {
            listener.message(text, id);
        }
    }
}
