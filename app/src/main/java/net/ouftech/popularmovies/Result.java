package net.ouftech.popularmovies;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by antoi on 27-02-18.
 */

public class Result {

    private static final String PAGE_KEY = "page";
    private static final String TOTAL_PAGES_KEY = "total_pages";
    private static final String RESULTS_KEY = "results";

    @SerializedName(PAGE_KEY)
    public int page;

    @SerializedName(TOTAL_PAGES_KEY)
    public int totalPages;

    @SerializedName(RESULTS_KEY)
    public ArrayList<Movie> movies;
}
