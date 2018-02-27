package net.ouftech.popularmovies;

import com.google.gson.annotations.SerializedName;

/**
 * Created by antoi on 26-02-18.
 */

public class Movie {

    public static final String TITLE_KEY = "title";
    public static final String POSTER_KEY = "poster_path";
    public static final String RELEASE_DATE_KEY = "release_date";
    public static final String OVERVIEW_KEY = "overview";
    public static final String VOTE_AVERAGE_KEY = "vote_average";

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

}
