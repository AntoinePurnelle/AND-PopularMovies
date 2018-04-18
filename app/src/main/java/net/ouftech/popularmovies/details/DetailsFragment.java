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

package net.ouftech.popularmovies.details;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import net.ouftech.popularmovies.MainActivity;
import net.ouftech.popularmovies.R;
import net.ouftech.popularmovies.data.MovieContract.MovieEntry;
import net.ouftech.popularmovies.model.Country;
import net.ouftech.popularmovies.model.Genre;
import net.ouftech.popularmovies.model.Movie;
import net.ouftech.popularmovies.commons.BaseFragment;
import net.ouftech.popularmovies.commons.CallException;
import net.ouftech.popularmovies.commons.CollectionUtils;
import net.ouftech.popularmovies.commons.Logger;
import net.ouftech.popularmovies.commons.NetworkUtils;
import net.ouftech.popularmovies.model.Review;
import net.ouftech.popularmovies.model.ReviewsResult;
import net.ouftech.popularmovies.model.Video;
import net.ouftech.popularmovies.model.VideosResult;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A fragment for displaying an image.
 */
@FragmentWithArgs
public class DetailsFragment extends BaseFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.ic_star_1_iv)
    AppCompatImageView icStar1Iv;
    @BindView(R.id.ic_star_2_iv)
    AppCompatImageView icStar2Iv;
    @BindView(R.id.ic_star_3_iv)
    AppCompatImageView icStar3Iv;
    @BindView(R.id.ic_star_4_iv)
    AppCompatImageView icStar4Iv;
    @BindView(R.id.ic_star_5_iv)
    AppCompatImageView icStar5Iv;
    @BindView(R.id.rating_tv)
    TextView ratingTv;
    @BindView(R.id.overview_tv)
    TextView overviewTv;
    @BindView(R.id.tagline_tv)
    TextView taglineTv;
    @BindView(R.id.detail_toolbar)
    Toolbar detailToolbar;
    @BindView(R.id.backdrop_iv)
    ImageView backdropIv;
    @BindView(R.id.original_title_tv)
    TextView originalTitleTv;
    @BindView(R.id.original_language_tv)
    TextView originalLanguageTv;
    @BindView(R.id.date_tv)
    TextView dateTv;
    @BindView(R.id.runtime_tv)
    TextView runtimeTv;
    @BindView(R.id.countries_label_tv)
    TextView countriesLabelTv;
    @BindView(R.id.countries_tv)
    TextView countriesTv;
    @BindView(R.id.genres_label_tv)
    TextView genresLabelTv;
    @BindView(R.id.genres_tv)
    TextView genresTv;
    @BindView(R.id.detail_progressbar)
    ProgressBar progressBar;
    @BindView(R.id.original_title_layout)
    LinearLayout originalTitleLayout;
    @BindView(R.id.original_language_layout)
    LinearLayout originalLanguageLayout;
    @BindView(R.id.date_layout)
    LinearLayout dateLayout;
    @BindView(R.id.countries_layout)
    LinearLayout countriesLayout;
    @BindView(R.id.genres_layout)
    LinearLayout genresLayout;
    @BindView(R.id.runtime_layout)
    LinearLayout runtimeLayout;
    @BindView(R.id.videos_rv)
    RecyclerView videosRV;
    @BindView(R.id.image)
    protected ImageView imageView;
    @BindView(R.id.open_reviews_iv)
    protected AppCompatImageView reviewsButton;
    @BindView(R.id.detail_favorite_fab)
    protected FloatingActionButton favoriteFab;

    Unbinder unbinder;

    @NonNull
    @Override
    protected String getLotTag() {
        return "DetailsFragment";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_details;
    }

    @Arg
    int position;
    private Movie movie;
    private VideosAdapter videosAdapter;

    private static final Object MOVIE_LOCK = new Object();

    private static final String[] MOVIE_DETAIL_PROJECTION = {
            MovieEntry.COLUMN_TAGLINE,
            MovieEntry.COLUMN_PRODUCTION_COUNTRIES,
            MovieEntry.COLUMN_GENRES,
            MovieEntry.COLUMN_RUNTIME,
            MovieEntry.COLUMN_VIDEOS,
            MovieEntry.COLUMN_REVIEWS
    };

    public static final int INDEX_COLUMN_TAGLINE = 0;
    public static final int INDEX_COLUMN_PRODUCTION_COUNTRIES = 1;
    public static final int INDEX_COLUMN_GENRES = 2;
    public static final int INDEX_COLUMN_RUNTIME = 3;
    public static final int INDEX_COLUMN_VIDEOS = 4;
    public static final int INDEX_COLUMN_REVIEWS = 5;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        if (view == null)
            return null;
        unbinder = ButterKnife.bind(this, view);

        if (getActivity() instanceof MainActivity && ((MainActivity) getActivity()).getMovies() != null)
            movie = ((MainActivity) getActivity()).getMovies().get(position);

        if (movie == null) {
            Logger.e(getLotTag(), new NullPointerException("Movie is null at DetailsFragment creation"));

            if (getActivity() != null) {
                Toast.makeText(getContext(), R.string.error_message, Toast.LENGTH_LONG).show();
                getActivity().onBackPressed();
            }

            return view;
        }

        imageView.setTransitionName(movie.id);

        videosAdapter = new VideosAdapter();
        videosRV.setAdapter(videosAdapter);


        displayBaseDetails();
        displayDeepDetails();
        initReviewsButton();
        if (movie.hasVideosLoaded)
            displayVideos(movie.videos);

        if (movie.hasDetailsLoadedFromDB) {
            // Movie comes from DB --> update from network
            loadMovie();
            loadVideos();
            loadReviews(0);
        } else {
            // Movie is coming from network --> first, try to query from DB
            if (getActivity() != null)
                getActivity().getSupportLoaderManager().initLoader(Integer.parseInt(movie.id), null, this);
        }

        // Load the image with Glide to prevent OOM error when the image drawables are very large.
        URL url = getContext() == null ? null : NetworkUtils.getSmallImageURL(getContext(), movie.posterPath);
        if (url != null) {
            Glide.with(this)
                    .load(url)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable>
                                target, boolean isFirstResource) {
                            // The postponeEnterTransition is called on the parent ImagePagerFragment, so the
                            // startPostponedEnterTransition() should also be called on it to get the transition
                            // going in case of a failure.
                            if (getParentFragment() != null)
                                getParentFragment().startPostponedEnterTransition();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable>
                                target, DataSource dataSource, boolean isFirstResource) {
                            // The postponeEnterTransition is called on the parent ImagePagerFragment, so the
                            // startPostponedEnterTransition() should also be called on it to get the transition
                            // going when the image is ready.
                            if (getParentFragment() != null)
                                getParentFragment().startPostponedEnterTransition();
                            return false;
                        }
                    })
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_movie_24dp);
            if (getParentFragment() != null)
                getParentFragment().startPostponedEnterTransition();
        }

        url = NetworkUtils.getLargeImageURL(getContext(), movie.backdropPath);
        if (url != null) {
            Glide.with(this)
                    .load(url)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable>
                                target, boolean isFirstResource) {
                            // The postponeEnterTransition is called on the parent ImagePagerFragment, so the
                            // startPostponedEnterTransition() should also be called on it to get the transition
                            // going in case of a failure.
                            if (getParentFragment() != null)
                                getParentFragment().startPostponedEnterTransition();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable>
                                target, DataSource dataSource, boolean isFirstResource) {
                            // The postponeEnterTransition is called on the parent ImagePagerFragment, so the
                            // startPostponedEnterTransition() should also be called on it to get the transition
                            // going when the image is ready.
                            if (getParentFragment() != null)
                                getParentFragment().startPostponedEnterTransition();
                            return false;
                        }
                    })
                    .into(backdropIv);
        } else {
            backdropIv.setImageResource(R.drawable.ic_movie_24dp);
            if (getParentFragment() != null)
                getParentFragment().startPostponedEnterTransition();
        }

        detailToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        detailToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed(); // Implemented by activity
            }
        });

        return view;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle loaderArgs) {
        if (getContext() == null) {
            Logger.e(getLotTag(), new NullPointerException(String.format("Cannot create CursorLoader %s. Context is null", loaderId)));
            return null;
        }

        return new CursorLoader(getContext(),
                MovieEntry.buildMovieUriWithId(movie.id),
                MOVIE_DETAIL_PROJECTION,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        boolean cursorHasValidData = data != null && data.moveToFirst();

        if (cursorHasValidData) {
            Gson gson = new Gson();
            movie.tagline = data.getString(INDEX_COLUMN_TAGLINE);
            movie.countries = new ArrayList<>(Arrays.asList(gson.fromJson(data.getString(INDEX_COLUMN_PRODUCTION_COUNTRIES), Country[].class)));
            movie.genres = new ArrayList<>(Arrays.asList(gson.fromJson(data.getString(INDEX_COLUMN_GENRES), Genre[].class)));
            movie.videos = new ArrayList<>(Arrays.asList(gson.fromJson(data.getString(INDEX_COLUMN_VIDEOS), Video[].class)));
            movie.reviews = new ArrayList<>(Arrays.asList(gson.fromJson(data.getString(INDEX_COLUMN_REVIEWS), Review[].class)));
            movie.runtime = data.getInt(INDEX_COLUMN_RUNTIME);

            movie.hasDetailsLoadedFromDB = true;
            movie.isFavorite = true;
            displayDeepDetails();
            displayVideos(movie.videos);
            initReviewsButton();
            setProgressBarVisibility(View.GONE);
        }

        // Finally, update from network
        loadMovie();
        loadVideos();
        loadReviews(0);
    }

    private void loadMovie() {
        if (!movie.hasDetailsLoadedFromNetwork && getActivity() != null) {
            setProgressBarVisibility(View.VISIBLE);

            final String id = movie.id;
            NetworkUtils.getMovie(getActivity(), id, new Callback<Movie>() {
                @Override
                public void onResponse(@NonNull Call<Movie> call, @NonNull Response<Movie> response) {
                    Movie tempMovie = response.body();

                    if (tempMovie == null) {

                        ResponseBody errorBody = response.errorBody();
                        CallException callException = new CallException(response.code(), response.message(), errorBody, call);

                        if (errorBody == null) {
                            Logger.e(getLotTag(), String.format("Error while executing getMovie call for movie %s", id), callException);
                        } else {
                            Logger.e(getLotTag(), String.format("Error while executing getMovie call for movie %s. ErrorBody = %s", id, errorBody), callException);
                        }

                        setProgressBarVisibility(View.GONE);
                        return;
                    }

                    synchronized (MOVIE_LOCK) {
                        movie.countries = tempMovie.countries;
                        movie.genres = tempMovie.genres;
                        movie.runtime = tempMovie.runtime;
                        movie.tagline = tempMovie.tagline;
                        movie.hasDetailsLoadedFromNetwork = true;
                        updateMovieInDBIfNecessary();
                    }

                    displayDeepDetails();
                    setProgressBarVisibility(View.GONE);
                }

                @Override
                public void onFailure(@NonNull Call<Movie> call, @NonNull Throwable t) {
                    Logger.e(getLotTag(), String.format("Error while executing getMovie call for movie %s", id), t);
                    setProgressBarVisibility(View.GONE);
                }
            });
        }
    }

    private void loadVideos() {
        if (!movie.hasVideosLoaded && getActivity() != null) {

            final String id = movie.id;
            NetworkUtils.getVideos(getActivity(), id, new Callback<VideosResult>() {
                @Override
                public void onResponse(@NonNull Call<VideosResult> call, @NonNull Response<VideosResult> response) {
                    VideosResult videosResult = response.body();

                    if (videosResult == null) {

                        ResponseBody errorBody = response.errorBody();
                        CallException callException = new CallException(response.code(), response.message(), errorBody, call);

                        if (errorBody == null) {
                            Logger.e(getLotTag(), String.format("Error while executing getVideos call for movie %s", id), callException);
                        } else {
                            Logger.e(getLotTag(), String.format("Error while executing getVideos call for movie %s. ErrorBody = %s", id, errorBody), callException);
                        }

                        setProgressBarVisibility(View.GONE);
                        videosRV.setVisibility(View.GONE);
                        return;
                    }

                    movie.hasVideosLoaded = true;
                    displayVideos(videosResult.videos);
                    updateMovieInDBIfNecessary();
                }

                @Override
                public void onFailure(@NonNull Call<VideosResult> call, @NonNull Throwable t) {
                    Logger.e(getLotTag(), String.format("Error while executing getVideos call for movie %s", id), t);
                    setProgressBarVisibility(View.GONE);
                    videosRV.setVisibility(View.GONE);
                }
            });
        }
    }

    private void loadReviews(final int page) {
        if (getActivity() != null) {

            if (movie.shouldLoadReviewPage(page)) {

                final String id = movie.id;
                NetworkUtils.getReviews(getActivity(), id, new Callback<ReviewsResult>() {
                    @Override
                    public void onResponse(@NonNull Call<ReviewsResult> call, @NonNull Response<ReviewsResult> response) {
                        ReviewsResult reviewsResult = response.body();

                        if (reviewsResult == null) {

                            ResponseBody errorBody = response.errorBody();
                            CallException callException = new CallException(response.code(), response.message(), errorBody, call);

                            if (errorBody == null) {
                                Logger.e(getLotTag(), String.format("Error while executing getReviews call for movie %s and page %s", id, page), callException);
                            } else {
                                Logger.e(getLotTag(), String.format("Error while executing getReviews call for movie %s and page %s. ErrorBody = %s", id, page, errorBody), callException);
                            }

                            return;
                        }

                        movie.addReviews(reviewsResult.reviews);
                        movie.reviewsPagesCount = reviewsResult.totalPages;
                        movie.reviewsPagesLoadedCount = page;
                        loadReviews(page + 1);
                        initReviewsButton();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ReviewsResult> call, @NonNull Throwable t) {
                        Logger.e(getLotTag(), String.format("Error while executing getReviews call for movie %s and page %s", id, page), t);
                        setProgressBarVisibility(View.GONE);
                    }
                });
            } else {
                // has finished loading, can update in DB
                updateMovieInDBIfNecessary();
            }
        }
    }

    private void displayVideos(@Nullable ArrayList<Video> videos) {
        if (videosRV == null)
            return;

        if (videos == null) {
            videosRV.setVisibility(View.GONE);
            return;
        }

        synchronized (MOVIE_LOCK) {
            movie.setVideos(videos);
            videosAdapter.swapData(videos);
        }
    }

    private void displayBaseDetails() {
        synchronized (MOVIE_LOCK) {
            if (detailToolbar != null)
                detailToolbar.setTitle(movie.title);
            if (ratingTv != null)
                ratingTv.setText(String.valueOf(movie.voteAverage + "/10"));
            if (overviewTv != null)
                overviewTv.setText(movie.overview);
            displayRatingStars(movie.voteAverage);

            displayValue(null, taglineTv, movie.tagline);
            displayValue(dateLayout, dateTv, getLocaleDate(movie.releaseDate));
            displayValue(originalLanguageLayout, originalLanguageTv, movie.getDisplayLanguage());
            if (!Locale.getDefault().getLanguage().equals(movie.originalLanguage))
                displayValue(originalTitleLayout, originalTitleTv, movie.getOriginalTitleIfDifferent());
            else
                displayValue(originalTitleLayout, originalTitleTv, null);
        }
    }

    private void displayDeepDetails() {
        displayValue(runtimeLayout, runtimeTv, getDisplayRuntime());

        if (countriesLabelTv != null && CollectionUtils.getSize(movie.countries) > 1)
            countriesLabelTv.setText(getString(R.string.countries));
        displayValue(countriesLayout, countriesTv, movie.getCountriesString());

        if (genresLabelTv != null && CollectionUtils.getSize(movie.genres) == 1)
            genresLabelTv.setText(getString(R.string.genre));
        displayValue(genresLayout, genresTv, movie.getGenresString());

        initFabButtonIcon();
    }

    private void displayValue(@Nullable View layout, @Nullable TextView valueTv, @Nullable String value) {
        if (!TextUtils.isEmpty(value)) {
            if (layout != null)
                layout.setVisibility(View.VISIBLE);
            if (valueTv != null)
                valueTv.setText(value);
        } else {
            if (layout != null)
                layout.setVisibility(View.GONE);
        }
    }

    private String getLocaleDate(@Nullable String dateString) {
        if (TextUtils.isEmpty(dateString))
            return "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd", Locale.getDefault());

        try {
            Date date = sdf.parse(dateString);
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getContext());
            return dateFormat.format(date);
        } catch (ParseException e) {
            loge(String.format("Error while parsing date %s", dateString), e);
            return "";
        }
    }

    private String getDisplayRuntime() {
        if (movie == null || movie.runtime <= 0 || getContext() == null)
            return null;

        int hours = movie.runtime / 60;
        int minutes = movie.runtime % 60;

        String hoursString;
        String minutesString;

        if (hours == 0)
            hoursString = "";
        else
            hoursString = String.valueOf(hours) + getContext().getString(R.string.hour_unit);

        if (minutes == 0)
            minutesString = "";
        else if (minutes < 10)
            minutesString = "0" + minutes + getContext().getString(R.string.minute_unit);
        else
            minutesString = String.valueOf(minutes) + getContext().getString(R.string.minute_unit);

        return hoursString + minutesString;
    }

    private void displayRatingStars(float ratingFloat) {
        if (icStar1Iv == null)
            return;

        int rating = Math.round(ratingFloat);

        initStar(icStar1Iv, rating, 1, 2);
        initStar(icStar2Iv, rating, 3, 4);
        initStar(icStar3Iv, rating, 5, 6);
        initStar(icStar4Iv, rating, 7, 8);
        initStar(icStar5Iv, rating, 9, 10);
    }

    private void initStar(@NonNull AppCompatImageView star, int value, int min, int max) {
        if (value < min)
            star.setImageResource(R.drawable.ic_star_border_24dp);
        else if (value >= max)
            star.setImageResource(R.drawable.ic_star_24dp);
        else
            star.setImageResource(R.drawable.ic_star_half_24dp);
    }

    @OnClick(R.id.open_reviews_iv)
    protected void onOpenReviewsClick() {
        if (getActivity() == null)
            return;

        Intent reviewsIntent = new Intent(getActivity(), ReviewsActivity.class);
        reviewsIntent.putExtra(ReviewsActivity.MOVIE_ARG_KEY, movie);
        getActivity().startActivity(reviewsIntent);
    }

    @OnClick(R.id.detail_favorite_fab)
    protected void onFavoriteFabClick() {
        if (getContext() == null)
            return;

        if (movie.isFavorite) {
            movie.isFavorite = false;
            // TODO delete from DB
        } else {
            movie.isFavorite = true;
            ContentResolver contentResolver = getContext().getContentResolver();
            contentResolver.insert(
                    MovieEntry.CONTENT_URI,
                    movie.toContentValues());
        }

        initFabButtonIcon();
    }

    protected void updateMovieInDBIfNecessary() {
        if (getContext() == null)
            return;

        if (!movie.isFavorite)
            return; // Movie not in DB

        Logger.d(getLotTag(), String.format("Updating Movie %s", movie.id));

        ContentResolver contentResolver = getContext().getContentResolver();
        contentResolver.update(
                MovieEntry.CONTENT_URI,
                movie.toContentValues(), null, new String[]{movie.id});
    }

    private void initFabButtonIcon() {
        if (favoriteFab != null)
            favoriteFab.setImageResource(movie.isFavorite ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private synchronized void setProgressBarVisibility(final int visibility) {
        if (isRunning() && progressBar != null)
            getBaseActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(visibility);
                }
            });
    }

    public void initReviewsButton() {
        if (reviewsButton != null)
            reviewsButton.setVisibility(CollectionUtils.isEmpty(movie.reviews) ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // Nothing to implement
    }
}
