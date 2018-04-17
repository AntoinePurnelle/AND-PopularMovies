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

package net.ouftech.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

import net.ouftech.popularmovies.model.Movie;

public class MovieContract {

    public static final String CONTENT_AUTHORITY = "net.ouftech.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIE = "movie";

    public static final class MovieEntry implements BaseColumns {
        
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIE)
                .build();

        public static final String TABLE_NAME = "movie";

        public static final String COLUMN_ID = Movie.ID_KEY;
        public static final String COLUMN_TITLE = Movie.TITLE_KEY;
        public static final String COLUMN_POSTER = Movie.POSTER_KEY;
        public static final String COLUMN_RELEASE_DATE = Movie.RELEASE_DATE_KEY;
        public static final String COLUMN_OVERVIEW = Movie.OVERVIEW_KEY;
        public static final String COLUMN_VOTE_AVERAGE = Movie.VOTE_AVERAGE_KEY;
        public static final String COLUMN_VOTE_COUNT = Movie.VOTE_COUNT_KEY;
        public static final String COLUMN_ORIGINAL_LANGUAGE = Movie.ORIGINAL_LANGUAGE_KEY;
        public static final String COLUMN_ORIGINAL_TITLE = Movie.ORIGINAL_TITLE_KEY;
        public static final String COLUMN_BACKDROP_PATH = Movie.BACKDROP_PATH_KEY;
        public static final String COLUMN_TAGLINE = Movie.TAGLINE_KEY;
        public static final String COLUMN_PRODUCTION_COUNTRIES = Movie.PRODUCTION_COUNTRIES_KEY;
        public static final String COLUMN_GENRES = Movie.GENRES_KEY;
        public static final String COLUMN_RUNTIME = Movie.RUNTIME_KEY;
        public static final String COLUMN_VIDEOS = "videos";
        public static final String COLUMN_REVIEWS = "reviews";

    }
}
