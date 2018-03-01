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
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import net.ouftech.popularmovies.commons.BaseFragment;
import net.ouftech.popularmovies.commons.NetworkUtils;

import butterknife.BindView;

/**
 * A fragment for displaying an image.
 */
@FragmentWithArgs
public class ImageFragment extends BaseFragment {

    @NonNull
    @Override
    protected String getLotTag() {
        return "ImageFragment";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_image;
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
                .load(NetworkUtils.getW342ImageURL(movie.poster))
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
        return view;
    }
}
