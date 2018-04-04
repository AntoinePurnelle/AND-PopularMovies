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

package net.ouftech.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import net.ouftech.popularmovies.commons.CollectionUtils;


import java.util.ArrayList;
import java.util.Locale;

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
    public static final String VOTE_COUNT_KEY = "vote_count";
    public static final String ORIGINAL_LANGUAGE_KEY = "original_language";
    public static final String ORIGINAL_TITLE_KEY = "original_title";
    public static final String BACKDROP_PATH_KEY = "backdrop_path";
    public static final String TAGLINE_KEY = "tagline";
    public static final String PRODUCTION_COUNTRIES_KEY = "production_countries";
    public static final String GENRES_KEY = "genres";
    public static final String RUNTIME_KEY = "runtime";

    public boolean hasDetailsLoaded = false;
    public boolean hasVideosLoaded = false;

    @SerializedName(ID_KEY)
    public String id;

    @SerializedName(TITLE_KEY)
    public String title;

    @SerializedName(POSTER_KEY)
    public String posterPath;

    @SerializedName(RELEASE_DATE_KEY)
    public String releaseDate;

    @SerializedName(OVERVIEW_KEY)
    public String overview;

    @SerializedName(TAGLINE_KEY)
    public String tagline;

    @SerializedName(VOTE_AVERAGE_KEY)
    public float voteAverage;

    @SerializedName(VOTE_COUNT_KEY)
    public String voteCount;

    @SerializedName(ORIGINAL_LANGUAGE_KEY)
    public String originalLanguage;

    @SerializedName(ORIGINAL_TITLE_KEY)
    public String originalTitle;

    @SerializedName(BACKDROP_PATH_KEY)
    public String backdropPath;

    @SerializedName(RUNTIME_KEY)
    public int runtime;

    @SerializedName(PRODUCTION_COUNTRIES_KEY)
    public ArrayList<Country> countries;

    @SerializedName(GENRES_KEY)
    public ArrayList<Genre> genres;

    public ArrayList<Video> videos;

    @Nullable
    public String getCountriesString() {
        if (CollectionUtils.isEmpty(countries))
            return null;

        ArrayList<String> countriesStrings = new ArrayList<>(countries.size());
        for (Country country : countries) {
            countriesStrings.add(country.name);
        }

        return TextUtils.join(", ", countriesStrings);
    }

    @Nullable
    public String getGenresString() {
        if (CollectionUtils.isEmpty(genres))
            return null;

        ArrayList<String> genresStrings = new ArrayList<>(genres.size());
        for (Genre genre : genres) {
            genresStrings.add(genre.name);
        }

        return TextUtils.join(", ", genresStrings);
    }

    @Nullable
    public String getDisplayLanguage() {
        if (TextUtils.isEmpty(originalLanguage))
            return null;

        Locale loc = new Locale(originalLanguage);
        return loc.getDisplayLanguage(loc);
    }

    @Nullable
    public String getOriginalTitleIfDifferent() {
        if (TextUtils.isEmpty(originalTitle) || originalTitle.equals(title))
            return null;

        return originalTitle;
    }

    public void setVideos(@NonNull ArrayList<Video> videos) {
        this.videos = videos;
        for (Video video : videos) {
            if (!Video.SITE_YOUTUBE.equals(video.site))
                this.videos.remove(video);
        }
    }

    protected Movie(Parcel in) {
        hasDetailsLoaded = in.readByte() != 0x00;
        id = in.readString();
        title = in.readString();
        posterPath = in.readString();
        releaseDate = in.readString();
        overview = in.readString();
        tagline = in.readString();
        voteAverage = in.readFloat();
        voteCount = in.readString();
        originalLanguage = in.readString();
        originalTitle = in.readString();
        backdropPath = in.readString();
        runtime = in.readInt();
        if (in.readByte() == 0x01) {
            countries = new ArrayList<>();
            in.readList(countries, Country.class.getClassLoader());
        } else {
            countries = null;
        }
        if (in.readByte() == 0x01) {
            genres = new ArrayList<>();
            in.readList(genres, Genre.class.getClassLoader());
        } else {
            genres = null;
        }
        if (in.readByte() == 0x01) {
            videos = new ArrayList<>();
            in.readList(videos, Video.class.getClassLoader());
        } else {
            videos = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (hasDetailsLoaded ? 0x01 : 0x00));
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(posterPath);
        dest.writeString(releaseDate);
        dest.writeString(overview);
        dest.writeString(tagline);
        dest.writeFloat(voteAverage);
        dest.writeString(voteCount);
        dest.writeString(originalLanguage);
        dest.writeString(originalTitle);
        dest.writeString(backdropPath);
        dest.writeInt(runtime);
        if (countries == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(countries);
        }
        if (genres == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(genres);
        }
        if (videos == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(videos);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
