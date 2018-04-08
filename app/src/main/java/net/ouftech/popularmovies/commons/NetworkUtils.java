/*
 * Copyright 2018 Antoine PURNELLE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ouftech.popularmovies.commons;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import net.ouftech.popularmovies.model.Movie;
import net.ouftech.popularmovies.model.Result;
import net.ouftech.popularmovies.R;
import net.ouftech.popularmovies.model.VideosResult;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * Created by antoine.purnelle@ouftech.net on 27-02-18.
 */

public class NetworkUtils {

    @NonNull
    protected static String getLotTag() {
        return "NetworkUtils";
    }

    public interface TMDBClient {
        @GET("3/movie/{path}")
        Call<Result> getMoviesList(@Path("path") String path, @QueryMap HashMap<String, String> parameters);
        @GET("3/movie/{id}")
        Call<Movie> getMovie(@Path("id") String path, @QueryMap HashMap<String, String> parameters);
        @GET("3/movie/{id}/videos")
        Call<VideosResult> getVideos(@Path("id") String path, @QueryMap HashMap<String, String> parameters);
    }

    private static final String TMDB_BASE_URL =
            "https://api.themoviedb.org/";
    public static final String TMDB_POPULAR_PATH = "popular";
    public static final String TMDB_TOP_RATED_PATH = "top_rated";

    private static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    private static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v=";
    private static final String YOUTUBE_IMAGE_BASE_URL = "https://img.youtube.com/vi/%s/maxresdefault.jpg";

    private static final String API_KEY_PARAM = "api_key";
    private static final String LANGUAGE_PARAM = "language";
    private static final String IMAGE_LANGUAGE_PARAM = "include_image_language";

    private static TMDBClient client;

    private static TMDBClient getTMDBClient() {
        if (client == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(TMDB_BASE_URL)
                    .addConverterFactory(
                            GsonConverterFactory.create()
                    );

            Retrofit retrofit = builder.client(httpClient.build()).build();

            client = retrofit.create(TMDBClient.class);
        }

        return client;
    }

    private static HashMap<String, String> getParams(@NonNull Context context) {
        HashMap<String, String> params = new HashMap<>();

        String apiKey = context.getString(R.string.movie_db_api_key);
        String localLanguage = Locale.getDefault().getLanguage();
        String imageLanguage = localLanguage + ",null";

        params.put(API_KEY_PARAM, apiKey);
        params.put(LANGUAGE_PARAM, localLanguage);
        params.put(IMAGE_LANGUAGE_PARAM, imageLanguage);

        return params;
    }

    public static void getMovie(@Nullable Context context, @NonNull final String id, @NonNull Callback<Movie> callback) {
        if (context == null) {
            Logger.w(getLotTag(), "Cannot build URL", new NullPointerException("Context is null"), false);
            return;
        }

        Call<Movie> call = getTMDBClient().getMovie(id, getParams(context));
        call.enqueue(callback);
    }

    public static void getMovies(@NonNull String path, @Nullable Context context, @NonNull Callback<Result> callback) {
        if (context == null) {
            Logger.w(getLotTag(), "Cannot build URL", new NullPointerException("Context is null"), false);
            return;
        }

        Call<Result> call = getTMDBClient().getMoviesList(path, getParams(context));
        call.enqueue(callback);
    }

    public static void getVideos(@Nullable Context context, @NonNull final String id, @NonNull Callback<VideosResult> callback) {
        if (context == null) {
            Logger.w(getLotTag(), "Cannot build URL", new NullPointerException("Context is null"), false);
            return;
        }

        Call<VideosResult> call = getTMDBClient().getVideos(id, getParams(context));
        call.enqueue(callback);
    }

    public static void openYoutubeVideo(Context context, String videoId) {
        if (context == null)
            return;

        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(YOUTUBE_BASE_URL + videoId));

        context.startActivity(webIntent);
    }

    public static URL getYoutubeThumbnailURL(String videoId) {

        URL url = null;
        try {
            url = new URL(String.format(YOUTUBE_IMAGE_BASE_URL, videoId));
        } catch (MalformedURLException e) {
            Logger.e(getLotTag(), "Error while building URL", e);
        }
        return url;
    }

    @Nullable
    public static URL getSmallImageURL(@NonNull Context context, String imagePath) {
        return buildImageUrl(context.getString(R.string.small_image), imagePath);
    }

    @Nullable
    public static URL getLargeImageURL(@NonNull Context context, String imagePath) {
        return buildImageUrl(context.getString(R.string.large_image), imagePath);
    }

    @Nullable
    private static URL buildImageUrl(String baseURL, String imagePath) {
        if (TextUtils.isEmpty(baseURL) || TextUtils.isEmpty(imagePath)) {
            Logger.w(getLotTag(), "Cannot build URL", new NullPointerException(String.format("Base URL [%s] or Image Path [%s] is null or empty", baseURL, imagePath)), false);
            return null;
        }

        URL url = null;
        try {
            url = new URL(TMDB_IMAGE_BASE_URL + baseURL + imagePath);
        } catch (MalformedURLException e) {
            Logger.e(getLotTag(), "Error while building URL", e);
        }
        return url;
    }

}
