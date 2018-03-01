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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import net.ouftech.popularmovies.commons.BaseActivity;
import net.ouftech.popularmovies.commons.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import icepick.State;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<ArrayList<Movie>> {

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

    private static final String KEY_CURRENT_POSITION = "currentPosition";
    private static final String KEY_MOVIES = "movies";
    private static final int MOVIES_LOADER = 42;

    public static int currentPosition;
    private GridFragment gridFragment;

    protected ArrayList<Movie> movies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION, 0);
            movies = savedInstanceState.getParcelableArrayList(KEY_MOVIES);
            gridFragment = (GridFragment) fragmentManager.findFragmentByTag(GridFragment.class.getSimpleName());
            gridFragment.swapData(movies);
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
    public Loader<ArrayList<Movie>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<ArrayList<Movie>>(getApplicationContext()) {

            ArrayList<Movie> result;

            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                if (result != null)
                    deliverResult(result);
                else
                    forceLoad();
            }

            @Override
            public ArrayList<Movie> loadInBackground() {

                if (loadingIndicator != null)
                    loadingIndicator.setVisibility(View.VISIBLE);

                ArrayList<Movie> popularMovies = null;
                ArrayList<Movie> topRatedMovies = null;
                try {
                    URL popularMoviesURL = NetworkUtils.getPopularMoviesURL(MainActivity.this);
                    String popularMoviesResponse = NetworkUtils.getResponseFromHttpUrl(popularMoviesURL);
                    popularMovies = new Gson().fromJson(popularMoviesResponse, Result.class).movies;
                    logd("popularMovies " + popularMovies.size());
                } catch (IOException e) {
                    loge("Error while requesting movies", e);
                }

                try {
                    URL topRatedMoviesURL = NetworkUtils.getTopRatedMoviesURL(MainActivity.this);
                    String topRatedMoviesResponse = NetworkUtils.getResponseFromHttpUrl(topRatedMoviesURL);
                    topRatedMovies = new Gson().fromJson(topRatedMoviesResponse, Result.class).movies;
                    logd("topRatedMovies " + topRatedMovies.size());
                } catch (IOException e) {
                    loge("Error while requesting movies", e);
                }

                logd("finished");
                return popularMovies;
            }

            @Override
            public void deliverResult(ArrayList<Movie> data) {
                result = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Movie>> loader, ArrayList<Movie> data) {
        if (loadingIndicator != null)
            loadingIndicator.setVisibility(View.INVISIBLE);

        if (data != null) {
            movies = data;
            gridFragment.swapData(data);
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Movie>> loader) {

    }

    private void showErrorMessage() {
        gridContainer.setVisibility(View.GONE);
        errorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_POSITION, currentPosition);
        outState.putParcelableArrayList(KEY_MOVIES, movies);
    }

    public ArrayList<Movie> getMovies() {
        return movies;
    }
}
