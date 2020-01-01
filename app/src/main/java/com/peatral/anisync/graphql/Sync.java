package com.peatral.anisync.graphql;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.exception.ApolloException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.peatral.anisync.App;
import com.peatral.anisync.QuickSettingsService;
import com.peatral.anisync.R;
import com.peatral.anisync.lib.AnimeEntry;
import com.peatral.anisync.lib.ListStatus;
import com.peatral.anisync.clients.AnilistApolloClient;

import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Sync {

    public static final int ID_STARTED = 0;
    public static final int ID_FINISHED = 1;
    public static final int ID_ERROR = 2;
    public static final int ID_STOPPED = 3;

    private boolean syncing = false;

    //private int synced = 0;
    //private int toSync = 0;
    //private int failed = 0;

    private static Sync ourInstance = new Sync();

    private QuickSettingsService tileService;

    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder builder;

    private SyncListener listener;

    private List<AnimeEntry> animes, failed, synced;

    private SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());

    public static final String CHANNEL_ID = "com.peatral.maltoanilist";
    public static final int notificationId = 0;

    public static Sync getInstance() {
        if(ourInstance == null) ourInstance = new Sync();
        return ourInstance;
    }

    public Sync() {
        createNotificationChannel();
    }

    private void addFailed(AnimeEntry anime) {
        failed.add(anime);
        updateSynced();
    }

    private void addSynced(AnimeEntry anime) {
        synced.add(anime);
        updateSynced();
    }

    public void fetchAnimeList() {
        notification(ID_STARTED);
        syncing = true;

        animes = new ArrayList<>();
        failed = new ArrayList<>();
        synced = new ArrayList<>();

        String username_mal = prefs.getString("malUsername", "");
        int id_anilist = prefs.getInt("anilistUserId", -1);

        if(id_anilist == -1 || username_mal.equals("")) {
            reset(ID_ERROR);
            return;
        }


        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.jikan.moe/v3/user/" + username_mal + "/animelist/all")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                reset(ID_ERROR);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String body = response.body().string();
                Log.d("ANIME LIST BODY", body);
                Object obj = JSONValue.parse(body);

                List<AnimeEntry> listMal = new ArrayList<>();

                try {
                    JSONObject jsonObj = (JSONObject) obj;
                    JSONArray array = (JSONArray) jsonObj.get("anime");
                    if (array != null) {
                        for (Object listObj : array) {
                            JSONObject anime = (JSONObject) listObj;
                            AnimeEntry animeEntry = new AnimeEntry();
                            animeEntry.malId = ((Long) anime.get("mal_id")).intValue();
                            animeEntry.score = ((Long) anime.get("score")).intValue();
                            animeEntry.progress = ((Long) anime.get("watched_episodes")).intValue();
                            animeEntry.name = ((String) anime.get("title"));
                            animeEntry.image_url = ((String) anime.get("image_url"));
                            animeEntry.status = ListStatus.fromJikanId(((Long) anime.get("watching_status")).intValue());
                            listMal.add(animeEntry);
                        }
                    } else {
                        reset(ID_ERROR);
                        try {
                            String message = (String) jsonObj.get("message");
                            Log.d("ERROR", message);
                        } catch (ClassCastException e) {
                            e.printStackTrace();
                        }

                    }

                } catch (ClassCastException e) {
                    e.printStackTrace();
                }

                UserAnimeListQuery userAnimeListQuery = UserAnimeListQuery.builder()
                        .userid(id_anilist)
                        .build();
                AnilistApolloClient.getApolloClient(App.getContext()).query(userAnimeListQuery).enqueue(new ApolloCall.Callback<UserAnimeListQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull com.apollographql.apollo.api.Response<UserAnimeListQuery.Data> response) {
                        Log.d("PAGE DATA", response.data().toString());
                        if (response.data() != null && response.data().MediaListCollection != null) {
                            List<AnimeEntry> listAnilist = new ArrayList<>();
                            for (UserAnimeListQuery.List mediaList : response.data().MediaListCollection().lists()) {
                                for (UserAnimeListQuery.Entry entry : mediaList.entries()) {
                                    AnimeEntry anime = new AnimeEntry();
                                    anime.progress = entry.progress();
                                    if (entry.score != null) anime.score = entry.score().intValue();
                                    anime.status = ListStatus.fromAnilist(entry.status().rawValue());
                                    UserAnimeListQuery.Media media = entry.media();
                                    if (media != null) {
                                        try { //Weird shit, too tired todo something here
                                            anime.malId = media.idMal();
                                        } catch(NullPointerException e) {
                                            e.printStackTrace();
                                        }
                                        anime.name = media.title().romaji();
                                    }
                                    listAnilist.add(anime);
                                }
                            }
                            animes = filterIgnored(prefs, AnimeEntry.findDifference(listMal, listAnilist));
                            Log.d("LIST TO COMPARE", animes.toString());
                            if (animes.size() > 0) {
                                syncAnimeList(animes);
                            } else {
                                reset(ID_FINISHED);
                            }
                        } else {
                            reset(ID_ERROR);
                        }
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        reset(ID_ERROR);
                    }
                });

            }
        });
    }

    public void syncAnimeList(List<AnimeEntry> list) {

        for (AnimeEntry anime : list) {
            if (!syncing) break;
            if (prefs.contains(anime.getIdPreferenceKey()) && prefs.getInt(anime.getIdPreferenceKey(), -1) != -1) {
                int id = prefs.getInt(anime.getIdPreferenceKey(), -1);
                anime.id = id;
                if (anime.id != -1) {
                    syncAnime(anime);
                } else {
                    addFailed(anime);
                }

            } else {
                AnimeByMalIDQuery malIDQuery = AnimeByMalIDQuery.builder()
                        .idMal(anime.malId)
                        .build();
                AnilistApolloClient.getApolloClient(App.getContext()).query(malIDQuery).enqueue(new ApolloCall.Callback<AnimeByMalIDQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull com.apollographql.apollo.api.Response<AnimeByMalIDQuery.Data> response) {
                        if (response.data().Media() != null) {
                            anime.id = response.data().Media().id;

                            Log.d("RESPONSE CALLBACK", response.data().toString());
                            if (anime.id != -1) {
                                syncAnime(anime);
                                prefs.edit().putInt(anime.getIdPreferenceKey(), anime.id).commit();
                            } else {
                                addFailed(anime);
                            }
                        } else {
                            addFailed(anime);
                        }

                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        addFailed(anime);
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            if (!syncing) return;
        }

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            if(syncing) reset(ID_ERROR);
        }, 30000);


    }

    public void syncAnime(AnimeEntry anime) {
        if (!syncing) return;
        SaveMediaListEntryMutation mutation = SaveMediaListEntryMutation.builder()
                .mediaId(anime.id)
                .progress(anime.progress)
                .scoreRaw(anime.score * 10)
                .status(anime.status.getMediaListStatus())
                .build();
        AnilistApolloClient.getApolloClient(App.getContext()).mutate(mutation).enqueue(new ApolloCall.Callback<SaveMediaListEntryMutation.Data>() {
            @Override
            public void onResponse(@NotNull com.apollographql.apollo.api.Response<SaveMediaListEntryMutation.Data> response) {
                Log.d("MUTATION RESPONSE", response.data().toString());
                anime.synced = true;
                if (response.data() == null) {
                    anime.synced = false;
                    addFailed(anime);
                } else {
                    addSynced(anime);
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                addFailed(anime);
            }
        });
    }

    public boolean isSyncing() {
        return syncing;
    }

    public void updateSynced() {
        updateNotification();

        Gson gson = new Gson();
        String jsonFailed = gson.toJson(failed);
        String jsonSynced = gson.toJson(synced);
        prefs.edit()
                .putString("lastSynced", jsonSynced)
                .putString("lastFailed", jsonFailed)
                .commit();

        Log.d("SYNCED", (synced.size() + failed.size()) + "/" + animes.size());
        if (synced.size() + failed.size() == animes.size()) {
            reset(ID_FINISHED);
            System.out.println("Animes: " + animes);
            System.out.println("Synced: " + synced);
            System.out.println("Failed: " + failed);
        }

        if (tileService != null) {
            tileService.updateTile();
        }
    }

    public QuickSettingsService getTileService() {
        return tileService;
    }

    public void setTileService(QuickSettingsService tileService) {
        this.tileService = tileService;
    }

    public void reset(int id) {
        Log.d("RESET", "ID: " + id);
        syncing = false;
        notification(id);
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
                text = App.getContext().getString(R.string.sync_started);
                notificationManager = NotificationManagerCompat.from(App.getContext());
                builder = new NotificationCompat.Builder(App.getContext(), CHANNEL_ID);
                builder.setContentTitle(App.getContext().getString(R.string.tile_label))
                        .setSmallIcon(R.drawable.ic_fab_sync)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setOngoing(true);
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
        if (builder != null && notificationManager != null) {
            builder.setContentText(text)
                    .setProgress(0, 0, false);
            if(id == ID_STARTED) {
                builder.setProgress(1, 0, false);
            } else {
                builder.setOngoing(false);
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
        if (tileService != null) {
            tileService.updateTile();
        }
    }

    private void updateNotification() {
        if (builder != null && notificationManager != null) {
            builder.setProgress(animes.size(), synced.size(), false)
                    .setContentText( App.getContext().getString(R.string.notification_text_progress, failed.size(), synced.size(), animes.size()));
            notificationManager.notify(getClass().getPackage().getName(), notificationId, builder.build());
        }
    }

    public void setListener(SyncListener listener) {
        this.listener = listener;
    }

    public static List<AnimeEntry> filterIgnored(SharedPreferences prefs, List<AnimeEntry> listIn) {
        Gson gson = new Gson();
        String ignString = prefs.getString("listIgnored", "");
        if (!ignString.equals("")) {
            List<AnimeEntry> ign = gson.fromJson(ignString, new TypeToken<List<AnimeEntry>>() {}.getType());
            List<AnimeEntry> toRemove = new ArrayList<>();
            for (AnimeEntry a1 : listIn) for (AnimeEntry a2 : ign) if (a1.malId == a2.malId) toRemove.add(a1);
            for (AnimeEntry a1 : toRemove) listIn.remove(a1);
        }
        return listIn;
    }
}
