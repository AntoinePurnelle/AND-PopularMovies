package net.ouftech.popularmovies;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by antoi on 27-02-18.
 */

public class Result {

    public static final String PAGE_KEY = "page";
    public static final String TOTAL_PAGES_KEY = "total_pages";
    public static final String RESULTS_KEY = "results";

    @SerializedName(PAGE_KEY)
    public int page;

    @SerializedName(TOTAL_PAGES_KEY)
    public int totalPages;

    @SerializedName(RESULTS_KEY)
    public List<Movie> movies;
}
