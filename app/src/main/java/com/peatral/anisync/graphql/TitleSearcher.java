package com.peatral.anisync.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peatral.anisync.graphql.classes.Media;
import com.peatral.anisync.graphql.classes.Page;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Response;

public class TitleSearcher implements Iterator {
    private String title;
    private int currentPage = 0;

    private Page page;

    private static final String query = "query MediaByTitle{Page (page: %d) {pageInfo { total currentPage lastPage hasNextPage perPage } media (search: \"%s\", type: ANIME) {id idMal coverImage{extraLarge large medium color} title {romajiTitle: romaji englishTitle: english nativeTitle: native}}}}";


    public TitleSearcher(String title) {
        this.title = title;

    }

    @Override
    public boolean hasNext() {
        if (page != null)
            return page.getPageInfo().hasNextPage();
        else
            return true;
    }

    @Override
    public List<Media> next() {
        currentPage += 1;
        List<Media> result = new ArrayList<>();
        try {
            Response response = Requests.getResponseFromRequest(String.format(query, currentPage, title));

            String s = response.body().string();
            System.out.println(s);
            JSONObject data = (JSONObject) JSONValue.parse(s);
            ObjectMapper mapper = new ObjectMapper();
            page = mapper.readValue(((JSONObject) ((JSONObject) data.get("data")).get("Page")).toJSONString(), Page.class);

            for (Media media : page.getMedia()) result.add(media);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return result;
    }
}
