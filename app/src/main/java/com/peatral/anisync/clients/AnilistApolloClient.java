package com.peatral.anisync.clients;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.apollographql.apollo.ApolloClient;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

public class AnilistApolloClient {
    private static final String BASE_URL = "https://graphql.anilist.co";
    private static ApolloClient myApolloClient;

    public static ApolloClient getApolloClient(Context c) {
        if (myApolloClient == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(c);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder().method(original.method(), original.body());
                        builder.header("Authorization", "Bearer " + SP.getString("access_token", "NO_VALID_TOKEN"));
                        builder.header("Content-Type", "application/json");
                        builder.header("Accept", "application/json");
                        return chain.proceed(builder.build());
                    })
                    .build();
            myApolloClient = ApolloClient.builder()
                    .serverUrl(BASE_URL)
                    .okHttpClient(okHttpClient)
                    .build();
        }
        return myApolloClient;
    }
}
