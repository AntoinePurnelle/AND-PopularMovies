package net.ouftech.popularmovies;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import net.ouftech.popularmovies.commons.BaseActivity;
import net.ouftech.popularmovies.commons.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<List<Movie>> {

    @NonNull
    @Override
    protected String getLotTag() {
        return "MainActivity";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }


    private static final int MOVIES_LOADER = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        loadMovies();
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
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<List<Movie>>(getApplicationContext()) {

            List<Movie> result;

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                //mLoadingIndicator.setVisibility(View.VISIBLE);

                if (result != null)
                    deliverResult(result);
                else
                    forceLoad();
            }

            @Override
            public List<Movie> loadInBackground() {

                List<Movie> popularMovies = null;
                List<Movie> topRatedMovies = null;
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
            public void deliverResult(List<Movie> data) {
                result = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> data) {
        data.size();
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {

    }
}
