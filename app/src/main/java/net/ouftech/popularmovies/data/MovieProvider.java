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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.ouftech.popularmovies.commons.Logger;
import net.ouftech.popularmovies.data.MovieContract.MovieEntry;

public class MovieProvider extends ContentProvider {

    public static final int CODE_MOVIES = 100;
    public static final int CODE_MOVIE = 101;

    private MovieDbHelper dbHelper;
    private static final UriMatcher uriMatcher = buildUriMatcher();

    @Override
    public boolean onCreate() {
        Logger.d(getLogTag(), "onCreate");
        dbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;

        switch (uriMatcher.match(uri)) {
            case CODE_MOVIES:
                cursor = dbHelper.getReadableDatabase().query(
                        MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_MOVIE:
                String normalizedUtcDateString = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{normalizedUtcDateString};

                cursor = dbHelper.getReadableDatabase().query(
                        MovieEntry.TABLE_NAME,
                        projection,
                        MovieEntry.COLUMN_ID + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return cursor;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        int numRowsDeleted = 0;
        if (null == selection)
            selection = "1";

        switch (uriMatcher.match(uri)) {

            case CODE_MOVIES:
                db.beginTransaction();
                try {
                    numRowsDeleted = dbHelper.getWritableDatabase().delete(
                            MovieEntry.TABLE_NAME,
                            selection,
                            selectionArgs);

                    db.setTransactionSuccessful();
                } catch (SQLiteException e) {
                    Logger.e(getLogTag(), "Error while deleting an entity in DB", e);
                } finally {
                    db.endTransaction();
                }

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (numRowsDeleted != 0 && getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);

        Logger.d(getLogTag(), String.format("%s row(s) deleted", numRowsDeleted));

        return numRowsDeleted;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {

            case CODE_MOVIES:
                db.beginTransaction();
                long id = -1;
                try {
                    id = db.insert(MovieEntry.TABLE_NAME, null, values);
                    db.setTransactionSuccessful();
                } catch (SQLiteException e) {
                    Logger.e(getLogTag(), "Error while inserting an entity in DB", e);
                } finally {
                    db.endTransaction();
                }

                if (id != -1 && getContext() != null)
                    getContext().getContentResolver().notifyChange(uri, null);

                Logger.d(getLogTag(), String.format("Row %s inserted", id));

                return uri;
            default:
                throw new UnsupportedOperationException("Unsupported uri for insert: " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {

            case CODE_MOVIES:
                db.beginTransaction();
                int rowsUpdated = 0;
                try {
                    rowsUpdated = db.update(MovieEntry.TABLE_NAME, values, MovieEntry.COLUMN_ID + " = ?", selectionArgs);
                    db.setTransactionSuccessful();
                } catch (SQLiteException e) {
                    Logger.e(getLogTag(), "Error while updating an entity in DB", e);
                } finally {
                    db.endTransaction();
                }

                if (rowsUpdated > 0 && getContext() != null)
                    getContext().getContentResolver().notifyChange(uri, null);

                Logger.d(getLogTag(), String.format("%s row(s) affected by update", rowsUpdated));

                return rowsUpdated;
            default:
                throw new UnsupportedOperationException("Unsupported uri for insert: " + uri);
        }
    }

    @Override
    public void shutdown() {
        dbHelper.close();
        super.shutdown();
    }

    /**
     * Creates the UriMatcher that will match each URI to the following constants
     * <ul>
     * <li>{@link #CODE_MOVIES}</li>
     * <li>{@link #CODE_MOVIE}</li>
     * </ul>
     *
     * @return A UriMatcher that correctly matches the constants
     */
    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, MovieContract.PATH_MOVIE, CODE_MOVIES);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", CODE_MOVIE);

        return matcher;
    }

    @NonNull
    private String getLogTag() {
        return "MovieProvider";
    }
}
