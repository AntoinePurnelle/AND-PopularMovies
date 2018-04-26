/*
 * Parts of this class have been inspired by Google's Android Fragment Transitions: RecyclerView to ViewPager
 * available at https://github.com/google/android-transition-examples/tree/master/GridToPager
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

package net.ouftech.popularmovies;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.util.ListUtils;
import com.google.gson.Gson;

import net.ouftech.popularmovies.data.MovieContract;
import net.ouftech.popularmovies.data.MovieContract.MovieEntry;
import net.ouftech.popularmovies.model.Country;
import net.ouftech.popularmovies.model.Genre;
import net.ouftech.popularmovies.model.Movie;
import net.ouftech.popularmovies.model.Result;
import net.ouftech.popularmovies.commons.BaseActivity;
import net.ouftech.popularmovies.commons.CallException;
import net.ouftech.popularmovies.commons.CollectionUtils;
import net.ouftech.popularmovies.commons.Logger;
import net.ouftech.popularmovies.commons.NetworkUtils;
import net.ouftech.popularmovies.model.Review;
import net.ouftech.popularmovies.model.Video;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import io.fabric.sdk.android.Fabric;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @NonNull
    @Override
    protected String getLotTag() {
        return "MainActivity";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @BindView(R.id.pb_loading_indicator)
    protected ProgressBar loadingIndicator;
    @BindView(R.id.tv_error_message_display)
    protected TextView errorMessageDisplay;
    @BindView(R.id.tv_empty_message_display)
    protected TextView emptyMessageDisplay;
    @BindView(R.id.fragment_container)
    protected View gridContainer;
    @BindView(R.id.navigation)
    protected BottomNavigationView bottomNavigationView;

    private static final String KEY_CURRENT_FAVORITES_POSITION = "currentFavoritesPosition";
    private static final String KEY_CURRENT_POPULAR_POSITION = "currentPopularPosition";
    private static final String KEY_CURRENT_TOP_RATED_POSITION = "currentTopRatedPosition";
    private static final String KEY_SHOWING_DETAILS = "isShowingDetails";
    private static final String KEY_FAVORITES_MOVIES = "favoritesMovies";
    private static final String KEY_POPULAR_MOVIES = "popularMovies";
    private static final String KEY_TOP_RATED_MOVIES = "topRatedMovies";
    private static final String KEY_SORT_CRITERIA = "sortCriteria";
    private static final int MOVIES_LOADER = 42;
    public static final int SORT_FAVORITES = 0;
    public static final int SORT_POPULAR = 1;
    public static final int SORT_TOP_RATED = 2;

    public static int currentFavoritesPosition = 0;
    public static int currentPopularPosition = 0;
    public static int currentTopRatedPosition = 0;
    private GridFragment gridFragment;
    private boolean isShowingDetails = false;
    private static int sortCriteria = SORT_POPULAR;

    protected ArrayList<Movie> favoriteMovies = new ArrayList<>();
    protected ArrayList<Movie> popularMovies = new ArrayList<>();
    protected ArrayList<Movie> topRatedMovies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        FragmentManager fragmentManager = getSupportFragmentManager();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_favorite:
                        hideErrorMessage();
                        gridFragment.swapData(favoriteMovies);
                        sortCriteria = SORT_FAVORITES;
                        restartLoader();
                        emptyMessageDisplay.setVisibility(favoriteMovies == null || favoriteMovies.isEmpty() ? View.VISIBLE : View.GONE);
                        return true;
                    case R.id.navigation_popular:
                        unsetFavorites(popularMovies);
                        gridFragment.swapData(popularMovies);
                        sortCriteria = SORT_POPULAR;
                        loadMovies(popularMovies, NetworkUtils.TMDB_POPULAR_PATH);
                        emptyMessageDisplay.setVisibility(View.GONE);
                        return true;
                    case R.id.navigation_top_rated:
                        unsetFavorites(topRatedMovies);
                        gridFragment.swapData(topRatedMovies);
                        sortCriteria = SORT_TOP_RATED;
                        loadMovies(topRatedMovies, NetworkUtils.TMDB_TOP_RATED_PATH);
                        emptyMessageDisplay.setVisibility(View.GONE);
                        return true;
                }
                return false;
            }
        });

        if (savedInstanceState != null) {
            currentFavoritesPosition = savedInstanceState.getInt(KEY_CURRENT_POPULAR_POSITION, 0);
            currentPopularPosition = savedInstanceState.getInt(KEY_CURRENT_POPULAR_POSITION, 0);
            currentTopRatedPosition = savedInstanceState.getInt(KEY_CURRENT_TOP_RATED_POSITION, 0);
            isShowingDetails = savedInstanceState.getBoolean(KEY_SHOWING_DETAILS);
            favoriteMovies = savedInstanceState.getParcelableArrayList(KEY_FAVORITES_MOVIES);
            popularMovies = savedInstanceState.getParcelableArrayList(KEY_POPULAR_MOVIES);
            topRatedMovies = savedInstanceState.getParcelableArrayList(KEY_TOP_RATED_MOVIES);
            sortCriteria = savedInstanceState.getInt(KEY_SORT_CRITERIA, SORT_POPULAR);
            gridFragment = (GridFragment) fragmentManager.findFragmentByTag(GridFragment.class.getSimpleName());


            switch (sortCriteria) {
                case SORT_POPULAR:
                    bottomNavigationView.setSelectedItemId(R.id.navigation_popular);
                    loadMovies(popularMovies, NetworkUtils.TMDB_POPULAR_PATH);
                    emptyMessageDisplay.setVisibility(View.GONE);
                    break;
                case SORT_TOP_RATED:
                    bottomNavigationView.setSelectedItemId(R.id.navigation_top_rated);
                    loadMovies(topRatedMovies, NetworkUtils.TMDB_TOP_RATED_PATH);
                    emptyMessageDisplay.setVisibility(View.GONE);
                    break;
                case SORT_FAVORITES:
                default:
                    bottomNavigationView.setSelectedItemId(R.id.navigation_favorite);
                    emptyMessageDisplay.setVisibility(favoriteMovies == null || favoriteMovies.isEmpty() ? View.VISIBLE : View.GONE);
            }

            if (isShowingDetails)
                bottomNavigationView.setVisibility(View.GONE);

            // Return here to prevent adding additional GridFragments when changing orientation.
            return;
        } else {
            getSupportLoaderManager().initLoader(MOVIES_LOADER, null, this);
        }

        gridFragment = new GridFragment();
        fragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, gridFragment, GridFragment.class.getSimpleName())
                .commit();

        bottomNavigationView.setSelectedItemId(R.id.navigation_popular);
    }

    public void restartLoader() {
        if (sortCriteria == SORT_FAVORITES)
            getSupportLoaderManager().restartLoader(MOVIES_LOADER, null, MainActivity.this);
    }

    private void swapData() {

        switch (sortCriteria) {
            case SORT_POPULAR:
                gridFragment.swapData(popularMovies);
                break;
            case SORT_TOP_RATED:
                gridFragment.swapData(topRatedMovies);
                break;
            case SORT_FAVORITES:
            default:
                gridFragment.swapData(favoriteMovies);
        }
    }

    private void unsetFavorites(List<Movie> movies) {
        if (movies != null) {
            for (Movie movie : movies) {
                movie.isFavorite = false;
                movie.hasDetailsLoadedFromDB = false;
            }
        }
    }

    private void loadMovies(@NonNull final ArrayList<Movie> movies, @NonNull String path) {
        if (!isRunning())
            return;

        if (movies.isEmpty()) {
            setProgressBarVisibility(View.VISIBLE);
            NetworkUtils.getMovies(path, this, new Callback<Result>() {
                @Override
                public void onResponse(@NonNull Call<Result> call, @NonNull Response<Result> response) {
                    Result result = response.body();

                    if (result == null) {
                        ResponseBody errorBody = response.errorBody();
                        CallException callException = new CallException(response.code(), response.message(), errorBody, call);

                        if (errorBody == null) {
                            Logger.e(getLotTag(), "Error while executing getPopularMovies call", callException);
                        } else {
                            Logger.e(getLotTag(), String.format("Error while executing getTopRatedMovies call. ErrorBody = %s", errorBody), callException);
                        }
                        setProgressBarVisibility(View.GONE);
                        showErrorMessage();
                        swapData();
                        return;
                    }

                    movies.addAll(result.movies);
                    if (CollectionUtils.isEmpty(movies)) {
                        Logger.e(getLotTag(), "Error while executing getPopularMovies call. Returned list is empty", new CallException(response.code(), response.message(), null, call));
                        showErrorMessage();
                    }
                    swapData();
                    setProgressBarVisibility(View.GONE);
                }

                @Override
                public void onFailure(@NonNull Call<Result> call, @NonNull Throwable t) {
                    Logger.e(getLotTag(), "Error while executing getPopularMovies call", t);
                    setProgressBarVisibility(View.GONE);
                    showErrorMessage();
                }
            });
        } else {
            swapData();
            setProgressBarVisibility(View.GONE);
        }
    }

    private void hideErrorMessage() {
        gridContainer.setVisibility(View.VISIBLE);
        errorMessageDisplay.setVisibility(View.GONE);
    }

    private void showErrorMessage() {
        gridContainer.setVisibility(View.GONE);
        errorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_FAVORITES_POSITION, currentFavoritesPosition);
        outState.putInt(KEY_CURRENT_POPULAR_POSITION, currentPopularPosition);
        outState.putInt(KEY_CURRENT_TOP_RATED_POSITION, currentTopRatedPosition);
        outState.putInt(KEY_SORT_CRITERIA, sortCriteria);
        outState.putBoolean(KEY_SHOWING_DETAILS, isShowingDetails);
        outState.putParcelableArrayList(KEY_POPULAR_MOVIES, popularMovies);
        outState.putParcelableArrayList(KEY_TOP_RATED_MOVIES, topRatedMovies);
        outState.putParcelableArrayList(KEY_FAVORITES_MOVIES, favoriteMovies);
    }

    public ArrayList<Movie> getMovies() {

        switch (sortCriteria) {
            case SORT_POPULAR:
                return popularMovies;
            case SORT_TOP_RATED:
                return topRatedMovies;
            case SORT_FAVORITES:
            default:
                return favoriteMovies;
        }
    }

    public void onItemClicked(int currentPosition) {
        isShowingDetails = true;
        setCurrentPosition(currentPosition);
        if (bottomNavigationView != null) {
            bottomNavigationView.animate()
                    .alpha(0.0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            bottomNavigationView.setVisibility(View.GONE);
                        }
                    });
        }
    }

    public static int getCurrentPosition() {
        switch (sortCriteria) {
            case SORT_POPULAR:
                return MainActivity.currentPopularPosition;
            case SORT_TOP_RATED:
                return MainActivity.currentTopRatedPosition;
            case SORT_FAVORITES:
                return MainActivity.currentFavoritesPosition;
            default:
                return 0;
        }
    }

    public void setCurrentPosition(int currentPosition) {

        switch (sortCriteria) {
            case SORT_POPULAR:
                MainActivity.currentPopularPosition = currentPosition;
                break;
            case SORT_TOP_RATED:
                MainActivity.currentTopRatedPosition = currentPosition;
                break;
            case SORT_FAVORITES:
                MainActivity.currentFavoritesPosition = currentPosition;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isShowingDetails) {

            if (sortCriteria == SORT_FAVORITES)
                restartLoader();

            isShowingDetails = false;
            if (bottomNavigationView != null) {
                bottomNavigationView.setVisibility(View.VISIBLE);
                bottomNavigationView.animate()
                        .alpha(1.0f)
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                bottomNavigationView.setVisibility(View.VISIBLE);
                            }
                        });
            }
        }
    }

    public int getBottomNavigationViewHeight() {
        return bottomNavigationView == null ? 0 : bottomNavigationView.getMeasuredHeight();
    }

    private void setProgressBarVisibility(final int visibility) {
        if (isRunning() && loadingIndicator != null)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingIndicator.setVisibility(visibility);
                }
            });
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case MOVIES_LOADER:
                Uri queryUri = MovieEntry.CONTENT_URI;
                String sortOrder = MovieEntry.COLUMN_TITLE + " ASC";

                return new CursorLoader(this,
                        queryUri,
                        null,
                        null,
                        null,
                        sortOrder);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Logger.d(getLotTag(), "");

        favoriteMovies = new ArrayList<>();

        if (data.moveToFirst()) {
            emptyMessageDisplay.setVisibility(View.GONE);

            do {
                Movie movie = new Movie();
                movie.id = data.getString(data.getColumnIndex(MovieEntry.COLUMN_ID));
                movie.title = data.getString(data.getColumnIndex(MovieEntry.COLUMN_TITLE));
                movie.posterPath = data.getString(data.getColumnIndex(MovieEntry.COLUMN_POSTER));
                movie.releaseDate = data.getString(data.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE));
                movie.overview = data.getString(data.getColumnIndex(MovieEntry.COLUMN_OVERVIEW));
                movie.voteAverage = data.getFloat(data.getColumnIndex(MovieEntry.COLUMN_VOTE_AVERAGE));
                movie.voteCount = data.getString(data.getColumnIndex(MovieEntry.COLUMN_VOTE_COUNT));
                movie.originalLanguage = data.getString(data.getColumnIndex(MovieEntry.COLUMN_ORIGINAL_LANGUAGE));
                movie.originalTitle = data.getString(data.getColumnIndex(MovieEntry.COLUMN_ORIGINAL_TITLE));
                movie.backdropPath = data.getString(data.getColumnIndex(MovieEntry.COLUMN_BACKDROP_PATH));
                movie.tagline = data.getString(data.getColumnIndex(MovieEntry.COLUMN_TAGLINE));
                movie.runtime = data.getInt(data.getColumnIndex(MovieEntry.COLUMN_RUNTIME));

                Gson gson = new Gson();
                String stringValue = data.getString(data.getColumnIndex(MovieEntry.COLUMN_PRODUCTION_COUNTRIES));
                if (!TextUtils.isEmpty(stringValue) && !"null".equals(stringValue))
                    movie.countries = new ArrayList<>(Arrays.asList(gson.fromJson(stringValue, Country[].class)));

                stringValue = data.getString(data.getColumnIndex(MovieEntry.COLUMN_GENRES));
                if (!TextUtils.isEmpty(stringValue) && !"null".equals(stringValue))
                    movie.genres = new ArrayList<>(Arrays.asList(gson.fromJson(stringValue, Genre[].class)));

                stringValue = data.getString(data.getColumnIndex(MovieEntry.COLUMN_VIDEOS));
                if (!TextUtils.isEmpty(stringValue) && !"null".equals(stringValue))
                    movie.videos = new ArrayList<>(Arrays.asList(gson.fromJson(stringValue, Video[].class)));

                stringValue = data.getString(data.getColumnIndex(MovieEntry.COLUMN_REVIEWS));
                if (!TextUtils.isEmpty(stringValue) && !"null".equals(stringValue))
                    movie.reviews = new ArrayList<>(Arrays.asList(gson.fromJson(stringValue, Review[].class)));

                movie.hasDetailsLoadedFromDB = true;
                movie.isFavorite = true;

                favoriteMovies.add(movie);
            } while (data.moveToNext());
        } else {
            if (sortCriteria == SORT_FAVORITES)
                emptyMessageDisplay.setVisibility(View.VISIBLE);
        }

        if (sortCriteria == SORT_FAVORITES)
            swapData();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        favoriteMovies = new ArrayList<>();

        if (sortCriteria == SORT_FAVORITES)
            swapData();
    }

    public static int getSortCriteria() {
        return sortCriteria;
    }
}
