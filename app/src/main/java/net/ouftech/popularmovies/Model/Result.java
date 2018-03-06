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

package net.ouftech.popularmovies.Model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by antoine.purnelle@ouftech.net on 27-02-18.
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
