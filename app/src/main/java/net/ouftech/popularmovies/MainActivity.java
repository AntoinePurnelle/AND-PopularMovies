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
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import net.ouftech.popularmovies.Model.Movie;
import net.ouftech.popularmovies.Model.Result;
import net.ouftech.popularmovies.commons.BaseActivity;
import net.ouftech.popularmovies.commons.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import butterknife.BindView;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Pair<ArrayList<Movie>, ArrayList<Movie>>> {

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
    @BindView(R.id.fragment_container)
    protected View gridContainer;
    @BindView(R.id.navigation)
    protected BottomNavigationView bottomNavigationView;

    private static final String KEY_CURRENT_POSITION = "currentPosition";
    private static final String KEY_SHOWING_DETAILS = "isShowingDetails";
    private static final String KEY_POPULAR_MOVIES = "popularMovies";
    private static final String KEY_TOP_RATED_MOVIES = "topRatedMovies";
    private static final String KEY_SORT_CRITERIA = "sortCriteria";
    private static final int MOVIES_LOADER = 42;
    private static final int SORT_POPULAR = 0;
    private static final int SORT_TOP_RATED = 1;

    public static int currentPosition;
    private GridFragment gridFragment;
    private boolean isShowingDetails = false;
    private int sortCriteria = 0;

    protected ArrayList<Movie> popularMovies;
    protected ArrayList<Movie> topRatedMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        FragmentManager fragmentManager = getSupportFragmentManager();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_popular:
                        gridFragment.swapData(popularMovies);
                        sortCriteria = SORT_POPULAR;
                        swapData();
                        return true;
                    case R.id.navigation_top_rated:
                        gridFragment.swapData(topRatedMovies);
                        sortCriteria = SORT_TOP_RATED;
                        swapData();
                        return true;
                }
                return false;
            }
        });

        if (savedInstanceState != null) {
            setCurrentPosition(savedInstanceState.getInt(KEY_CURRENT_POSITION, 0));
            isShowingDetails = savedInstanceState.getBoolean(KEY_SHOWING_DETAILS);
            popularMovies = savedInstanceState.getParcelableArrayList(KEY_POPULAR_MOVIES);
            topRatedMovies = savedInstanceState.getParcelableArrayList(KEY_TOP_RATED_MOVIES);
            sortCriteria = savedInstanceState.getInt(KEY_SORT_CRITERIA, SORT_POPULAR);
            gridFragment = (GridFragment) fragmentManager.findFragmentByTag(GridFragment.class.getSimpleName());
            if (sortCriteria == SORT_TOP_RATED)
                bottomNavigationView.setSelectedItemId(R.id.navigation_top_rated);
            swapData();

            if (isShowingDetails)
                bottomNavigationView.setVisibility(View.GONE);

            // Return here to prevent adding additional GridFragments when changing orientation.
            return;
        }

        loadMovies();

        gridFragment = new GridFragment();
        fragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, gridFragment, GridFragment.class.getSimpleName())
                .commit();
    }

    private void swapData() {
        if (sortCriteria == SORT_POPULAR)
            gridFragment.swapData(popularMovies);
        else
            gridFragment.swapData(topRatedMovies);
    }

    private void loadMovies() {

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String[]> moviesLoader = loaderManager.getLoader(MOVIES_LOADER);
        if (moviesLoader == null) {
            loaderManager.initLoader(MOVIES_LOADER, null, this);
        } else {
            loaderManager.restartLoader(MOVIES_LOADER, null, this);
        }

    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<Pair<ArrayList<Movie>, ArrayList<Movie>>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Pair<ArrayList<Movie>, ArrayList<Movie>>>(getApplicationContext()) {

            Pair<ArrayList<Movie>, ArrayList<Movie>> result;

            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                if (result != null)
                    deliverResult(result);
                else
                    forceLoad();
            }

            @Override
            public Pair<ArrayList<Movie>, ArrayList<Movie>> loadInBackground() {

                if (loadingIndicator != null)
                    loadingIndicator.setVisibility(View.VISIBLE);

                ArrayList<Movie> popularMovies = null;
                ArrayList<Movie> topRatedMovies = null;
                try {
                    URL popularMoviesURL = NetworkUtils.getPopularMoviesURL(MainActivity.this);
                    String popularMoviesResponse = NetworkUtils.getResponseFromHttpUrl(popularMoviesURL);
                    if (!TextUtils.isEmpty(popularMoviesResponse)) {
                        popularMovies = new Gson().fromJson(popularMoviesResponse, Result.class).movies;
                        logd("popularMovies " + popularMovies.size());
                    }
                } catch (IOException e) {
                    loge("Error while requesting movies", e);
                }

                try {
                    URL topRatedMoviesURL = NetworkUtils.getTopRatedMoviesURL(MainActivity.this);
                    String topRatedMoviesResponse = NetworkUtils.getResponseFromHttpUrl(topRatedMoviesURL);
                    if (!TextUtils.isEmpty(topRatedMoviesResponse)) {
                        topRatedMovies = new Gson().fromJson(topRatedMoviesResponse, Result.class).movies;
                        logd("topRatedMovies " + topRatedMovies.size());
                    }
                } catch (IOException e) {
                    loge("Error while requesting movies", e);
                }

                logd("finished");
                return new Pair<>(popularMovies, topRatedMovies);
            }

            @Override
            public void deliverResult(Pair<ArrayList<Movie>, ArrayList<Movie>> data) {
                result = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Pair<ArrayList<Movie>, ArrayList<Movie>>> loader, Pair<ArrayList<Movie>, ArrayList<Movie>> data) {
        if (loadingIndicator != null)
            loadingIndicator.setVisibility(View.INVISIBLE);

        if (data != null) {
            popularMovies = data.first;
            topRatedMovies = data.second;
            gridFragment.swapData(popularMovies);
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<Pair<ArrayList<Movie>, ArrayList<Movie>>> loader) {

    }

    private void showErrorMessage() {
        gridContainer.setVisibility(View.GONE);
        errorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_POSITION, currentPosition);
        outState.putInt(KEY_SORT_CRITERIA, sortCriteria);
        outState.putBoolean(KEY_SHOWING_DETAILS, isShowingDetails);
        outState.putParcelableArrayList(KEY_POPULAR_MOVIES, popularMovies);
        outState.putParcelableArrayList(KEY_TOP_RATED_MOVIES, topRatedMovies);
    }

    public ArrayList<Movie> getMovies() {
        if (sortCriteria == SORT_POPULAR)
            return popularMovies;
        else
            return topRatedMovies;
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

    public void setCurrentPosition(int currentPosition) {
        MainActivity.currentPosition = currentPosition;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isShowingDetails) {
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
}
