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
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import net.ouftech.popularmovies.commons.BaseFragment;
import net.ouftech.popularmovies.commons.NetworkUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A fragment for displaying an image.
 */
@FragmentWithArgs
public class DetailsFragment extends BaseFragment {

    @BindView(R.id.title_tv)
    AppCompatTextView titleTv;
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
    @BindView(R.id.date_tv)
    TextView dateTv;
    @BindView(R.id.overview_tv)
    TextView overviewTv;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        if (view == null)
            return null;

        if (getActivity() instanceof MainActivity && ((MainActivity) getActivity()).getMovies() != null)
            movie = ((MainActivity) getActivity()).getMovies().get(position);

        if (movie != null)
            imageView.setTransitionName(movie.id);

        // Load the image with Glide to prevent OOM error when the image drawables are very large.
        Glide.with(this)
                .load(NetworkUtils.getW185ImageURL(movie.poster))
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
        unbinder = ButterKnife.bind(this, view);

        titleTv.setText(movie.title);
        ratingTv.setText(movie.voteAverage);
        dateTv.setText(getLocaleDate(movie.releaseDate));
        overviewTv.setText(movie.overview);
        displayRatingStars(movie.voteAverage);

        return view;
    }

    private String getLocaleDate(@Nullable String dateString) {
        if (TextUtils.isEmpty(dateString))
            return "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");

        try {
            Date date = sdf.parse(dateString);
            java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getContext());
            return dateFormat.format(date);
        } catch (ParseException e) {
            loge(String.format("Error while parsing date %s", dateString), e);
            return "";
        }
    }

    private void displayRatingStars(String ratingString) {
        int rating = TextUtils.isEmpty(ratingString) ? 0 : (int) Math.round(Float.parseFloat(ratingString));

        initStar(icStar1Iv, rating, 1, 2);
        initStar(icStar2Iv, rating, 3, 4);
        initStar(icStar3Iv, rating, 5, 6);
        initStar(icStar4Iv, rating, 7, 8);
        initStar(icStar5Iv, rating, 9, 10);
    }

    private void initStar(@NonNull AppCompatImageView star, int value, int min, int max) {
        if (value < min)
            star.setImageResource(R.drawable.ic_star_border_white_24dp);
        else if (value >= max)
            star.setImageResource(R.drawable.ic_star_white_24dp);
        else
            star.setImageResource(R.drawable.ic_star_half_white_24dp);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
