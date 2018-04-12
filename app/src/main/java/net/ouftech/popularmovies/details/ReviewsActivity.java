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

package net.ouftech.popularmovies.details;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import net.ouftech.popularmovies.commons.BaseActivity;
import net.ouftech.popularmovies.R;
import net.ouftech.popularmovies.model.Movie;

import butterknife.BindView;

public class ReviewsActivity extends BaseActivity {

    @NonNull
    @Override
    protected String getLotTag() {
        return "ReviewsActivity";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_reviews;
    }

    public static final String MOVIE_ARG_KEY = "movie";

    @BindView(R.id.reviews_recycler_view)
    protected RecyclerView recyclerView;
    @BindView(R.id.reviews_toolbar)
    protected Toolbar toolbar;

    private Movie movie;
    private ReviewsAdapter reviewsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null && getIntent().getParcelableExtra(MOVIE_ARG_KEY) != null && getIntent().getParcelableExtra(MOVIE_ARG_KEY) instanceof Movie)
            movie = getIntent().getParcelableExtra(MOVIE_ARG_KEY);

        reviewsAdapter = new ReviewsAdapter();
        recyclerView.setAdapter(reviewsAdapter);
        reviewsAdapter.swapData(movie.reviews);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                ((LinearLayoutManager) recyclerView.getLayoutManager()).getOrientation());
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider));
        recyclerView.addItemDecoration(dividerItemDecoration);

        toolbar.setTitle(movie.title);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Implemented by activity
            }
        });
    }
}
