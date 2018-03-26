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

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import net.ouftech.popularmovies.model.Movie;
import net.ouftech.popularmovies.commons.BaseFragment;
import net.ouftech.popularmovies.commons.CallException;
import net.ouftech.popularmovies.commons.CollectionUtils;
import net.ouftech.popularmovies.commons.Logger;
import net.ouftech.popularmovies.commons.NetworkUtils;
import net.ouftech.popularmovies.model.VideosResult;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A fragment for displaying an image.
 */
@FragmentWithArgs
public class DetailsFragment extends BaseFragment {

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
    @BindView(R.id.image)
    protected ImageView imageView;

    private static final String movieLock = "movieLock";

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

        if (movie != null)
            imageView.setTransitionName(movie.id);

        loadMovie();
        loadVideos();
        displayData();

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

        return view;
    }

    private void loadMovie() {
        if (!movie.hasDetailsLoaded && getActivity() != null) {
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

                    synchronized (movieLock) {
                        movie.countries = tempMovie.countries;
                        movie.genres = tempMovie.genres;
                        movie.runtime = tempMovie.runtime;
                        movie.countries = tempMovie.countries;
                        movie.tagline = tempMovie.tagline;
                        movie.hasDetailsLoaded = true;
                    }
                    displayData();
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
                        return;
                    }

                    synchronized (movieLock) {
                        movie.videos = videosResult.videos;
                        movie.hasVideosLoaded = true;
                    }
                }

                @Override
                public void onFailure(@NonNull Call<VideosResult> call, @NonNull Throwable t) {
                    Logger.e(getLotTag(), String.format("Error while executing getVideos call for movie %s", id), t);
                    setProgressBarVisibility(View.GONE);
                }
            });
        }
    }

    private void displayData() {
        synchronized (movieLock) {
            if (detailToolbar != null)
                detailToolbar.setTitle(movie.title);
            if (ratingTv != null)
                ratingTv.setText(String.valueOf(movie.voteAverage + "/10"));
            if (overviewTv != null)
                overviewTv.setText(movie.overview);
            displayRatingStars(movie.voteAverage);

            displayValue(null, taglineTv, movie.tagline);
            displayValue(dateLayout, dateTv, getLocaleDate(movie.releaseDate));
            displayValue(runtimeLayout, runtimeTv, getDisplayRuntime());
            displayValue(originalLanguageLayout, originalLanguageTv, movie.getDisplayLanguage());
            if (!Locale.getDefault().getLanguage().equals(movie.originalLanguage))
                displayValue(originalTitleLayout, originalTitleTv, movie.getOriginalTitleIfDifferent());
            else
                displayValue(originalTitleLayout, originalTitleTv, null);

            if (countriesLabelTv != null && CollectionUtils.getSize(movie.countries) > 1)
                countriesLabelTv.setText(getString(R.string.countries));
            displayValue(countriesLayout, countriesTv, movie.getCountriesString());

            if (genresLabelTv != null && CollectionUtils.getSize(movie.genres) == 1)
                genresLabelTv.setText(getString(R.string.genre));
            displayValue(genresLayout, genresTv, movie.getGenresString());
        }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    synchronized
    private void setProgressBarVisibility(final int visibility) {
        if (isRunning() && progressBar != null)
            getBaseActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(visibility);
                }
            });
    }
}
