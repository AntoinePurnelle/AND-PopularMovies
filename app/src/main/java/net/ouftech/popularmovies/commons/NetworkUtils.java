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
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import net.ouftech.popularmovies.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Scanner;

/**
 * Created by antoine.purnelle@ouftech.net on 27-02-18.
 */

public class NetworkUtils {

    @NonNull
    protected static String getLotTag() {
        return "NetworkUtils";
    }

    private static final String TMDB_BASE_URL =
            "https://api.themoviedb.org/3/movie";
    private static final String TMDB_POPULAR_URL =
            TMDB_BASE_URL + "/popular";
    private static final String TMDB_TOP_RATED_URL =
            TMDB_BASE_URL + "/top_rated";

    private static final String TMDB_IMAGE_BASE_URL =
            "https://image.tmdb.org/t/p/";

    private static final String API_KEY_PARAM = "api_key";
    private static final String LANGUAGE_PARAM = "language";
    private static final String IMAGE_LANGUAGE_PARAM = "include_image_language";

    @Nullable
    public static URL getMovieURL(@Nullable Context context, @NonNull String id) {
        return buildMoviesUrl(context, TMDB_BASE_URL + "/" + id);
    }

    @Nullable
    public static URL getPopularMoviesURL(@Nullable Context context) {
        return buildMoviesUrl(context, TMDB_POPULAR_URL);
    }

    @Nullable
    public static URL getTopRatedMoviesURL(@Nullable Context context) {
        return buildMoviesUrl(context, TMDB_TOP_RATED_URL);
    }

    @Nullable
    private static URL buildMoviesUrl(@Nullable Context context, String baseURL) {
        if (context == null) {
            Logger.w(getLotTag(), "Cannot build URL", new NullPointerException("Context is null"), false);
            return null;
        }

        if (TextUtils.isEmpty(baseURL)) {
            Logger.e(getLotTag(), "Cannot build URL", new NullPointerException("Base URL is empty"));
            return null;
        }

        String apiKey = context.getString(R.string.movie_db_api_key);
        String localLanguage = Locale.getDefault().getLanguage();
        String imageLanguage = localLanguage + ",null";

        Uri builtUri = Uri.parse(baseURL).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, apiKey)
                .appendQueryParameter(LANGUAGE_PARAM, localLanguage)
                .appendQueryParameter(IMAGE_LANGUAGE_PARAM, imageLanguage)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
            Logger.d(getLotTag(), "Built URL " + url);
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
            Logger.d(getLotTag(), "Built URL " + url);
        } catch (MalformedURLException e) {
            Logger.e(getLotTag(), "Error while building URL", e);
        }
        return url;
    }

    public static String getResponseFromHttpUrl(@Nullable URL url) throws IOException {
        if (url == null)
            return null;

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

}
