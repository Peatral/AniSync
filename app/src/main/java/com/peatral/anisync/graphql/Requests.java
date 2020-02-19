package com.peatral.anisync.graphql;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peatral.anisync.App;
import com.peatral.anisync.activities.MainActivity;
import com.peatral.anisync.graphql.classes.*;
import com.peatral.anisync.graphql.enums.MediaListStatus;
import com.peatral.anisync.graphql.enums.MediaType;
import io.aexp.nodes.graphql.*;
import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Requests {

    public static final String URL_STRING = "https://graphql.anilist.co";

    public static GraphQLTemplate graphQLTemplate = new GraphQLTemplate();
    public static Map headers = new HashMap<String, String>();
    static {
        headers.put("user-agent", "JS graphql");
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
    }

    public static void setToken(String token) {
        headers.put("Authorization", "Bearer " + token);
    }

    private static GraphQLRequestEntity.RequestBuilder getBuilder() {
        try {
            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(App.getContext());
            setToken(SP.getString("access_token", "NO_VALID_TOKEN"));
            return GraphQLRequestEntity.Builder().url(URL_STRING);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static GraphQLRequestEntity mediaListCollectionRequest(int userId) {
        return getBuilder()
                .arguments(new Arguments("MediaListCollection",
                        new Argument<>("userId", userId),
                        new Argument("type", MediaType.ANIME)
                ))
                .request(MediaListCollection.class)
                .headers(headers)
                .build();
    }

    public static GraphQLRequestEntity animeByMalIdRequest(int idMal) {
        return getBuilder()
                .arguments(new Arguments("Media",
                        new Argument<>("idMal", idMal),
                        new Argument("type", MediaType.ANIME)
                ))
                .request(Media.class)
                .headers(headers)
                .build();
    }

    public static GraphQLRequestEntity viewerRequest() {
        return getBuilder()
                .request(Viewer.class)
                .headers(headers)
                .build();
    }

    public static GraphQLRequestEntity mediaListEntryRequest(int mediaId, MediaListStatus status, int progress, float score) {
        return getBuilder()
                .arguments(new Arguments("SaveMediaListEntry",
                                new Argument<>("mediaId", mediaId),
                                new Argument<>("status", status),
                                new Argument<>("progress", progress),
                                new Argument<>("score", score)
                        )
                )
                .request(SaveMediaListEntry.class)
                .headers(headers)
                .build();
    }

    public static GraphQLResponseEntity<MediaListCollection> fetchMediaListCollection(int userId) {
        return graphQLTemplate.query(mediaListCollectionRequest(userId), MediaListCollection.class);
    }

    public static GraphQLResponseEntity<Media> fetchAnimeByMalId(int idMal) {
        return graphQLTemplate.query(animeByMalIdRequest(idMal), Media.class);
    }

    public static GraphQLResponseEntity<Viewer> fetchViewer() {
        return graphQLTemplate.query(viewerRequest(), Viewer.class);
    }

    public static GraphQLResponseEntity<SaveMediaListEntry> mutateMediaListEntry(int mediaId, MediaListStatus status, int progress, float score) {
        return graphQLTemplate.mutate(mediaListEntryRequest(mediaId, status, progress, score), SaveMediaListEntry.class);
    }

    public static List<Media> getAniIds(List<Media> malIds) {

        OkHttpClient client = new OkHttpClient();

        Headers.Builder headersBuilder = new Headers.Builder();
        for (String header : (String[]) headers.keySet().toArray(new String[]{})) {
            headersBuilder.add(header, (String) headers.get(header));
        }

        StringBuilder malIdsString = new StringBuilder();
        for (int i = 0; i < malIds.size(); i++) {
            malIdsString.append(malIds.get(i).getIdMal());
            if (i < malIds.size()-1) malIdsString.append(", ");
        }
        String fullRequest = String.format("query AnimeByMalIDQuery {Page (page: 1) {pageInfo {total currentPage lastPage hasNextPage perPage} media (idMal_in: [%s], type: ANIME) {id idMal coverImage{extraLarge large medium color} title {romajiTitle: romaji englishTitle: english nativeTitle: native}}}}", malIdsString);

        JSONObject query = new JSONObject();
        query.put("query", fullRequest);
        RequestBody body = RequestBody.create(query.toJSONString(), okhttp3.MediaType.parse("application/json; charset=utf-8")); // new
        Request request = new Request.Builder()
                .url(URL_STRING)
                .post(body)
                .headers(headersBuilder.build())
                .build();
        try {
            Response response = client.newCall(request).execute();

            String s = response.body().string();

            JSONObject data = (JSONObject) JSONValue.parse(s);
            ObjectMapper mapper = new ObjectMapper();
            Page page = mapper.readValue(((JSONObject)((JSONObject) data.get("data")).get("Page")).toJSONString(), Page.class);
            List<Media> result = new ArrayList<>();
            for (int i = 0; i < malIds.size(); i++) {
                boolean found = false;
                for (Media media : page.getMedia()) {
                    if (media.getIdMal() == i) {
                        result.add(media);
                        found = true;
                        break;
                    }
                }
                if (found) {
                    System.out.println("Looked up ID for " + malIds.get(i).getTitle().getRomajiTitle());
                } else {
                    System.out.println("Did not find ID for " + malIds.get(i).getTitle().getRomajiTitle());
                }
            }
            return result;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Response batchMutation(List<String> requests) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Headers.Builder headersBuilder = new Headers.Builder();
        for (String header : (String[]) headers.keySet().toArray(new String[]{})) {
            headersBuilder.add(header, (String) headers.get(header));
        }

        String fullRequest = "";
        int i = 0;
        for (String requestString : requests) {
            fullRequest += requestString.trim().substring(0, requestString.length()-2).replace("query {", "q"+i+": ");
            i++;
        }

        JSONObject query = new JSONObject();
        query.put("query", "mutation {" + fullRequest + "} ");

        RequestBody body = RequestBody.create(query.toJSONString(), okhttp3.MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(URL_STRING)
                .post(body)
                .headers(headersBuilder.build())
                .build();
        return client.newCall(request).execute();
    }

    public static List<MediaList> getMalList(String usernameMal) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.jikan.moe/v3/user/" + usernameMal + "/animelist/all")
                .build();
        try {
            Response response = client.newCall(request).execute();
            String body = response.body().string();
            Object obj = JSONValue.parse(body);

            List<MediaList> listMal = new ArrayList<>();

            JSONObject jsonObj = (JSONObject) obj;
            JSONArray array = (JSONArray) jsonObj.get("anime");
            if (array != null) {
                for (Object listObj : array) {
                    JSONObject anime = (JSONObject) listObj;

                    MediaList mediaList = new MediaList();

                    Media media = new Media();
                    Title title = new Title();
                    title.setRomajiTitle((String) anime.get("title"));
                    media.setTitle(title);
                    MediaCoverImage coverImage = new MediaCoverImage();
                    String image_url = (String) anime.get("image_url");
                    coverImage.setExtraLarge(image_url);
                    coverImage.setLarge(image_url);
                    coverImage.setMedium(image_url);
                    media.setCoverImage(coverImage);
                    media.setIdMal(((Long) anime.get("mal_id")).intValue());

                    mediaList.setMedia(media);
                    mediaList.setProgress(((Long) anime.get("watched_episodes")).intValue());
                    mediaList.setStatus(MediaListStatus.fromJikanId(((Long) anime.get("watching_status")).intValue()));
                    mediaList.setScore(((Long) anime.get("score")).floatValue());

                    listMal.add(mediaList);
                }
            }
            return listMal;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
