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

package net.ouftech.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by antoine.purnelle@ouftech.net on 26-02-18.
 */

public class Movie implements Parcelable {

    public static final String ID_KEY = "id";
    public static final String TITLE_KEY = "title";
    public static final String POSTER_KEY = "poster_path";
    public static final String RELEASE_DATE_KEY = "release_date";
    public static final String OVERVIEW_KEY = "overview";
    public static final String VOTE_AVERAGE_KEY = "vote_average";

    @SerializedName(ID_KEY)
    public String id;

    @SerializedName(TITLE_KEY)
    public String title;

    @SerializedName(POSTER_KEY)
    public String poster;

    @SerializedName(RELEASE_DATE_KEY)
    public String releaseDate;

    @SerializedName(OVERVIEW_KEY)
    public String overview;

    @SerializedName(VOTE_AVERAGE_KEY)
    public String voteAverage;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.id,
                this.title,
                this.poster,
                this.releaseDate,
                this.overview,
                this.voteAverage});
    }
}
