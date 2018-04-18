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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import net.ouftech.popularmovies.commons.Logger;
import net.ouftech.popularmovies.data.MovieContract.MovieEntry;

public class MovieDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Logger.d(getLogTag(), "onCreate");
        final String SQL_CREATE_MOVIE_TABLE =
                "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                        MovieEntry.COLUMN_ID + " VARCHAR PRIMARY KEY, " +
                        MovieEntry.COLUMN_TITLE + " VARCHAR NOT NULL," +
                        MovieEntry.COLUMN_POSTER + " VARCHAR NOT NULL, " +
                        MovieEntry.COLUMN_RELEASE_DATE + " VARCHAR NOT NULL, " +
                        MovieEntry.COLUMN_OVERVIEW + " VARCHAR NOT NULL, " +
                        MovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                        MovieEntry.COLUMN_VOTE_COUNT + " VARCHAR NOT NULL, " +
                        MovieEntry.COLUMN_ORIGINAL_LANGUAGE + " VARCHAR NOT NULL, " +
                        MovieEntry.COLUMN_ORIGINAL_TITLE + " VARCHAR NOT NULL, " +
                        MovieEntry.COLUMN_BACKDROP_PATH + " VARCHAR NOT NULL, " +
                        MovieEntry.COLUMN_TAGLINE + " VARCHAR NOT NULL, " +
                        MovieEntry.COLUMN_PRODUCTION_COUNTRIES + " VARCHAR NOT NULL, " +
                        MovieEntry.COLUMN_GENRES + " VARCHAR NOT NULL, " +
                        MovieEntry.COLUMN_RUNTIME + " INTEGER NOT NULL, " +
                        MovieEntry.COLUMN_VIDEOS + " VARCHAR NOT NULL, " +
                        MovieEntry.COLUMN_REVIEWS + " VARCHAR NOT NULL, " +
                        " UNIQUE (" + MovieEntry.COLUMN_ID + ") ON CONFLICT REPLACE);";

        Logger.d(getLogTag(), String.format("Executing create DB command %s", SQL_CREATE_MOVIE_TABLE));
        db.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        Logger.d(getLogTag(), "onOpen");
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.d(getLogTag(), "onUpgrade");
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(db);
    }

    @NonNull
    private String getLogTag() {
        return "MovieDbHelper";
    }
}
